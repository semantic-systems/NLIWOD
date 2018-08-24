# HAWK

Hybrid Question Answering (hawk) -- is going to drive forth the OKBQA vision of hybrid question answering using Linked Data and full-text indizes. 

Performance benchmarks can be done on the QALD-5 hybrid benchmark (test+train)

The old repository can be found at [https://github.com/aksw/hawk](https://github.com/aksw/hawk)

## Restful Service

You can test Hawk features through two web service endpoints:

```bash
curl localhost:8181/simple-search?q=What+is+the+capital+of+Germany+%3F``
```

or

```bash
curl -X POST -d "query=What is the capital o Germany?&lang=en" http://localhost:8181/ask-gerbil
```

## Running HAWK

### Prerequisites

* Before running Hawk make sure there is a Fuseki SPARQL endpoint at `http://localhost:3030/ds/query` (**RECOMMENDED**). By default, Hawk points to SPARQL endpoint at `http://131.234.28.52:3030/ds/sparql`, but you can switch to your local instance in the [application.properties](src/main/resources/application.properties).

* Make sure there is an available Stanford CoreNLP server at `http://localhost:9000` (**RECOMMENDED**). Default server is `http://139.18.2.39:9000`, but you can switch to your local instance in the [application.properties](src/main/resources/application.properties).

### Deploying HAWK via Docker

Prior deploying a HAWK instance, it needs to connect to a SPARQL endpoint, and interact with a Stanford CoreNLP server.
For deploying a *containerized* SPARQL endpoint locally look at [deploy-scripts/README.md](deploy-scripts/README.md).
In case you also want to intall a local *containerized* instance of Stanford CoreNLP server, run the following commands:

```bash
docker run -d -p 9000:9000 --name coreNLP motiz88/corenlp
```

If you already have both requirements running, you can now deploy a Hawk docker file using the following commands:

```bash
cd ..
docker build -f qa.hawk/deploy-scripts/Dockerfile-hawk -t hawk .
docker run -d --name hawk -p 8181:8181 --restart=always hawk
```

### Building HAWK

Using either `mvn package` or `mvn install` there will be two artifacts generated (and installed) :

* hawk-(version).jar only contains hawk and can be used as a dependency for other projects.

* hawk-(version)-bootable-with-spring.jar does not only contains hawk, but makes it runnable using Spring Maven plugin.


### Running HAWK via Spring Maven plugin

```bash
mvn spring-boot:run
```
