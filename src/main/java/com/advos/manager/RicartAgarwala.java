package com.advos.manager;

import com.advos.cs.CriticalSection;
import com.advos.utils.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RicartAgarwala extends RoucairolCarvalhoManager {
    private static final Logger logger = LoggerFactory.getLogger(RicartAgarwala.class);

    public RicartAgarwala(CriticalSection cs, List<Integer> neighbours, Node node) {
        super(cs, neighbours, node);
        super.setKeys(new ConcurrentHashMap<>(neighbours.stream().
                collect(Collectors.toMap(k -> k, k -> false))));
        logger.info(super.getKeys().toString());
    }

    @Override
    public String csLeave() {
        synchronized(node) {
            synchronized(this) {
                String str = super.csLeave();
                super.getKeys().replaceAll((k, v) -> false);
                return str;
            }
        }
    }
}
