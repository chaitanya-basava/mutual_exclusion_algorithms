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
            node.incrLamportClock();
            for (Map.Entry<Integer, Boolean> entry : super.getKeys().entrySet()) {
                if(Boolean.FALSE.equals(entry.getValue())) {
                    node.send(entry.getKey(), new Request(node.getLamportClock(), node.getNodeInfo().getId()));
                }
            }
        }

        while(super.checkCSPermission()) {
            MutualExclusionTesting.sleep(Config.RETRY_CS_PERMISSION_CHECK_DELAY);
        }

        super.setUsingCS(true);
    }

    @Override
    public void csLeave() {
        super.setRequestingCS(false);
        super.setUsingCS(false);
    }

}
