package com.advos.message;

public class Reply extends Message {
    public Reply(int sourceNodeId) {
        super("Reply message", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[ReplyMessage]----sourceNodeId:" + this.getSourceNodeId();
    }

    public static Reply deserialize(String serializedReplyMessage) {
        String[] replyMessage = serializedReplyMessage.split("----");
        return new Reply(Integer.parseInt(replyMessage[2].split(":")[1]));
    }
}
