import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from scipy.optimize import curve_fit
import os

# Define sigmoid function
def sigmoid(x, L, x0, k, b):
    return L / (1 + np.exp(-k * (x - x0))) + b

def plot_sigmoid_with_manual_jump(file_path, x_col, coop_col="final_coop",
                                  x_label="Initial Cooperation (%)",
                                  y_label="Final Cooperation (%)",
                                  title="Final Cooperation Curve with Phase Transition",
                                  output_filename="sigmoid_manual_jump.png",
                                  output_dir="images",
                                  scale_x=False,
                                  jump_x=22):  # You define the jump location

    # Load data
    df = pd.read_csv(file_path)
    if x_col not in df.columns or coop_col not in df.columns:
        raise ValueError("Required columns not found in the CSV file.")

    x = df[x_col].values * 100 if scale_x else df[x_col].values
    y = df[coop_col].values

    # Sort for consistent fitting
    sorted_idx = np.argsort(x)
    x = x[sorted_idx]
    y = y[sorted_idx]

    # Initial guess for sigmoid parameters: [L, x0, k, b]
    p0 = [max(y), np.median(x), 0.3, min(y)]

    try:
        popt, _ = curve_fit(sigmoid, x, y, p0, maxfev=10000)
        sigmoid_func = lambda x_: sigmoid(x_, *popt)
    except RuntimeError:
        print("Sigmoid fitting failed.")
        return

    # Smooth curve
    x_smooth = np.linspace(min(x), max(x), 500)
    y_smooth = sigmoid_func(x_smooth)

    # Plot
    plt.figure(figsize=(12, 7))
    plt.grid(True, which='both', linestyle='--', alpha=0.4)
    plt.plot(x, y, 'o', label='Cooperation Data', color='tab:blue', alpha=0.3)
    plt.plot(x_smooth, y_smooth, '-', label='Sigmoid Fit', color='tab:blue')
    plt.axvline(x=jump_x, color='blue', linestyle='--', linewidth=1.5)
    plt.annotate(f'Jump @ {jump_x:.0f}%', xy=(jump_x, 50),  # y=50 is arbitrary, adjust if needed
                 xytext=(jump_x + 2, 60),
                 arrowprops=dict(arrowstyle='->', color='blue'),
                 fontsize=10, color='blue')

    plt.xlabel(x_label, fontsize=12)
    plt.ylabel(y_label, fontsize=12)
    plt.title(title, fontsize=14, fontweight='bold')
    plt.ylim(0, 100)
    plt.legend()
    plt.tight_layout()

    os.makedirs(output_dir, exist_ok=True)
    output_path = os.path.join(output_dir, output_filename)
    plt.savefig(output_path, dpi=300)
    plt.show()


# Example usage
if __name__ == "__main__":
    plot_sigmoid_with_manual_jump(
        file_path="data_csv/varying_cooperation_15_30.csv",
        x_col="varying_param",
        title="Final Cooperation Curve with Phase Transition",
        output_filename="cooperation_results_manual_jump.png",
        scale_x=True,
        jump_x=22
    )
