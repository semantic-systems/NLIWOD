package org.aksw.hawk.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

/*
 * Given a relation (e.g. X " continued to serve in " Y) this index searches possible wordnet entities (X,Y) and 
 * gives their "confidence" based on co-occurences in the wikipedia corpus. The data is taken from the PATTY project
 * from Max Planck Institute Department of Databases and Information Systems.
 */
public class WikipediaPatternsConfidence_Index {
	/*
	 * Parameters
	 */
	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(Patty_relations.class);
	private int numberOfDocsRetrievedFromIndex = 100;

	public String FIELD_NAME_PATTERNTEXT = "patterntext";
	public String FIELD_NAME_CONFIDENCE = "confidence";
	public String FIELD_NAME_DOMAIN = "domain";
	public String FIELD_NAME_RANGE = "range";

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private WhitespaceAnalyzer analyzer;

	/*
	 * Constructor
	 */
	public WikipediaPatternsConfidence_Index() {
		try {
			File indexDir = new File("resources/WikipediaPatternsConfidenceIndex");
			analyzer = new WhitespaceAnalyzer(LUCENE_VERSION);
			// if no index exists, start by creating one
			if (!indexDir.exists()) {
				indexDir.mkdir();
				IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
				directory = new MMapDirectory(indexDir);
				iwriter = new IndexWriter(directory, config);
				index();
			} else {
				directory = new MMapDirectory(indexDir);
			}
			ireader = DirectoryReader.open(directory);
			isearcher = new IndexSearcher(ireader);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * Method to build the Index
	 */
	private void index() {
		try {
			Path currentDir = Paths.get(".");
			Path path = currentDir.resolve("resources/wikipedia-patterns.txt");
			Stream<String> lines = Files.lines(path);
			lines.forEach(x -> lineToFieldsAndAdd(x));
			lines.close();
			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * Helper Method to build the index
	 */
	private void addToIndex(String pattern, String confidence, String domain, String range) {
		try {
			Document doc = new Document();
			doc.add(new TextField(FIELD_NAME_PATTERNTEXT, pattern, Field.Store.YES));
			doc.add(new StringField(FIELD_NAME_CONFIDENCE, confidence, Field.Store.YES));
			doc.add(new StringField(FIELD_NAME_DOMAIN, domain, Field.Store.YES));
			doc.add(new StringField(FIELD_NAME_RANGE, range, Field.Store.YES));
			iwriter.addDocument(doc);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * Yet another helper method to build the index "relations" is of the form:
	 * relation1;$relation2;$relation3.. thus to get the right tokens one has to
	 * split at ";$" and add individually.
	 */
	private void lineToFieldsAndAdd(String line) {
		String[] fields = line.split("\\t");
		String relations = fields[1];
		String confidence = fields[2];
		String domain = fields[3];
		String range = fields[4];
		String[] tokens = relations.split("\\;\\$");
		for (String token : tokens) {
			addToIndex(token, confidence, domain, range);
		}
	}

	/*
	 * Method to search the index. We use a PhraseQuery, a WhiteSpaceAnalyzer
	 * and a filter to get a result, iff it matches the "pattern" exactly.
	 */
	public ArrayList<String[]> search(String pattern) {
		ArrayList<String[]> values = new ArrayList<String[]>();
		try {
			PhraseQuery query = new PhraseQuery();
			String[] words = pattern.split(" ");
			for (String word : words) {
				query.add(new Term(FIELD_NAME_PATTERNTEXT, word));
			}
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);
			isearcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (ScoreDoc hit : hits) {
				String[] hitList = new String[3];
				Document hitDoc = isearcher.doc(hit.doc);
				String hitConfidence = hitDoc.get(FIELD_NAME_CONFIDENCE);
				String hitDomain = hitDoc.get(FIELD_NAME_DOMAIN);
				String hitRange = hitDoc.get(FIELD_NAME_RANGE);
				hitList[0] = hitConfidence;
				hitList[1] = hitDomain;
				hitList[2] = hitRange;
				/*
				 * because, e.g. "contained" will get hits at "contained in" we
				 * have to filter by string equality
				 */
				if (hitDoc.get(FIELD_NAME_PATTERNTEXT).equals(pattern)) {
					values.add(hitList);
					log.debug(pattern + " -> " + hitDoc.get(FIELD_NAME_PATTERNTEXT) + " " + hitConfidence + " " + hitDomain + " " + hitRange);
				}
			}
			System.out.println(collector.toString());
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		return values;
	}
}
