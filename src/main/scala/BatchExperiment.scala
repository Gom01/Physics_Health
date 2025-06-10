import java.io.{File, PrintWriter}
import scala.util.Random
import scala.concurrent._
import scala.concurrent.duration._
import java.util.concurrent.Executors

object BatchExperiment {
  val timer = 10000

  def run(
           filename: String = "src/main/python/results.csv",
           numActors: Int = 1000,
           steps: Int = 1200,
           velocity: Double = 2.0,
           imitation: Double = 0.5,
           range: Double = 10.0,
           temptation: Double = 1.5,
           repeats: Int = 5
         ): Unit = {

    val coopRates = (0 to 100 by 5).map(_ / 100.0)
    val grid = Grid(400, 400)

    val threadPool = Executors.newFixedThreadPool(8)
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(threadPool)

    try {
      val futures = coopRates.map { coopInit =>
        Future {
          val averagedRuns = (1 to repeats).map { _ =>
            val rand = new Random()
            var actors = (0 until numActors).map { i =>
              val isCoop = rand.nextDouble() < coopInit
              Actor(i, rand.nextDouble() * grid.width, rand.nextDouble() * grid.height, isCoop, timer=timer)
            }.toList

            val coopHistory = scala.collection.mutable.ListBuffer.empty[Double]

            for (_ <- 0 until steps) {
              val moved = Simulation.actors_move(actors, grid, velocity, rand)
              actors = Simulation.interactions_actors(
                moved,
                imitation,
                range,
                rand,
                reward = 1.0,
                punishment = 0.5,
                sucker = 0.0,
                temptation,
                influence = 0,
              )
              val coopRate = 100.0 * actors.count(_.cooperate) / numActors
              coopHistory.append(coopRate)
            }

            // Moyenne glissante sur les X derniers pas
            val windowSize = 100
            coopHistory.takeRight(windowSize).sum / windowSize
          }

          val averaged = averagedRuns.sum / repeats
          println(f"Initial $coopInit%1.2f → Moyenne sur $repeats runs: $averaged%2.2f")
          (coopInit * 100, averaged)
        }
      }

      val results = Await.result(Future.sequence(futures), Duration.Inf).sortBy(_._1)

      val writer = new PrintWriter(new File(filename))
      writer.println("initial_coop,final_coop,speed,changestrategy,range,temptation")
      results.foreach { case (initial, finalC) =>
        writer.println(s"$initial,$finalC,$velocity,$imitation,$range,$temptation")
      }
      writer.close()

      println(s"Résultats écrits dans $filename")
    } finally {
      threadPool.shutdown()
    }
  }
}
