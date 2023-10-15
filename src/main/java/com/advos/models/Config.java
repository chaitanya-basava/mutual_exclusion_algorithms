package com.advos.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Config {
    public static final int INIT_DELAY = 5000;
    public static final int RETRY_CLIENT_CONNECTION_DELAY = 500;
    public static final long EXPIRATION_TIME = 1000;
    public static final String MESSAGE_DELIMITER = "[end]";
    private final int n;
    private final int meanInterRequestDelay;
    private final int meanCSExecutionTime;
    private final int maxNumRequests;
    private final Map<Integer, NodeInfo> nodes = new HashMap<>();

    public Config(int n, int meanInterRequestDelay, int meanCSExecutionTime, int maxNumRequests) {
        this.n = n;
        this.meanInterRequestDelay = meanInterRequestDelay;
        this.meanCSExecutionTime = meanCSExecutionTime;
        this.maxNumRequests = maxNumRequests;
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

    public int getMaxNumRequests() {
        return maxNumRequests;
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
                .append("\nNodes: ").append(this.getN())
                .append("\nminPerActive: ").append(this.getMeanInterRequestDelay())
                .append("\nmaxPerActive: ").append(this.getMeanCSExecutionTime())
                .append("\nminSendDelay: ").append(this.getMaxNumRequests());

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
