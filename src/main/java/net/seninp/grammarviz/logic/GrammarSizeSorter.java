package net.seninp.grammarviz.logic;

import java.util.Comparator;
import net.seninp.gi.rulepruner.SampledPoint;

/**
 * A sorter implementation for parameters optimization process sampling.
 * 
 * @author psenin
 *
 */
public class GrammarSizeSorter implements Comparator<SampledPoint> {

	@Override
	public int compare(SampledPoint o1, SampledPoint o2) {
		if (o1.getGrammarSize() < o2.getGrammarSize())
			return -1;
		if (o1.getGrammarSize() > o2.getGrammarSize())
			return 1;
		return 0;
	}
}
