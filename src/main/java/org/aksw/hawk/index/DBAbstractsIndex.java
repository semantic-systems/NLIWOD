package org.aksw.hawk.index;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
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

import com.google.common.base.Joiner;

public class DBAbstractsIndex {

	private Logger log = LoggerFactory.getLogger(DBAbstractsIndex.class);
	public static String FIELD_NAME_SUBJECT = "subject";
	public static String FIELD_NAME_PREDICATE = "predicate";
	public static String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 100000;
	public Version LUCENE_VERSION = Version.LUCENE_46;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;

	public DBAbstractsIndex() {
		try {
			File index = new File("resources/indexAbstract");
			if (!index.exists()) {
				index.mkdir();
				directory = new MMapDirectory(index);
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

	public List<Document> askForPredicateWithBoundAbstract(String targetPredicate, String boundAbstract) {
		// hack to remove escaped spaces so parsing errors would not happen
		// i.e. <anti-apartheid_activist> becomes <anti-apartheid activist>
		targetPredicate = targetPredicate.replaceAll("_", "\\s");
		List<Document> triples = new ArrayList<Document>();
		try {
			log.debug("\t start asking index...");
			boundAbstract = URLDecoder.decode(boundAbstract, "UTF-8");
			BooleanQuery bq = new BooleanQuery();
			TermQuery termSubject = new TermQuery(new Term(FIELD_NAME_SUBJECT, "\"" + boundAbstract + "\""));
			bq.add(termSubject, BooleanClause.Occur.MUST);

			PrefixQuery query = new PrefixQuery(new Term(FIELD_NAME_OBJECT, targetPredicate));
			bq.add(query, BooleanClause.Occur.MUST);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			Query q = new QueryParser(LUCENE_VERSION, FIELD_NAME_SUBJECT, new KeywordAnalyzer()).parse(bq.toString());
			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String s = hitDoc.get(FIELD_NAME_SUBJECT);
				String p = hitDoc.get(FIELD_NAME_PREDICATE);
				String o = hitDoc.get(FIELD_NAME_OBJECT);
				log.debug("\tFound in document: " + s + "\n" + p + "\n" + o);
				triples.add(hitDoc);
			}

		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return triples;
	}

	public List<String> listAbstractsContaining(String tokens) {
		List<String> triples = new ArrayList<String>();
		try {
			log.debug("\t start asking index...");

			String[] words = tokens.split(" ");
			// PhraseQuery query = new PhraseQuery();
			// for (String token : words) {
			// query.add(new Term(FIELD_NAME_OBJECT, token));
			// }
			// BooleanQuery q = new BooleanQuery();
			// q.add(query, BooleanClause.Occur.MUST);

			SpanQuery[] clauses = new SpanQuery[words.length];
			for (int i = 0; i < words.length; i++) {
				clauses[i] = new SpanMultiTermQueryWrapper<FuzzyQuery>(new FuzzyQuery(new Term(FIELD_NAME_OBJECT, words[i])));
			}
			SpanNearQuery query = new SpanNearQuery(clauses, 0, true);

			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				String s = hitDoc.get(FIELD_NAME_SUBJECT);
				triples.add(s);
			}

		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return triples;
	}

	private void index() {
		String baseURI = "http://dbpedia.org";
		try {
			Properties prop = new Properties();
			InputStream input = getClass().getClassLoader().getResourceAsStream("hawk.properties");
			prop.load(input);
			String file = prop.getProperty("abstracts");

			// Analyzer analyzer = new StandardAnalyzer(LUCENE_VERSION);

			Analyzer analyzer = new Analyzer() {
				@Override
				protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
					Tokenizer whitespaceTokenizer = new WhitespaceTokenizer(LUCENE_VERSION, reader);
					PatternReplaceFilter in = new PatternReplaceFilter(whitespaceTokenizer, Pattern.compile("[^a-zA-Z0-9\\-]"), "", true);
					TrimFilter trimFilter = new TrimFilter(LUCENE_VERSION, in);
					return new TokenStreamComponents(whitespaceTokenizer, trimFilter);
				}

			};
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

			log.info("Start parsing.");
			file = prop.getProperty("redirects");
			OnlineStatementHandlerRedirects oshr = new OnlineStatementHandlerRedirects(iwriter);
			parser = new TurtleParser();
			parser.setRDFHandler(oshr);
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
				log.error("Could not create index: ", e);
			}
		}
	}

	class OnlineStatementHandlerRedirects extends RDFHandlerBase {
		private IndexWriter iwriter;

		public OnlineStatementHandlerRedirects(IndexWriter iwriter) {
			this.iwriter = iwriter;
		}

		@Override
		public void handleStatement(Statement st) {
			String subject = st.getSubject().stringValue();
			subject = subject.replaceAll("_", " ").replaceAll("http://dbpedia.org/resource/", "");
			String label = new String();
			for (String w : subject.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
				label += w + " ";
			}
			label.trim();

			String predicate = st.getPredicate().stringValue();
			String object = st.getObject().stringValue();
			try {
				addDocumentToIndex(iwriter, object, predicate, label);
			} catch (IOException e) {
				log.error("Could not create index: ", e);
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
			log.error(e.getLocalizedMessage(), e);
		}
	}

	public static void main(String args[]) {
		DBAbstractsIndex index = new DBAbstractsIndex();
		// index.askForPredicateWithBoundAbstract("assassin",
		// "http://dbpedia.org/resource/Martin_Luther_King%2C_Jr.");

		System.out.println(Joiner.on("\n").join(index.listAbstractsContaining("first man in space")));

		index.close();

	}

}
