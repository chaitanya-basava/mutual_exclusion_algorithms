package com.advos.message;

import com.advos.models.Config;

public abstract class Message {
    private final long clock;
    private final int sourceNodeId;

    protected Message(long clock, int sourceNodeId) {
        this.clock = clock;
        this.sourceNodeId = sourceNodeId;
    }

    public long getClock() {
        return clock;
    }

    public int getSourceNodeId() {
        return sourceNodeId;
    }

    public String toString() {
        return "----clock:" + this.getClock() +
                "----sourceNodeId:" + this.getSourceNodeId();
    }

    public static String msgPreProcess(String msg) {
        return msg.split(Config.MESSAGE_DELIMITER)[0];
    }
}
