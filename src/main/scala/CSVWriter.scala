import java.io.{File, PrintWriter}

//creation of CSV file
class CSVWriter(filename: String) {
  private val writer = new PrintWriter(new File(filename))
  
  //Write header of the simulation
  def writeHeader(): Unit = {
    writer.println("varying_param,final_coop,final_clusters,influencer_coop,influencer_non_coop,steps,velocity,imitation,range,temptation")
  }

  //Write each lines (Average of 50 last steps)
  def writeLine(
                 varyingParam: Double,
                 avgCoop: Double,
                 avgClusters: Double,
                 influencerCoop: Int,
                 influencerDefect: Int,
                 steps: Int,
                 velocity: Double,
                 imitation: Double,
                 range: Double,
                 temptation: Double
               ): Unit = {
    writer.println(f"$varyingParam%.2f,$avgCoop%.2f,$avgClusters%.2f,$influencerCoop,$influencerDefect,$steps,$velocity%.2f,$imitation%.2f,$range%.2f,$temptation%.2f")
  }
  def close(): Unit = writer.close()
}
