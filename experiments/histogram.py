import os
import sys

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns


if __name__ == "__main__":
    table = sys.argv[1]
    results_dir = sys.argv[3]

    with open(sys.argv[2]) as f:
        entropy = pd.read_csv(f, header=None)

    # create lineplot from runtimes
    df = pd.DataFrame(data=entropy)

    # append all columns in df to list
    clmns = []
    for i in range(0, len(df.columns)):
        clmns.extend(df.iloc[:, i])

    custom = {"grid.color": "black", "grid.linestyle": "-", "grid.linewidth": 0.5}
    sns.set_style("whitegrid", rc={
        'xtick.bottom': True,
        'ytick.left': True,
    })
    f, (ax_top, ax_bottom) = plt.subplots(ncols=1, nrows=2, sharex='all', gridspec_kw={'hspace': 0.15, 'height_ratios': [1, 3], 'wspace': 0.1, 'width_ratios': [1]}, figsize=(8, 3))
    # create histplot from clmns
    sns.set(font_scale=1.5)
    print("Number of cells:", len(clmns))
    print("Number of cells with entropy 1:", clmns.count(1.0))
    bins = [0.6, 0.625, 0.65, 0.675, 0.7, 0.725, 0.75, 0.775, 0.8, 0.825, 0.85, 0.875, 0.9, 0.925, 0.95, 0.975, 1.0, 1.025]
    sns.histplot(data=clmns, bins=bins, color="g", ax=ax_top)
    sns.histplot(data=clmns, bins=bins, color="g", ax=ax_bottom)

    ax_top.set_ylim(bottom=1080, top=1090)
    ax_top.set_xlim(left=0.5)
    ax_top.tick_params(bottom=False)
    ax_top.set(xticklabels=[]) 
    ax_top.set(xlabel=None)
    ax_bottom.set_ylim(0, 31)
    ax_bottom.set(yticks=[10, 20, 30])

    sns.despine(ax=ax_bottom)
    sns.despine(ax=ax_top, bottom=True)

    ax = ax_top
    d = .015  # how big to make the diagonal lines in axes coordinates
    # arguments to pass to plot, just so we don't keep repeating them
    kwargs = dict(transform=ax.transAxes, color='k', clip_on=False)
    ax.plot((0.01-d, 1+d), (0, 0), **kwargs)

    ax2 = ax_bottom
    kwargs.update(transform=ax2.transAxes)  # switch to the bottom axes
    ax2.plot((0.01-d, 1+d), (1.0075-d, 1.0075-d), **kwargs)
    # log scale
    ax_top.set(ylabel="")
    ax_bottom.set(ylabel="", xlabel="Entropy")
    ax_bottom.set(xticks=[0.5, 0.6, 0.7, 0.8, 0.9, 1.0125], xticklabels=["0.5", "0.6", "0.7", "0.8", "0.9", "1.0"])

    if not os.path.exists(f"{results_dir}/{table}"):
        os.makedirs(f"{results_dir}/{table}", exist_ok=True)

    f.text(0.00, 0.5, "Frequency", va='center', rotation='vertical')
    plt.savefig(f"{results_dir}/{table}/{table}_hist.pdf", bbox_inches='tight')
