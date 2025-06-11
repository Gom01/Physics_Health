import java.io.{File, PrintWriter}
import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

class BatchSimulation(
                       val velocity: Double,
                       val temptation: Double,
                       val cooperation: Double,
                       val repeats: Int,
                       val steps: Int,
                       val range: Double,
                       val imitation: Double,
                       val timer: Int,
                       val numActors: Int
                     )
{
  val grid = Grid(400, 400)

  // This buildActors is a helper for general actor creation
  def buildActors(coopInit: Double, rand: Random): List[Actor] = {
    (0 until numActors).map { i =>
      val isCoop = rand.nextDouble() < coopInit
      Actor(i, rand.nextDouble() * grid.width, rand.nextDouble() * grid.height, isCoop, timer=timer)
    }.toList
  }

  def runVarying[T](
                     values: Seq[T], // Sequence of values for the parameter being varied
                     filename: String,
                     label: String, // Label for console output (e.g., "InitialCoop", "Temptation", "Velocity")
                     // Function to create initial actors for a given `param` and `Random` instance
                     buildInitialActors: (Random, T) => List[Actor],
                     // Function to perform one simulation step, using `param` as the varying input
                     simulateStep: (List[Actor], Random, T) => List[Actor],
                     extractX: T => Double, // Function to convert the `param` to a Double for the x-axis in CSV
                     fixedInfo: String // A string describing the fixed parameters for the CSV header/data
                   ): Unit = {
    val threadPool = Executors.newFixedThreadPool(8)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

    try {
      val futures = values.map { param =>
        Future {
          val averages = (1 to repeats).map { _ =>
            val rand = new Random()
            var actors = buildInitialActors(rand, param) // Initialize actors, potentially using 'param'

            val coopHistory = scala.collection.mutable.ListBuffer.empty[Double]

            for (_ <- 0 until steps) {
              actors = simulateStep(actors, rand, param) // Perform simulation step, passing 'param'
              val coopRate = 100.0 * actors.count(_.cooperate) / numActors
              coopHistory.append(coopRate)
            }

            // Average cooperation rate over the last 100 steps
            coopHistory.takeRight(100).sum / 100
          }

          val average = averages.sum / repeats
          println(f"$label ${extractX(param)}%1.2f → Moyenne: $average%2.2f")
          (extractX(param), average) // Tuple (x_value, final_coop)
        }
      }
      val results = Await.result(Future.sequence(futures), Duration.Inf).sortBy(_._1)

      val writer = new PrintWriter(new File(filename))
      // Adjust CSV header to be more descriptive based on the varying parameter and fixed info
      writer.println(s"varying_param_value,final_coop,${fixedInfo.replace("=", "_")}") // Replaces '=' for valid CSV header
      results.foreach { case (x, finalC) =>
        writer.println(s"$x,$finalC,$fixedInfo") // Write fixed info to data row
      }
      writer.close()
      println(s"Results written to $filename")
    } finally {
      threadPool.shutdown()
    }
  }
}
