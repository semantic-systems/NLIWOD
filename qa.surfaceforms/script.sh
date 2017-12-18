!#/bin/bash

mkdir data/2016-10
mkdir data/2016-10/en
cd data/2016-10/en

wget http://downloads.dbpedia.org/2016-10/core-i18n/en/labels_en.ttl.bz2
bzip2 -d labels_en.ttl.bz2

wget http://downloads.dbpedia.org/2016-10/core-i18n/en/redirects_en.ttl.bz2
bzip2 -d redirects_en.ttl.bz2

wget http://downloads.dbpedia.org/2016-10/core-i18n/en/disambiguations_en.ttl.bz2
bzip2 -d disambiguations_en.ttl.bz2

wget http://downloads.dbpedia.org/2016-10/core-i18n/en/interlanguage_links_en.ttl.bz2
bzip2 -d interlanguage_links_en.ttl.bz2

wget http://downloads.dbpedia.org/2016-10/dbpedia_2016-10.nt

wget http://downloads.dbpedia.org/2016-10/core-i18n/en/infobox_property_definitions_en.ttl.bz2
bzip2 -d infobox_property_definitions_en.ttl.bz2
