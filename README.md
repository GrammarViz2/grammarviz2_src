SAX-VSM
============

This project provides the source code released to support our ICDM publication:

Senin, P.; Malinchik, S., [*SAX-VSM: Interpretable Time Series Classification Using SAX and Vector Space Model*](http://www2.hawaii.edu/~senin/assets/papers/sax-vsm-icdm13-short.FINAL_DRAFT.pdf) Data Mining (ICDM), 2013 IEEE 13th International Conference on, pp.1175,1180, 7-10 Dec. 2013.

that is based on the following work:

[1] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[2] Salton, G., Wong, A., Yang., C., [*A vector space model for automatic indexing*](http://dl.acm.org/citation.cfm?id=361220). Commun. ACM 18, 11, 613–620, 1975.

[3] Finkel, Daniel E. [*DIRECT Optimization Algorithm User Guide*](http://www4.ncsu.edu/~ctk/Finkel_Direct/DirectUserGuide_pdf.pdf), 2003.

I am still working to make the code user friendly, if you have any suggestions, please email me.

1.0 BUILDING
------------
The code is written in Java and we use Ant to build it:
	
	$ ant -f jar.build.xml 
	Buildfile: /media/Stock/git/sax-vsm_classic.git/jar.build.xml
	...
	[jar] Building jar: /media/Stock/git/sax-vsm_classic.git/sax-vsm-classic20.jar
	[delete] Deleting directory /media/Stock/git/sax-vsm_classic.git/tmp
	BUILD SUCCESSFUL

2.0 OPTIMIZING DISCRETIZATION PARAMETERS
------------
The code implements a modified for SAX-VSM DIRECT algorithm. Below is the trace of running sampler for Gun/Point dataset. The series in this dataset have length 150, so I define the sliding window range as [10-150], PAA size as [5-75], and the alphabet [2-18]. This is the run trace:

	$java -cp "sax-vsm-classic20.jar" edu.hawaii.jmotif.direct.SAXVSMDirectSampler data/Gun_Point/Gun_Point_TRAIN data/Gun_Point/Gun_Point_TEST 10 150 5 75 2 18 1 20
	running sampling for CLASSIC strategy...
	iteration: 0, minimal value 0.18 at 80, 40, 10
	iteration: 1, minimal value 0.04 at 80, 17, 10
	iteration: 2, minimal value 0.02 at 80, 17, 15
	min CV error 0.02 reached at [80, 17, 15], 
	running sampling for EXACT strategy...
	iteration: 0, minimal value 0.0 at 80, 40, 10
	iteration: 1, minimal value 0.0 at 80, 40, 10
	iteration: 2, minimal value 0.0 at 80, 40, 10
	min CV error 0.00 reached at [80, 40, 10], [33, 17, 10], 
	running sampling for NOREDUCTION strategy...
	iteration: 0, minimal value 0.0 at 80, 40, 10
	iteration: 1, minimal value 0.0 at 80, 40, 10
	iteration: 2, minimal value 0.0 at 80, 40, 10
	min CV error 0.00 reached at [80, 40, 10], [33, 17, 10], 
	classification results: CLASSIC, window 80, PAA 17, alphabet 15,  accuracy 0.94667,  error 0.05333
	classification results: EXACT, window 33, PAA 17, alphabet 10,  accuracy 0.98667,  error 0.01333
	classification results: NOREDUCTION, window 33, PAA 17, alphabet 10,  accuracy 0.98,  error 0.02

In addition to a modified DIRECT version, which samples only integer points, we provide a reference implementation. For many datasets, the continuous sampler not only finds better parameters (while converging for much longer), but provides an effective visualization of optimal parameter ranges:

![An example of DIRECT samplers run](https://raw.githubusercontent.com/jMotif/sax-vsm_classic/master/RCode/figures/direct_sampling_arrowhead.png)

3.0 EXPLORING PATTERNS
------------
The class named `SAXVSMPatternExplorer` prints the most significant class-characteristic patterns, their weights, and the time-series that contain those. The `best_words_heat.R` script allows to plot these. Here is an example for the Gun/Point data:

![An example of class-characteristic patterns locations in Gun/Point data](https://raw.githubusercontent.com/jMotif/sax-vsm_classic/master/RCode/figures/gun_point_heat.png)

4.0 CLASSIFICATION
------------
`SAXVSMClassifier` implements the classification procedure. It reads both tran and test datasets, discretizes series, builds TFIDF vectors, and performs the classifciation:

	$ java -cp "sax-vsm-classic20.jar" edu.hawaii.jmotif.direct.SAXVSMClassifier data/Gun_Point/Gun_Point_TRAIN data/Gun_Point/Gun_Point_TEST 33 17 5 EXACT
	processing paramleters: [data/Gun_Point/Gun_Point_TRAIN, data/Gun_Point/Gun_Point_TEST, 33, 17, 5, EXACT]
	...
	classification results: EXACT, window 33, PAA 17, alphabet 5,  accuracy 0.98,  error 0.02
	
5.0 NOTES
------------
Note, that the default choice for the best parameters validation on TEST data is a parameters set corresponding to the shortest sliding window, which you may want to change - for example to choose the point whose neighborhood contains the highest density of sampled points.

Also note that code implements 5 ways the TF (term frequency value) can be computed:

	double tfValue = Math.log(1.0D + Integer.valueOf(wordInBagFrequency).doubleValue());
	// double tfValue = 1.0D + Math.log(Integer.valueOf(wordInBagFrequency).doubleValue());
	// double tfValue = normalizedTF(bag, word.getKey());
	// double tfValue = augmentedTF(bag, word.getKey());
	// double tfValue = logAveTF(bag, word.getKey());

For many datasets, these yield quite different accuracy.

Finally, note, that when cosine similarity is computed within the classification procedure, it may happen that its value is the same for all classes. In that case, the current implementation considers that the time series was missclassified, but you may want to assign it to one of the classes randomly.

6.0 ACCURACY TABLE
------------
The following table was obtained in automated mode using the following command:

	java -Xmx16G -cp "sax-vsm.jar" \\
	  edu.hawaii.jmotif.direct.SAXVSMContinuousDirectSampler \\
	  CBF/CBF_TRAIN CBF/CBF_TEST 10 120 10 60 2 16 1 30
	...

which choses a parameters set yielding the minimal CV error. If CV error is the same for a number of sets, the sampler choses a set with the smallest sliding window.

| Dataset                 | Classes |  Length | Euclidean 1NN | DTW 1NN | SAX-VSM |
|-------------------------|:-------:|:-------:|--------------:|--------:|--------:|
| 50words                 |    50   |   270   |         0.369 |   0.310 |   0.374 |
| Adiac                   |    37   |   176   |         0.389 |   0.396 |   0.417 |
| Arrowhead (Projectile)  |    3    | 495-625 |         0.320 |   0.320 |   0.406 |
| Beef                    |    5    |   470   |         0.467 |   0.500 |   0.467 |
| CBF                     |    3    |   128   |         0.148 |   0.003 |   0.007 |
| ChlorineConcentration   |    3    |   166   |         0.350 |   0.352 |   0.341 |
| CinC_ECG_torso          |    4    |   1639  |         0.103 |   0.349 |   0.344 |
| Coffee                  |    2    |   286   |         0.250 |   0.179 |   0.000 |
| Cricket                 |    2    |   308   |         0.051 |   0.010 |   0.816 |
| Cricket_X               |    12   |    30   |         0.426 |   0.223 |   0.308 |
| Cricket_Y               |    12   |    30   |         0.356 |   0.208 |   0.318 |
| Cricket_Z               |    12   |    30   |         0.379 |   0.208 |   0.297 |
| DiatomSizeReduction     |    4    |   345   |         0.065 |   0.033 |   0.121 |
| ECG200                  |    2    |    96   |         0.120 |   0.230 |   0.140 |
| ECGFiveDays             |    2    |   136   |         0.065 |   0.232 |   0.003 |
| ElectricDevices         |    7    |    96   |         0.913 |   0.913 |   0.323 |
| FaceAll                 |    14   |   131   |         0.286 |   0.192 |   0.244 |
| FaceFour                |    4    |   350   |         0.216 |   0.170 |   0.114 |
| FacesUCR                |    14   |   131   |         0.231 |   0.095 |   0.100 |
| Fish                    |    7    |   463   |         0.217 |   0.167 |   0.017 |
| Gun_Point               |    2    |   150   |         0.087 |   0.093 |   0.013 |
| Haptics                 |    5    |   1092  |         0.630 |   0.623 |   0.575 |
| InlineSkate             |    7    |   1882  |         0.658 |   0.616 |   0.593 |
| ItalyPowerDemand        |    2    |    24   |         0.095 |   0.050 |   0.089 |
| Lighting2               |    2    |   637   |         0.246 |   0.131 |   0.230 |
| Lighting7               |    7    |   319   |         0.425 |   0.274 |   0.342 |
| MALLAT                  |    8    |   1024  |         0.086 |   0.066 |   0.199 |
| Mallet                  |    8    |   256   |         0.035 |   0.024 |   0.035 |
| MedicalImages           |    10   |    99   |         0.316 |   0.433 |   0.516 |
| MoteStrain              |    2    |    84   |         0.121 |   0.165 |   0.117 |
| OliveOil                |    4    |   570   |         0.133 |   0.133 |   0.133 |
| OSULeaf                 |    6    |   427   |         0.483 |   0.409 |   0.153 |
| Passgraph               |    2    |   364   |         0.366 |   0.282 |   0.344 |
| Shield                  |    3    |   1223  |         0.140 |   0.140 |   0.124 |
| SonyAIBORobot Surface   |    2    |    70   |         0.304 |   0.275 |   0.306 |
| SonyAIBORobot SurfaceII |    2    |    65   |         0.141 |   0.169 |   0.126 |
| StarLightCurves         |    3    |   1024  |         0.063 |   0.093 |   0.108 |
| SwedishLeaf             |    15   |   128   |         0.213 |   0.210 |   0.275 |
| Symbols                 |    6    |   398   |         0.101 |   0.050 |   0.089 |
| Synthetic_control       |    6    |    60   |         0.120 |   0.007 |   0.013 |
| Trace                   |    4    |   275   |         0.240 |   0.000 |   0.000 |
| Two_Patterns            |    4    |   128   |         0.090 |   0.000 |   0.004 |
| TwoLeadECG              |    2    |    82   |         0.253 |   0.096 |   0.011 |
| uWaveGestureLibrary_X   |    8    |   315   |         0.261 |   0.273 |   0.324 |
| uWaveGestureLibrary_Y   |    8    |   315   |         0.338 |   0.366 |   0.364 |
| uWaveGestureLibrary_Z   |    8    |   315   |         0.350 |   0.342 |   0.357 |
| Wafer                   |    2    |   152   |         0.005 |   0.020 |   0.002 |
| WordsSynonyms           |    25   |   270   |         0.382 |   0.351 |   0.436 |
| Yoga                    |    2    |   426   |         0.170 |   0.164 |   0.151 |
