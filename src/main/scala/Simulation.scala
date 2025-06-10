object Simulation {

  //Move all my actors based on a random value
  def actors_move(actors: List[Actor], grid: Grid, velocity:Double, rand: scala.util.Random): List[Actor] = {
    actors.map(act => act.move(velocity, grid, rand))
  }

  
  def timer(actors : List[Actor]) : List[Actor] = {
    actors.map(actor => actor.copy(timer = actor.timer - 1))
  }

  //Calculates the distance between 2 actors
  def distance(a1: Actor, a2: Actor): Double = {
    val dx = a1.posX - a2.posX
    val dy = a1.posY - a2.posY
    math.sqrt(dx * dx + dy * dy)
  }
  
  // Based on all the actors create all the interactions (new actors)
  def interactions_actors(
                           actors: List[Actor],
                           proba_of_changing: Double,
                           range: Double,
                           rand: scala.util.Random,
                           reward: Double, 
                           punishment: Double, 
                           sucker: Double,
                           temptation: Double,
                           influence : Double
                         ): List[Actor] = {
    // Going through all the actors
    actors.map { actor =>
      // Determine interaction range depending on influence
      val actualRange = if (actor.isInfluencer) {
        range * influence
      } else {
        range
      }
      val neighbors = actors.filter(other =>
        other != actor && distance(actor, other) <= actualRange
      )

      // Go through all the neighbors and calculate their score (begin with current actor)
      val interacted = neighbors.foldLeft(actor) {
        // Update score of actor -> newA (after interaction)
        (a, neighbor) =>
          val newA = a.interact(neighbor, reward, punishment, sucker, temptation)
          // Try to copy its strategy 
          newA.copy_strategy(neighbor, proba_of_changing, rand)
      }
      interacted
    }
  }
}