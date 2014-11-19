SAX-VSM
============

This project provides the source code released to support our ICDM publication:

Senin, P.; Malinchik, S., [*SAX-VSM: Interpretable Time Series Classification Using SAX and Vector Space Model*](http://www2.hawaii.edu/~senin/assets/papers/sax-vsm-icdm13-short.FINAL_DRAFT.pdf) Data Mining (ICDM), 2013 IEEE 13th International Conference on, pp.1175,1180, 7-10 Dec. 2013.

that is based on the following work:

[2] Lin, J., Keogh, E., Wei, L. and Lonardi, S., [*Experiencing SAX: a Novel Symbolic Representation of Time Series*](http://cs.gmu.edu/~jessica/SAX_DAMI_preprint.pdf). [DMKD Journal](http://link.springer.com/article/10.1007%2Fs10618-007-0064-z), 2007.

[3] Salton, G., Wong, A., Yang., C., [*A vector space model for automatic indexing*](http://dl.acm.org/citation.cfm?id=361220). Commun. ACM 18, 11, 613–620, 1975.

I am still working to make the code user friendly, if you have any suggestions, please email me.

BUILDING
------------
The code is written in Java and I use Ant to build it:
	
	$ ant -f jar.build.xml 
	Buildfile: /media/Stock/git/sax-vsm_classic.git/jar.build.xml
	...
	[jar] Building jar: /media/Stock/git/sax-vsm_classic.git/sax-vsm-classic20.jar
	[delete] Deleting directory /media/Stock/git/sax-vsm_classic.git/tmp
	BUILD SUCCESSFUL

FINDING BEST DISCRETIZATION PARAMETERS
------------
You need to run a DIRECT smpler that is tailored for SAX-VSM algorithm. Below is the trace of running sampler for Gun/Point dataset. The series in this dataset have length 150, so I define the sliding window range as [10-150], PAA size as [5-75] while the alphabet [2-18]. This is the run trace:

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
  

Note, that by default, for the validation, the algorithm chooses the parameters corresponding to shortest sliding window, which you may want to change - for example to choose the point which neighborhood contains the most sampled density.

Also note that code implements 5 ways the TF (term frequency value) can be computed:

	double tfValue = Math.log(1.0D + Integer.valueOf(wordInBagFrequency).doubleValue());
	// double tfValue = 1.0D + Math.log(Integer.valueOf(wordInBagFrequency).doubleValue());
	// double tfValue = normalizedTF(bag, word.getKey());
	// double tfValue = augmentedTF(bag, word.getKey());
	// double tfValue = logAveTF(bag, word.getKey());
  
For many datasets, these likely to yield different accuracy.
