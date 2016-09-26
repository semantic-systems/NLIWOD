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
