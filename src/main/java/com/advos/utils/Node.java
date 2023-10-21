package com.advos.utils;

import com.advos.MutualExclusionTesting;
import com.advos.message.*;
import com.advos.models.Config;
import com.advos.models.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Node {
    private static final Logger logger = LoggerFactory.getLogger(Node.class);
    private final Config config;
    private final NodeInfo nodeInfo;
    private final Map<Integer, Channel> inChannels = new HashMap<>();
    private final Map<Integer, Channel> outChannels = new HashMap<>();

    public Node(Config config, NodeInfo nodeInfo) {
        this.config = config;
        this.nodeInfo = nodeInfo;

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
            channel.sendMessage(new Connection(this.nodeInfo.getId()));
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

    private void send(int destId, Message message) {
        this.outChannels.get(destId).sendMessage(message);
    }

    @Override
    public String toString() {
        return ("[Node ID] " + this.nodeInfo.getId() + "\n" + this.nodeInfo);
    }

    public NodeInfo getNodeInfo() {
        return this.nodeInfo;
    }

    public void close() {
        this.outChannels.values().forEach(Channel::close);
        this.inChannels.values().forEach(Channel::close);
    }
}
