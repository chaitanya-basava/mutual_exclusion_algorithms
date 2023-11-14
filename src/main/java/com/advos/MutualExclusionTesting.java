package com.advos;

import com.advos.models.Config;
import com.advos.utils.ConfigParser;
import com.advos.utils.Node;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutualExclusionTesting {
    private static final Logger logger = LoggerFactory.getLogger(MutualExclusionTesting.class);
    private final Node node;
    private static String configFile;

    private static CommandLine parseArgs(String[] args) {
        Options options = new Options();

        Option nodeIdOption = new Option("id", "nodeId", true, "id of the node to be run");
        nodeIdOption.setRequired(true);
        options.addOption(nodeIdOption);

        Option configFileOption = new Option("c", "configFile", true, "config file path");
        configFileOption.setRequired(true);
        options.addOption(configFileOption);

        Option protocolOption = new Option("p", "protocol", true, "protocol to use");
        options.addOption(protocolOption);

        Option logPathOption = new Option("l", "log_file_path", true, "Path to log CS details");
        logPathOption.setRequired(true);
        options.addOption(logPathOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            logger.error(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        return null;
    }

    MutualExclusionTesting(String[] args) {
        CommandLine cmd = MutualExclusionTesting.parseArgs(args);
        int nodeId = Integer.parseInt(cmd.getOptionValue("nodeId"));
        String logPath = cmd.getOptionValue("log_file_path");
        int protocol = Integer.parseInt(cmd.getOptionValue("protocol", "1"));
        MutualExclusionTesting.configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(false);
        try {
            configParser.parseConfig(MutualExclusionTesting.configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Config config = configParser.getConfig();

        this.node = new Node(config, config.getNode(nodeId), logPath, protocol);
    }

    public void execute() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup, "Shutdown Listener"));

        logger.info("\n");
        this.node.startAlgorithm();
        this.node.stopAlgorithm();
        this.node.saveCSUsageDetails();

        System.exit(0);
    }

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void cleanup() {
        node.close();
    }
}
