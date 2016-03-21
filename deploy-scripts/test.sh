APACHE_JENA_BIN=~/apache-jena-2.12.1/bin
JENA_FUSEKI_JAR=~/jena-fuseki1-1.3.1/fuseki-server.jar

## QUERYING FUSEKI
$APACHE_JENA_BIN/tdbquery --loc=hawk_data_10-2015_dbpediatbd --desc=fuseki_hawk_assembler.ttl "SELECT * WHERE {?s ?p ?o.} LIMIT 10" 

$APACHE_JENA_BIN/tdbquery --loc=hawk_data_10-2015_dbpediatbd --desc=fuseki_hawk_assembler.ttl "SELECT * WHERE {?s <http://dbpedia.org/ontology/abstract> ?o.} LIMIT 10"

$APACHE_JENA_BIN/tdbquery --loc=hawk_data_10-2015_dbpediatbd --desc=fuseki_hawk_assembler.ttl "PREFIX text: <http://jena.apache.org/text#> SELECT * WHERE {?s ?p ?o. ?o text:query (<http://dbpedia.org/ontology/abstract> 'anti-apartheid activist' )} LIMIT 10"

## STARTING FUSEKI
#./fuseki-server --conf=hawk_assembler.ttl &
