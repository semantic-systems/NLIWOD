# Test queries on http://localhost:3030
SELECT * WHERE {?s ?p ?o.} LIMIT 10

SELECT * WHERE {?s <http://dbpedia.org/ontology/abstract> ?o.} LIMIT 10

PREFIX text: <http://jena.apache.org/text#> SELECT * WHERE {?s ?p ?o. ?o text:query (<http://dbpedia.org/ontology/abstract> 'anti-apartheid activist' )} LIMIT 10

