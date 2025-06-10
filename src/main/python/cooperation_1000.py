import pandas as pd
import matplotlib.pyplot as plt
import numpy as np

# Load the CSV results
df = pd.read_csv("results.csv")

# Extract x (initial coop) and y (final coop)
x = df['initial_coop'].values
y = df['final_coop'].values

# Fit a 5th-degree polynomial regression
coeffs = np.polyfit(x, y, deg=6)
poly_fn = np.poly1d(coeffs)

# Generate smooth x values for curve
x_smooth = np.linspace(x.min(), x.max(), 300)
y_smooth = poly_fn(x_smooth)

# Plot data and regression curve
plt.figure(figsize=(12, 7))
plt.plot(x, y, 'o', label='Simulation Data')
plt.plot(x_smooth, y_smooth, '-', color='red', label='Polynomial Regression (deg=6)')
plt.title("Initial vs Final Cooperation with Regression Curve")
plt.xlabel("Initial Cooperation (%)")
plt.ylabel("Final Cooperation (%)")
plt.legend()
plt.grid(True)
plt.tight_layout()

# Save and show
plt.savefig("images/coop_vs_init_regression.png", dpi=300)
plt.show()
