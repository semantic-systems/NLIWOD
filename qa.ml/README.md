# Machine Learning over Question Answering System

This project aims at combining different QA Systems to improve the overall performance.

## Available QA Systems on the Web

Currently, we work on log files provided from the QALD-5 challenge. In the future, we want to use several web services.

Action items: Implement various wrappers for webservices to train and test on. Store the results in files.
* NLSearch https://wikidata.metaphacts.com/resource/NLSearch
* YodaQA http://live.ailao.eu/
* Start http://start.csail.mit.edu/index.php
* QAKIS http://qakis.org/qakis2/
* TBSL http://linkedspending.aksw.org/tbsl/
* SINA http://sina.aksw.org/
* LODQA http://lodqa.org/
* HAWK http://hawk.aksw.org/
* poweraqua http://poweraqua.open.ac.uk:8080/poweraqua
* gAnswer http://59.108.48.18:8080/gAnswer/ganswer.jsp

No demo found sofar for
* ISOFT
* QAnswer
* APEQ
* SemGraphQA
* Xser
* Freya
* questIO
* DEANNA
* Intui3
* RTV
* CASIA
* MHE
* SemSeK


For more systems see page 7 http://www.semantic-web-journal.net/system/files/swj1205.pdf

##The Experiment Idea


Our experiment implementation is based on https://weka.wikispaces.com/Programmatic+Use.

###Simple Decision Making

For each system, we learn a classifier whether this system can answer the features of the class or not. 
We choose the one with the highest probability as the system that should answer.

###Enhanced solution

We try to learn how to combine answer sets.
We use a multilayer perceptron for the following:
* input: system, confidence (from simple solution), feature of question, F-measure of the system
* output: classification value c

For each system with c>0.5 we merge the answers and analyze whether union or intersection is better
