package com.advos.message;

public class Terminate extends Message {
    public Terminate(long clock, int sourceNodeId) {
        super(clock, sourceNodeId);
    }

    @Override
    public String toString() {
        return "[Terminate]" + super.toString();
    }

    public static Terminate deserialize(String serializedRequestMessage) {
        String[] requestMessage = serializedRequestMessage.split("----");
        return new Terminate(
                Integer.parseInt(requestMessage[1].split(":")[1]),
                Integer.parseInt(requestMessage[2].split(":")[1])
        );
    }
}

