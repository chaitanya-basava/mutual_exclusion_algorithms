package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.message.*;
import com.advos.models.CriticalSectionDetails;
import com.advos.models.CriticalSectionPreviousRunDetails;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RoucairolCarvalhoManager extends MutexManager {
    private static final Logger logger = LoggerFactory.getLogger(RoucairolCarvalhoManager.class);

    public RoucairolCarvalhoManager(
            CriticalSection cs,
            List<Integer> neighbours,
            Node node
    ) {
        super(cs, node);
        super.setKeys(new ConcurrentHashMap<>(neighbours.stream().
                collect(Collectors.toMap(k -> k, k -> node.getNodeInfo().getId() < k))));
        logger.info(super.getKeys().toString());
    }

    @Override
    public void csEnter() {
        synchronized(node) {
            synchronized(this) {
                super.setCSDetails(node.getLamportClock());
                super.setRequestingCS(true);

                for (Map.Entry<Integer, Boolean> entry : super.getKeys().entrySet()) {
                    if(Boolean.FALSE.equals(entry.getValue())) {
                        node.send(entry.getKey(), new Request(node.getLamportClock(), node.getNodeInfo().getId()), false);
                        this.getCurrentCSDetails().incrementMsgCount();
                    }
                }
                node.incrLamportClock();
            }
        }

        while(!super.checkCSPermission());
    }

    @Override
    public String csLeave() {
        synchronized(node) {
            synchronized(this) {
                super.setRequestingCS(false);
                super.setUsingCS(false);

                CriticalSectionPreviousRunDetails newPrevRun = new CriticalSectionPreviousRunDetails(
                        node.getNodeInfo().getId(),
                        super.getCsCounter(),
                        super.getCurrentCSDetails().getCSExitTime(),
                        super.getCurrentCSDetails().getCSExitTimestamp()
                );

//                logger.info("Sending reply to " + super.getDifferedRequests().size()
//                        + " differed requests " + super.getDifferedRequests());
                for(int differedNodeId: super.getDifferedRequests()) {
                    node.send(differedNodeId,
                            new Reply(node.getLamportClock(), node.getNodeInfo().getId(), newPrevRun), true);
                    this.getCurrentCSDetails().incrementMsgCount(1);
                }
//                logger.info(super.getKeys().toString());

                super.clearDifferedRequests();

                CriticalSectionDetails prevCSDetails = super.addAndClearCurrentCSDetails();

                super.setPreviousCSDetails(newPrevRun);
                return prevCSDetails.toString();
            }
        }
    }

    private void sendReplyAndRequest(Message msg) {
        synchronized(node) {
            synchronized(this) {
                node.send(msg.getSourceNodeId(),
                        new Reply(node.getLamportClock(), node.getNodeInfo().getId(), super.getPreviousCSDetails()), true);
                node.send(msg.getSourceNodeId(), new Request(super.getCurrentCSDetails().getCSRequestTime(), node.getNodeInfo().getId()), true);

                this.getCurrentCSDetails().incrementMsgCount();
                this.getCurrentCSDetails().incrementMsgCount();

//                logger.info("[PASSING TO ANOTHER HIGHER PRIORITY NODE] " + msg.getSourceNodeId() +
//                        " (" + msg.getClock() + " " + super.getCurrentCSDetails().getCSRequestTime() + ")");
//                logger.info(super.getKeys().toString());
            }
        }
    }

    private void sendReply(Message msg) {
        node.send(msg.getSourceNodeId(),
                new Reply(node.getLamportClock(), node.getNodeInfo().getId(), super.getPreviousCSDetails()), true);
//        logger.info("[SEND REPLY TO ANOTHER NODE (not waiting on CS)] " + msg.getSourceNodeId());
//        logger.info(super.getKeys().toString());
    }

    private void differRequest(Message msg) {
        super.setDifferedRequests(msg.getSourceNodeId());
//        logger.info("[DIFFERING REQUEST] of " + msg.getSourceNodeId() +
//                " (" + msg.getClock() + " " + super.getCurrentCSDetails().getCSRequestTime() + ") or " +
//                "CS is being used (true/false): " + super.getUsingCS());
//        logger.info(super.getKeys().toString());
    }

    @Override
    public void processCSRequest(Message msg) {
        synchronized(node) {
            synchronized(this) {
                if(super.getUsingCS()) {
                    this.differRequest(msg);
                    return;
                }

                if(!super.getRequestingCS()) {
                    this.sendReply(msg);
                    return;
                }

                if(msg.getClock() < super.getCurrentCSDetails().getCSRequestTime()) {
                    // requesting node has lower clock (higher priority)
                    this.sendReplyAndRequest(msg);
                } else if(msg.getClock() == super.getCurrentCSDetails().getCSRequestTime() &&
                        msg.getSourceNodeId() < node.getNodeInfo().getId()) {
                    // same clock, then nodeId is tye breaker
                    this.sendReplyAndRequest(msg);
                } else {
                    // any other case differ
                    this.differRequest(msg);
                }
            }
        }
    }
}
