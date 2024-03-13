import math
import sys

import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns


def calculate_accuracy(delta, eps):
    # 2 * ln(2/delta) / eps^2
    return 2 * math.log(2/delta) / eps**2


if __name__ == '__main__':
    results_dir = sys.argv[1]
    deltas = [0.1, 0.01, 0.001, 0.0001]
    epsilons = np.arange(0.001, 0.1, 0.0001)
    accuracies = []
    for d in deltas:
        accuracies.append([calculate_accuracy(d, eps) for eps in epsilons])
    accuracies = np.array(accuracies)

    plt.figure(figsize=(10, 4))
    sns.set_theme(style="whitegrid", font_scale=1.5)
    plt.yscale("log")
    for i in range(len(deltas)):
        # use log scale
        plt.plot(epsilons, accuracies[i], label=f"\u03B4={deltas[i]}")
    plt.legend()
    plt.xlabel("\u03B5")
    plt.ylabel("Iterations (log scale)")
    plt.savefig(f"{results_dir}/accuracy.pdf", bbox_inches='tight')

    plt.figure(figsize=(10, 3.1))
    sns.set_theme(style="whitegrid", font_scale=1.5)
    plt.yscale("log")
    for i in range(len(deltas)):
        # use log scale
        plt.plot(epsilons, accuracies[i], label=f"\u03B4={deltas[i]}")
    plt.legend()
    plt.xlabel("\u03B5")
    plt.ylabel("Iterations (log scale)")
    plt.yticks([1000, 10000, 100000, 1000000, 10000000])
    plt.savefig(f"{results_dir}/accuracy_compact.pdf", bbox_inches='tight')
