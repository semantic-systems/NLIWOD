
Docker: https://hub.docker.com/r/stain/jena-fuseki/

Jena Text Index: https://jena.apache.org/documentation/query/text-query.html


mkdir data
docker run -d --name fuseki -p 3030:3030 -v data:/fuseki -e ADMIN_PASSWORD=pw123 -it stain/jena-fuseki 
