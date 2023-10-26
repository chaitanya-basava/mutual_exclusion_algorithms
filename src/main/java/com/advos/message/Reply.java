package com.advos.message;

public class Reply extends Message {
    public Reply(long clock, int sourceNodeId) {
        super(clock, sourceNodeId);
    }

    @Override
    public String toString() {
        return "[Reply]" + super.toString();
    }

    public static Reply deserialize(String serializedReplyMessage) {
        String[] replyMessage = Message.msgPreProcess(serializedReplyMessage).split("----");
        return new Reply(
                Integer.parseInt(replyMessage[1].split(":")[1]),
                Integer.parseInt(replyMessage[2].split(":")[1])
        );
    }
}
