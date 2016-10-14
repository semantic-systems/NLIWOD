#Question Answering Systems

This collection includes the following systems:
* YodaQA http://live.ailao.eu/
	* does not return DBResources; we remedy that by using a query 
* Start http://start.csail.mit.edu/index.php
	* unreliable through non-uniform delivery of data 
* QAKIS http://qakis.org/qakis2/
* SINA http://sina.aksw.org/
	* many timeouts 
* HAWK http://hawk.aksw.org/

Not yet implemented
* OKBQA http://ws.okbqa.org/~testuser02/ 
* LODQA http://lodqa.org/
* TBSL http://linkedspending.aksw.org/tbsl/
* NLSearch https://wikidata.metaphacts.com/resource/NLSearch
* gAnswer http://59.108.48.18:8080/gAnswer/ganswer.jsp
* poweraqua http://poweraqua.open.ac.uk:8080/poweraqua

ACTION: Clean up both lists

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
* Alexandria http://alexandria.neofonie.de/

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


## Maven Dependency
This library is available as snapshot here: http://maven.aksw.org/archiva/#artifact~snapshots/org.aksw.qa/datasets

```
<dependency>
  <groupId>org.aksw</groupId>
  <artifactId>qa.systems</artifactId>
  <version>0.0.2</version>
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

Look for more interesting libraries here: http://maven.aksw.org/
