package org.aksw.hawk.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class DBOIndex {

	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(DBOIndex.class);
	public String FIELD_NAME_SUBJECT = "subject";
	public String FIELD_NAME_PREDICATE = "predicate";
	public String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 100;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private SimpleAnalyzer analyzer;

	public DBOIndex() {
		try {
			File index = new File("resources/indexOntology");
			analyzer = new SimpleAnalyzer(LUCENE_VERSION);
			if (!index.exists()) {
				index.mkdir();
				IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
				directory = new MMapDirectory(index);
				iwriter = new IndexWriter(directory, config);
				index();
			} else {
				directory = new MMapDirectory(index);
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	public ArrayList<String> search(final String object) {
		ArrayList<String> uris = Lists.newArrayList();
		try {
			log.debug("\t start asking index...");

			// remove hyphens assertTrue
			// if (object.contains("-")) {
			// object = "\"" + object.replace("-", " ") + "\"";
			// }
			// FuzzyQuery q = new FuzzyQuery(new Term(FIELD_NAME_OBJECT,
			// object), 0);

			QueryParser qp = new QueryParser(LUCENE_VERSION, FIELD_NAME_OBJECT, analyzer);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(qp.createPhraseQuery(FIELD_NAME_OBJECT, object), collector);
			// isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (ScoreDoc hit : hits) {
				Document hitDoc = isearcher.doc(hit.doc);
				uris.add(hitDoc.get(FIELD_NAME_SUBJECT));
			}
			log.debug("\t finished asking index...");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + object, e);
		}
		return uris;
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	private void index() {
		try {
			Model dbpedia = ModelFactory.createDefaultModel();
			Properties prop = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("hawk.properties");
			prop.load(input);
			String file = prop.getProperty("owl");

			dbpedia.read(getClass().getClassLoader().getResourceAsStream(file), "RDF/XML");
			StmtIterator stmts = dbpedia.listStatements(null, RDFS.label, (RDFNode) null);
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				RDFNode label = stmt.getObject();
				if (label.asLiteral().getLanguage().equals("en")) {
					addDocumentToIndex(stmt.getSubject(), "rdfs:label", label.asLiteral().getString());
					NodeIterator comment = dbpedia.listObjectsOfProperty(stmt.getSubject(), RDFS.comment);
					while (comment.hasNext()) {
						RDFNode next = comment.next();
						if (next.asLiteral().getLanguage().equals("en")) {
							addDocumentToIndex(stmt.getSubject(), "rdfs:comment", next.asLiteral().getString());
						}
					}
				}
			}
			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	private void addDocumentToIndex(final Resource resource, final String predicate, final String object) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_SUBJECT, resource.getURI(), Store.YES));
		doc.add(new StringField(FIELD_NAME_PREDICATE, predicate, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, object, Store.YES));
		iwriter.addDocument(doc);
	}

}
