# Question Answering (QA) commons
Common utilities to load, store and measure various formats of question answering (QA) datasets and systems.

## Use GERBIL for evaluation of your system against others

With the help of this library, we set up a GERBIL instance (gerbil-qa.aksw.org/gerbil) where you can upload your systems output (use JSON or XML QALD writer) and benchmark your performance.
Advantages: The experiment is archived, citable and comparable because every systems uses the same metrics, datasets and intermediate steps. 

You sure can use also just this library for local testing.

Hint: You can also downlaod GERBIL (https://github.com/aksw/gerbil), check out the branch QuestionAnswering! For example, you can also use that repo as library.

## Maven Dependency
This library is available as snapshot here: http://maven.aksw.org/archiva/#artifact~snapshots/org.aksw.qa/commons

```
<dependency>
  <groupId>org.aksw.qa</groupId>
  <artifactId>commons</artifactId>
  <version>0.4.18</version>
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
