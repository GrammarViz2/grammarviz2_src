GrammarViz 2.0
==========

GrammarViz 2.0 source code public repository. For the documentation, please visit http://grammarviz2.github.io/grammarviz2_site. This code is released under [GPL v.2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).

GrammarViz 2.0 is implemented in Java and is based on [Symbolic Aggregate approXimaion (SAX)](https://github.com/jMotif/SAX), Grammatical Inference with [Sequitur](https://github.com/jMotif/GI) and [Re-Pair](https://github.com/jMotif/GI), and [algorithmic (Kolmogorov) complexity](https://en.wikipedia.org/wiki/Kolmogorov_complexity). Its GUI enables interactive time series exploration workflow that allows for variable length recurrent and anomalous patterns discovery from time series [4]. It also implements the "**Rule Density Curve**" and "**Rare Rule Anomaly**" (RRA) algorithms for time series anomaly discovery [5].

References:

[1] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[2] Nevill-Manning, C.G., Witten, I.H., *Identifying Hierarchical Structure in Sequences: A linear-time algorithm.* [arXiv:cs/9709102](http://arxiv.org/abs/cs/9709102), 1997.

[3] Larsson, N. J., Moffat, A., *Offline Dictionary-Based Compression*, IEEE 88 (11): 1722–1732, doi:[10.1109/5.892708](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=892708), 2000.

[4] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**GrammarViz 2.0: a tool for grammar-based pattern discovery in time series**](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

[5] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**Time series anomaly discovery with grammar-based compression**](https://csdl-techreports.googlecode.com/svn/trunk/techreports/2014/14-05/14-05.pdf), The International Conference on Extending Database Technology, EDBT 15.

## Building

We use Maven and Java 7 to build an executable. The code is also built and tested by Travis CI [![Build Status](https://travis-ci.org/GrammarViz2/grammarviz2_src.svg?branch=master)](https://travis-ci.org/GrammarViz2/grammarviz2_src).

<pre>

$ java -version
java version "1.7.0_80"
Java(TM) SE Runtime Environment (build 1.7.0_80-b15)
Java HotSpot(TM) 64-Bit Server VM (build 24.80-b11, mixed mode)

$ mvn -version
Apache Maven 2.2.1 (rdebian-8)
Java version: 1.7.0_80
Java home: /usr/lib/jvm/java-7-oracle/jre
Default locale: fr_FR, platform encoding: UTF-8
OS name: "linux" version: "3.2.0-86-generic" arch: "amd64" Family: "unix"

$ mvn package -Psingle
[INFO] Scanning for projects...
....

[INFO] Building jar: /media/Stock/git/grammarviz2_src.git/target/grammarviz2-0.0.1-SNAPSHOT-jar-with-dependencies.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESSFUL
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5 seconds
[INFO] Finished at: Wed Jun 17 15:43:01 CEST 2015
[INFO] Final Memory: 47M/238M
[INFO] ------------------------------------------------------------------------
</pre>

## Running 

To run the GrammarViz 2.0 GUI use `net.seninp.grammarviz.GrammarVizGUI` class, or run the `jar` from the command line: `$ java -Xmx2g -jar target/grammarviz2-0.0.1-SNAPSHOT-jar-with-dependencies.jar` (here I have allocated max of 2Gb of memory for the software).

## Made with Aloha!
![Made with Aloha!](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/aloha.jpg)

