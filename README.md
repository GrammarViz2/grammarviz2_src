# GrammarViz 3.0 (3.0.1 release, 2026)

![maven build](https://github.com/GrammarViz2/grammarviz2_src/actions/workflows/maven.yml/badge.svg)
[![codecov.io](https://codecov.io/github/GrammarViz2/grammarviz2_src/coverage.svg?branch=master)](https://codecov.io/github/GrammarViz2/grammarviz2_src?branch=master)
[![License](https://img.shields.io/:license-gpl2-green.svg)](https://www.gnu.org/licenses/gpl-2.0.html)
[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/summary/new_code?id=GrammarViz2_grammarviz2_src)

GrammarViz 3.0 source code public repository. This code is released under [GPL v.2.0](https://www.gnu.org/licenses/old-licenses/gpl-2.0.en.html).

> For the detailed software description, please visit our [demo site](https://grammarviz2.github.io/grammarviz2_site).

## In a nutshell
GrammarViz 3.0 is a software for *time series exploratory analysis* with GUI and CLI interfaces. The GUI enables interactive time series exploration workflow that allows for variable length recurrent and anomalous patterns discovery from time series [4]:
![GrammarViz 3.0 screen](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/src/resources/assets/screen.png)

It is implemented in Java and is based on continuous signal discretization with [SAX](https://github.com/jMotif/SAX), Grammatical Inference with [Sequitur](https://github.com/jMotif/GI) and [Re-Pair](https://github.com/jMotif/GI), and [algorithmic (Kolmogorov) complexity](https://en.wikipedia.org/wiki/Kolmogorov_complexity).

Cross-language checks for the shared SAX and GI layers (discord search, sliding-window SAX, RePair) live in [jmotif-conformance](https://github.com/jMotif/jmotif-conformance).

In contrast with 2.0, GrammarViz 3.0 introduces an approach for the grammar rule pruning and the automated discretization parameters selection procedure based on the greedy grammar rule pruning and MDL -- by sampling a possible parameters space, it finds a parameters set which produces the most _concise_ grammar _describing_ the observed time series the best, which often is close to the optimal -- here _concise_ and _describing_ are based on other specific criteria.

### What's new in the 3.0.1 release

Maintenance release: bumps the jMotif stack to **2.0.1** (`jmotif-sax`, `jmotif-gi`), aligns
SLF4J/Logback/JaCoCo with the rest of the family, and installs SAX/GI from source in CI
until `jmotif-sax` 2.0.1 is on Maven Central. No application behavior changes.

### What's new in the 3.0.0 release

This release modernizes the build and fixes a cluster of long-standing defects in the
automated parameter-selection ("Guess parameters") workflow. Highlights:

* **Builds on Java 21** against the aligned [jMotif](https://github.com/jMotif) 2.0.0 libraries
  (`jmotif-sax` and `jmotif-gi` 2.0.0); CI runs on Java 21 and 25.
* **The parameter guesser now honors your input.** Previously the window/PAA/alphabet ranges,
  step sizes, and minimal-cover threshold typed into the guesser dialog were silently
  discarded, and the inclusive maximum of each range was never actually evaluated -- both are
  fixed, with input validation on the dialog.
* **Coverage-aware selection.** The guesser now picks the most concise parameter set whose
  pruned grammar meets your minimal rule-cover threshold, instead of ignoring coverage; if
  none qualify it falls back gracefully and tells you so.
* **No more silent hangs.** A degenerate range or sampling error used to leave the UI stuck
  on "Stop!" indefinitely; it now reports the problem and resets.
* **RePair is the default** grammar-induction algorithm (selectable in Options, alongside
  Sequitur).
* First unit-test coverage for the grammar reductor and the parameter selector (26 tests).

See [CHANGELOG.md](CHANGELOG.md) for the full list, including behavior changes for existing
users.

It also implements the "**Rule Density Curve**" and "**Rare Rule Anomaly (RRA)**" algorithms for time series anomaly discovery [5], that significantly outperform HOT-SAX algorithm for time series discord discovery, which is current state of the art. In the table below, the algorithms performance is measured in the amount of calls to the distance function (less is better). The last column shows the RRA performance improvement over HOT-SAX:

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


## References

[1] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](https://web.archive.org/web/2021/http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[2] Nevill-Manning, C.G., Witten, I.H., *Identifying Hierarchical Structure in Sequences: A linear-time algorithm.* [arXiv:cs/9709102](http://arxiv.org/abs/cs/9709102), 1997.

[3] Larsson, N. J., Moffat, A., *Offline Dictionary-Based Compression*, IEEE 88 (11): 1722–1732, doi:[10.1109/5.892708](http://ieeexplore.ieee.org/xpl/articleDetails.jsp?arnumber=892708), 2000.

## [Citing this work](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/citation.bib)

[4] Pavel Senin, Jessica Lin, Xing Wang, Tim Oates, Sunil Gandhi, Arnold P. Boedihardjo, Crystal Chen, and Susan Frankenstein. 2018. GrammarViz 3.0: Interactive Discovery of Variable-Length Time Series Patterns. ACM Trans. Knowl. Discov. Data 12, 1, Article 10 (February 2018), 28 pages. DOI: https://doi.org/10.1145/3051126

[5] Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M.,  [**Time series anomaly discovery with grammar-based compression**](https://github.com/csdl/techreports/raw/master/techreports/2014/14-05/14-05.pdf), The International Conference on Extending Database Technology, EDBT 15.

## Building

We use Maven and Java 21 to build an executable. Version **3.0.1** depends on
**`jmotif-sax` 2.0.1** and **`jmotif-gi` 2.0.1**, which are not yet on Maven Central —
install them from sibling checkouts first:

<pre>
$ git clone https://github.com/jMotif/SAX.git ../SAX
$ git clone https://github.com/jMotif/GI.git ../GI
$ mvn -f ../SAX/pom.xml install -P single -DskipTests
$ mvn -f ../GI/pom.xml install -DskipTests
$ mvn -Psingle package
</pre>

The GitHub Actions CI clones and installs SAX/GI before each build. Below is a full build
trace on Java 21 after the local installs above:

<pre>

$ java -version
openjdk version "21.0.11" 2026-04-21
OpenJDK Runtime Environment (build 21.0.11+10-1-24.04.2-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.11+10-1-24.04.2-Ubuntu, mixed mode, sharing)

$ mvn -version
Apache Maven 3.8.7
Java version: 21.0.11, vendor: Ubuntu, runtime: /usr/lib/jvm/java-21-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8

$ mvn package -Psingle
[INFO] Scanning for projects...
....

[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running net.seninp.grammarviz.view.TestGrammarReductor
[INFO] Running net.seninp.grammarviz.view.TestParamsSamplerSelection
[INFO] Running net.seninp.grammarviz.view.TestParamsSamplerGrid
[INFO] Running net.seninp.grammarviz.anomaly.TestRRAanomaly
[INFO] Running net.seninp.tinker.TestInterval
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] --- jacoco-maven-plugin:0.8.15:report (report) @ grammarviz2 ---
[INFO] Analyzed bundle 'GrammarViz2' with 25 classes
[INFO]
[INFO] --- maven-assembly-plugin:3.3.0:single (make-assembly) @ grammarviz2 ---
[INFO] Building jar: target/grammarviz2-3.0.1.jar
[INFO] Building jar: target/grammarviz2-3.0.1-jar-with-dependencies.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  11.205 s
[INFO] ------------------------------------------------------------------------
</pre>

## Running
To run the GrammarViz 3.0 GUI use the `net.seninp.grammarviz.GrammarVizGUI` class, or run the self-contained `jar` from the command line: `$ java -Xmx4g -jar target/grammarviz2-3.0.1-jar-with-dependencies.jar` (here I have allocated a max of 4 GB of memory for GrammarViz).

## CLI interface
By using CLI as discussed in [these tutorials](https://grammarviz2.github.io/grammarviz2_site/anomaly/experience-a2/), it is possible to save the inferred grammar, motifs, and discords.

## Made with Aloha!
![Made with Aloha!](https://raw.githubusercontent.com/GrammarViz2/grammarviz2_src/master/src/resources/assets/aloha.jpg)

