import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
import os

# Define model functions
def exp_decay(x, a, b, c):
    return a * np.exp(-b * x) + c

def alt_decay(x, a, b, c):
    """Alternative decay function (like inverse sigmoid)."""
    return a / (1 + b * x) + c

def fit_and_plot(file_path, x_col, coop_col="final_coop", cluster_col="final_clusters",
                 x_label="X", title="Cooperation and Cluster Trend Fitting",
                 output_filename="fitted_plot.png", output_dir="images"):

    # Load data
    df = pd.read_csv(file_path)
    if x_col not in df or coop_col not in df or cluster_col not in df:
        print("Missing required columns.")
        return

    x = df[x_col].values
    y_coop = df[coop_col].values
    y_cluster = df[cluster_col].values

    # Fit cooperation with alt decay (smooth inverse shape)
    try:
        popt_coop, _ = curve_fit(alt_decay, x, y_coop, p0=[100, 0.5, 0])
        coop_func = lambda x_val: alt_decay(x_val, *popt_coop)
    except RuntimeError:
        print("Cooperation fit failed.")
        return

    # Fit cluster with exponential decay
    try:
        popt_cluster, _ = curve_fit(exp_decay, x, y_cluster, p0=[6, 1, 0])
        cluster_func = lambda x_val: exp_decay(x_val, *popt_cluster)
    except RuntimeError:
        print("Cluster fit failed.")
        return

    # Smooth evaluation
    x_smooth = np.linspace(min(x), max(x), 300)
    y_coop_smooth = coop_func(x_smooth)
    y_cluster_smooth = cluster_func(x_smooth)

    # Plotting
    fig, ax1 = plt.subplots(figsize=(12, 7))
    ax1.grid(True, which='both', linestyle='--', linewidth=0.5, alpha=0.7)

    ax1.set_xlabel(x_label)
    ax1.set_ylabel("Final Cooperation (%)", color='tab:blue')
    ax1.set_ylim(0, 100)
    ax1.plot(x, y_coop, 'o', color='tab:blue', alpha=0.3, label="Cooperation Data")
    ax1.plot(x_smooth, y_coop_smooth, '-', color='tab:blue', label="Coop Trend (Decay)")
    ax1.tick_params(axis='y', labelcolor='tab:blue')

    # Optional: find and annotate the inflection or steepest drop region
    deriv = np.gradient(y_coop_smooth, x_smooth)
    steepest_idx = np.argmin(deriv)
    x_drop = x_smooth[steepest_idx]
    y_drop = y_coop_smooth[steepest_idx]

    ax1.axvline(x=x_drop, color='gray', linestyle='--', linewidth=1.5)
    ax1.annotate(f'Steep Drop: {x_drop:.2f}', xy=(x_drop, y_drop),
                 xytext=(x_drop + 0.2, y_drop + 5),
                 arrowprops=dict(arrowstyle="->", color='gray'),
                 fontsize=10, color='gray')

    # Plot cluster data on second y-axis
    ax2 = ax1.twinx()
    ax2.set_ylabel("Final Clusters", color='tab:red')
    ax2.plot(x, y_cluster, 's', color='tab:red', alpha=0.3, label="Cluster Data")
    ax2.plot(x_smooth, y_cluster_smooth, '--', color='tab:red', label="Cluster Trend (Exp Decay)")
    ax2.tick_params(axis='y', labelcolor='tab:red')

    # Combine legends
    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc='upper right')

    # Title and save
    fig.suptitle(title, fontsize=14, fontweight='bold')
    fig.tight_layout(rect=[0, 0.03, 1, 0.95])

    os.makedirs(output_dir, exist_ok=True)
    out_path = os.path.join(output_dir, output_filename)
    plt.savefig(out_path, dpi=300)
    plt.show()

    return coop_func, cluster_func

# Example usage
if __name__ == "__main__":
    fit_and_plot(
        file_path="data_csv/varying_temptation.csv",
        x_col="varying_param",
        x_label="Temptation (T)",
        title="Final Cooperation & Cluster Count vs Temptation",
        output_filename="temptation_results.png"
    )
