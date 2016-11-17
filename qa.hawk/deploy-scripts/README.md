# Indexing of DBpedia and Deployment with docker

##Indexing

Run ´./index.sh´ This can take several hours.
This will create two folders which than need to be moved to a subfolder called ´data´.

##Deployment with docker

Use wget to download the latest data from http://139.18.2.164/rusbeck/hawk/index_2016-04.zip and unzip it.

Now use docker to build an image and run it
```
docker build -f Dockerfile-fuseki -t fuseki .
docker run -d --name fuseki-data -p 3030:3030 -v `pwd`/data/:/jena-fuseki/data --restart=always fuseki 
```

##Deployment without docker

Download apache-jena-fuseki-2.4.0 from https://www.apache.org/dist/jena/binaries/apache-jena-fuseki-2.4.1.zip and unzip it. 

```
export FUSEKI_HOME=apache-jena-fuseki-2.4.0/
java -Xmx32G -jar apache-jena-fuseki-2.4.0/fuseki-server.jar --conf=fuseki_hawk_assembler.ttl --timeout=10000
```
