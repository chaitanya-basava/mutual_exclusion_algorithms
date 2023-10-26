package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.message.Message;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MutexManager {
    private static final Logger logger = LoggerFactory.getLogger(MutexManager.class);
    private final AtomicInteger csCounter;
    private final AtomicBoolean requestingCS;
    private final AtomicBoolean usingCS;
    public final Node node;
    private final CriticalSection cs;
    private Map<Integer, Boolean> keys;
    private long csRequestTime;
    private final List<Integer> differedRequests;

    protected MutexManager(CriticalSection cs, Node node) {
        this.cs = cs;
        this.node = node;
        this.usingCS = new AtomicBoolean(false);
        this.requestingCS = new AtomicBoolean(false);
        this.csCounter = new AtomicInteger(0);
        this.differedRequests = new ArrayList<>();
    }

    public final void executeCS() {
        this.cs.execute();
        logger.info("Executed critical section - " + this.incrCsCounter());
    }

    public abstract void csEnter();

    public abstract void csLeave();

    public abstract void processCSRequest(Message msg);

    public final void processCSReply(Message msg) {
        synchronized(node) {
            synchronized(this) {
                this.keys.put(msg.getSourceNodeId(), true);
            }
        }
    }

    public final Map<Integer, Boolean> getKeys() {
        return keys;
    }

    public final void setKeys(Map<Integer, Boolean> keys) {
        this.keys = keys;
    }

    public final void setKey(int nodeId, boolean permission) {
        this.keys.put(nodeId, permission);
    }

    public final int getCsCounter() {
        return csCounter.get();
    }

    public final int incrCsCounter() {
        return this.csCounter.incrementAndGet();
    }

    public final List<Integer> getDifferedRequests() {
        return differedRequests;
    }

    public final void setDifferedRequests(int nodeId) {
        this.differedRequests.add(nodeId);
    }

    public final void clearDifferedRequests() {
        this.differedRequests.clear();
    }

    public final boolean getRequestingCS() {
        return this.requestingCS.get();
    }

    public final void setRequestingCS(boolean requestingCS) {
        this.requestingCS.set(requestingCS);
    }

    public final boolean getUsingCS() {
        return this.usingCS.get();
    }

    public final void setUsingCS(boolean requestingCS) {
        this.usingCS.set(requestingCS);
    }

    public final long getCSRequestTime() { return csRequestTime; }

    public final void setCSRequestTime(long csRequestTime) { this.csRequestTime = csRequestTime; }

    public final boolean checkCSPermission() {
        synchronized (node) {
            synchronized (this) {
                for (boolean key : this.keys.values()) {
                    if (!key) return false;
                }

                this.setUsingCS(true);
                return true;
            }
        }
    }
}
