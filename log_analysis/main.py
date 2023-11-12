import os
import re
import sys


class NodeData:
    def __init__(self, node_id, messages_exchanged, cs_req_start_timestamp,
                 cs_execution_start_timestamp, cs_execution_end_timestamp, prev_run_details,
                 cs_req_start_time,
                 cs_execution_start_time, cs_execution_end_time):
        self.node_id = node_id
        self.messages_exchanged = messages_exchanged
        self.cs_req_start_timestamp = cs_req_start_timestamp
        self.cs_execution_start_timestamp = cs_execution_start_timestamp
        self.cs_execution_end_timestamp = cs_execution_end_timestamp
        self.prev_run_details = prev_run_details
        self.response_time = cs_execution_end_timestamp - cs_req_start_timestamp
        self.cs_execution_time = cs_execution_end_timestamp - cs_execution_start_timestamp
        self.synchronous_delay = abs(cs_execution_start_timestamp - self.prev_run_details.prev_cs_end_timestamp)
        self.cs_req_start_time = cs_req_start_time
        self.cs_execution_start_time = cs_execution_start_time
        self.cs_execution_end_time = cs_execution_end_time

    def __str__(self):
        t = f"NodeId: {self.node_id}\n"
        t += f"Messages Exchanged: {self.messages_exchanged}\n"
        t += f"CS Request Start Timestamp: {self.cs_req_start_timestamp}\n"
        t += f"CS Execution Start Timestamp: {self.cs_execution_start_timestamp}\n"
        t += f"CS Execution End Timestamp: {self.cs_execution_end_timestamp}\n"
        t += f"Previous Run Details - NodeId: {self.prev_run_details.node_id}\n"
        t += f"Previous Run Details - CS Count: {self.prev_run_details.cs_count}\n"
        t += f"Previous Run Details - Prev CS End Timestamp: {self.prev_run_details.prev_cs_end_timestamp}\n"
        t += f"Response Time: {self.response_time}\n"
        t += f"CS Execution Time: {self.cs_execution_time}\n"
        t += f"Synchronous Delay: {self.synchronous_delay}\n"
        t += "------------------------------\n"

        return t


class PrevRunDetails:
    def __init__(self, node_id, cs_count, prev_cs_end_timestamp):
        self.node_id = node_id
        self.cs_count = cs_count
        self.prev_cs_end_timestamp = prev_cs_end_timestamp


def parse_line(_parts):
    _dict = {}
    for part in _parts:
        key_value = part.split(':')
        if len(key_value) == 2:
            key, value = key_value
            value = re.sub(r'\D+$', '', value.strip())
            _dict[key.strip()] = value.strip()

    return _dict


def parse_data(_line):
    # Split the string by '----' to separate the data
    data_dict = parse_line(_line.split('----'))

    # Extract previous run details after splitting by ';;;;'
    prev_data_dict = parse_line(_line.split('----')[-2].split(';;;;'))

    # Create the PrevRunDetails object
    prev_run_details = PrevRunDetails(
        node_id=int(prev_data_dict.get('Node Id')),
        cs_count=int(prev_data_dict.get('cs count')),
        prev_cs_end_timestamp=int(prev_data_dict.get('timestamp'))
    )

    # Create the NodeData object
    node_data = NodeData(
        node_id=int(data_dict.get('Node Id')),
        messages_exchanged=int(data_dict.get('Messages exchanged')),
        cs_req_start_timestamp=int(data_dict.get('CS request start timestamp')),
        cs_execution_start_timestamp=int(data_dict.get('CS execution start timestamp')),
        cs_execution_end_timestamp=int(data_dict.get('CS execution end timestamp')),
        prev_run_details=prev_run_details,
        cs_req_start_time=int(data_dict.get('CS request start time')),
        cs_execution_start_time=int(data_dict.get('CS execution start time')),
        cs_execution_end_time=int(data_dict.get('CS execution end time')),
    )

    return node_data


def check_for_intersections(data_list):
    count = 0

    for i in range(len(data_list)):
        for j in range(i + 1, len(data_list)):
            start1 = int(data_list[i].cs_execution_start_time)
            end1 = int(data_list[i].cs_execution_end_time)
            start2 = int(data_list[j].cs_execution_start_time)
            end2 = int(data_list[j].cs_execution_end_time)

            # Check if the current pair of intervals intersect
            if start1 < end2 and start2 < end1:
                count += 1
                print(f"Intersection found between Node {data_list[i].node_id} "
                      f"({start1} to {end1}) and Node {data_list[j].node_id} "
                      f"({start2} to {end2})")

    return count


def calculate_throughput(data_list):
    if not data_list:
        return 0

    # Initialize the earliest and latest timestamps
    earliest_start = float('inf')
    latest_end = 0

    # Iterate over all entries to find the earliest start time and the latest end time
    for data in data_list:
        start_time = int(data.cs_execution_start_timestamp)
        end_time = int(data.cs_execution_end_timestamp)
        earliest_start = min(earliest_start, start_time)
        latest_end = max(latest_end, end_time)

    # Calculate the total time period in milliseconds
    total_time_period = latest_end - earliest_start

    # Convert total_time_period to seconds if needed (timestamps are in milliseconds)
    total_time_period_seconds = total_time_period / 1000

    # Count the total number of completed requests
    number_of_requests = len(data_list)

    # Calculate the throughput (requests per second)
    if total_time_period_seconds > 0:
        throughput = number_of_requests / total_time_period_seconds
    else:
        throughput = 0

    return throughput


if __name__ == '__main__':
    directory = sys.argv[1]
    node_data_list = []
    out_files = [f for f in os.listdir(directory) if f.endswith('.out')]

    print(out_files)

    for filename in out_files:
        with open(f"{directory}/{filename}", 'r') as file:
            for line in file:
                node_data_object = parse_data(line)
                # print(node_data_object)

                node_data_list.append(node_data_object)

    print(f"Found {check_for_intersections(node_data_list)} time(s) CS safety got violated")
    print("------------------------------\n")

    system_throughput = calculate_throughput(node_data_list)
    avg_msg_complexity = sum(node_data.messages_exchanged for node_data in node_data_list) / len(node_data_list)
    avg_response_time = sum(node_data.response_time for node_data in node_data_list) / len(node_data_list)

    config = directory.split('/')[-2]
    run = int(directory.split('/')[-1])

    s = f"Run: {run}\n"
    s += f"System Throughput: {system_throughput} requests per second\n"
    s += f"Average Message Complexity: {avg_msg_complexity} messages per CS\n"
    s += f"Average Response Time: {avg_response_time} milliseconds per CS\n"
    s += "Messages: " + " ".join([str(node_data.messages_exchanged) for node_data in node_data_list]) + "\n"

    file_name = f"./{config}.txt"
    with open(file_name, 'w' if run == 1 else 'a') as file:
        file.write(s)

    with open(file_name, 'r') as file:
        print(file.read())
