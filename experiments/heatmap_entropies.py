import os
import sys

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from matplotlib import pyplot
from matplotlib.backends.backend_pdf import PdfPages


def plot_heatmap(input, out, values, normalize, vmin=None, cbar=False):
    annot = values if values else None
    fontsize = 10

    if normalize:
        vmin = 0.5

    # read csv
    with open(input) as f:
        entropy = pd.read_csv(f, header=None)
        num_lines = len(entropy)
        if num_lines > 25:
            fontsize = fontsize * 25 / num_lines

    # create lineplot from runtimes
    df = pd.DataFrame(data=entropy)
        
    # create heatmap from dataframe with square cells
    custom_palette = sns.color_palette("Blues_r", n_colors=400) + ["#FFFFFF"] 
    
    if cbar:
        pyplot.figure(figsize=(150, 150))
        fig, (cax, ax) = plt.subplots(nrows=2,  gridspec_kw={"height_ratios": [0.025, 1]})
        hm = sns.heatmap(df, ax=ax, cbar=False, xticklabels=False, yticklabels=False, cmap=custom_palette, vmin=vmin, vmax=1, annot=annot, annot_kws={"size": fontsize})
        hm.tick_params(axis='both', which='major', labelsize=10, labelbottom=False, bottom=False, top=False, labeltop=True)
        fig.colorbar(ax.get_children()[0], cax=cax, orientation="horizontal")

    else:
        ax = sns.heatmap(df, cbar=False, xticklabels=False, yticklabels=False, cmap=custom_palette, vmin=vmin, vmax=1, annot=annot, annot_kws={"size": fontsize})
    for i in range(len(df.columns)):
        ax.vlines(i, 0, 150, color='darkgray', linestyles='solid', lw=0.5)

    # add frames
    max_x = len(df.columns)
    max_y = len(df.index)
    ax.hlines(0, 0, max_x, color='black', linestyles='solid', lw=2)
    ax.hlines(max_y, 0, max_x, color='black', linestyles='solid', lw=3)
    ax.vlines(0, 0, max_y, color='black', linestyles='solid', lw=3)
    ax.vlines(max_x, 0, max_y, color='black', linestyles='solid', lw=3)
    plt.savefig(out, bbox_inches='tight')
    print("saved to", out)
    print("min", df.min().min())
    plt.clf()


def plot_zoom(table, zoomed_entropies_path, zoomed_table_path, results_dir):
    if table != "satellites":
        print("zooming only available for table satellites")
        return

    with open(zoomed_entropies_path) as f:
        entropy_zooms = pd.read_csv(f, header=None)

    with open(zoomed_table_path) as f:
        annot = pd.read_csv(f, header=None)
    
    custom_palette = sns.color_palette("Blues_r", n_colors=400) + ["#FFFFFF"] 

    # create dataframe from last 3 columns
    clmnsdf = [2, 2, 5, 2, 7]
    clmns = [2, 8, 5, 8, 7]
    df2 = pd.DataFrame(data=entropy_zooms.iloc[:, clmnsdf])
    annotdf = pd.DataFrame(data=annot.iloc[:, clmns])
        
    # create heatmap from dataframe with square cells
    ax2 = sns.heatmap(df2, annot_kws={'size': 10}, xticklabels=["MeanRadius", "...", "DiscoveredBy", "...", "Planet"], yticklabels=False, cbar=False, annot=annotdf, fmt='', cmap=custom_palette)
    ax2.tick_params(axis='both', which='major', labelsize=10, labelbottom=False, bottom=False, top=False, labeltop=True)
    plt.savefig(f"{results_dir}/{table}/{table}_zoom.pdf")


def plot_relation(input, out):
    with open(input, encoding="utf8") as f:
        data = pd.read_csv(f, header=None)

    df = pd.DataFrame(data=data)

    fig, ax = plt.subplots(figsize=(12, 4))
    ax.axis('tight')
    ax.axis('off')
    ax.table(cellText=df.values, loc='center')

    pp = PdfPages(out)
    pp.savefig(fig, bbox_inches='tight')
    pp.close()


if __name__ == "__main__":
    table = sys.argv[1]
    datapath = sys.argv[2]
    results_dir = sys.argv[3]
    rest_args = sys.argv[3:]
    bar = "bar" in rest_args
    zoom = "zoom" in rest_args
    values = "values" in rest_args
    normalize = "normalize" in rest_args
    relation = "relation" in rest_args

    if not os.path.exists(f"{results_dir}/{table}"):
        os.makedirs(f"{results_dir}/{table}", exist_ok=True)

    if relation:
        plot_relation(datapath, f"{results_dir}/{table}/{table}.pdf")
        quit()

    if bar:
        plot_heatmap(datapath, f"{results_dir}/{table}/{table}_heatmap_entropies.pdf", values, normalize, cbar=True)
    else:
        plot_heatmap(datapath, f"{results_dir}/{table}/{table}_heatmap_entropies.pdf", values, normalize)

    if zoom:
        plot_zoom(table, rest_args[rest_args.index("zoom") + 1], rest_args[rest_args.index("zoom") + 2], results_dir)
