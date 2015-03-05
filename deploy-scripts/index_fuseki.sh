#!/bin/bash

#Script Setup
APACHE_JENA_BIN=/home/ivan/Soft/Installed/jena/apache-jena-2.12.1/bin
JENA_FUSEKI_JAR=/home/ivan/Soft/Installed/jena/jena-fuseki-1.1.1/fuseki-server.jar
PATH=$PATH:$APACHE_JENA_BIN
DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )

#Mennheim DBpedia server
echo "$DIR/dbpedia_2014.owl"
if [ ! -f "$DIR/dbpedia_2014.owl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/dbpedia_2014.owl.bz2 
  bunzip2 dbpedia_2014.owl.bz2 
fi
if [ ! -f "$DIR/disambiguations_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/disambiguations_en.ttl.bz2
  bunzip2 disambiguations_en.ttl.bz2
fi
if [ ! -f "$DIR/instance_types_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/instance_types_en.ttl.bz2
  bunzip2 instance_types_en.ttl.bz2
fi
if [ ! -f "$DIR/long_abstracts_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/long_abstracts_en.ttl.bz2 
  bunzip2 long_abstracts_en.ttl.bz2
fi
if [ ! -f "$DIR/pnd_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/pnd_en.ttl.bz2
  bunzip2 pnd_en.ttl.bz2
fi
if [ ! -f "$DIR/labels_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/labels_en.ttl.bz2
  bunzip2 labels_en.ttl.bz2
fi
if [ ! -f "$DIR/mappingbased_properties_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/mappingbased_properties_en.ttl.bz2 
  bunzip2 mappingbased_properties_en.ttl.bz2
fi
if [ ! -f "$DIR/persondata_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/persondata_en.ttl.bz2
  bunzip2 persondata_en.ttl.bz2
fi
if [ ! -f "$DIR/specific_mappingbased_properties_en.ttl" ];
then
  wget http://data.dws.informatik.uni-mannheim.de/dbpedia/2014/en/specific_mappingbased_properties_en.ttl.bz2
  bunzip2 specific_mappingbased_properties_en.ttl.bz2
fi

#Titan server
if [ ! -f "$DIR/en_surface_forms.ttl" ];
then
  wget http://139.18.2.164/rusbeck/hawk/en_surface_forms.ttl
fi
if [ ! -f "$DIR/redirect_resolved.ttl" ];
then
  wget http://139.18.2.164/rusbeck/hawk/redirect_resolved.ttl
fi
if [ ! -f "$DIR/fuseki_hawk_assembler.ttl" ];
then
  wget http://139.18.2.164/rusbeck/hawk/fuseki_hawk_assembler.ttl
fi

#KIT Servers
if [ ! -f "$DIR/pagerank_en_2014.ttl" ];
then
  wget http://people.aifb.kit.edu/ath/pagerank_en_2014.ttl.bz2
  bunzip2 pagerank_en_2014.ttl.bz2
fi

#Takes a lot of time!
tdbloader2 --loc=dbpediatbd dbpedia_2014.owl disambiguations_en.ttl instance_types_en.ttl long_abstracts_en.ttl pagerank_en_2014.ttl pnd_en.ttl en_surface_forms.ttl labels_en.ttl mappingbased_properties_en.ttl persondata_en.ttl redirect_resolved.ttl specific_mappingbased_properties_en.ttl

#java -Xmx6G -cp $JENA_FUSEKI_JAR jena.textindexer --desc=fuseki_hawk_assembler.ttl
