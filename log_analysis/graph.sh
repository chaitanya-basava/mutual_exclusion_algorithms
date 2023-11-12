#!/bin/bash

rsa_path="~/.ssh/id_rsa_dc"
net_id="sxb220302"
config_base_path="/home/012/s/sx/$net_id"

mkdir "./results/"

scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config1.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config2.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config3.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config4.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/config5.txt" ./results/

scp -i "$rsa_path" "$net_id@dc01:$config_base_path/configd_1.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/configd_2.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/configd_3.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/configd_4.txt" ./results/
scp -i "$rsa_path" "$net_id@dc01:$config_base_path/configd_5.txt" ./results/

python3 ./graph.py

rm -rf ./results/config.txt
rm -rf ./results/config1.txt
rm -rf ./results/config2.txt
rm -rf ./results/config3.txt
rm -rf ./results/config4.txt
rm -rf ./results/config5.txt

rm -rf ./results/configd_1.txt
rm -rf ./results/configd_2.txt
rm -rf ./results/configd_3.txt
rm -rf ./results/configd_4.txt
rm -rf ./results/configd_5.txt
