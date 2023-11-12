import re
import numpy as np
import matplotlib.pyplot as plt
from collections import defaultdict


def process_file(file_path):
    pattern = (r"Run: (\d+)|System Throughput: ([\d.]+) requests per second|Average Message Complexity: ([\d.]+) "
               r"messages per CS|Average Response Time: ([\d.]+) milliseconds per CS|Messages: (.+)")

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

    return data


def save_line_charts(_experiment_data):
    # Extracting metrics from the data
    avg_throughput = [np.mean([run_data['System Throughput'] for run_data in exp_data.values()])
                      for exp_data in _experiment_data]
    avg_message_complexity = [np.mean([run_data['Average Message Complexity'] for run_data in exp_data.values()])
                              for exp_data in _experiment_data]
    avg_response_time = [np.mean([run_data['Average Response Time'] for run_data in exp_data.values()])
                         for exp_data in _experiment_data]

    exps = [10, 100, 250, 500, 750, 1000]

    # Creating the line charts
    plt.figure(figsize=(15, 5))

    # System Throughput Line Chart
    plt.subplot(1, 3, 1)
    plt.plot(exps, avg_throughput, marker='o', color='b')
    plt.title('System Throughput (requests per second)')
    plt.xlabel('mean CS execution time')
    plt.ylabel('Throughput')

    # Average Message Complexity Line Chart
    plt.subplot(1, 3, 2)
    plt.plot(exps, avg_message_complexity, marker='o', color='g')
    plt.title('Average Message Complexity (messages per CS)')
    plt.xlabel('mean CS execution time')
    plt.ylabel('Message Complexity')

    # Average Response Time Line Chart
    plt.subplot(1, 3, 3)
    plt.plot(exps, avg_response_time, marker='o', color='r')
    plt.title('Average Response Time (per CS)')
    plt.xlabel('mean CS execution time')
    plt.ylabel('Response Time')

    plt.subplots_adjust(hspace=0.5)

    # Save the figure
    plt.savefig('./results/line_charts.png')
    plt.close()


def save_box_plots(_experiment_data):
    # Creating the box plots
    plt.figure(figsize=(15, 10))

    # System Throughput Box Plot
    plt.subplot(4, 1, 1)
    throughput_data = [[run_data['System Throughput'] for run_data in exp_data.values()]
                       for exp_data in _experiment_data]
    plt.boxplot(throughput_data, patch_artist=True)
    plt.title('System Throughput per Experiment (requests per second)')
    plt.xlabel('Experiment')
    plt.ylabel('Throughput')

    # Message Complexity Box Plot
    plt.subplot(4, 1, 2)
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

    # Message Complexity Box Plot
    plt.subplot(4, 1, 3)
    message_complexity_data = [[run_data['Average Message Complexity'] for run_data in exp_data.values()]
                               for exp_data in _experiment_data]
    plt.boxplot(message_complexity_data, patch_artist=True)
    plt.title('Average Message Complexity (messages per CS)')
    plt.xlabel('Experiment')
    plt.ylabel('Avg Message Complexity')

    # Average Response Time Box Plot
    plt.subplot(4, 1, 4)
    response_time_data = [[run_data['Average Response Time'] for run_data in exp_data.values()]
                          for exp_data in _experiment_data]
    plt.boxplot(response_time_data, patch_artist=True)
    plt.title('Average Response Time per Experiment (per CS)')
    plt.xlabel('Experiment')
    plt.ylabel('Response Time')

    plt.subplots_adjust(hspace=0.5)

    # Save the figure
    plt.savefig('./results/box_plots.png')
    plt.close()


experiment_data = []

for i in range(0, 6):
    experiment_data.append(process_file(f"./results/config{i}.txt" if i != 0 else "./results/config.txt"))

save_line_charts(experiment_data)
save_box_plots(experiment_data)
