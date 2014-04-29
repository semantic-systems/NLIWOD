package org.aksw.hawk.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DBOIndex {

	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(DBOIndex.class);
	public String FIELD_NAME_SUBJECT = "subject";
	public String FIELD_NAME_PREDICATE = "predicate";
	public String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 1;

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

	public String search(String object) {
		try {
			log.debug("\t start asking index...");

			QueryParser parser = new QueryParser(LUCENE_VERSION, FIELD_NAME_OBJECT, analyzer);
			parser.setDefaultOperator(QueryParser.Operator.OR);
			Query q = parser.parse(QueryParserBase.escape(object));
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);

			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String o = hitDoc.get(FIELD_NAME_SUBJECT);
				log.debug("\t finished asking index...");
				return o;
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + object);
		}
		return null;
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
			InputStream url = ClassLoader.getSystemResourceAsStream("dbpedia_3.9.owl");
			dbpedia.read(url, "RDF/XML");
			StmtIterator stmts = dbpedia.listStatements(null, RDFS.label, (RDFNode) null);
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				RDFNode label = stmt.getObject();
				if (label.asLiteral().getLanguage().equals("en")) {
					addDocumentToIndex(stmt.getSubject(), "rdfs:label", label.asLiteral());
					NodeIterator comment = dbpedia.listObjectsOfProperty(stmt.getSubject(), RDFS.comment);
					while (comment.hasNext()) {
						RDFNode next = comment.next();
						if (next.asLiteral().getLanguage().equals("en")) {
							addDocumentToIndex(stmt.getSubject(), "rdfs:comment", next);
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

	private void addDocumentToIndex(Resource resource, String predicate, RDFNode next) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_SUBJECT, resource.getURI(), Store.YES));
		doc.add(new StringField(FIELD_NAME_PREDICATE, predicate, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, next.asLiteral().getString(), Store.YES));
		iwriter.addDocument(doc);
	}

	public static void main(String args[]) {
		DBOIndex index = new DBOIndex();
		System.out.println(index.search("graduate"));

	}
}
