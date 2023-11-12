import re
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict


def process_file(file_path):
    pattern = (r"Run: (\d+)|System Throughput: ([\d.]+) requests per second|Average Message Complexity: ([\d.]+) "
               r"messages per CS|Average Response Time: ([\d.]+) milliseconds per CS|Messages: (.+)|"
               r"Average Synchronization Delay: ([\d.]+) milliseconds|SyncDelays: (.+)")

    data = defaultdict(dict)
    with open(file_path, 'r') as file:
        for line in file:
            match = re.match(pattern, line.strip())
            if match:
                if match.group(1):  # Run number
                    current_run = int(match.group(1))
                elif match.group(2):  # System Throughput
                    data[current_run]['System Throughput'] = float(match.group(2))
                elif match.group(3):  # Average Message Complexity
                    data[current_run]['Average Message Complexity'] = float(match.group(3))
                elif match.group(4):  # Average Response Time
                    data[current_run]['Average Response Time'] = float(match.group(4))
                elif match.group(5):  # Messages
                    messages = list(map(int, match.group(5).split()))
                    data[current_run]['Messages'] = messages
                elif match.group(6):  # Average Synchronization Delay
                    data[current_run]['Average Synchronization Delay'] = float(match.group(6))
                elif match.group(7):  # Sync delays
                    delays = list(map(int, match.group(7).split()))
                    data[current_run]['SyncDelays'] = delays

    return data


def save_line_charts(_experiment_data, xlabel='mean CS execution time'):
    # Extracting metrics from the data
    avg_throughput = [np.mean([run_data['System Throughput'] for run_data in exp_data.values()])
                      for exp_data in _experiment_data]
    avg_message_complexity = [np.mean([run_data['Average Message Complexity'] for run_data in exp_data.values()])
                              for exp_data in _experiment_data]
    avg_response_time = [np.mean([run_data['Average Response Time'] for run_data in exp_data.values()])
                         for exp_data in _experiment_data]
    avg_synch_delay = [np.mean([run_data['Average Synchronization Delay'] for run_data in exp_data.values()])
                       for exp_data in _experiment_data]

    exps = [10, 100, 250, 500, 750, 1000]

    # Creating the line charts
    plt.figure(figsize=(15, 5))

    # System Throughput Line Chart
    plt.subplot(2, 1, 1)
    plt.plot(exps, avg_throughput, marker='o', color='b')
    plt.title('System Throughput (requests per second)')
    plt.xlabel(xlabel)
    plt.ylabel('Throughput')

    # Average Message Complexity Line Chart
    plt.subplot(2, 1, 2)
    plt.plot(exps, avg_message_complexity, marker='o', color='g')
    plt.title('Average Message Complexity (messages per CS)')
    plt.xlabel(xlabel)
    plt.ylabel('Message Complexity')

    plt.subplots_adjust(hspace=0.5)

    # Save the figure
    plt.savefig('./results/line_charts_1_d.png')
    plt.close()

    plt.figure(figsize=(15, 5))

    # Average Response Time Line Chart
    plt.subplot(2, 1, 1)
    plt.plot(exps, avg_response_time, marker='o', color='r')
    plt.title('Average Response Time (per CS)')
    plt.xlabel(xlabel)
    plt.ylabel('Response Time')

    # Average Sync Delay Line Chart
    plt.subplot(2, 1, 2)
    plt.plot(exps, avg_synch_delay, marker='o', color='r')
    plt.title('Average Synchronization Delay')
    plt.xlabel(xlabel)
    plt.ylabel('Synchronization Delay')

    plt.subplots_adjust(hspace=0.5)

    # Save the figure
    plt.savefig('./results/line_charts_2_d.png')
    plt.close()


def save_box_plots(_experiment_data):
    # Creating the box plots
    plt.figure(figsize=(15, 10))

    # Message Complexity Box Plot
    plt.subplot(2, 1, 1)
    message_complexity_data = []
    for exp_data in _experiment_data:
        te = []
        for run_data in exp_data.values():
            te += run_data['Messages']

        message_complexity_data.append(te)

    plt.boxplot(message_complexity_data, patch_artist=True)
    plt.title('Message Complexity (messages per CS)')
    plt.xlabel('Experiment')
    plt.ylabel('Message Complexity')

    plt.subplot(2, 1, 2)
    sync_delay_data = []
    for exp_data in _experiment_data:
        te = []
        for run_data in exp_data.values():
            te += run_data['SyncDelays']

        sync_delay_data.append(te)

    plt.boxplot(sync_delay_data, patch_artist=True)
    plt.title('Synchronization Delays')
    plt.xlabel('Experiment')
    plt.ylabel('Synchronization Delay')

    plt.subplots_adjust(hspace=0.5)

    # Save the figure
    plt.savefig('./results/box_plots_d.png')
    plt.close()


experiment_data = []

for i in range(0, 6):
    experiment_data.append(process_file(f"./results/configd_{i}.txt" if i != 0 else "./results/config.txt"))

save_line_charts(experiment_data, 'mean inter req delay')
save_box_plots(experiment_data)
