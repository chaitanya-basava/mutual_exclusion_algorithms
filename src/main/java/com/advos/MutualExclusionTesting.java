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
    private final int nodeId;
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

        Option verboseOption = new Option("v", "verbose", false, "Program verbosity");
        options.addOption(verboseOption);

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
        boolean verbose = cmd.hasOption("v");
        this.nodeId = Integer.parseInt(cmd.getOptionValue("nodeId"));
        MutualExclusionTesting.configFile = cmd.getOptionValue("configFile");

        ConfigParser configParser = new ConfigParser(verbose);
        try {
            configParser.parseConfig(MutualExclusionTesting.configFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Config config = configParser.getConfig();

        this.node = new Node(config, config.getNode(this.nodeId));
    }

    public void execute() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Terminating node " + this.nodeId + "!!!");
            this.cleanup();
            logger.info("\n");
        }, "Shutdown Listener"));

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
