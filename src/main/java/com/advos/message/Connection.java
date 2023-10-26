package com.advos.message;

public class Connection extends Message {
    public Connection(int sourceNodeId) {
        super(-1, sourceNodeId);
    }

    @Override
    public String toString() {
        return "[Connection]----sourceNodeId:" + this.getSourceNodeId();
    }

    public static Connection deserialize(String serializedConnectionMessage) {
        String[] connectionMessage = Message.msgPreProcess(serializedConnectionMessage).split("----");
        return new Connection(Integer.parseInt(connectionMessage[1].split(":")[1]));
    }
}
