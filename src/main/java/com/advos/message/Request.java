package com.advos.message;

public class Request extends Message {
    public Request(long clock, int sourceNodeId) {
        super(clock, sourceNodeId);
    }

    @Override
    public String toString() {
        return "[Request]" + super.toString();
    }

    public static Request deserialize(String serializedRequestMessage) {
        String[] requestMessage = serializedRequestMessage.split("----");
        return new Request(
                Integer.parseInt(requestMessage[1].split(":")[1]),
                Integer.parseInt(requestMessage[2].split(":")[1])
        );
    }
}
