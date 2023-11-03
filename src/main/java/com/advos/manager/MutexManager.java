package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.message.Message;
import com.advos.message.Reply;
import com.advos.models.CriticalSectionDetails;
import com.advos.models.CriticalSectionPreviousRunDetails;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.channels.FileLock;
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
    private final List<Integer> differedRequests;
    private final List<CriticalSectionDetails> allCSDetails;
    private CriticalSectionDetails csDetails;
    private final AtomicInteger csSafetyCompromisedCount = new AtomicInteger(0);
    private CriticalSectionPreviousRunDetails previousCSDetails = new CriticalSectionPreviousRunDetails(-1, -1, 0, 0);

    protected MutexManager(CriticalSection cs, Node node) {
        this.cs = cs;
        this.node = node;
        this.usingCS = new AtomicBoolean(false);
        this.requestingCS = new AtomicBoolean(false);
        this.csCounter = new AtomicInteger(0);
        this.differedRequests = new ArrayList<>();
        this.allCSDetails = new ArrayList<>();
    }

    public final void executeCS() {
        // testing if CS is being accessed concurrently
        File file = new File("cs_concurrency_test.txt");
        FileOutputStream out = null;
        FileInputStream in = null;
        FileLock lock = null;
        try {
            out = new FileOutputStream(file);
            lock = out.getChannel().tryLock();

            if(lock == null) {
                in = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                logger.error("[FATAL] CS: " + this.getCsCounter() +
                        " being executed concurrently. " + bufferedReader.readLine());

                bufferedReader.close();
                inputStreamReader.close();
                this.csSafetyCompromisedCount.incrementAndGet();
            } else {
                BufferedOutputStream bw = new BufferedOutputStream(out);
                bw.write(("CS being used by node - " + this.node.getNodeInfo().getId()).getBytes());
                bw.close();
                this.cs.execute();
            }

        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            try {
                if(lock != null && lock.isValid()) {
                    lock.release();
                }
                if(out != null) out.close();
                if(in != null) in.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }

        logger.info("Executed critical section - " + this.incrCsCounter());
    }

    public abstract void csEnter();

    public abstract void csLeave();

    public abstract void processCSRequest(Message msg);

    public final void processCSReply(Reply msg) {
        synchronized(node) {
            synchronized(this) {
                this.setPreviousCSDetails(msg.getPrevRun());
                this.keys.put(msg.getSourceNodeId(), true);
//                logger.info("[RECEIVED CS REPLY FROM] " + msg.getSourceNodeId());
//                logger.info(this.getKeys().toString());
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
        Collections.shuffle(differedRequests);
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

    public final boolean checkCSPermission() {
        synchronized (node) {
            synchronized (this) {
                for (boolean flag : this.keys.values()) {
                    if (!flag) return false;
                }

                this.setUsingCS(true);
                return true;
            }
        }
    }

    public final void setCSDetails(long entryClock) {
        this.csDetails = new CriticalSectionDetails(this.node.getNodeInfo().getId(), entryClock);
    }

    public final void setCSUseStartTime(long useClock) {
        this.csDetails.setCSExecuteTime(useClock, true);
    }

    public final void closeCSDetails(long exitClock) {
        this.csDetails.setCSExitTime(exitClock, true);
        this.csDetails.setPreviousRunDetails(this.getPreviousCSDetails());
    }

    public final CriticalSectionDetails getCurrentCSDetails() {
        return this.csDetails;
    }

    public final CriticalSectionDetails addAndClearCurrentCSDetails() {
        CriticalSectionDetails temp = new CriticalSectionDetails(
                this.csDetails.getNodeId(),
                this.csDetails.getCSRequestTime(),
                this.csDetails.getCSExitTime(),
                this.csDetails.getCSExecuteTime(),
                this.csDetails.getMsgCount(),
                this.csDetails.getCSRequestTimestamp(),
                this.csDetails.getCSExitTimestamp(),
                this.csDetails.getCSExecuteTimestamp()
        );
        temp.setPreviousRunDetails(this.csDetails.getPreviousRunDetails());
        this.allCSDetails.add(temp);
        this.csDetails = null;

        return this.allCSDetails.get(this.allCSDetails.size() - 1);
    }

    public final List<CriticalSectionDetails> getAllCSDetails() {
        return allCSDetails;
    }

    public final int getCsSafetyCompromisedCount() {
        return this.csSafetyCompromisedCount.get();
    }

    public CriticalSectionPreviousRunDetails getPreviousCSDetails() {
        return previousCSDetails;
    }

    public void setPreviousCSDetails(CriticalSectionPreviousRunDetails previousCSDetails) {
        if(this.previousCSDetails.getTimestamp() < previousCSDetails.getTimestamp()) {
            this.previousCSDetails = previousCSDetails;
        }
    }
}
