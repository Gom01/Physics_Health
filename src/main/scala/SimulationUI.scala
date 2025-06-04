import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.paint.Color
import scalafx.scene.canvas.{Canvas, GraphicsContext}
import scalafx.scene.control.{Button, Label, Slider, ScrollPane}
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
    // Line chart to track cooperation over time
    val timeAxis = new NumberAxis("Time", 0, 1000, 100)
    val percentAxis = new NumberAxis("Percentage (%)", 0, 100, 10)
    val lineChart = new LineChart[Number, Number](timeAxis, percentAxis)
    lineChart.title = "Evolution of Cooperation and Defection"

    val coopSeries = new XYChart.Series[Number, Number] {
      name = "Cooperators"
    }
    val defectSeries = new XYChart.Series[Number, Number] {
      name = "Defectors"
    }

    lineChart.data.value.addAll(coopSeries, defectSeries)

    val canvas = new Canvas(canvasWidth, canvasHeight)
    val gc: GraphicsContext = canvas.graphicsContext2D

    // UI sliders
    val sliderActors = new Slider(0, 5000, 50) { showTickLabels = true; prefWidth = 250 }
    val sliderCoop = new Slider(0, 100, 50) { showTickLabels = true; prefWidth = 250 }
    val sliderVelocity = new Slider(0.1, 10.0, 2.0) { showTickLabels = true; prefWidth = 250 }
    val sliderImitation = new Slider(0, 100, 50) { showTickLabels = true; prefWidth = 250 }
    val sliderRange = new Slider(1, 100, 10) { showTickLabels = true; prefWidth = 250 }

    val sliderTemptation = new Slider(1.1, 1.99, 1.5) {
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 0.5
      prefWidth = 250
    }

    val btnStart = new Button("Start simulation")

    def drawActors(): Unit = {
      gc.fill = Color.White
      gc.fillRect(0, 0, canvasWidth, canvasHeight)

      val range = sliderRange.value.value
      gc.stroke = Color.LightGray
      actors.foreach { a =>
        gc.strokeOval(a.posX - range, a.posY - range, range * 2, range * 2)
      }

      actors.foreach { a =>
        gc.fill = if (a.cooperate) Color.Green else Color.Red
        gc.fillOval(a.posX - a.radius, a.posY - a.radius, a.radius * 2, a.radius * 2)
      }
    }

    def initActors(n: Int, coopRate: Double): List[Actor] = {
      List.fill(n) {
        val isCoop = rand.nextDouble() < coopRate
        Actor(
          posX = rand.nextDouble() * grid.width,
          posY = rand.nextDouble() * grid.height,
          cooperate = isCoop
        )
      }
    }

    val timer = AnimationTimer { _ =>
      if (isRunning) {
        val velocity = sliderVelocity.value.value
        val imitationProb = sliderImitation.value.value / 100.0
        val transmissionRadius = sliderRange.value.value

        val r = 1.0
        val p = 0.5
        val s = 0.0
        val t = sliderTemptation.value.value

        val moved = Simulation.actors_move(actors, grid, velocity, rand)
        actors = Simulation.interactions_actors(moved, imitationProb, transmissionRadius, rand, r, p, s, t)

        // Update chart
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
      actors = initActors(count, coopRate)
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
        new Label("Cooperation (%) :"), sliderCoop,
        new Label("Speed :"), sliderVelocity,
        new Label("Change strategy (%) :"), sliderImitation,
        new Label("Range (radius) :"), sliderRange,
        new Label("Temptation (T) - you defect, they cooperate:"), sliderTemptation,
        btnStart
      )
    }

    val rightPane = new VBox {
      spacing = 20
      padding = Insets(10)
      children = Seq(controlPanel, lineChart)
    }

    stage = new JFXApp3.PrimaryStage {
      title = "Simulation"
      scene = new Scene(canvasWidth + 500, canvasHeight + 200) {
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
