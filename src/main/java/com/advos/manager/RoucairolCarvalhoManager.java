package com.advos.manager;

import com.advos.MutualExclusionTesting;
import com.advos.cs.CriticalSection;
import com.advos.message.*;
import com.advos.models.Config;
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
        super.setRequestingCS(true);

        synchronized(node) {
            synchronized(this) {
                node.incrLamportClock();
                super.setCSRequestTime(node.getLamportClock());
                for (Map.Entry<Integer, Boolean> entry : super.getKeys().entrySet()) {
                    if(Boolean.FALSE.equals(entry.getValue())) {
                        node.send(entry.getKey(), new Request(node.getLamportClock(), node.getNodeInfo().getId()), false);
                    }
                }
            }
        }

        while(super.checkCSPermission()) {
            MutualExclusionTesting.sleep(Config.RETRY_CS_PERMISSION_CHECK_DELAY);
        }
    }

    @Override
    public void csLeave() {
        synchronized(node) {
            synchronized(this) {
                super.setRequestingCS(false);
                super.setUsingCS(false);

                for(int differedNodeId: super.getDifferedRequests()) {
                    node.send(differedNodeId, new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                    super.setKey(differedNodeId, false);
                }

                super.clearDifferedRequests();

                logger.info("\nCompleted using CS " + super.getCsCounter() +
                        " time(s)\nstart:" + super.getCSRequestTime() + " end: " + node.getLamportClock() + "\n");
            }
        }
    }

    @Override
    public void processCSRequest(Message msg) {
        synchronized(node) {
            synchronized(this) {
                if(super.getUsingCS()) {
                    super.setDifferedRequests(msg.getSourceNodeId());
                    return;
                }

                if(!super.getRequestingCS()) {
                    node.send(msg.getSourceNodeId(), new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                    return;
                }

                if(msg.getClock() < super.getCSRequestTime()) { // requesting node has lower clock (higher priority)
                    node.send(msg.getSourceNodeId(), new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                    node.send(msg.getSourceNodeId(), new Request(super.getCSRequestTime(), node.getNodeInfo().getId()), true);
                } else if(msg.getClock() == super.getCSRequestTime()) { // same clock
                    if(msg.getSourceNodeId() < node.getNodeInfo().getId()) {
                        node.send(msg.getSourceNodeId(), new Reply(node.getLamportClock(), node.getNodeInfo().getId()), true);
                        node.send(msg.getSourceNodeId(), new Request(super.getCSRequestTime(), node.getNodeInfo().getId()), true);
                    } else {
                        super.setDifferedRequests(msg.getSourceNodeId());
                    }
                } else {
                    super.setDifferedRequests(msg.getSourceNodeId());
                }
            }
        }
    }
}
