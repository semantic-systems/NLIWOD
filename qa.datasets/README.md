#Question answering datasets

This collection includes the following datasets or respectivly looks at the following datasets to be included:

* added: http://www.okbqa.org/nlq
* added: http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/
* added: http://linkedspending.org

in production (adding answers):
* https://www.stonetemple.com/great-knowledge-box-showdown/

to analyze: 
* GeoQuery, Free917, WebQuestions, SimpleQuestions, GraphQuestions, and QALD
* https://www.microsoft.com/en-us/download/details.aspx?id=52763
* http://datasets.maluuba.com/NewsQA
* https://github.com/ysu1989/GraphQuestions GraphQuestions: compositional questions; multiple paraphrases for each question 
* http://qallme.fbk.eu/index.php?location=benchmark
* http://agarciaduran.org/ 30M questions to freebase
* https://github.com/brmson/yodaqa/wiki/Benchmarks
 * https://github.com/brmson/dataset-factoid-curated for evolution 
 * https://github.com/brmson/dataset-factoid-movies for domain-specific
 * https://github.com/brmson/dataset-factoid-webquestions for a suit
* GeoQuery, Free917, WebQuestions, SimpleQuestions, GraphQuestions, and QALD
* https://github.com/ysu1989/GraphQuestions GraphQuestions: compositional and paraphrased questions 
* http://www-nlp.stanford.edu/software/sempre/
* http://research.microsoft.com/en-us/um/redmond/projects/mctest/ 
* https://sites.google.com/site/trecliveqa2015/
* http://trec.nist.gov/data/qa.html
* http://trec.nist.gov/data/qamain.html
* http://trec.nist.gov/data/qa/add_qaresources.html
* http://www.slideshare.net/andrenfreitas/schema-agnostic
* http://research.microsoft.com/en-us/downloads/88c0021c-328a-4148-a158-a42d7331c6cf/
* http://projects.semwebcentral.org/ (?)
* http://research.signalmedia.co/newsir16/signal-dataset.html
* Kaggle AI challenge or in general multiple choice questions (e.g., QALD entrance exams)
* needs computation: https://github.com/deepmind/rc-data/
* Federated Queries https://code.google.com/archive/p/fbench/
* http://talc1.loria.fr/webnlg/stories/deliverables.html
* http://nlp.stanford.edu/blog/wikitablequestions-a-complex-real-world-question-understanding-dataset/
* https://cs.umd.edu/~miyyer/qblearn/
out of scope (?) :
* http://alt.qcri.org/semeval2015/task3/ SemEval-2015 Task 3: Answer Selection in Community Question Answering 
* http://alt.qcri.org/semeval2016/task3/ Semeval-2016 Task 3: Community Question Answering
* https://www.microsoft.com/en-us/download/details.aspx?id=52419 WikiQA (big!)
* https://stanford-qa.com/ 100k+ questions with leaderboard 
* http://www.aclweb.org/anthology/P/P16/P16-1145.pdf WIKIREADING, largest dataset!
* http://www.cl.ecei.tohoku.ac.jp/rite2/doku.php?id=wiki:resources only japanese
* QGSTEC automatic question generation workshop
* http://www.cs.cmu.edu/~ark/QA-data/ 
* http://webscope.sandbox.yahoo.com/catalog.php?datatype=l
* http://jmcauley.ucsd.edu/data/amazon/qa/
* http://www.aclweb.org/aclwiki/index.php?title=Question_Answering_(State_of_the_art) 
* https://stackoverflow.blog/2009/06/stack-overflow-creative-commons-data-dump/
* https://medium.com/startup-grind/fueling-the-ai-gold-rush-7ae438505bc2#.51rfdj1kd 

## Focus
This collection aims at becoming a central focus point of question answering research. Using deeper analysis (sentiment, clustering, topic) of a questions will help to understand arising difficulties within QA systems. Moreover, this collection will help also semantic search, e.g. keyword search, phrase search, in later stages.

## Maven Dependency
This library is available as snapshot here: http://maven.aksw.org/archiva/#artifact~snapshots/org.aksw.qa/datasets

```
<dependency>
  <groupId>org.aksw.qa</groupId>
  <artifactId>datasets</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
Add the following repository:
```
<repository>
			<id>maven.aksw.internal</id>
			<name>University Leipzig, AKSW Maven2 Repository</name>
			<url>http://maven.aksw.org/archiva/repository/internal</url>
		</repository>
		<repository>
			<id>maven.aksw.snapshots</id>
			<name>University Leipzig, AKSW Maven2 Repository</name>
			<url>http://maven.aksw.org/archiva/repository/snapshots</url>
</repository>
```

Look for more interesting libraries here: http://maven.aksw.org/archiva/#browse/org.aksw.qa 
