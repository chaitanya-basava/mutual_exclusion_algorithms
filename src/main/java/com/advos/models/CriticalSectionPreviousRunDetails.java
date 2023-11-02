package com.advos.models;

public class CriticalSectionPreviousRunDetails {
    private final int nodeId;
    private final int csCount;
    private final long clock;
    private final long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public CriticalSectionPreviousRunDetails(int nodeId, int csCount, long clock, long timestamp) {
        this.nodeId = nodeId;
        this.csCount = csCount;
        this.clock = clock;
        this.timestamp = timestamp;
    }

    public String toString() {
        return "Node Id:" + this.nodeId +
                ";;;;cs count:" + this.csCount +
                ";;;;clock:" + this.clock +
                ";;;;timestamp:" + this.timestamp;
    }

    public static CriticalSectionPreviousRunDetails deserialize(String str) {
        String[] msg = str.split(";;;;");
        return new CriticalSectionPreviousRunDetails(
                Integer.parseInt(msg[0].split(":")[1]),
                Integer.parseInt(msg[1].split(":")[1]),
                Long.parseLong(msg[2].split(":")[1]),
                Long.parseLong(msg[3].split(":")[1])
        );
    }
}
