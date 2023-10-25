package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private final Map<Integer, Boolean> differedRequests;

    protected MutexManager(CriticalSection cs, Node node) {
        this.cs = cs;
        this.node = node;
        this.usingCS = new AtomicBoolean(false);
        this.requestingCS = new AtomicBoolean(false);
        this.csCounter = new AtomicInteger(0);
        this.differedRequests = new ConcurrentHashMap<>();
    }

    public void executeCS() {
        this.cs.execute();
        logger.info("Executed critical section - " + this.incrCsCounter());
    }

    public abstract void csEnter();

    public abstract void csLeave();

    public Map<Integer, Boolean> getKeys() {
        return keys;
    }

    public void setKeys(Map<Integer, Boolean> keys) {
        this.keys = keys;
    }

    public int getCsCounter() {
        return csCounter.get();
    }

    public int incrCsCounter() {
        return this.csCounter.incrementAndGet();
    }

    public Map<Integer, Boolean> getDifferedRequests() {
        return differedRequests;
    }

    public void setDifferedRequests(int nodeId) {
        this.differedRequests.put(nodeId, true);
    }

    public void clearDifferedRequests() {
        this.differedRequests.clear();
    }

    public boolean getRequestingCS() {
        return this.requestingCS.get();
    }

    public void setRequestingCS(boolean requestingCS) {
        this.requestingCS.set(requestingCS);
    }

    public boolean getUsingCS() {
        return this.usingCS.get();
    }

    public void setUsingCS(boolean requestingCS) {
        this.usingCS.set(requestingCS);
    }

    public boolean checkCSPermission() {
        synchronized(node) {
            for (boolean key : this.keys.values()) {
                if (!key) return false;
            }

            return true;
        }
    }
}
