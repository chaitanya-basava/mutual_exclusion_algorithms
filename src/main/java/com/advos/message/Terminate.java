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
        String[] terminateMessage = Message.msgPreProcess(serializedRequestMessage).split("----");
        return new Terminate(
                Integer.parseInt(terminateMessage[1].split(":")[1]),
                Integer.parseInt(terminateMessage[2].split(":")[1])
        );
    }
}

