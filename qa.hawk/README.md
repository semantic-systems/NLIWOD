HAWK
====

Hybrid Question Answering (hawk) -- is going to drive forth the OKBQA vision of hybrid question answering using Linked Data and full-text indizes. 

Performance benchmarks can be done on the QALD-5 hybrid benchmark (test+train)

The old repository can be found at https://github.com/aksw/hawk

Restful Service
===
``curl localhost:8181/search?q=What+is+the+capital+of+Germany+%3F``
will return a UUID.
``curl http://localhost:8181/status?UUID=00000000-0000-0000-0000-000000000001`` gives you status updates

or

``curl localhost:8181/simple-search?query=What+is+the+capital+of+Germany+%3F``

Running HAWK
==
!!!Before running hawk make sure there is a SPARQL endpoint at http://localhost: or http://131.234.28.52:3030/ds/sparql!!! <br>
!!!Make sure there is an available Stanford CoreNLP server at http://localhost: or http://akswnc9.informatik.uni-leipzig.de:9000/!!!

Running HAWK via Docker
===

HAWK will connect with a SPARQL endpoint on localhost.
There is also a public hawk-specific sparql endpoint http://131.234.28.52:3030/
For starting our specific SPARQL endpoint locally look at deploy-scripts/README.md
If you already have it running, you can build a hawk docker file using the following commands.

```
cd ..
docker build -f qa.hawk/deploy-scripts/Dockerfile-hawk -t hawk .
docker run -d --name hawk -p 8181:8181 --restart=always hawk
```

Building HAWK
===
Using mvn package or mvn install there will be two artifacts generated (and installed) :

-> hawk-(version).jar only contains hawk and can be used as a dependency for other projects.

-> hawk-(version)-bootable-with-spring.jar does not only contains hawk, but makes it runnable 
   via spring (see Running HAWK via maven)


Running  HAWK via Maven
===
```
mvn spring-boot:run
```
