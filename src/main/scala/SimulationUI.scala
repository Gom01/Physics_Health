import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.control.{Button, Label, Slider, ScrollPane, TextField}
import scalafx.scene.layout.{VBox, BorderPane, StackPane}
import scalafx.scene.chart.{LineChart, NumberAxis, XYChart}
import scalafx.animation.AnimationTimer
import scalafx.geometry.Insets

import scala.util.Random

object SimulationUI extends JFXApp3 {
  val canvasWidth = 400
  val canvasHeight = 400
  val grid = Grid(canvasWidth, canvasHeight)
  var actors: List[Actor] = List.empty
  val rand = new Random()
  var isRunning = false
  var timeStep = 0

  override def start(): Unit = {
    val timeAxis = new NumberAxis("Time", 0, 1000, 100)
    val percentAxis = new NumberAxis("Percentage (%)", 0, 100, 10)
    val lineChart = new LineChart[Number, Number](timeAxis, percentAxis)
    lineChart.title = "Evolution of Cooperation and Defection"
    val coopSeries = new XYChart.Series[Number, Number] { name = "Cooperators" }
    val defectSeries = new XYChart.Series[Number, Number] { name = "Defectors" }
    lineChart.data.value.addAll(coopSeries, defectSeries)

    val canvas = new Canvas(canvasWidth, canvasHeight)
    val gc: GraphicsContext = canvas.graphicsContext2D
    var clusterColorMap = Map[Set[Int], Color]()

    val sliderActors = new Slider(0, 5000, 900) { showTickLabels = true; prefWidth = 250 }
    val fieldInfluencerCoop = new TextField { promptText = "# cooperative"; text = "2"; prefWidth = 100 }
    val fieldInfluencerDefect = new TextField { promptText = "# defectors"; text = "3"; prefWidth = 100 }
    val sliderCoop = new Slider(0, 100, 69) { showTickLabels = true; prefWidth = 250 }
    val sliderVelocity = new Slider(0.1, 10.0, 2.0) { showTickLabels = true; prefWidth = 250 }
    val sliderImitation = new Slider(0, 100, 50) { showTickLabels = true; prefWidth = 250 }
    val sliderRange = new Slider(1, 100, 10) { showTickLabels = true; prefWidth = 250 }
    val sliderTimer = new Slider(1, 1500, 2000) { showTickLabels = true; showTickMarks = true; majorTickUnit = 5000; blockIncrement = 1000; prefWidth = 250 }
    val sliderTemptation = new Slider(1.1, 1.99, 1.5) {
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 0.5
      prefWidth = 250
    }

    val influence: Double = 3.0
    val btnStart = new Button("Start simulation")
    val labelClusterCount = new Label("Clusters: 0")

    def drawActors(): Unit = {
      gc.fill = Color.White
      gc.fillRect(0, 0, canvasWidth, canvasHeight)
      val baseRange = sliderRange.value.value

      val clusters = Simulation.findClusters(actors, baseRange)
      labelClusterCount.text = s"Clusters: ${clusters.size}"

      val updatedColorMap = clusters.map { cluster =>
        val idSet = cluster.map(_.id)
        val isCoop = cluster.head.cooperate
        val color = randomColor(isCoop)
        idSet -> color
      }.toMap
      clusterColorMap = updatedColorMap

      for ((cluster, color) <- clusters.zip(clusters.map(c => updatedColorMap(c.map(_.id))))) {
        gc.fill = color
        cluster.foreach { a =>
          gc.fillOval(a.posX - a.radius, a.posY - a.radius, a.radius * 2, a.radius * 2)
          if (a.isInfluencer) {
            gc.stroke = Color.Black
            gc.strokeOval(a.posX - a.radius, a.posY - a.radius, a.radius * 2, a.radius * 2)
          }
        }
      }

      actors.foreach { a =>
        val range = baseRange * (if (a.isInfluencer) influence else 1.0)
        gc.stroke = if (a.isInfluencer) Color.DarkBlue else Color.rgb(150, 150, 150, 0.2)
        gc.strokeOval(a.posX - range, a.posY - range, range * 2, range * 2)
      }
    }

    def randomColor(isCoop: Boolean): Color = {
      if (isCoop) Color.rgb(0, 100 + rand.nextInt(156), 0)
      else Color.rgb(150 + rand.nextInt(106), 0, 0)
    }

    def initActors(n: Int, coopRate: Double, influencerCoop: Int, influencerDefect: Int): List[Actor] = {
      val influencerCount = influencerCoop + influencerDefect
      val influencerIds = rand.shuffle(0 until n).take(influencerCount).toList
      val coopIds = influencerIds.take(influencerCoop).toSet
      val defectIds = influencerIds.drop(influencerCoop).toSet

      (0 until n).map { i =>
        val isInfluencer = coopIds.contains(i) || defectIds.contains(i)
        val isCoop = if (coopIds.contains(i)) true
        else if (defectIds.contains(i)) false
        else rand.nextDouble() < coopRate

        Actor(
          id = i,
          posX = rand.nextDouble() * grid.width,
          posY = rand.nextDouble() * grid.height,
          cooperate = isCoop,
          isInfluencer = isInfluencer,
          influence = if (isInfluencer) influence else 1.0,
          timer = sliderTimer.value.value.toInt
        )
      }.toList
    }

    val timer = AnimationTimer { _ =>
      if (isRunning) {
        val velocity = sliderVelocity.value.value
        val imitationProb = sliderImitation.value.value / 100.0
        val transmissionRadius = sliderRange.value.value
        val resetValue = sliderTimer.value.value.toInt
        val r = 1.0; val p = 0.5; val s = 0.0; val t = sliderTemptation.value.value

        val moved = Simulation.actors_move(actors, grid, velocity, rand)
        val timed = Simulation.timer(moved)
        actors = Simulation.interactions_actors(timed, imitationProb, transmissionRadius, rand, r, p, s, t, influence, resetValue)

        if (actors.nonEmpty) {
          val coopCount = actors.count(_.cooperate)
          val coopPercent = 100.0 * coopCount / actors.length
          val defectPercent = 100.0 - coopPercent
          coopSeries.data().add(XYChart.Data[Number, Number](timeStep, coopPercent))
          defectSeries.data().add(XYChart.Data[Number, Number](timeStep, defectPercent))
          if (coopSeries.data().size > 1000) coopSeries.data().remove(0)
          if (defectSeries.data().size > 1000) defectSeries.data().remove(0)
        }

        drawActors()
        timeStep += 1
      }
    }

    btnStart.onAction = _ => {
      val count = sliderActors.value.value.toInt
      val coopRate = sliderCoop.value.value / 100.0
      val influencerCoop = fieldInfluencerCoop.text.value.toIntOption.getOrElse(0)
      val influencerDefect = fieldInfluencerDefect.text.value.toIntOption.getOrElse(0)
      actors = initActors(count, coopRate, influencerCoop, influencerDefect)
      coopSeries.data().clear()
      defectSeries.data().clear()
      timeStep = 0
      isRunning = true
      timer.start()
    }

    val controlPanel = new VBox {
      spacing = 10
      padding = Insets(10)
      children = Seq(
        new Label("Actors :"), sliderActors,
        new Label("Influencer Cooperative (n) :"), fieldInfluencerCoop,
        new Label("Influencer Defectors (n) :"), fieldInfluencerDefect,
        new Label("Cooperation (%) :"), sliderCoop,
        new Label("Speed :"), sliderVelocity,
        new Label("Change strategy (%) :"), sliderImitation,
        new Label("Range (radius) :"), sliderRange,
        new Label("Temptation (T) - you defect, they cooperate:"), sliderTemptation,
        new Label("Timer (frames)"), sliderTimer,
        btnStart,
        labelClusterCount
      )
    }

    val rightPane = new VBox {
      spacing = 20
      padding = Insets(10)
      children = Seq(controlPanel, lineChart)
    }

    stage = new JFXApp3.PrimaryStage {
      title = "Simulation"
      scene = new Scene(canvasWidth + 250, canvasHeight + 100) {
        root = new BorderPane {
          left = new StackPane {
            children = canvas
            padding = Insets(20)
            style = "-fx-background-color: #f5f5f5;"
          }
          right = new ScrollPane {
            content = rightPane
            fitToWidth = true
          }
        }
      }
    }
  }
}
