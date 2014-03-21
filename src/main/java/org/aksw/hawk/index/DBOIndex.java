package org.aksw.hawk.index;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
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
	private int numberOfDocsRetrievedFromIndex = 100;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private SimpleAnalyzer analyzer;

	public DBOIndex() {
		directory = new RAMDirectory();
		analyzer = new SimpleAnalyzer(LUCENE_VERSION);
		index();
	}

	public List<String> search(String subject, String predicate, String object) {
		try {

			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			log.debug("\t start asking index...");
			Query q = null;
			Analyzer analyzer = new SimpleAnalyzer(LUCENE_VERSION);
			QueryParser parser = new QueryParser(LUCENE_VERSION, FIELD_NAME_OBJECT, analyzer);
			parser.setDefaultOperator(QueryParser.Operator.OR);
			q = parser.parse(QueryParser.escape(object));
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String s = hitDoc.get(FIELD_NAME_SUBJECT);
				String p = hitDoc.get(FIELD_NAME_PREDICATE);
				String o = hitDoc.get(FIELD_NAME_OBJECT);
				// TODO build clever return
				System.out.println(s + p + o);
			}
			log.debug("\t finished asking index...");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + subject);
		}
		return null;
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	private void index() {
		try {
			IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
			iwriter = new IndexWriter(directory, config);

			Model dbpedia = ModelFactory.createDefaultModel();
			dbpedia.read("dbpedia_3.9.owl", "RDF/XML");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		index.search(null, null, "writer");

	}
}
