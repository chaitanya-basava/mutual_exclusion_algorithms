package com.advos.message;

public class Connection extends Message {
    public Connection(int sourceNodeId) {
        super("Connection request message", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[ConnectionMessage]----sourceNodeId:" + this.getSourceNodeId();
    }

    public static Connection deserialize(String serializedConnectionMessage) {
        String[] connectionMessage = serializedConnectionMessage.split("----");
        return new Connection(Integer.parseInt(connectionMessage[2].split(":")[1]));
    }
}
