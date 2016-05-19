#!/bin/bash

wget http://archive.apache.org/dist/jena/binaries/apache-jena-3.0.1.zip
unzip apache-jena-3.0.1.zip
rm apache-jena-3.0.1.zip

wget http://archive.apache.org/dist/jena/binaries/jena-fuseki1-1.3.1-distribution.zip
unzip jena-fuseki1-1.3.1-distribution.zip
rm jena-fuseki1-1.3.1-distribution.zip

#Script Setup for Linux-based systems
export JENA_HOME=apache-jena-3.0.1
export FUSEKI_HOME=jena-fuseki1-1.3.1

APACHE_JENA_BI=apache-jena-3.0.1/bin
JENA_FUSEKI_JAR=jena-fuseki1-1.3.1/fuseki-server.jar

PATH=$PATH:$APACHE_JENA_BIN
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

#Mannheim DBpedia server
echo "$DIR/dbpedia_2015-10.owl"
if [ ! -f "$DIR/dbpedia_2015-10.owl" ];
then
  wget http://downloads.dbpedia.org/2015-10/dbpedia_2015-10.owl
fi
if [ ! -f "$DIR/disambiguations_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/disambiguations_en.ttl.bz2
  bunzip2 disambiguations_en.ttl.bz2
fi
if [ ! -f "$DIR/instance_types_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/instance_types_en.ttl.bz2
  bunzip2 instance_types_en.ttl.bz2
fi
if [ ! -f "$DIR/long_abstracts_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/long_abstracts_en.ttl.bz2 
  bunzip2 long_abstracts_en.ttl.bz2
fi
if [ ! -f "$DIR/pnd_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/pnd_en.ttl.bz2
  bunzip2 pnd_en.ttl.bz2
fi
if [ ! -f "$DIR/labels_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/labels_en.ttl.bz2
  bunzip2 labels_en.ttl.bz2
fi
if [ ! -f "$DIR/mappingbased_literals_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/mappingbased_literals_en.ttl.bz2
  bunzip2 mappingbased_literals_en.ttl.bz2
fi
if [ ! -f "$DIR/mappingbased_objects_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/mappingbased_objects_en.ttl.bz2
  bunzip2 mappingbased_objects_en.ttl.bz2
fi
if [ ! -f "$DIR/persondata_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/persondata_en.ttl.bz2
  bunzip2 persondata_en.ttl.bz2
fi
if [ ! -f "$DIR/specific_mappingbased_properties_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/specific_mappingbased_properties_en.ttl.bz2
  bunzip2 specific_mappingbased_properties_en.ttl.bz2
fi
if [ ! -f "$DIR/transitive_redirects_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2015-10/core-i18n/en/transitive_redirects_en.ttl.bz2
  bunzip2 transitive_redirects_en.ttl.bz2
fi
#Titan server
if [ ! -f "$DIR/en_surface_forms.ttl" ];
then
  wget http://139.18.2.164/rusbeck/hawk/en_surface_forms.ttl
fi
#if [ ! -f "$DIR/redirect_resolved.ttl" ];
#then
#  wget http://139.18.2.164/rusbeck/hawk/redirect_resolved.ttl
#fi
#if [ ! -f "$DIR/fuseki_hawk_assembler.ttl" ];
#then
# wget http://139.18.2.164/rusbeck/hawk/fuseki_hawk_assembler.ttl
#fi

#KIT Servers
if [ ! -f "$DIR/pagerank_en_2015-10.ttl" ];
then
  wget http://people.aifb.kit.edu/ath/download/pagerank_en_2015-10.ttl.bz2
  bunzip2 pagerank_en_2015-10.ttl.bz2
fi

#Takes a lot of time!

sort labels_en.ttl > labels_en.ttl.sorted 
sort transitive_redirects_en.ttl  > transitive_redirects_en.ttl.sorted
join -1 1 -2 1 labels_en.ttl.sorted transitive_redirects_en.ttl.sorted  > join.tsv

cat join.tsv | awk '{sub("<http://www.w3.org/2000/01/rdf-schema#label>","",$0);sub("<http://dbpedia.org/ontology/wikiPageRedirects>","",$0); sub(" .  "," ",$0); print $0}'| awk -F">  \"" '{print "\""$2 }'| awk -F"@en <" '{print $2 " <http://www.w3.org/2000/01/rdf-schema#label> " $1}' | awk '{sub(" . "," ",$0);  print "<"$0 " . " }' > redirect_labels.ttl

mkdir hawk_data_10-2015_dbpediatbd/

$APACHE_JENA_BIN/tdbloader2 --loc=hawk_data_10-2015_dbpediatbd dbpedia_2015-10.owl disambiguations_en.ttl instance_types_en.ttl long_abstracts_en.ttl pnd_en.ttl  labels_en.ttl mappingbased_objects_en.ttl mappingbased_literals_en.ttl persondata_en.ttl specific_mappingbased_properties_en.ttl transitive_redirects_en.ttl en_surface_forms.ttl pagerank_en_2015-10.ttl redirect_labels.ttl

java -Xmx16G -cp $JENA_FUSEKI_JAR jena.textindexer --desc=fuseki_hawk_assembler.ttl


#Clean up

rm mappingbased_literals_en.ttl redirect_labels.ttl transitive_redirects_en.ttl.sorted join.tsv mappingbased_objects_en.ttl dbpedia_2015-10.owl labels_en.ttl pagerank_en_2015-10.ttl specific_mappingbased_properties_en.ttl disambiguations_en.ttl labels_en.ttl.sorted persondata_en.ttl en_surface_forms.ttl instance_types_en.ttl long_abstracts_en.ttl pnd_en.ttl transitive_redirects_en.ttl
