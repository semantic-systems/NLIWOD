#Question Answering Systems

| System        |                   Webservice URI                  | Comment                   | Included|
|---------------|:-------------------------------------------------:|---------------------------|---------|
| **-QALD 1** |
| FREYA         |                                                   |                           |         |
| poweraqua     | http://poweraqua.open.ac.uk:8080/poweraqua        |  webservice not reachable |         |
| SWIP          |                                                   |                           |         |
| **-QALD 2** |
| Alexandria    | http://alexandria.neofonie.de/                    |                           |         |
| SemSeK        |                                                   |                           |         |
| MHE           |                                                   |                           |         |
| QAKIS         | http://qakis.org/qakis2/                          |                           |    X    |
| **-QALD 3** |
| squal2sparql  |                                                   |                           |         |
| CASIA         |                                                   |                           |         |
| Scalewelis    |                                                   |                           |         |
| RTV           |                                                   |                           |         |
| Intui2        |                                                   |                           |         |
| **-QALD 4** |
| Xser          |                                                   |                           |         |
| gAnswer       | http://59.108.48.18:8080/gAnswer/ganswer.jsp      |                           |         |
| Intui3        |                                                   |                           |         |
| ISOFT         |                                                   |                           |         |
| RO_FII        |                                                   |                           |         |
| **-QALD 5** |
| APEQ          |                                                   |                           |         |
| QAnswer       |                                                   |                           |         |
| SemGraphQA    |                                                   |                           |         |
| YodaQA        | http://live.ailao.eu/                             |                           |     X   |
| HAWK          | http://hawk.aksw.org/                             |                           |     X   |
| **-QALD 6** |
| CANaLI        |                                                   |                           |         |
| PersionQA     |                                                   |                           |         |
| UTQA          |                                                   |                           |         |
| KGWAnswer     |                                                   |                           |         |
| NbFramework   |                                                   |                           |         |
| UIQA          |                                                   |                           |         |
| **-Miscelleanous** |
| NLSearch      | https://wikidata.metaphacts.com/resource/NLSearch |                           |         |
| OKBQA         | http://ws.okbqa.org/~testuser02/                  |                           |         |
| DEANNA        |                                                   |                           |         |
| questIO       |                                                   |                           |         |
| SINA          | http://sina.aksw.org/                             | very slow                 |     X   |
| Start         | http://start.csail.mit.edu/index.php              | non-uniform answer format |     X   |
| TBSL          | http://linkedspending.aksw.org/tbsl/              | unstable                  |         |
| LODQA         | http://lodqa.org/                                 | only searches for datasets|         |

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
