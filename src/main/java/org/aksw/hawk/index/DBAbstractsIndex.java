package org.aksw.hawk.index;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.helpers.RDFHandlerBase;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBAbstractsIndex {

	private Logger log = LoggerFactory.getLogger(DBAbstractsIndex.class);
	public String FIELD_NAME_SUBJECT = "subject";
	public String FIELD_NAME_PREDICATE = "predicate";
	public String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 1000;
	public Version LUCENE_VERSION = Version.LUCENE_46;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;

	// private HashMap<String, List<Triple>> cache;

	public DBAbstractsIndex() {
		try {
			File index = new File("indexAbstract");
			if (!index.exists()) {
				index.mkdir();
				directory = new MMapDirectory(index);
				index();
			} else {
				directory = new MMapDirectory(index);
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
			// cache = new HashMap<String, List<Triple>>();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	public List<String> search(String subject, String predicate, String object) {
		List<String> triples = new ArrayList<String>();
		// BooleanQuery bq = new BooleanQuery();
		try {
			// if (cache.containsKey(subject+predicate+object)) {
			// return cache.get(subject+predicate+object);
			// }
			log.debug("\t start asking index...");
			// Query q = null;
			// Analyzer analyzer = new SimpleAnalyzer(LUCENE_VERSION);
			// QueryParser parser = new QueryParser(LUCENE_VERSION,
			// FIELD_NAME_OBJECT, analyzer);
			// parser.setDefaultOperator(QueryParser.Operator.AND);
			// q = parser.parse(QueryParser.escape(object));
			// bq.add(q, BooleanClause.Occur.MUST);

			Term term = new Term(FIELD_NAME_OBJECT, object  );
			PrefixQuery query = new PrefixQuery(term);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String s = hitDoc.get(FIELD_NAME_SUBJECT);
				String p = hitDoc.get(FIELD_NAME_PREDICATE);
				String o = hitDoc.get(FIELD_NAME_OBJECT);
				if(s.contains("Martin"))
				log.debug(s + "==>");
			}
			log.debug("\t finished asking index...");
			// cache.put(subject+predicate+object, triples);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + subject);
		}
		return triples;
	}

	private void index() {
		String file = "/data/r.usbeck/Dropbox/DBpedia/long_abstracts_en.ttl";
		String baseURI = "http://dbpedia.org";
		try {
			Analyzer analyzer = new SimpleAnalyzer(LUCENE_VERSION);
			IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
			IndexWriter iwriter = new IndexWriter(directory, config);

			iwriter.commit();

			log.info("Start parsing.");
			OnlineStatementHandler osh = new OnlineStatementHandler(iwriter);
			RDFParser parser = new TurtleParser();
			parser.setRDFHandler(osh);
			parser.setStopAtFirstError(false);
			parser.parse(new FileReader(file), baseURI);
			log.info("Finished parsing.");

			iwriter.commit();
			iwriter.close();
		} catch (RDFParseException | RDFHandlerException | IOException e) {
			log.error("Could not create index: ", e);
		}

	}

	class OnlineStatementHandler extends RDFHandlerBase {
		private IndexWriter iwriter;

		public OnlineStatementHandler(IndexWriter iwriter) {
			this.iwriter = iwriter;
		}

		@Override
		public void handleStatement(Statement st) {
			String subject = st.getSubject().stringValue();
			String predicate = st.getPredicate().stringValue();
			String object = st.getObject().stringValue();
			try {
				addDocumentToIndex(iwriter, subject, predicate, object);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addDocumentToIndex(IndexWriter iwriter, String subject, String predicate, String object) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_SUBJECT, subject, Store.YES));
		doc.add(new StringField(FIELD_NAME_PREDICATE, predicate, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, object, Store.YES));
		iwriter.addDocument(doc);
	}

	public void close() {
		try {
			ireader.close();
			directory.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage());
		}
	}

	public static void main(String args[]) {
		DBAbstractsIndex index = new DBAbstractsIndex();
		index.search(null, null, "assassin");
		index.close();

	}
}
