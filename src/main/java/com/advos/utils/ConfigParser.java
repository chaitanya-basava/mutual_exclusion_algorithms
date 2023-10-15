package com.advos.utils;

import com.advos.models.Config;
import com.advos.models.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private Config config = null;
    private final boolean verbose;
    private static final Logger logger = LoggerFactory.getLogger(ConfigParser.class);

    public ConfigParser(boolean verbose) {
        this.verbose = verbose;
    }

    public void parseConfig(String fileName) throws Exception {
        String line;
        int lineCount = 0;
        Map<String, List<Integer>> hostPortMap = new HashMap<>();

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while((line = br.readLine()) != null) {
                line = line.trim();

                // check for begins with unsigned integer
                Pattern pattern = Pattern.compile("^\\d+");
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find()) continue;

                // remove string after '#' (if any)
                line = line.split("#")[0].trim();

                // split the string delimited by space(s)
                String[] tokens = line.split("\\s+");
                List<String> validTokens = new ArrayList<>();
                for (String token : tokens) {
                    if (!token.isEmpty()) {
                        validTokens.add(token);
                    }
                }

                if (validTokens.isEmpty()) continue;

                if (lineCount == 0) {
                    // first valid line parsing logic (0)
                    try {
                        if(validTokens.size() != 4) {
                            throw new NumberFormatException("not valid first line, has only "
                                    + validTokens.size() + " numbers, need 4 elements");
                        }

                        this.config = new Config(
                                Integer.parseInt(validTokens.get(0)),
                                Integer.parseInt(validTokens.get(1)),
                                Integer.parseInt(validTokens.get(2)),
                                Integer.parseInt(validTokens.get(3))
                        );
                    } catch (NumberFormatException e) {
                        if(this.verbose) logger.error(e.getMessage());
                        continue;
                    }
                } else if (this.config != null && lineCount <= this.config.getN()) {
                    // next n valid lines parsing logic (1...n)

                    int nodeID;
                    int listenPort;
                    String hostName;
                    try {
                        if(validTokens.size() != 3) {
                            throw new NumberFormatException("not valid line, need 3 elements, has "
                                    + validTokens.size());
                        }

                        nodeID = Integer.parseInt(validTokens.get(0));
                        hostName = validTokens.get(1);
                        listenPort = Integer.parseInt(validTokens.get(2));

                        if(this.config.checkNode(nodeID)) throw new Exception("node " + nodeID + " already added");
                        if(
                                hostPortMap.containsKey(hostName) &&
                                        hostPortMap.get(hostName).contains(listenPort)
                        ) throw new Exception("host: " + hostName + " and port: " + listenPort + " already taken");
                    } catch (Exception e) {
                        if(this.verbose) logger.error(e.getMessage());
                        continue;
                    }

                    this.config.setNode(nodeID, new NodeInfo(nodeID, hostName, listenPort));
                    if(hostPortMap.containsKey(hostName)) hostPortMap.get(hostName).add(listenPort);
                    else {
                        List<Integer> temp = new ArrayList<>();
                        temp.add(listenPort);
                        hostPortMap.put(hostName, temp);
                    }

                } else if (this.config != null && lineCount > this.config.getN()) break;

                lineCount++;
            }

            config.populateNeighbours();

            if(this.verbose) {
                logger.info("Parsed Config file is as follows:" + getConfig().toString());
            }
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    public Config getConfig() {
        return this.config;
    }

    public static void main(String[] args) throws Exception {
        ConfigParser configParser = new ConfigParser(true);
        configParser.parseConfig(
                Objects.requireNonNull(ConfigParser.class.
                                getClassLoader().
                                getResource("config.txt")).
                        getPath()
        );
    }
}
