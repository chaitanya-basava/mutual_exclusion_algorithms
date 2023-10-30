package com.advos.utils;

import com.advos.MutualExclusionTesting;
import com.advos.message.*;
import com.advos.models.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class Channel {
    private static final Logger logger = LoggerFactory.getLogger(Channel.class);

    public Socket getSocket() {
        return socket;
    }

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final Node node;
    private int neighbourId;

    public Channel(Socket socket, Node node, int neighbourId) {
        this.node = node;
        this.neighbourId = neighbourId;
        try {
            this.socket = socket;
            this.out = new DataOutputStream(socket.getOutputStream());
            this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Channel(String hostname, int port, Node node, int neighbourId) throws IOException {
        this(new Socket(hostname, port), node, neighbourId);
    }

    public void receiveUrgentMessage() {
        StringBuilder msg = new StringBuilder();
        String line;
        while(true) {
            try {
                line = this.in.readUTF();
                msg.append(line);

                if(msg.toString().endsWith(Config.MESSAGE_DELIMITER) && (msg.toString().contains("[Connection]"))) {
                    this.neighbourId = Connection.deserialize(msg.toString()).getSourceNodeId();
                    break;
                }
            } catch (EOFException ignored) {
                MutualExclusionTesting.sleep(Config.RETRY_MESSAGE_READING_DELAY);
            }
            catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void receiveMessage() {
        StringBuilder msg = new StringBuilder();
        String line;
        while (true) {
            try {
                line = this.in.readUTF();
                msg.append(line);

                if(msg.toString().endsWith(Config.MESSAGE_DELIMITER)) {
                    synchronized(node) {
                        if(msg.toString().contains("[Request]")) {
                            Request request = Request.deserialize(msg.toString());
                            node.updateLamportClock(request.getClock());
                            this.node.processRequestMsg(request);
                        } else if(msg.toString().contains("[Reply]")) {
                            Reply reply = Reply.deserialize(msg.toString());
                            node.updateLamportClock(reply.getClock());
                            this.node.processReplyMsg(reply);
                        } else if(msg.toString().contains("[Terminate]")) {
                            Terminate terminate = Terminate.deserialize(msg.toString());
                            node.updateLamportClock(terminate.getClock());
                            this.node.processTerminateMsg(terminate);
                        }

                        msg = new StringBuilder();
                    }
                }
            } catch (IOException | ClassCastException e) {
                try {
                    if(e.getMessage().equals("Stream closed") || e.getMessage().equals("Socket closed")) break;
                } catch (NullPointerException ignored) {
                    continue;
                }
                logger.error(e.getMessage());
                MutualExclusionTesting.sleep(Config.RETRY_MESSAGE_READING_DELAY);
            }
        }
    }

    public void sendMessage(Message message) {
        try{
            this.out.writeUTF(message.toString() + Config.MESSAGE_DELIMITER);
            this.out.flush();
        } catch (IOException e) {
            logger.error("Error while sending to " + this.neighbourId + ": " + e.getMessage());
        }
    }

    public int getNeighbourId() {
        return neighbourId;
    }

    public void close() {
        if(!socket.isClosed() && socket.isConnected()) {
            try {
                logger.info("Closing channel with " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                socket.close();
                in.close();
                out.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }
}
