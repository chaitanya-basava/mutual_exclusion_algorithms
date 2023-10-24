package com.advos.message;

public class Release extends Message {
    public Release(long clock, int sourceNodeId) {
        super(clock, sourceNodeId);
    }

    @Override
    public String toString() {
        return "[Release]" + super.toString();
    }

    public static Release deserialize(String serializedReleaseMessage) {
        String[] releaseMessage = serializedReleaseMessage.split("----");
        return new Release(
                Integer.parseInt(releaseMessage[1].split(":")[1]),
                Integer.parseInt(releaseMessage[2].split(":")[1])
        );
    }
}
