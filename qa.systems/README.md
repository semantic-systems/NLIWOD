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
| YodaQA        | http://live.ailao.eu/                             | webservice not working    |     X   |
| HAWK          | http://hawk.aksw.org/                             | webservice not working    |     X   |
| **-QALD 6** |
| CANaLI        |                                                   |                           |         |
| PersionQA     |                                                   |                           |         |
| UTQA          |                                                   |                           |         |
| KGWAnswer     |                                                   |                           |         |
| NbFramework   |                                                   |                           |         |
| UIQA          |                                                   |                           |         |
| **-QALD 9** |
| WDAqua-core1  | http://qanswer-frontend.univ-st-etienne.fr/       |                           |     X   |
| gAnswer       | http://ganswer.gstore-pku.com/                    |                           |     X   |
| TeBaQA        | http://139.18.2.39:8187/                          |                           |     X   |
| QASystem      | http://qald-beta.cs.upb.de:80/gerbil              |                           |     X   |
| **-Miscelleanous** |
| NLSearch      | https://wikidata.metaphacts.com/resource/NLSearch |                           |         |
| OKBQA         | http://ws.okbqa.org/wui-2016/                     |                           |     X   |
| DEANNA        |                                                   |                           |         |
| questIO       |                                                   |                           |         |
| SINA          | http://sina.aksw.org/                             | webservice not reachable  |     X   |
| Start         | http://start.csail.mit.edu/index.php              | non-uniform answer format |     X   |
| TBSL          | http://linkedspending.aksw.org/tbsl/              | unstable                  |         |
| LODQA         | http://lodqa.org/                                 | only searches for datasets|         |
| AskNow        | https://asknowdemo.sda.tech/                      |                           |     X   |
| Platypus      | https://projetpp.github.io/demo.html              |                           |     X   |
| Quepy         | http://quepy.machinalis.com/                      | non-uniform answer format |     X   |
| SorokinQA         | http://semanticparsing.ukp.informatik.tu-darmstadt.de:5000/question-answering/static/index.html  | very slow                 |     X   |
| FRANKENSTEIN         | http://frankenstein.qanary-qa.com/                     |  |     X   |
| QUINT         | https://gate.d5.mpi-inf.mpg.de/quint/quint                      |  |     X   |

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
