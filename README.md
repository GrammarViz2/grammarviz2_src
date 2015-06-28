GrammarViz 2.0 [![Build Status](https://travis-ci.org/GrammarViz2/grammarviz2_src.svg?branch=master)](https://travis-ci.org/GrammarViz2/grammarviz2_src)
==========

GrammarViz 2.0 source code public repository. This code is released under [GPL v.2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).

##### For the detailed software description, please visit our [demo site](http://grammarviz2.github.io/grammarviz2_site).

0.0 In a nutshell
------------
GrammarViz 2.0 is a software for *time series exploratory analysis* with GUI and CLI interfaces. The GUI enables interactive time series exploration workflow that allows for variable length recurrent and anomalous patterns discovery from time series [4]:
![GrammarViz2 screen](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/src/resources/assets/screen.png)

It is implemented in Java and is based on continuous signal discretization with [SAX](https://github.com/jMotif/SAX), Grammatical Inference with [Sequitur](https://github.com/jMotif/GI) and [Re-Pair](https://github.com/jMotif/GI), and [algorithmic (Kolmogorov) complexity](https://en.wikipedia.org/wiki/Kolmogorov_complexity). 

GrammarViz2 also implements the "**Rule Density Curve**" and "**Rare Rule Anomaly (RRA)**" algorithms for time series anomaly discovery [5], that significantly outperform HOT-SAX algorithm for time series discord discovery  which is current state of the art. In the table below, the algorithms performance is measured in the amount of calls to the distance function (less is better). The last column shows the RRA performance improvement over HOT-SAX:

| Dataset and SAX parameters         | Dataset size    | Brute Force          | HOT-SAX     | RRA        | Reduction |
|:-----------------------------------|--------:|---------------------:|------------:|-----------:|------:|
| [Daily commute](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/anomaly_pruned_hilbert_curve_4Sequitur.csv) (350,15,4)           | 17,175  | 271,442,101          | 879,067     | 112,405    | 87.2% |
| [Dutch power demand](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/dutch_power_demand.txt) (750,6,3)       | 35,040  | 1.13 * 10^9          | 6,196,356   | 327,950    | 95.7% |
| [ECG 0606](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/ecg0606_1.csv) (120,4,4)                 | 2,300   | 4,241,541            | 72,390      | 16,717     | 76.9% |
| [ECG 308](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/stdb_308_0.txt) (300,4,4)                  | 5,400   | 23,044,801           | 327,454     | 14,655     | 95.5% |
| [ECG 15](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/chfdbchf15_1.csv) (300,4,4)                   | 15,000  | 207,374,401          | 1,434,665   | 111,348    | 92.2% |
| [ECG 108](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/mitdbx_mitdbx_108_1.txt) (300,4,4)                  | 21,600  | 441,021,001          | 6,041,145   | 150,184    | 97.5% |
| [ECG 300](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/300_signal1.txt) (300,4,4)                  | 536,976 | 288 * 10^9           | 101,427,254 | 17,712,845 | 82.6% |
| [ECG 318](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/318_signal1.txt) (300,4,4)                  | 586,086 | 343 * 10^9           | 45,513,790  | 10,000,632 | 78.0% |
| [Respiration, NPRS 43](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/nprs43.txt) (128,5,4)     | 4,000   | 14,021,281           | 89,570      | 45,352     | 49.3% |
| [Respiration, NPRS 44](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/nprs44.txt) (128,5,4)     | 24,125  | 569,753,031          | 1,146,145   | 257,529    | 77.5% |
| [Video dataset](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/ann_gun_CentroidA1.csv) (150,5,3)      | 11,251  | 119,935,353          | 758,456     | 69,910     | 90.8% |
| [Shuttle telemetry, TEK14](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/TEK14.txt) (128,4,4) | 5,000   | 22,510,281           | 691,194     | 48,226     | 93.0% |
| [Shuttle telemetry, TEK16](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/TEK16.txt) (128,4,4) | 5,000   | 22,491,306           | 61,682      | 15,573     | 74.8% |
| [Shuttle telemetry, TEK17](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/data/TEK17.txt) (128,4,4) | 5,000   | 22,491,306           | 164,225     | 78,211     | 52.4% |


### References:

[1] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[2] Nevill-Manning, C.G., Witten, I.H., *Identifying Hierarchical Structure in Sequences: A linear-time algorithm.* [arXiv:cs/9709102](http://arxiv.org/abs/cs/9709102), 1997.

[3] Larsson, N. J., Moffat, A., *Offline Dictionary-Based Compression*, IEEE 88 (11): 1722–1732, doi:[10.1109/5.892708](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=892708), 2000.

### Citing this work:

[4] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**GrammarViz 2.0: a tool for grammar-based pattern discovery in time series**](http://www2.hawaii.edu/~senin/assets/papers/grammarviz2.pdf), ECML/PKDD Conference, 2014.

[5] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**Time series anomaly discovery with grammar-based compression**](https://csdl-techreports.googlecode.com/svn/trunk/techreports/2014/14-05/14-05.pdf), The International Conference on Extending Database Technology, EDBT 15.

1.0 Building
------------

We use Maven and Java 7 to build an executable.

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

2.0 Running
------------
To run the GrammarViz 2.0 GUI use `net.seninp.grammarviz.GrammarVizGUI` class, or run the `jar` from the command line: `$ java -Xmx2g -jar target/grammarviz2-0.0.1-SNAPSHOT-jar-with-dependencies.jar` (here I have allocated max of 2Gb of memory for the software).

3.0 CLI interface
------------
It is possible to save the inferred grammar, motifs, and discords using the CLI interface as discussed in [these tutorials](http://grammarviz2.github.io/grammarviz2_site/experiences/).

## Made with Aloha!
![Made with Aloha!](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/src/resources/assets/aloha.jpg)

