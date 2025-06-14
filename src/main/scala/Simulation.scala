import scala.annotation.tailrec

object Simulation {

  // Creation of the initial Actors (placed randomly)
  def initActors(n: Int, coopRate: Double, influencerCoop: Int, influencerDefect: Int, grid: Grid, rand: scala.util.Random, timerDuration: Int, influenceMultiplier: Double): List[Actor] = {

    // Use id to separate coop. and defect. and influencers
    val influencerCount = influencerCoop + influencerDefect
    val influencerIds = rand.shuffle(0 until n).take(influencerCount).toList
    val coopIds = influencerIds.take(influencerCoop).toSet
    val defectIds = influencerIds.drop(influencerCoop).toSet

    // Creation of the list of actors based on their id
    (0 until n).map { i =>
      val isInfluencer = coopIds.contains(i) || defectIds.contains(i)
      val isCoop = if (coopIds.contains(i)) true else if (defectIds.contains(i)) false else rand.nextDouble() < coopRate
      Actor(
        id = i,
        posX = rand.nextDouble() * grid.width,
        posY = rand.nextDouble() * grid.height,
        cooperate = isCoop,
        isInfluencer = isInfluencer,
        influence = if (isInfluencer) influenceMultiplier else 1.0,
        timer = timerDuration
      )
    }.toList
  }


  //Move all my actors based on a random value
  def actors_move(actors: List[Actor], grid: Grid, velocity:Double, rand: scala.util.Random): List[Actor] = {
    actors.map(act => act.move(velocity, grid, rand))
  }

  //Reduce the timer of my actors
  def timer(actors: List[Actor]): List[Actor] = {
    actors.map(actor => actor.copy(timer = math.max(0, actor.timer - 1)))
  }

  //Calculates the distance between 2 actors
  def distance(a1: Actor, a2: Actor): Double = {
    val dx = a1.posX - a2.posX
    val dy = a1.posY - a2.posY
    math.sqrt(dx * dx + dy * dy)
  }

  // Based on all the actors create all the interactions (new actors)
  def interactions_actors(actors: List[Actor], proba_of_changing: Double, range: Double, rand: scala.util.Random, reward: Double, punishment: Double, sucker: Double, temptation: Double, influence: Double, resetValue: Int): List[Actor] = {
    // Goes through all the actors of the map and check which are interacting with me (new list with new score of actors)
    val scoredActors = actors.map { actor =>
      val neighbors = actors.filter(other =>
        other != actor && distance(actor, other) <= (if (actor.isInfluencer) range * influence else range)
      )
      // accumulate my score (depending on the number of neighbors)
      neighbors.foldLeft(actor) { (a, neighbor) =>
        a.interact(neighbor, reward, punishment, sucker, temptation)
      }
    }
    
    // Decide to change it's strategy or not (return list of actors which changed or not their strategies)
    scoredActors.map { actor =>
      val neighbors = scoredActors.filter(other =>
        other != actor && distance(actor, other) <= (if (other.isInfluencer) range * influence else range)
      )
      neighbors.foldLeft(actor) { (a, neighbor) =>
        a.copy_strategy(neighbor, proba_of_changing, rand, resetValue)
      }
    }
  }

  // Goes through all actors if not visited then goes to it's neighbors, then continue. If no more neighbors create new cluster and repeat
  def findClusters(actors: List[Actor], range: Double): List[Set[Actor]] = {

    //Check if the actors is a neighbor (bigger range)
    def isNeighbor(a1: Actor, a2: Actor): Boolean =
      a1.cooperate == a2.cooperate && distance(a1, a2) <= range * 3

    //Take one actor then goes to it's neighbor, then to it's neighbors,... (find all connected neighbors)
    def dfs(actor: Actor, visited: Set[Int], cluster: Set[Actor]): (Set[Int], Set[Actor]) = {
      val neighbors = actors.filter(a => !visited.contains(a.id) && isNeighbor(actor, a))
      val newVisited = visited ++ neighbors.map(_.id) // already-explored actor
      val newCluster = cluster ++ neighbors // set of connected actors

      //goes to all the neighbors of my current actor (expand the cluster)
      neighbors.foldLeft((newVisited, newCluster)) {
        case ((vis, clust), neigh) => //current visited, cluster and neighbor
          val (vis2, clust2) = dfs(neigh, vis, clust) // pass updated visited+cluster to next neighbors
          (vis2, clust2)
      }
    }

    //Goes through all actors which haven't been visited and start DFS (accumulate clusters)
    @scala.annotation.tailrec
    def explore(remaining: List[Actor], visited: Set[Int], clusters: List[Set[Actor]]): List[Set[Actor]] = remaining match {
      case Nil => clusters
      case head :: tail if visited.contains(head.id) => explore(tail, visited, clusters)
      case head :: tail =>
        val (newVisited, newCluster) = dfs(head, visited + head.id, Set(head))
        explore(tail, newVisited, newCluster :: clusters)
    }

    explore(actors, Set.empty, Nil)
  }

}