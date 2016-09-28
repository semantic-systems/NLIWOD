# Possible Services

 * http://wdaqua-qanary.univ-st-etienne.fr/#/overview , modules from WDAqua project
 * https://github.com/dbpedia-spotlight/dbpedia-spotlight , working ner + nif tool
 * https://github.com/w3c/web-annotation/issues/356 , open question
 * https://freme-project.github.io/api-doc/simple.html
 * https://github.com/NLP2RDF/software , covering stanford
 * http://wit.istc.cnr.it/stlab-tools/fred , fred as sentiment tool
 * wrapping of services using http://restdesc.org/ and https://spring.io/guides/gs/service-registration-and-discovery/
 
# Running this application
`mvn clean package spring-boot:run`

# Tasks

- [ ] @J: Simple Core Komponente über Templates bauen, die disambiguierte Entities brauch
- [ ] @J: Class Index als webservice
- [ ] @J: Property Index als webservice
- [ ] @N: Loader für Registry
- [ ] @N: Webservices von oben einpflegen
- [ ] @J,N: Vorschlag machen, wie man generierte Pipelines speichert (erstmal in memory) und so online stellt, dass man sie benutzen kann