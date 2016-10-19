#Question Answering Systems

This collection includes the following systems:
* YodaQA
* Start 
* QAKIS
* SINA
* HAWK 

In the future, we want to support more systems.

| System        |                   Webservice URI                  | Comment                                                      |
|---------------|:-------------------------------------------------:|--------------------------------------------------------------|
| **-QALD 1** |
| FREYA         |                                                   |                                                              |
| poweraqua     | http://poweraqua.open.ac.uk:8080/poweraqua        |                                                              |
| SWIP          |                                                   |                                                              |
| **-QALD 2** |
| Alexandria    | http://alexandria.neofonie.de/                    |                                                              |
| SemSeK        |                                                   |                                                              |
| MHE           |                                                   |                                                              |
| QAKIS         | http://qakis.org/qakis2/                          |                                                              |
| **-QALD 3** |
| squal2sparql  |                                                   |                                                              |
| CASIA         |                                                   |                                                              |
| Scalewelis    |                                                   |                                                              |
| RTV           |                                                   |                                                              |
| Intui2        |                                                   |                                                              |
| **-QALD 4** |
| Xser          |                                                   |                                                              |
| gAnswer       | http://59.108.48.18:8080/gAnswer/ganswer.jsp      |                                                              |
| Intui3        |                                                   |                                                              |
| ISOFT         |                                                   |                                                              |
| RO_FII        |                                                   |                                                              |
| **-QALD 5** |
| APEQ          |                                                   |                                                              |
| QAnswer       |                                                   |                                                              |
| SemGraphQA    |                                                   |                                                              |
| YodaQA        | http://live.ailao.eu/                             | does not return DBResources; we remedy that by using a query |
| HAWK          | http://hawk.aksw.org/                             |                                                              |
| **-QALD 6** |
| CANaLI        |                                                   |                                                              |
| PersionQA     |                                                   |                                                              |
| UTQA          |                                                   |                                                              |
| KGWAnswer     |                                                   |                                                              |
| NbFramework   |                                                   |                                                              |
| UIQA          |                                                   |                                                              |
| **-Miscelleanous** |
| NLSearch      | https://wikidata.metaphacts.com/resource/NLSearch |                                                              |
| OKBQA         | http://ws.okbqa.org/~testuser02/                  |                                                              |
| DEANNA        |                                                   |                                                              |
| questIO       |                                                   |                                                              |
| SINA          | http://sina.aksw.org/                             | very slow                                                    |
| Start         | http://start.csail.mit.edu/index.php              | unreliable through non-uniform delivery of data              |
| TBSL          | http://linkedspending.aksw.org/tbsl/              | unstable                                                     |
| LODQA         | http://lodqa.org/                                 |                                                              |

For more systems see page 7 of http://www.semantic-web-journal.net/system/files/swj1205.pdf


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
