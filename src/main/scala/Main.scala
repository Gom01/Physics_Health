object Main {
  def main(args: Array[String]): Unit = {
    SimulationUI.main(args)
    //BatchExperiment.run()
  }

  //Problème : j'ai des soucis de mesure (disposition des personnes)

  //Do clusters of non-compliance(defectors) form ?
    // Oui je peux directement les voir dans la simu
   //Utiliser les différents paramètre pour vérifier (trouver les phases)
  //Can they persist over time even when surrounded by cooperators ?
  //Autres idée ajouter une personne ou plusieurs avec de plus grands range
  //Après un certains nombre de steps ils ne change plus d'idées

  //Taux de vaccination en suisse = 68.77%
  //https://www.rts.ch/info/dossiers/2020/l-epidemie-de-coronavirus/12210226-la-vaccination-contre-le-covid19-en-chiffres-et-en-cartes.html
  //Densité d'habitant en suisse par km^2 = 219
  //Grille de 400px (taille suisse 41285km^2) -> 56.3 habitants / pixels^2 
  // 900 agents chaque agent représente 10'000 personnes 

}