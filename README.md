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

The normalization threshold (used in SAX discretization) is also quite important hidden parameter -- changing it from 0.001 to 0.01 may significantly change the classification accuracy on a number of datasets where the original signal standard deviation is small, such as Beef.

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
| 50words                     | 50      | 270     | 0.3692        | 0.3099   | 0.3736  |
| Adiac                       | 37      | 176     | 0.3887        | 0.396    | 0.4169  |
| Arrowhead                   | 3       | 495-625 | 0.5029        | 0.5314   | 0.3429  |
| ARSim                       | 2       | 500     | 0.4890        | 0.4035   | 0.4405  |
| Beef                        | 5       | 470     | 0.4667        | 0.5000   | 0.2333  |
| CBF                         | 3       | 128     | 0.1478        | 0.0033   | 0.0100  |
| ChlorineConcentration       | 3       | 166     | 0.3500        | 0.3516   | 0.3409  |
| CinC_ECG_torso              | 4       | 1,639   | 0.1029        | 0.3493   | 0.3442  |
| Coffee                      | 2       | 286     | 0.2500        | 0.1786   | 0.0000  |
| Cricket                     | 2       | 308     | 0.0510        | 0.0102   | 0.8163  |
| Cricket_X                   | 12      | 300     | 0.4256        | 0.2231   | 0.3077  |
| Cricket_Y                   | 12      | 300     | 0.3564        | 0.2077   | 0.3180  |
| Cricket_Z                   | 12      | 300     | 0.3795        | 0.2077   | 0.2974  |
| DiatomSizeReduction         | 4       | 345     | 0.0654        | 0.0327   | 0.1209  |
| Earthquakes                 | 2       | 512     | 0.3022        | 0.295    | 0.2518  |
| ECG200                      | 2       | 96      | 0.1200        | 0.2300   | 0.1400  |
| ECGFiveDays                 | 2       | 136     | 0.2033        | 0.2323   | 0.0012  |
| ElectricDevices             | 7       | 96      | 0.4559        | 0.3298   | 0.3739  |
| FaceAll                     | 14      | 131     | 0.2864        | 0.1923   | 0.2450  |
| FaceFour                    | 4       | 350     | 0.2159        | 0.1705   | 0.11364 |
| FacesUCR                    | 14      | 131     | 0.2307        | 0.0951   | 0.1088  |
| Fish                        | 7       | 463     | 0.2171        | 0.1657   | 0.0171  |
| FordA                       | 2       | 500     | 0.3136        | 0.2758   | 0.18561 |
| FordB                       | 2       | 500     | 0.4037        | 0.3407   | 0.3309  |
| GunPoint                    | 2       | 150     | 0.0867        | 0.0933   | 0.0133  |
| HandOutlines                | 2       | 2,709   | 0.1378        | 0.1189   | 0.0703  |
| Haptics                     | 5       | 1,092   | 0.6299        | 0.6234   | 0.5844  |
| InlineSkate                 | 7       | 1,882   | 0.6582        | 0.6164   | 0.5927  |
| ItalyPowerDemand            | 2       | 24      | 0.0447        | 0.0496   | 0.0894  |
| Lighting2                   | 2       | 637     | 0.2459        | 0.1311   | 0.2131  |
| Lighting7                   | 7       | 319     | 0.4247        | 0.2740   | 0.3973  |
| MALLAT                      | 8       | 1,024   | 0.0857        | 0.0661   | 0.1992  |
| Mallet                      | 8       | 256     | 0.0346        | 0.0236   | 0.0351  |
| MedicalImages               | 10      | 99      | 0.3158        | 0.2632   | 0.5158  |
| MoteStrain                  | 2       | 84      | 0.1214        | 0.1653   | 0.1246  |
| NonInvasiveFetalECG_Thorax1 | 42      | 750     | 0.1710        | 0.2097   | 0.2921  |
| OliveOil                    | 4       | 570     | 0.1333        | 0.1333   | 0.1333  |
| OSULeaf                     | 6       | 427     | 0.4835        | 0.4091   | 0.1653  |
| Passgraph                   | 2       | 364     | 0.3740        | 0.2901   | 0.3435  |
| Shield                      | 3       | 1,179   | 0.1395        | 0.1395   | 0.1240  |
| SonyAIBORobotSurface        | 2       | 70      | 0.3045        | 0.2745   | 0.3062  |
| SonyAIBORobotSurfaceII      | 2       | 65      | 0.1406        | 0.1689   | 0.1259  |
| StarLightCurves             | 3       | 1,024   | 0.1512        | 0.2080   | 0.07722 |
| SwedishLeaf                 | 15      | 129     | 0.2112        | 0.0503   | 0.2784  |
| Symbols                     | 6       | 398     | 0.1005        | 0.0067   | 0.1085  |
| SyntheticControl            | 6       | 60      | 0.1200        | 0.0000   | 0.0167  |
| Trace                       | 4       | 275     | 0.2400        | 0.0000   | 0.0000  |
| Two_Patterns                | 4       | 128     | 0.0933        | 0.0957   | 0.0040  |
| TwoLeadECG                  | 2       | 82      | 0.2529        | 0.0957   | 0.0141  |
| uWaveGestureLibrary_X       | 8       | 315     | 0.260748      | 0.272473 | 0.3230  |
| uWaveGestureLibrary_Y       | 8       | 315     | 0.338358      | 0.365997 | 0.3638  |
| uWaveGestureLibrary_Z       | 8       | 315     | 0.350363      | 0.3417   | 0.3565  |
| Wafer                       | 2       | 152     | 0.0045        | 0.0201   | 0.0010  |
| WordsSynonyms               | 25      | 270     | 0.3824        | 0.3511   | 0.4404  |
| Yoga                        | 2       | 426     | 0.1697        | 0.1637   | 0.1510  |
