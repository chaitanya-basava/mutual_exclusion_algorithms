package com.advos.models;

import java.util.concurrent.atomic.AtomicInteger;

public class CriticalSectionDetails {
    private final AtomicInteger msgCount;
    private final int nodeId;
    private long csRequestTime;
    private long csExitTime;
    private long csExecuteTime;
    private long csRequestTimestamp;
    private long csExitTimestamp;
    private long csExecuteTimestamp;
    private CriticalSectionPreviousRunDetails previousRunDetails = null;

    public CriticalSectionDetails(int nodeId, long csRequestTime) {
        this.msgCount = new AtomicInteger(0);
        this.nodeId = nodeId;
        this.setCSRequestTime(csRequestTime, true);
    }

    public CriticalSectionDetails(int nodeId,
                                  long csRequestTime,
                                  long csExitTime,
                                  long csExecuteTime,
                                  int msgCount,
                                  long csRequestTimestamp,
                                  long csExitTimestamp,
                                  long csExecuteTimestamp) {
        this.msgCount = new AtomicInteger(0);
        this.nodeId = nodeId;
        this.setCSRequestTime(csRequestTime, false);
        this.setCSExitTime(csExitTime, false);
        this.setCSExecuteTime(csExecuteTime, false);
        this.setMsgCount(msgCount);
        this.csRequestTimestamp = csRequestTimestamp;
        this.csExitTimestamp = csExitTimestamp;
        this.csExecuteTimestamp = csExecuteTimestamp;
    }

    public final long getCSRequestTime() { return csRequestTime; }

    public final void setCSRequestTime(long csRequestTime, boolean updateTimestamp) {
        if(updateTimestamp) this.csRequestTimestamp = System.currentTimeMillis();
        this.csRequestTime = csRequestTime;
    }

    public final long getCSExitTime() { return csExitTime; }

    public final void setCSExitTime(long csExitTime, boolean updateTimestamp) {
        if(updateTimestamp) this.csExitTimestamp = System.currentTimeMillis();
        this.csExitTime = csExitTime;
    }

    public final long getCSExecuteTime() { return csExecuteTime; }

    public final void setCSExecuteTime(long csExecuteTime, boolean updateTimestamp) {
        if(updateTimestamp) this.csExecuteTimestamp = System.currentTimeMillis();
        this.csExecuteTime = csExecuteTime;
    }

    public int getMsgCount() { return msgCount.get(); }

    public void setMsgCount(int msgCount) { this.msgCount.set(msgCount); }

    public void incrementMsgCount() { this.msgCount.incrementAndGet(); }

    public void incrementMsgCount(int num) { this.msgCount.addAndGet(num); }

    public long getCSRequestTimestamp() { return csRequestTimestamp; }

    public long getCSExitTimestamp() { return csExitTimestamp; }

    public long getCSExecuteTimestamp() { return csExecuteTimestamp; }

    public int getNodeId() { return nodeId; }

    public CriticalSectionPreviousRunDetails getPreviousRunDetails() { return previousRunDetails; }

    public  void setPreviousRunDetails(CriticalSectionPreviousRunDetails previousRunDetails) { this.previousRunDetails = previousRunDetails; }

    @Override
    public String toString() {
        return "Node Id:" + this.getNodeId() +
                "----Messages exchanged:" + this.getMsgCount() +
                "----CS request start time:" + this.getCSRequestTime() +
                "----CS execution end time:" + this.getCSExitTime() +
                "----CS request start timestamp:" + this.getCSRequestTimestamp() +
                "----CS execution end timestamp:" + this.getCSExitTimestamp() +
                "----CS execution start time:" + this.getCSExecuteTime() +
                "----CS execution start timestamp:" + this.getCSExecuteTimestamp() +
                "----Response Time:" + (this.getCSExitTimestamp() - this.getCSRequestTimestamp()) + " ms" +
                "----" + this.getPreviousRunDetails() +
                "----" + (this.getCSExecuteTimestamp() - this.getPreviousRunDetails().getTimestamp()) + "\n";
    }

    /*
    public static CriticalSectionDetails deserialize(String str) {
        String[] msg = str.split("----");
        CriticalSectionDetails temp = new CriticalSectionDetails(
                Integer.parseInt(msg[0].split(":")[1]),
                Long.parseLong(msg[2].split(":")[1]),
                Long.parseLong(msg[3].split(":")[1]),
                Long.parseLong(msg[6].split(":")[1]),
                Integer.parseInt(msg[1].split(":")[1]),
                Long.parseLong(msg[4].split(":")[1]),
                Long.parseLong(msg[5].split(":")[1]),
                Long.parseLong(msg[7].split(":")[1])
        );
        temp.setPreviousRunDetails(CriticalSectionPreviousRunDetails.deserialize(msg[9]));
        return temp;
    }
    */
}
