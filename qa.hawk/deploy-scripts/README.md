# Indexing of DBpedia and Deployment with docker

## Indexing

Run ´./index_fuseki.sh´ This can take several hours.
This will create two folders under a subfolder called ´data´.

## Deployment with docker

Use wget to download the latest data from http://hobbitdata.informatik.uni-leipzig.de/hawk/hawk_dbpedia_2016-10.tar.gz and unzip it.

Now use docker to build an image and run it
```
docker build -f Dockerfile-fuseki -t fuseki .
docker run -d --name fuseki-data -p 3030:3030 -v `pwd`/data/:/jena-fuseki/data --restart=always fuseki 
```

## Deployment without docker

Download apache-jena-fuseki-3.4.0 from http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-3.4.0.zip and unzip it. 

```
export FUSEKI_HOME=apache-jena-fuseki-3.4.0/
java -Xmx32G -jar apache-jena-fuseki-3.4.0/fuseki-server.jar --conf=fuseki_text.ttl --timeout=10000
```
