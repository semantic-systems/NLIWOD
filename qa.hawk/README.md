HAWK
====

Hybrid Question Answering (hawk) -- is going to drive forth the OKBQA vision of hybrid question answering using Linked Data and full-text indizes. 

Performance benchmarks can be done on the QALD-5 hybrid benchmark (test+train)

The old repository can be found at https://github.com/aksw/hawk

Restful Service
===
A online service can be found at http://titan.informatik.uni-leipzig.de:8181/

``curl localhost:8181/search?q=What+is+the+capital+of+Germany+%3F``
will return a UUID.
``curl http://localhost:8181/status?UUID=00000000-0000-0000-0000-000000000001`` gives you status updates

or

``curl localhost:8181/simple-search?q=What+is+the+capital+of+Germany+%3F``

Running HAWK
==
!!!Befor running hawk make sure there is a SPARQL endpoint at http://139.18.2.164:3030/ds/sparql or http://titan.informatik.uni-leipzig.de:3030/ds/sparql!!!
!!!Make sure there is an available Stanford CoreNLP server at http://localhost: or http://titan.informatik.uni-leipzig.de:9000/!!!

Running HAWK via Docker
===

HAWK will connect with a SPARQL endpoint on localhost.
There is also a public hawk-specific sparql endpoint http://titan.informatik.uni-leipzig.de:3030/
For starting our specific SPARQL endpoint locally look at deploy-scripts/README.md
If you already have it running, you can build a hawk docker file using the following commands.

``
cd ..
docker build -f qa.hawk/deploy-scripts/Dockerfile-hawk -t hawk .
docker run -d --name hawk -p 8181:8181 --restart=always hawk
``

Running  HAWK via Maven
===
```
mvn spring-boot:run
```
