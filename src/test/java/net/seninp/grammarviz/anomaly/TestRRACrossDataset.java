package net.seninp.grammarviz.anomaly;

import org.junit.Test;

import net.seninp.gi.GIAlgorithm;
import net.seninp.jmotif.sax.NumerosityReductionStrategy;
import net.seninp.jmotif.sax.discord.DiscordRecords;

public class TestRRACrossDataset {

  private static final double Z = 0.01;

  @Test
  public void testEcgRePairGuessParams() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 0);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 150, 7, 4,
            NumerosityReductionStrategy.NONE, Z),
        7, 5, Z);
    RRATestSupport.assertDiscordsAreValid("ecg RePair w150/p7/a4", series, discords);
  }

  @Test
  public void testEcgSequiturClassicParams() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 1600);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.SEQUITUR, series, 100, 3, 3,
            NumerosityReductionStrategy.NONE, 0.5),
        3, 3, 0.5);
    RRATestSupport.assertDiscordsAreValid("ecg Sequitur w100/p3/a3", series, discords);
  }

  @Test
  public void testEcgRePairPruned() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 1600);
    DiscordRecords discords = RRATestSupport.runRRAWithPruning(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 120, 5, 4,
            NumerosityReductionStrategy.NONE, Z),
        5, 5, Z);
    RRATestSupport.assertDiscordsAreValid("ecg RePair pruned w120/p5/a4", series, discords);
  }

  @Test
  public void testEcgRePairMindist() throws Exception {
    double[] series = RRATestSupport.loadSeries("src/resources/test-data/ecg0606_1.csv", 1600);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 100, 5, 4,
            NumerosityReductionStrategy.MINDIST, Z),
        5, 3, Z);
    RRATestSupport.assertDiscordsAreValid("ecg RePair MINDIST w100/p5/a4", series, discords);
  }

  @Test
  public void testChfdbRePair() throws Exception {
    double[] series = RRATestSupport.loadSeries("data/chfdbchf15_1.csv", 1800);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 100, 5, 4,
            NumerosityReductionStrategy.NONE, Z),
        5, 5, Z);
    RRATestSupport.assertDiscordsAreValid("chfdb RePair w100/p5/a4", series, discords);
  }

  @Test
  public void testGunSequitur() throws Exception {
    double[] series = RRATestSupport.loadSeries("data/ann_gun_CentroidA1.csv", 1800);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.SEQUITUR, series, 80, 4, 3,
            NumerosityReductionStrategy.NONE, Z),
        4, 5, Z);
    RRATestSupport.assertDiscordsAreValid("gun Sequitur w80/p4/a3", series, discords);
  }

  @Test
  public void testGunRePairAlternateAlphabet() throws Exception {
    double[] series = RRATestSupport.loadSeries("data/ann_gun_CentroidA2.csv", 1500);
    DiscordRecords discords = RRATestSupport.runRRA(series,
        RRATestSupport.inferGrammar(GIAlgorithm.REPAIR, series, 90, 6, 5,
            NumerosityReductionStrategy.NONE, Z),
        6, 5, Z);
    RRATestSupport.assertDiscordsAreValid("gun RePair w90/p6/a5", series, discords);
  }
}
