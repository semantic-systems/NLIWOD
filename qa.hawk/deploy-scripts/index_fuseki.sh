#!/bin/bash

# Apache Indexing Software
if [ ! -d "apache-jena-3.1.0" ];
then
	wget http://archive.apache.org/dist/jena/binaries/apache-jena-3.1.0.zip
	unzip apache-jena-3.1.0.zip
	rm apache-jena-3.1.0.zip
fi

if [ ! -d "apache-jena-fuseki-2.4.0" ];
then
	wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.4.0.zip
	unzip apache-jena-fuseki-2.4.0.zip
	rm apache-jena-fuseki-2.4.0.zip
fi

# DBpedia server
echo "dbpedia_2016-04.owl"
if [ ! -f "dbpedia_2016-04.owl" ];
then
  wget http://downloads.dbpedia.org/2016-04/dbpedia_2016-04.owl
fi
if [ ! -f "disambiguations_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/disambiguations_en.ttl.bz2
  bunzip2 disambiguations_en.ttl.bz2
fi
if [ ! -f "instance_types_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/instance_types_en.ttl.bz2
  bunzip2 instance_types_en.ttl.bz2
fi
if [ ! -f "long_abstracts_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/long_abstracts_en.ttl.bz2 
  bunzip2 long_abstracts_en.ttl.bz2
fi
if [ ! -f "labels_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/labels_en.ttl.bz2
  bunzip2 labels_en.ttl.bz2
fi
if [ ! -f "mappingbased_literals_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/mappingbased_literals_en.ttl.bz2
  bunzip2 mappingbased_literals_en.ttl.bz2
fi
if [ ! -f "mappingbased_objects_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/mappingbased_objects_en.ttl.bz2
  bunzip2 mappingbased_objects_en.ttl.bz2
fi
if [ ! -f "persondata_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/persondata_en.ttl.bz2
  bunzip2 persondata_en.ttl.bz2
fi
if [ ! -f "specific_mappingbased_properties_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/specific_mappingbased_properties_en.ttl.bz2
  bunzip2 specific_mappingbased_properties_en.ttl.bz2
fi
if [ ! -f "transitive_redirects_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/transitive_redirects_en.ttl.bz2
  bunzip2 transitive_redirects_en.ttl.bz2
fi
if [ ! -f "instance_types_transitive_en.ttl" ];
then
  wget http://downloads.dbpedia.org/2016-04/core-i18n/en/instance_types_transitive_en.ttl.bz2
  bunzip2 instance_types_transitive_en.ttl.bz2
fi

#Titan server
if [ ! -f "en_surface_forms.ttl" ];
then
  wget http://139.18.2.164/rusbeck/hawk/en_surface_forms.ttl
fi
#if [ ! -f "fuseki_hawk_assembler.ttl" ];
#then
#  wget http://139.18.2.164/rusbeck/hawk/fuseki_hawk_assembler.ttl
#fi

#KIT Servers
if [ ! -f "pagerank_en_2016-04.ttl" ];
then
  wget http://people.aifb.kit.edu/ath/download/pagerank_en_2016-04.ttl.bz2
  bunzip2 pagerank_en_2016-04.ttl.bz2
fi

#Create more labels for URIs from redirects (takes a long time)

if [ ! -f "redirect_labels.ttl" ];
then
	sort labels_en.ttl > labels_en.ttl.sorted 
	sort transitive_redirects_en.ttl  > transitive_redirects_en.ttl.sorted
	join -1 1 -2 1 labels_en.ttl.sorted transitive_redirects_en.ttl.sorted  > join.tsv
	cat join.tsv | awk '{sub("<http://www.w3.org/2000/01/rdf-schema#label>","",$0);sub("<http://dbpedia.org/ontology/wikiPageRedirects>","",$0); sub(" .  "," ",$0); print $0}'| awk -F">  \"" '{print "\""$2 }'| awk -F"@en <" '{print $2 " <http://www.w3.org/2000/01/rdf-schema#label> " $1}' | awk '{sub(" . "," ",$0);  print "<"$0 " . " }' > redirect_labels.ttl
fi


#Script Setup for Linux-based systems

export JENA_HOME=apache-jena-3.1.0
export FUSEKI_HOME=apache-jena-fuseki-2.4.0

APACHE_JENA_BIN=./apache-jena-3.1.0/bin
JENA_FUSEKI_JAR=apache-jena-fuseki-2.4.0/fuseki-server.jar

if [ ! -d "data/hawk_data_04-2016_dbpediatbd" ];
then
	mkdir data
	cd data
        mkdir hawk_data_04-2016_dbpediatbd/
        cd ..
	$APACHE_JENA_BIN/tdbloader2 --loc=data/hawk_data_04-2016_dbpediatbd dbpedia_2016-04.owl disambiguations_en.ttl instance_types_en.ttl long_abstracts_en.ttl labels_en.ttl mappingbased_objects_en.ttl mappingbased_literals_en.ttl persondata_en.ttl specific_mappingbased_properties_en.ttl en_surface_forms.ttl pagerank_en_2016-04.ttl redirect_labels.ttl instance_types_en.ttl instance_types_transitive_en.ttl
fi

if [ ! -d "data/hawk_data_04-2016_dbpedialucene" ];
then
	cd data
	mkdir hawk_data_04-2016_dbpedialucene
        cd ..
	java -Xmx16G -cp $JENA_FUSEKI_JAR jena.textindexer --desc=fuseki_hawk_assembler.ttl
fi


#Clean up

rm *.bz2
rm *.sorted
rm -r apache-jena-3.1.0*
rm -r apache-jena-fuseki-2.4.0		
rm dbpedia_2016-04.owl* 
rm disambiguations_en.ttl
rm mappingbased_literals_en.ttl
rm instance_types_en.ttl
rm mappingbased_objects_en.ttl
rm specific_mappingbased_properties_en.ttl
rm en_surface_forms.ttl
rm instance_types_transitive_en.ttl
rm pagerank_en_2016-04.ttl
rm join.tsv
rm transitive_redirects_en.ttl
rm labels_en.ttl
rm persondata_en.ttl
rm long_abstracts_en.ttl
rm redirect_labels.ttl
