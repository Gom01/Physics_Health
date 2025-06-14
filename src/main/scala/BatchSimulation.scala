import java.io.{File, PrintWriter}
import scala.util.Random

//Class which allows me to combine the results of multiples simulations (varying a specific parameter)
class BatchSimulation(val velocity: Double, val temptation: Double, val cooperation: Double, val repeats: Int, val steps: Int, val range: Double, val imitation: Double, val timer: Int, val numActors: Int, val influencerCoop: Int, val influencerDefect: Int, val influenceMultiplier: Double) {
  val grid = Grid(400, 400)

  //Creation of the grid of actors
  def buildActors(rand: Random, coopInit: Double): List[Actor] = {
    Simulation.initActors(
      n = numActors,
      coopRate = coopInit,
      influencerCoop = influencerCoop,
      influencerDefect = influencerDefect,
      grid = grid,
      rand = rand,
      timerDuration = timer,
      influenceMultiplier = influenceMultiplier
    )
  }


  // Runs multiple simulations varying a single parameter (gives some values)(give grid of actors)
  def runVarying[T](
                     values: Seq[T], // Varying values (temptation, velocity, actors)
                     filename: String,
                     label: String, // Which parameter is being varied
                     buildInitialActors: (Random, T) => List[Actor], // Grid of actors (initial state)
                     extractX: T => Double, // Convert value into double
                     fixedInfo: String // Info which stays the same (CSV)
                   )(
                     stepFunction: (List[Actor], Random, T) => List[Actor] // Step of simulation (create new list of actors)
                   ): Unit = {

    val seed = 42 //Seed used so that my simulation is always the same (position of actors when varying values

    val writer = new CSVWriter(filename)
    writer.writeHeader()

    // Loop over each value of the parameter being varied
    values.foreach { v =>
      println(s"Running for $label = $v")

      // Run multiple repetitions and collect cooperation & cluster averages
      val (coopResults, clusterResults) = (1 to repeats).map { r =>
        println(s"  Repetition $r")
        val rand = new Random(seed + r)

        // Build initial list of actors
        val initialActors = buildInitialActors(rand, v)

        // Simulate 'steps' time steps
        val evolvedActors = (0 until steps).foldLeft(initialActors) { (currentActors, step) =>
          if (step % 100 == 0) println(s"    Step $step")
          stepFunction(currentActors, rand, v)
        }

        // Add 50 steps and collect cooperation and cluster stats.
        val last50Stats = (0 until 50).foldLeft((evolvedActors, List.empty[(Double, Double)])) {
          case ((actorsAcc, statsAcc), _) => //(Current list of actors, stats collected)
            val updatedActors = stepFunction(actorsAcc, rand, v)
            val coopPercent = 100.0 * updatedActors.count(_.cooperate) / updatedActors.length //get percentage
            val clusters = Simulation.findClusters(updatedActors, range).size.toDouble //get number of cluster
            (updatedActors, statsAcc :+ (coopPercent, clusters))
        }._2

        val (coops, clusters) = last50Stats.unzip
        val avgCoop = coops.sum / coops.length
        val avgClusters = clusters.sum / clusters.length
        (avgCoop, avgClusters) 
      }.unzip //list of %cooperators and number of clusters 

      // Average across repetitions (multiple simulations)
      val avgCoopOverall = coopResults.sum / coopResults.length
      val avgClustersOverall = clusterResults.sum / clusterResults.length
      val xValue = extractX(v)

      // Write the result for this parameter value to CSV
      writer.writeLine(
        varyingParam = xValue,
        avgCoop = avgCoopOverall,
        avgClusters = avgClustersOverall,
        influencerCoop = influencerCoop,
        influencerDefect = influencerDefect,
        steps = steps,
        velocity = velocity,
        imitation = imitation,
        range = range,
        temptation = temptation
      )
    }
    writer.close()
    println(s"Finished all runs → $filename")
  }
}
