import os
import re
import sys


class NodeData:
    def __init__(self, node_id, messages_exchanged, cs_req_start_timestamp,
                 cs_execution_start_timestamp, cs_execution_end_timestamp, prev_run_details):
        self.node_id = node_id
        self.messages_exchanged = messages_exchanged
        self.cs_req_start_timestamp = cs_req_start_timestamp
        self.cs_execution_start_timestamp = cs_execution_start_timestamp
        self.cs_execution_end_timestamp = cs_execution_end_timestamp
        self.prev_run_details = prev_run_details
        self.response_time = cs_execution_end_timestamp - cs_req_start_timestamp
        self.cs_execution_time = cs_execution_end_timestamp - cs_execution_start_timestamp
        self.synchronous_delay = cs_execution_start_timestamp - self.prev_run_details.prev_cs_end_timestamp

    def __str__(self):
        s = f"NodeId: {self.node_id}\n"
        s += f"Messages Exchanged: {self.messages_exchanged}\n"
        s += f"CS Request Start Timestamp: {self.cs_req_start_timestamp}\n"
        s += f"CS Execution Start Timestamp: {self.cs_execution_start_timestamp}\n"
        s += f"CS Execution End Timestamp: {self.cs_execution_end_timestamp}\n"
        s += f"Previous Run Details - NodeId: {self.prev_run_details.node_id}\n"
        s += f"Previous Run Details - CS Count: {self.prev_run_details.cs_count}\n"
        s += f"Previous Run Details - Prev CS End Timestamp: {self.prev_run_details.prev_cs_end_timestamp}\n"
        s += f"Response Time: {self.response_time}\n"
        s += f"CS Execution Time: {self.cs_execution_time}\n"
        s += f"Synchronous Delay: {self.synchronous_delay}\n"
        s += "------------------------------\n"

        return s


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
    )

    return node_data


def check_for_intersections(data_list):
    count = 0

    # Sort the list based on the cs_execution_start_timestamp
    sorted_data = sorted(data_list, key=lambda x: int(x.cs_execution_start_timestamp))

    # Initialize the end time of the previous interval
    prev_end_time = -1

    for i, data in enumerate(sorted_data):
        start_time = int(data.cs_execution_start_timestamp)
        end_time = int(data.cs_execution_end_timestamp)

        # Check for intersection with the previous interval
        if start_time < prev_end_time:
            print(f"Intersection found at Node {data.node_id} starting at {start_time}"
                  f" - cs count: {data.prev_run_details.cs_count}")
            count += 1

        # Update the end time of the previous interval to the max end time seen so far
        prev_end_time = max(prev_end_time, end_time)

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
    out_files = [f for f in os.listdir(directory) if f.endswith('.out')]

    filename = 'sample.txt'
    node_data_list = []
    with open(filename, 'r') as file:
        for line in file:
            node_data_object = parse_data(line)
            print(node_data_object)

            node_data_list.append(node_data_object)

    print(f"Found {check_for_intersections(node_data_list)} time(s) CS safety got violated")
    print("------------------------------\n")

    system_throughput = calculate_throughput(node_data_list)
    avg_msg_complexity = sum(node_data.messages_exchanged for node_data in node_data_list) / len(node_data_list)
    avg_response_time = sum(node_data.response_time for node_data in node_data_list) / len(node_data_list)

    print(f"System Throughput: {system_throughput} requests per second")
    print(f"Average Message Complexity: {avg_msg_complexity} messages per CS")
    print(f"Average Response Time: {avg_response_time} milliseconds per CS")
