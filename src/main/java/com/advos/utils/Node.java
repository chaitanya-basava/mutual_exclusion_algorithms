package com.advos.utils;

import com.advos.MutualExclusionTesting;
import com.advos.cs.CriticalSection;
import com.advos.cs.TimeRunnerCriticalSection;
import com.advos.manager.MutexManager;
import com.advos.manager.RoucairolCarvalhoManager;
import com.advos.message.*;
import com.advos.models.Config;
import com.advos.models.CriticalSectionDetails;
import com.advos.models.NodeInfo;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final Config config;
    private final MutexManager mutexManager;
    private final NodeInfo nodeInfo;
    private final AtomicLong lamportClock;
    private final Map<Integer, Channel> inChannels = new HashMap<>();
    private final Map<Integer, Channel> outChannels = new HashMap<>();
    private final Set<Integer> terminate = new HashSet<>();
    private final String logPath;

    public Node(Config config, NodeInfo nodeInfo, String logPath) {
        this.config = config;
        this.logPath = logPath;
        this.nodeInfo = nodeInfo;
        this.lamportClock = new AtomicLong(0);

        CriticalSection cs = new TimeRunnerCriticalSection(this.config.getMeanCSExecutionTime());
        this.mutexManager = new RoucairolCarvalhoManager(cs, this.nodeInfo.getNeighbors(), this);

        new Thread(this::startServer, "Socket Server Thread").start();
        this.startClient();

        logger.info("node info with id: {}\n{}", this.getNodeInfo().getId(), this);

        MutualExclusionTesting.sleep(Config.INIT_DELAY);
    }

    private void startServer() {
        try(ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(nodeInfo.getPort()));
            while(true) {
                Socket socket = server.accept();
                Channel channel = new Channel(socket, this, -1);
                channel.receiveUrgentMessage();
                this.inChannels.put(channel.getNeighbourId(), channel);

                String socketInfo = "[Node " + channel.getNeighbourId() + "]" +
                        " for Node " + this.nodeInfo.getId();
                new Thread(channel::receiveMessage, socketInfo + " Message Listener Thread").start();
                logger.info("Received connection from " + socketInfo);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean createSocketChannel(String hostname, int port, int neighbourId) {
        try {
            Channel channel = new Channel(hostname, port, this, neighbourId);
            this.outChannels.put(neighbourId, channel);
            this.send(neighbourId, new Connection(this.nodeInfo.getId()), false);
            logger.info("Connected to " + channel.getSocket().getInetAddress().getHostAddress() + ":" + channel.getSocket().getPort());
        } catch (IOException e) {
            logger.error("Couldn't connect to " + hostname + ":" + port);
            MutualExclusionTesting.sleep(Config.RETRY_CLIENT_CONNECTION_DELAY);
            return false;
        }

        return true;
    }

    private void startClient() {
        List<NodeInfo> neighbours = this.nodeInfo.getNeighborNodesInfo();
        int idx = 0;
        while(idx != neighbours.size()) {
            NodeInfo neighbour = neighbours.get(idx);
            if(this.createSocketChannel(neighbour.getHost(), neighbour.getPort(), neighbour.getId())) {
                idx++;
            }
        }

        logger.info("Connected to " + this.outChannels.size() + " channel(s)");
    }

    public void send(int destId, Message message, boolean incrementClock) {
        synchronized(this) {
            if(incrementClock) this.incrLamportClock();
            this.outChannels.get(destId).sendMessage(message);

            if(message instanceof Reply) {
                this.mutexManager.setKey(destId, false);
            }
        }
    }

    @Override
    public String toString() {
        return ("[Node ID] " + this.nodeInfo.getId() + "\n" + this.nodeInfo);
    }

    public NodeInfo getNodeInfo() {
        return this.nodeInfo;
    }

    public long getLamportClock() {
        return lamportClock.get();
    }

    public void incrLamportClock() {
        lamportClock.incrementAndGet();
    }

    public void updateLamportClock(long msgClock) {
        this.lamportClock.set(Math.max(msgClock, this.getLamportClock()) + 1);
    }

    public void startAlgorithm() {
        ExponentialDistribution expDist = new ExponentialDistribution(this.config.getMeanInterRequestDelay());
        while(this.mutexManager.getCsCounter() < this.config.getMaxCsRequests()) {
            int t = (int) expDist.sample();
            logger.info("Waiting for " + t + " ms");
            MutualExclusionTesting.sleep(t);

            this.mutexManager.csEnter();
            this.mutexManager.executeCS();
            this.mutexManager.csLeave();
        }
    }

    public void stopAlgorithm() {
        this.incrLamportClock();
        for (int neighbourId: this.getNodeInfo().getNeighbors()) {
            this.send(neighbourId, new Terminate(this.getLamportClock(), this.getNodeInfo().getId()), false);
        }

        while(this.getTerminateSize() != this.config.getN() - 1) {
            MutualExclusionTesting.sleep(Config.RETRY_CS_PERMISSION_CHECK_DELAY);
        }

        logger.info("\nNumber of times CS safety got compromised (concurrent usage by multiple nodes): " +
                this.mutexManager.getCsSafetyCompromisedCount() + " for NODE " +
                this.getNodeInfo().getId() + "\nTermination condition met!!!\n");
    }

    public void saveCSUsageDetails() {
        try(FileWriter writer = new FileWriter(this.logPath + this.getNodeInfo().getId() + ".out")) {
            for(CriticalSectionDetails csDetails: this.mutexManager.getAllCSDetails()) {
                writer.write(csDetails.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void processRequestMsg(Message msg) {
        this.mutexManager.processCSRequest(msg);
    }

    public void processReplyMsg(Reply msg) {
        this.mutexManager.processCSReply(msg);
    }

    public void processTerminateMsg(Message msg) {
        this.addToTerminate(msg.getSourceNodeId());
    }

    public void addToTerminate(int nodeId) {
        this.terminate.add(nodeId);
        logger.info("Added node: [" + nodeId + "] to termination ready set");
    }

    public int getTerminateSize() {
        return this.terminate.size();
    }

    public void close() {
        this.outChannels.values().forEach(Channel::close);
        this.inChannels.values().forEach(Channel::close);
    }
}
