package com.advos.message;

import java.io.Serializable;

public abstract class Message implements Serializable {
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
}
