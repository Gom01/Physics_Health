# 🧠 Evolution of Cooperation – Scala Simulation

This project simulates how cooperation (like in the Prisoner's Dilemma) can grow or disappear in a group of moving agents. It includes both a batch simulation system and a real-time ScalaFX user interface.

## 🌍 Project Context: Adoption of Rules in Societies

I created this simulation to study how behaviors spread in a population — especially in situations like following or ignoring health rules.

In this model:

* Each agent moves in a 2D space.

* Agents interact with nearby agents using the **Prisoner's Dilemma** rules.

* Agents may change their behavior based on what others do and the score they achieve.

## 🦠 Real-world Case – Epidemic Compliance

> **Case 1**: The Federal Office for Health wants to understand if **groups of non-cooperative people** (rule breakers) can appear and persist in a population.

My model helps explore:

* How cooperation and non-cooperation spread.

* Whether groups of defectors can form.

* How speed, influence, and temptation change the final behavior of the population.

### 🎥 Simulation Video

[▶️ Click here to watch the demo video](/src/main/python/video/simulation.mkv)

## 📁 Project Files

### `SimulationExperiments.scala`

Runs several simulations, varying one parameter at a time (e.g., initial cooperation or speed). These simulations run in parallel using Scala `Future`.

### `BatchSimulation.scala`

This module contains the main simulation loop. It runs multiple repetitions, averages the results, and then uses the `CSVWriter` class to write the data into a CSV file.

### `Simulation.scala`

Handles core logic such as:

* Moving actors.

* Making them interact (cooperate or defect).

* Forming clusters of similar behavior.

### `Actor.scala`

Defines an agent. Each agent has a position, a strategy (cooperate or defect), and rules for interacting with others.

### `CSVWriter.scala`

Writes results from the simulation into `.csv` files for analysis.

### `SimulationUI.scala`

Provides a graphical interface to run a single simulation. It shows moving agents and a graph of cooperation over time, including sliders to change parameters.

## ▶️ How to Run

### Run the real-time UI simulation
