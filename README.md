GrammarViz 2.0
==========

GrammarViz 2.0 source code public repository. For the documentation, please visit http://grammarviz2.github.io/grammarviz2_site. The software is released under [GPL v.2.0.](http://www.gnu.org/licenses/gpl-2.0.html)

GrammarViz 2.0 implements in Java:

  - Symbolic Aggregate approXimaion (SAX) and its parallelized version [1]
  - Sequitur grammar induction algorithm, our implementation is based on the [reference Java code by Craig Nevill-Manning and Ian Witten](https://code.google.com/p/sequitur/) [2]
  - Re-Pair grammar induction algorithm and its parallelized version [3]
  - Interactive time series exploration workflow that allows for variable length recurrent and anomalous patterns discovery from time series [4]
  - "Rule Density" and "Rare Rule Anomaly" (RRA) algorithms for time series anomaly discovery [5]

References:

[1] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[2] Nevill-Manning, C.G., Witten, I.H., *Identifying Hierarchical Structure in Sequences: A linear-time algorithm.* [arXiv:cs/9709102](http://arxiv.org/abs/cs/9709102), 1997.

[3] Larsson, N. J., Moffat, A., *Offline Dictionary-Based Compression*, IEEE 88 (11): 1722–1732, doi:[10.1109/5.892708](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=892708), 2000.

[4] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**GrammarViz 2.0: a tool for grammar-based pattern discovery in time series**](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

[5] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**Time series anomaly discovery with grammar-based compression**](https://csdl-techreports.googlecode.com/svn/trunk/techreports/2014/14-05/14-05.pdf), The International Conference on Extending Database Technology, EDBT 15.

## Building

We use Ant for our builds. The code is also continously built by [Travis CI](https://travis-ci.org/GrammarViz2/grammarviz2_src).

First, make sure that Java and Ant are installed and running. My current dev. environment shows the next:

<pre>
$ java -version
java version "1.8.0_20"
Java(TM) SE Runtime Environment (build 1.8.0_20-b26)
Java HotSpot(TM) 64-Bit Server VM (build 25.20-b23, mixed mode)

$ ant -version
Apache Ant(TM) version 1.8.2 compiled on December 20 2010
</pre>

In order to compile the code, simply run:

<pre>
$ ant
Buildfile: /home/psenin/git/grammarviz2_src.git/build.xml

compile:
    [javac] Compiling 83 source files to /home/psenin/git/grammarviz2_src.git/build/classes

BUILD SUCCESSFUL
Total time: 10 seconds
</pre>

In order **to build a jar file**, run Ant with `jar.build.xml` build file:

<pre>
$ ant -f jar.build.xml 
Buildfile: /home/psenin/git/grammarviz2_src.git/jar.build.xml

clean:
   [delete] Deleting directory /home/psenin/git/grammarviz2_src.git/build

compile:
    [mkdir] Created dir: /home/psenin/git/grammarviz2_src.git/build/classes
    [javac] Compiling 128 source files to /home/psenin/git/grammarviz2_src.git/build/classes

jar:
    [mkdir] Created dir: /home/psenin/git/grammarviz2_src.git/tmp
     [copy] Copying 146 files to /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/JFreeChart/jcommon-1.0.16.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/JFreeChart/jfreechart-1.0.13.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/SwingX/swingx-all-1.6.4.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/miglayout/miglayout-4.0.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/DTW/fast-dtw.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/logger/logback-classic-1.1.2.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/logger/logback-core-1.1.2.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/logger/slf4j-api-1.7.7.jar into /home/psenin/git/grammarviz2_src.git/tmp
    [unjar] Expanding: /home/psenin/git/grammarviz2_src.git/lib/time/joda-time-2.1.jar into /home/psenin/git/grammarviz2_src.git/tmp
      [jar] Building jar: /home/psenin/git/grammarviz2_src.git/grammarviz20.jar
   [delete] Deleting directory /home/psenin/git/grammarviz2_src.git/tmp

BUILD SUCCESSFUL
Total time: 22 seconds
</pre>

## Running 

To run the GrammarViz 2.0 GUI use `edu.hawaii.jmotif.grammarviz.GrammarVizGUI` class, or run the `jar` from the command line: `$ java -Xmx2g -jar grammarviz20.jar` (here I have allocated max of 2Gb of memory for the software).

## Made with Aloha!
