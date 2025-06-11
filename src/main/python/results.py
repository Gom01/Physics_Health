import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os
import sys


# Write the content to results.csv

def plot_csv(file_path, x_col, y_col, x_label=None, y_label=None, title=None, output_filename=None, output_dir="images"):
    df = pd.read_csv(file_path)

    # Validate columns
    if x_col not in df.columns or y_col not in df.columns:
        raise ValueError(f"Columns '{x_col}' or '{y_col}' not found in file.")
    x = df[x_col].values
    y = df[y_col].values

    # Polynomial fit (using deg=6 as in previous successful plots)
    coeffs = np.polyfit(x, y, deg=4)
    poly_fn = np.poly1d(coeffs)

    x_smooth = np.linspace(x.min(), x.max(), 300)
    y_smooth = poly_fn(x_smooth)

    # Plot
    plt.figure(figsize=(12, 7))
    plt.plot(x, y, 'o', label='Simulation Data')
    plt.plot(x_smooth, y_smooth, '-', color='red', label='Polynomial Regression (deg=6)')
    plt.title(f"{title}")

    # Use custom labels if provided, otherwise default to column names
    plt.xlabel(x_label if x_label else x_col)
    plt.ylabel(y_label if y_label else y_col)

    plt.legend()
    plt.grid(True)
    plt.tight_layout()

    # Save
    os.makedirs(output_dir, exist_ok=True)
    # Use custom output filename if provided, otherwise default
    final_output_filename = output_filename if output_filename else f"{x_col}_vs_{y_col}.png"
    output_path = os.path.join(output_dir, final_output_filename)
    plt.savefig(output_path, dpi=300)
    plt.show()
    print(f"Saved to: {output_path}")

# Example usage demonstrating custom labels and output filename
if __name__ == "__main__":


    csv_path = "data_csv/varying_coop.csv"
    x_column = "final_coop"
    y_column = "varying_param_value"

    # Call plot_csv with custom labels and output filename
    plot_csv(
        file_path=csv_path,
        x_col=x_column,
        y_col=y_column,
        x_label="Cooperation Percentage(%)", # Custom x-axis label
        y_label="Cooperation Percentage (%)", # Custom y-axis label
        title="Cooperation vs Cooperation",
        output_filename="cooperation_cooperation_analysis2.png"
    )

    csv_path = "data_csv/varying_temptation.csv"
    x_column = "final_coop"
    y_column = "varying_param_value"

    # Call plot_csv with custom labels and output filename
    plot_csv(
    file_path=csv_path,
    x_col=x_column,
    y_col=y_column,
    x_label="Cooperation Percentage(%)", # Custom x-axis label
    y_label="Temptation ", # Custom y-axis label
    title="Cooperation vs temptation",
    output_filename="cooperation_temptation_analysis2.png")

    csv_path = "data_csv/varying_velocity.csv"
    x_column = "final_coop"
    y_column = "varying_param_value"

    # Call plot_csv with custom labels and output filename
    plot_csv(
    file_path=csv_path,
    x_col=x_column,
    y_col=y_column,
    x_label="Cooperation Percentage(%)", # Custom x-axis label
    y_label="Velocity ", # Custom y-axis label
    title="Cooperation vs velocity",
    output_filename="cooperation_velocity_analysis2.png"
    )

    csv_path = "data_csv/varying_txCoop.csv"
    x_column = "final_coop"
    y_column = "varying_param_value"

    # Call plot_csv with custom labels and output filename
    plot_csv(
    file_path=csv_path,
    x_col=x_column,
    y_col=y_column,
    x_label="Cooperation Percentage(%)", # Custom x-axis label
    y_label="varying_tx_coop ", # Custom y-axis label
    title="Cooperation vs varying_tx_coop",
    output_filename="cooperation_varying_tx_coop_analysis2.png"
)