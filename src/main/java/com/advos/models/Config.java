package com.advos.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Config {
    public static final int INIT_DELAY = 5000;
    public static final int RETRY_CLIENT_CONNECTION_DELAY = 500;
    public static final int RETRY_MESSAGE_READING_DELAY = 500;
    public static final int RETRY_CS_PERMISSION_CHECK_DELAY = 2000;
    public static final String MESSAGE_DELIMITER = "<end>";
    private final int n;
    private final int meanInterRequestDelay;
    private final int meanCSExecutionTime;
    private final int maxCsRequests;
    private final Map<Integer, NodeInfo> nodes = new HashMap<>();

    public Config(int n, int meanInterRequestDelay, int meanCSExecutionTime, int maxNumRequests) {
        this.n = n;
        this.meanInterRequestDelay = meanInterRequestDelay;
        this.meanCSExecutionTime = meanCSExecutionTime;
        this.maxCsRequests = maxNumRequests;
    }

    public void populateNeighbours() {
        for(Map.Entry<Integer, NodeInfo> node: nodes.entrySet()) {
            for(Map.Entry<Integer, NodeInfo> neighbor: nodes.entrySet()) {
                if(Objects.equals(node.getKey(), neighbor.getKey())) continue;
                nodes.get(node.getKey()).addNeighbor(nodes.get(neighbor.getKey()));
            }
        }
    }

    public int getN() {
        return n;
    }

    public int getMeanInterRequestDelay() {
        return meanInterRequestDelay;
    }

    public int getMeanCSExecutionTime() {
        return meanCSExecutionTime;
    }

    public int getMaxCsRequests() {
        return maxCsRequests;
    }

    public NodeInfo getNode(int idx) {
        return nodes.get(idx);
    }

    public boolean checkNode(int idx) {
        return nodes.containsKey(idx);
    }

    public void setNode(int node, NodeInfo nodeIfo) {
        nodes.put(node, nodeIfo);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str
                .append("\n# Nodes: ").append(this.getN())
                .append("\nMean inter request delay: ").append(this.getMeanInterRequestDelay())
                .append("\nMean CS execution time: ").append(this.getMeanCSExecutionTime())
                .append("\n# CS requests: ").append(this.getMaxCsRequests());

        for(Map.Entry<Integer, NodeInfo> entry: nodes.entrySet()) {
            str
                    .append("\n---------------Node: ")
                    .append(entry.getKey())
                    .append("---------------\n")
                    .append(entry.getValue());
        }

        return str.toString();
    }

    public Map<Integer, NodeInfo> getNodes() {
        return nodes;
    }
}
