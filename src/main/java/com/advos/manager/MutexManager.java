package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.utils.Node;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MutexManager {
    private final AtomicInteger csCounter;
    private final Node node;
    private final CriticalSection cs;
    private Map<Integer, Boolean> keys;

    protected MutexManager(CriticalSection cs, Node node) {
        this.cs = cs;
        this.node = node;
        this.csCounter = new AtomicInteger(0);
    }

    public void executeCS() {
        this.cs.execute();
        this.incrCsCounter();
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

    public void incrCsCounter() {
        this.csCounter.incrementAndGet();
    }
}
