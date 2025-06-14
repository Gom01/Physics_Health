object Main {
  def main(args: Array[String]): Unit = {
    
    //Creation of the base of simulation
    val runner_influencer = new BatchSimulation(
      velocity = 2,
      temptation = 1.5,
      cooperation = 0.5,
      repeats = 3,
      steps = 800,
      range = 10,
      imitation = 0.5,
      timer = 10000,
      numActors = 1000,
      influencerCoop = 6,
      influencerDefect = 0,
      influenceMultiplier = 3.0
    )
    val experiments_influencer = new SimulationExperiments(runner_influencer)
    experiments_influencer.runAll()
  }
}