package com.advos.message;

import com.advos.models.CriticalSectionPreviousRunDetails;

public class Reply extends Message {
    private final transient CriticalSectionPreviousRunDetails prevRun;

    public Reply(long clock, int sourceNodeId, CriticalSectionPreviousRunDetails prevRun) {
        super(clock, sourceNodeId);
        this.prevRun = prevRun;
    }

    public CriticalSectionPreviousRunDetails getPrevRun() { return this.prevRun; }

    @Override
    public String toString() {
        return "[Reply]" + super.toString() +
                "----" + this.prevRun.toString();
    }

    public static Reply deserialize(String serializedReplyMessage) {
        String[] replyMessage = Message.msgPreProcess(serializedReplyMessage).split("----");
        return new Reply(
                Integer.parseInt(replyMessage[1].split(":")[1]),
                Integer.parseInt(replyMessage[2].split(":")[1]),
                CriticalSectionPreviousRunDetails.deserialize(replyMessage[3])
        );
    }
}
