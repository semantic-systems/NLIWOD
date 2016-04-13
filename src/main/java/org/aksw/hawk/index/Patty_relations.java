package org.aksw.hawk.index;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Patty_relations {

	/*
	 * Parameters
	 */

	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(Patty_relations.class);
	private int numberOfDocsRetrievedFromIndex = 100;

	public String FIELD_NAME_URI = "uri";
	public String FIELD_NAME_OBJECT = "object";

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private StandardAnalyzer analyzer;

	/*
	 * Constructor
	 */

	public Patty_relations() {
		try {
			File indexDir = new File("resources/puttyRelations");
			analyzer = new StandardAnalyzer(LUCENE_VERSION);
			/*
			 * if no index exists, start by creating one:
			 */
			if (!indexDir.exists()) {
				// create the directory where the index is stored
				indexDir.mkdir();
				IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);
				directory = new MMapDirectory(indexDir);
				iwriter = new IndexWriter(directory, config);
				// create the index
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
	 * Creates the Lucene Index for dbpedia-relation-paraphrases.txt
	 */

	private void index() {
		try {
			Path currentDir = Paths.get(".");
			Path path = currentDir.resolve("resources/dbpedia-relation-paraphrases.txt");
			Stream<String> lines = Files.lines(path);
			lines.forEach(s -> lineSplitAndAddtoIndex(s));
			lines.close();
			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * addDocument is used by index()-method to... add documents!
	 */

	private void addDocumentToIndex(String relation, String data) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_URI, "http://dbpedia.org/ontology/" + relation, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, data, Store.YES));
		iwriter.addDocument(doc);
	}

	/*
	 * helper function for parsing the stream
	 */

	private void lineSplitAndAddtoIndex(String line) {
		String rel = line.split("\\t")[0];
		String dat = line.split("\\t")[1];
		try {
			addDocumentToIndex(rel, dat);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/*
	 * search method, copied
	 */

	public HashSet<String> search(String object) {
		ArrayList<String> uris = Lists.newArrayList();
		try {
			log.debug("\t start asking index...");

			Query q = new FuzzyQuery(new Term(FIELD_NAME_OBJECT, object), 0);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);

			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
				// log.debug(object + "->" + hitDoc.get(FIELD_NAME_URI) + ", " +
				// hitDoc.get(FIELD_NAME_OBJECT));
				uris.add(hitDoc.get(FIELD_NAME_URI));
			}
			log.debug("\t finished asking index...");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + object, e);
		}
		HashSet<String> setUris = Sets.newHashSet(uris);
		return setUris;
	}
}
