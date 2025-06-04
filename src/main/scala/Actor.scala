case class Actor(posX: Double, posY: Double, cooperate : Boolean, score : Double = 0) {
  val radius = 4

  def interact(other: Actor, reward: Double, punishment: Double, sucker: Double, temptation: Double): Actor = {
    (this.cooperate, other.cooperate) match {
      case (true, true) => copy(score = score + reward) // Mutual cooperation → R
      case (false, false) => copy(score = score + punishment) // Mutual defection → P
      case (true, false) => copy(score = score + sucker) // You cooperate, other defects → S
      case (false, true) => copy(score = score + temptation) // You defect, other cooperates → T
    }
  }


  //Copy other strategy if more successful (depending on the proba [0-1])
  def copy_strategy(other: Actor, probability: Double, rand: scala.util.Random): Actor = {
    if (other.score > score && rand.nextDouble() < probability)
      Actor(posX, posY, other.cooperate, score)
    else
      this
  }

  def move(velocity: Double, grid: Grid, rand: scala.util.Random): Actor = {
    // Return only valid position (inside borders)
    def inBounds(pos: Double, min: Double, max: Double): Double = {
      if (pos < min) min
      else if (pos > max) max
      else pos
    }
    // Choose a random direction
    val theta = rand.nextDouble() * 2 * math.Pi
    // Random distance (based on direction)
    val dx = velocity * math.cos(theta)
    val dy = velocity * math.sin(theta)
    val newX = inBounds(posX + dx, radius.toDouble, grid.width.toDouble - radius)
    val newY = inBounds(posY + dy, radius.toDouble, grid.height.toDouble - radius)
    Actor(newX, newY, cooperate, score)
  }
}
