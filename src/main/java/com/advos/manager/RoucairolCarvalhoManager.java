package com.advos.manager;

import com.advos.MutualExclusionTesting;
import com.advos.cs.CriticalSection;
import com.advos.message.*;
import com.advos.models.Config;
import com.advos.models.CriticalSectionDetails;
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

                node.incrLamportClock();
                for (Map.Entry<Integer, Boolean> entry : super.getKeys().entrySet()) {
                    if(Boolean.FALSE.equals(entry.getValue())) {
                        node.send(entry.getKey(), new Request(node.getLamportClock(), node.getNodeInfo().getId()), false);
                        this.getCurrentCSDetails().incrementMsgCount();
                    }
                }
            }
        }

        while(!super.checkCSPermission()) {
            MutualExclusionTesting.sleep(Config.RETRY_CS_PERMISSION_CHECK_DELAY);
        }

        super.setCSUseStartTime(node.getLamportClock());
    }

    @Override
    public void csLeave() {
        synchronized(node) {
            synchronized(this) {
                super.closeCSDetails(node.getLamportClock());
                super.setRequestingCS(false);
                super.setUsingCS(false);

                logger.info("Sending reply to " + super.getDifferedRequests().size()
                        + " differed requests " + super.getDifferedRequests());
                for(int differedNodeId: super.getDifferedRequests()) {
                    node.send(differedNodeId, new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                    super.setKey(differedNodeId, false);
                    this.getCurrentCSDetails().incrementMsgCount();
                }
                logger.info(super.getKeys().toString());

                super.clearDifferedRequests();

                CriticalSectionDetails prevCSDetails = super.addAndClearCurrentCSDetails();
                logger.info("Critical Section Details: " + prevCSDetails.toString());
            }
        }
    }

    private void sendReplyAndRequest(Message msg) {
        synchronized(node) {
            synchronized(this) {
                node.send(msg.getSourceNodeId(), new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                node.send(msg.getSourceNodeId(), new Request(super.getCurrentCSDetails().getCSRequestTime(), node.getNodeInfo().getId()), true);

                this.getCurrentCSDetails().incrementMsgCount();
                this.getCurrentCSDetails().incrementMsgCount();

                logger.info("[PASSING TO ANOTHER HIGHER PRIORITY NODE] " + msg.getSourceNodeId() +
                        " (" + msg.getClock() + " " + super.getCurrentCSDetails().getCSRequestTime() + ")");
                logger.info(super.getKeys().toString());
            }
        }
    }

    private void sendReply(Message msg) {
        node.send(msg.getSourceNodeId(), new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
        logger.info("[SEND REPLY TO ANOTHER NODE (not waiting on CS)] " + msg.getSourceNodeId());
        logger.info(super.getKeys().toString());
    }

    private void differRequest(Message msg) {
        super.setDifferedRequests(msg.getSourceNodeId());
        logger.info("[DIFFERING REQUEST] of " + msg.getSourceNodeId() +
                " (" + msg.getClock() + " " + super.getCurrentCSDetails().getCSRequestTime() + ") or " +
                "CS is being used (true/false): " + super.getUsingCS());
        logger.info(super.getKeys().toString());
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

                if(msg.getClock() < super.getCurrentCSDetails().getCSRequestTime()) { // requesting node has lower clock (higher priority)
                    this.sendReplyAndRequest(msg);
                } else if(msg.getClock() == super.getCurrentCSDetails().getCSRequestTime()) { // same clock
                    if(msg.getSourceNodeId() < node.getNodeInfo().getId()) {
                        this.sendReplyAndRequest(msg);
                    } else {
                        this.differRequest(msg);
                    }
                } else {
                    this.differRequest(msg);
                }
            }
        }
    }
}
