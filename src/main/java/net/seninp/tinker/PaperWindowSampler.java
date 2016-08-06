package net.seninp.tinker;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.seninp.jmotif.sax.SAXException;
import net.seninp.jmotif.sax.TSProcessor;

public class PaperWindowSampler {

  private static final String IN_DATA = "data/aa_synth/sine_0_a2.txt";

  private static final Logger LOGGER = LoggerFactory.getLogger(PaperWindowSampler.class);

  public static void main(String[] args) throws IOException, SAXException {

    double[] ts = TSProcessor.readFileColumn(IN_DATA, 0, 0);

    LOGGER.info("read " + ts.length + " points from " + IN_DATA);

  }

}
