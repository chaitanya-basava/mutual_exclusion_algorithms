package com.advos;

import com.advos.utils.ConfigParser;

public class Main {
    public static void main(String[] args) throws Exception {
        ConfigParser configParser = new ConfigParser(true);
        configParser.parseConfig("/Users/chaitanyabasava/IdeaProjects/mutual_exclusion_alogrithms/src/main/resources/config.txt");
    }
}
