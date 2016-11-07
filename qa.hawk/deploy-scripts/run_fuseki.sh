export FUSEKI_BASE=/tmp
export FUSEKI_HOME=/home/RicardoUsbeck/hawk/deploy-scripts/apache-jena-fuseki-2.4.0

java -Xmx32G -jar apache-jena-fuseki-2.4.0/fuseki-server.jar --conf=fuseki_hawk_assembler.ttl --timeout=10000 --verbose 
