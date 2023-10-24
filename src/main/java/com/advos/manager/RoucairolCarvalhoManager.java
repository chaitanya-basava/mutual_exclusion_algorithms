package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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

    }

    @Override
    public void csLeave() {

    }

}
