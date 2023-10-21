package com.advos.message;

public class Release extends Message {
    public Release(int sourceNodeId) {
        super("Release message", sourceNodeId);
    }

    @Override
    public String toString() {
        return "[ReleaseMessage]----sourceNodeId:" + this.getSourceNodeId();
    }

    public static Release deserialize(String serializedReleaseMessage) {
        String[] releaseMessage = serializedReleaseMessage.split("----");
        return new Release(Integer.parseInt(releaseMessage[2].split(":")[1]));
    }
}
