import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
class SimulationExperiments(runner: BatchSimulation) {

  //functions which allows me to use thread to create multiple simulation at the same time
  def runAll(): Unit = {
    val coopFuture = Future {runCooperationTest()}
    val tempFuture = Future {runTemptationTest()}
    val velFuture = Future {runVelocityTest()}
    Await.result(Future.sequence(Seq(coopFuture, tempFuture, velFuture)), Duration.Inf)
    }

  def runCooperationTest(): Unit = {
    //Use BatchSimulation function to create simulation (varying one value)
    runner.runVarying[Double](
      values = (0 to 100 by 2).map(_ / 100.0),
      filename = "src/main/python/data_csv/varying_cooperation_coop.csv",
      label = "InitialCoop",
      buildInitialActors = (rand, coopInit) => runner.buildActors(rand, coopInit),
      extractX = identity,
      fixedInfo = s"influencer_coop=${runner.influencerCoop},influencer_non_coop=${runner.influencerDefect},steps=${runner.steps},velocity=${runner.velocity},imitation=${runner.imitation},range=${runner.range},temptation=${runner.temptation}"
    ) { //This function launch a step of the simulations
      (actors, rand, _) =>
      Simulation.interactions_actors(
        Simulation.timer(Simulation.actors_move(actors, runner.grid, runner.velocity, rand)),
        runner.imitation,
        runner.range,
        rand,
        reward = 1.0,
        punishment = 0.5,
        sucker = 0.0,
        temptation = runner.temptation,
        influence = runner.influenceMultiplier,
        resetValue = runner.timer
      )
    }
  }

  def runTemptationTest(): Unit = {
    runner.runVarying[Double](
      values = (100 to 600 by 10).map(_ / 100.0),
      filename = "src/main/python/data_csv/varying_temptation.csv",
      label = "Temptation",
      buildInitialActors = (rand, _) => runner.buildActors(rand, runner.cooperation),
      extractX = identity,
      fixedInfo = s"initial_coop=${runner.cooperation},influencer_coop=${runner.influencerCoop},influencer_non_coop=${runner.influencerDefect},steps=${runner.steps},velocity=${runner.velocity},imitation=${runner.imitation},range=${runner.range}"
    ){ //This function launch a step of the simulations
      (actors, rand, temptationVal) =>
      Simulation.interactions_actors(
        Simulation.timer(Simulation.actors_move(actors, runner.grid, runner.velocity, rand)),
        runner.imitation,
        runner.range,
        rand,
        reward = 1.0,
        punishment = 0.5,
        sucker = 0.0,

        temptation = temptationVal,
        influence = runner.influenceMultiplier,
        resetValue = runner.timer
      )
    }
  }

  def runVelocityTest(): Unit = {
    runner.runVarying[Double](
      values = (0 to 120 by 5).map(_ /10.0),
      filename = "src/main/python/data_csv/varying_velocity.csv",
      label = "Velocity",
      buildInitialActors = (rand, _) => runner.buildActors(rand, runner.cooperation),
      extractX = identity,
      fixedInfo = s"initial_coop=${runner.cooperation},temptation=${runner.temptation},imitation=${runner.imitation},range=${runner.range}"
    ){  //This function launch a step of the simulations
      (actors, rand, velocityVal) =>
      Simulation.interactions_actors(
        Simulation.timer(Simulation.actors_move(actors, runner.grid, velocityVal, rand)),
        runner.imitation,
        runner.range,
        rand,
        reward = 1.0,
        punishment = 0.5,
        sucker = 0.0,
        temptation = runner.temptation,
        influence = runner.influenceMultiplier,
        resetValue = runner.timer
      )
    }
  }

}
