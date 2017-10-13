export FUSEKI_BASE=./apache-jena-fuseki-3.4.0
export FUSEKI_HOME=./apache-jena-fuseki-3.4.0

java -Xmx32G -jar apache-jena-fuseki-3.4.0/fuseki-server.jar --conf=fuseki_text.ttl --timeout=10000 --verbose 
