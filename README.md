# Mutual exclusion algorithms

This is the implementation of the `Roucairol and Carvalho` and `Ricart Agarwal` distributed
mutual exclusion algorithms, implemented as course project for Advanced OS (CS 6378).

It makes use of Lamport’s scalar clock for timestamping the application messages,
which are the CS request messages in this application.

More micro details about the implementation requirements can be found in the project description [here](project2.pdf).

## Requirements
1. java@1.8.0_341
2. maven@3.9.4

## Steps to compile and run the project

This code works by generating an executable jar file of the project and then uploading it into the dcXX machines (utd servers).
It is used for invoking the application on the respective machines (passed via config file) and execute the MAP protocol node on each of them.

It uses maven for managing the build and packages and hence is a requirement for compiling and running the application smoothly.

1. `cd` into the project's root directory
2. Execute the `launcher.sh` script as following to generate the jar and start the MAP protocol
```
bash launcher.sh <project_path> <path to config file on local> <project directory on dc machine> <netid> <rsa file path> <run id>
```

**Example:**
```
bash launcher.sh "/Users/chaitanyabasava/IdeaProjects/mutual_exclusion_algorithms" "/Users/chaitanyabasava/IdeaProjects/mutual_exclusion_algorithms/src/main/resources/configd_5.txt" "/home/012/s/sx/sxb220302/adv_os_proj_1" sxb220302 "~/.ssh/id_rsa_dc" 5
```

we can also pass the jar file path as `<project_path>` in case the jar has already been built.

**Example:**
```
bash launcher.sh "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/target/chandy_and_lamport_snapshot_protocol-1.0-SNAPSHOT-jar-with-dependencies.jar" "/Users/chaitanyabasava/Documents/chandy_and_lamport_snapshot_protocol/src/main/resources/config2.txt" "/home/012/s/sx/sxb220302/adv_os_proj_1" sxb220302 "~/.ssh/id_rsa_dc"
```

**NOTE:** This assumes you have done the steps to enable passwordless login to dcXX machine on your local machine and have the corresponding rsa pem file.

## Steps to clean up
You can use the cleanup script to clean up all the dangling processes (if any).
```
bash cleanup.sh <netid> <rsa file path>
```

**Example:**
```
bash cleanup.sh sxb220302 "~/.ssh/id_rsa_dc"
```

**NOTE:** The termination detection of the protocol should take care of this step though.

## Application execution

You may directly execute each node of the application manually using the following command
```
java -jar mutual_exclusion_algorithms-1.0.jar com.advos.MutualExclusionTesting -c <config_file_path> -l <log_path> -id <node_id> -p <1 or 2>
```

**NOTE:** The `hostname` and `port` are specified in the config file!

Protocol number:
1. Roucairol Carvalho
2. Ricart Agarwala

**Example:**
```
java -jar /home/012/s/sx/sxb220302/adv_os_proj_1/mutual_exclusion_algorithms-1.0.jar com.advos.MutualExclusionTesting -c /home/012/s/sx/sxb220302/adv_os_proj_1/config.txt -l /home/012/s/sx/sxb220302/adv_os_proj_1/../config5/9/ -id 9 -p 2
```
