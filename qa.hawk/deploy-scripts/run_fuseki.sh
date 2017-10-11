export FUSEKI_BASE=/tmp
export FUSEKI_HOME=/home/datascienceadmin/NLIWOD/qa.hawk/deploy-scripts/apache-jena-fuseki-3.4.0

java -Xmx32G -jar apache-jena-fuseki-3.4.0/fuseki-server.jar --conf=fuseki_new.ttl --timeout=10000 --verbose 
