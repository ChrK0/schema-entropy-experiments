import os
import sys

import matplotlib
import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from matplotlib import rcParams


def heatmap(data, rows, filename, height_percent=1.0):
    data = data.loc[data['rows'].isin(rows)]

    # create heatmap from runtimes
    akws = {"ha": 'right', "va": "top"}
    data = data.pivot(index="rows", columns="monte carlo", values="runtime")
    
    width, height = rcParams['figure.figsize']
    rcParams['figure.figsize'] = width, height_percent * height
    sns.set(rc={'figure.figsize': (width, height_percent * height)})
    palette = sns.light_palette("#ffcb3d", as_cmap=True)
    ax = sns.heatmap(data, cmap=palette, annot=True, fmt=".1f", cbar=False, annot_kws=akws)
    # show xlable at top
    plt.tick_params(axis='both', which='major', labelsize=12, labelbottom=False, bottom=False, top=False, labeltop=True)
    ax.xaxis.set_label_position('top') 
    ax.set(xlabel="Monte Carlo Iterations", ylabel="Number of Rows")
    ax.set_xticklabels(['1000', '10\'000', '100\'000', '1\'000\'000'], position=(0, 0.97))
    for t in ax.texts:
        trans = t.get_transform()
        offs = matplotlib.transforms.ScaledTranslation(0.46, -0.27, matplotlib.transforms.IdentityTransform())
        t.set_transform(offs + trans)

    border_width = 4
    ax.axhline(y=0, color='k', linewidth=border_width)
    ax.axhline(y=len(rows), color='k', linewidth=border_width)
    
    ax.axvline(x=0, color='k', linewidth=border_width)
    
    ax.axvline(x=4, color='k', linewidth=border_width)

    plt.savefig(filename, bbox_inches='tight')
    plt.close()


def div_or_nan(x, divisor):
    if isinstance(x, int) or x.isdigit():
        return int(x) / divisor
    else:
        return float('nan')


if __name__ == "__main__":
    table = sys.argv[1]
    datapath = sys.argv[2]
    results_dir = sys.argv[3]

    # read csv
    with open(datapath) as f:
        runtimes = pd.read_csv(f)

    df = pd.DataFrame(data=runtimes)
    df["runtime"] = df["runtime"].apply(div_or_nan, args=(1000,))

    rows10 = range(10, 160, 10)
    rows20 = [10, 40, 70, 100, 130, 150]

    if not os.path.exists(f"{results_dir}/{table}"):
        os.makedirs(f"{results_dir}/{table}", exist_ok=True)
    
    heatmap(df, rows10, f"{results_dir}/{table}/{table}_runtimes.pdf")
    heatmap(df, rows20, f"{results_dir}/{table}/{table}_runtimes_compact.pdf", height_percent=0.4)
