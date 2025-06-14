case class Actor(id: Int, posX: Double, posY: Double, cooperate: Boolean, score: Double = 0, isInfluencer : Boolean =false, influence:Double = 0, timer: Int) {
val radius : Int = 4

  //Prisoner's Dilemma
  def interact(other: Actor, reward: Double, punishment: Double, sucker: Double, temptation: Double): Actor = {
    (this.cooperate, other.cooperate) match {
      case (true, true) => copy(score = score + reward) // Mutual cooperation → R
      case (false, false) => copy(score = score + punishment) // Mutual defection → P
      case (true, false) => copy(score = score + sucker) // You cooperate, other defects → S
      case (false, true) => copy(score = score + temptation) // You defect, other cooperates → T
    }
  }

  //Copy other strategy if more successful (depending on the proba [0-1] set at 0.5) and change it's timer.
  def copy_strategy(other: Actor, probability: Double, rand: scala.util.Random, resetValue: Int = 10000): Actor =
    if (other.score > score && rand.nextDouble() < probability && this.timer > 0) this.copy(cooperate = other.cooperate, timer = resetValue) else this


  //Actor's move randomly (depending on velocity)
  def move(velocity: Double, grid: Grid, rand: scala.util.Random): Actor = {

    // Return only valid position (inside grid)
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
    Actor(this.id, newX, newY, this.cooperate, this.score, this.isInfluencer, this.influence, this.timer)
  }
}
