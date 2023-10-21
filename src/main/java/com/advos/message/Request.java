package com.advos.message;

public class Request extends Message {
    public Request(int sourceNodeId) {
        super("Request message", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[RequestMessage]----sourceNodeId:" + this.getSourceNodeId();
    }

    public static Request deserialize(String serializedRequestMessage) {
        String[] requestMessage = serializedRequestMessage.split("----");
        return new Request(Integer.parseInt(requestMessage[2].split(":")[1]));
    }
}
