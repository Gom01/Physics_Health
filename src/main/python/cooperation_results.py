import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os
from scipy.optimize import curve_fit

def plot_csv(file_path, x_col, coop_col="final_coop", cluster_col="final_clusters",
             x_label=None, y1_label="Final Cooperation (%)", y2_label="Final Clusters",
             title=None, output_filename=None, output_dir="images", scale_x=False):

    df = pd.read_csv(file_path)
    if x_col not in df.columns or coop_col not in df.columns or cluster_col not in df.columns:
        raise ValueError("Required columns not found in the CSV file.")

    x = df[x_col].values * 100 if scale_x else df[x_col].values
    y1 = df[coop_col].values
    y2 = df[cluster_col].values

    def sigmoid(x, L, k, x0):
        return L / (1 + np.exp(-k * (x - x0)))

    def gaussian(x, a, mu, sigma):
        return a * np.exp(-((x - mu) ** 2) / (2 * sigma ** 2))

    # Fit sigmoid
    p0_sigmoid = [100, 0.1, np.median(x)]
    params_sigmoid, _ = curve_fit(sigmoid, x, y1, p0=p0_sigmoid, maxfev=10000)
    x0_crit = params_sigmoid[2]

    # Fit gaussian
    p0_gauss = [max(y2), np.mean(x), np.std(x)]
    params_gauss, _ = curve_fit(gaussian, x, y2, p0=p0_gauss, maxfev=10000)
    mu_crit = params_gauss[1]

    x_smooth = np.linspace(x.min(), x.max(), 300)
    y1_smooth = sigmoid(x_smooth, *params_sigmoid)
    y2_smooth = gaussian(x_smooth, *params_gauss)

    fig, ax1 = plt.subplots(figsize=(12, 7))

    color1 = 'tab:blue'
    ax1.set_xlabel(x_label if x_label else x_col, fontsize=12)
    ax1.set_ylabel(y1_label, color=color1, fontsize=12)
    ax1.plot(x, y1, 'o', label='Cooperation Data', color=color1, markersize=7, alpha=0.3)
    ax1.plot(x_smooth, y1_smooth, '-', color=color1, label='Coop Trend')
    ax1.tick_params(axis='y', labelcolor=color1)
    ax1.grid(True, which='both', linestyle='--', alpha=0.4)
    ax1.set_ylim(0, 100)


    # Right Y-axis
    ax2 = ax1.twinx()
    color2 = 'tab:red'
    ax2.set_ylabel(y2_label, color=color2, fontsize=12)
    ax2.plot(x, y2, 's', label='Cluster Data', color=color2, markersize=7, alpha=0.3)
    ax2.plot(x_smooth, y2_smooth, '--', color=color2, label='Cluster Trend')
    ax2.tick_params(axis='y', labelcolor=color2)



    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper left', fontsize=11)

    fig.suptitle(title if title else f"{coop_col} and {cluster_col} vs {x_col}", fontsize=14, fontweight='bold')
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])

    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, output_filename or "coop_clusters.png")
    plt.savefig(output_path, dpi=300)
    plt.show()

# Example usage
if __name__ == "__main__":
    plot_csv(
        file_path="data_csv/varying_cooperation_coop.csv",
        x_col="varying_param",
        title="Final Cooperation & Cluster Count vs Initial Cooperation | Coop : 6",
        x_label="Initial Cooperation (%)",
        output_filename="cooperation_results_coop.png",
        scale_x=True
    )
