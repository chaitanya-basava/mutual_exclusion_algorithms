#!/bin/bash

rsa_path="~/.ssh/id_rsa_dc"
net_id="sxb220302"
config_base_path="/home/012/s/sx/$net_id"

config_file_name="config5"

scp -i "$rsa_path" "./main.py" "$net_id@dc01:$config_base_path/main.py"

dirs=$(ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id@dc01" "find $config_base_path/$config_file_name/ -maxdepth 1 -type d | sort")

for dir in $dirs
do
    if [ "$dir" = "$config_base_path/$config_file_name/" ]; then
        continue
    fi
    ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "$rsa_path" "$net_id@dc01" "python3 $config_base_path/main.py $dir"
done
