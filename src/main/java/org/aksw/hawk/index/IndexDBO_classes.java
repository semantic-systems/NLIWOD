package org.aksw.hawk.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.riot.RDFDataMgr;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
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
import org.apache.lucene.util.automaton.LevenshteinAutomata;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

public class IndexDBO_classes {

	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(IndexDBO_classes.class);
	public String FIELD_NAME_SUBJECT = "subject";
	public String FIELD_NAME_PREDICATE = "predicate";
	public String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 1000;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private SimpleAnalyzer analyzer;

	public IndexDBO_classes() {
		try {
			File index = new File("resources/ontologyClasses");
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

	public ArrayList<String> search(String object) {
		ArrayList<String> uris = Lists.newArrayList();
		try {
			log.debug("\t start asking index...");

			Query q = new FuzzyQuery(new Term(FIELD_NAME_OBJECT, object), LevenshteinAutomata.MAXIMUM_SUPPORTED_DISTANCE);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);

			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (int i = 0; i < hits.length; i++) {
				Document hitDoc = isearcher.doc(hits[i].doc);
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
			Model model = RDFDataMgr.loadModel("resources/dbpedia_3Eng_class.ttl");
			StmtIterator stmts = model.listStatements(null, RDFS.label, (RDFNode) null);
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				RDFNode label = stmt.getObject();
				addDocumentToIndex(stmt.getSubject(), "rdfs:label", label.asLiteral().getString());
			}

			iwriter.commit();
			model = RDFDataMgr.loadModel("resources/yagoClassLabel.ttl");
			stmts = model.listStatements(null, RDFS.label, (RDFNode) null);
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				RDFNode label = stmt.getObject();
				addDocumentToIndex(stmt.getSubject(), "rdfs:label", label.asLiteral().getString());
			}
			iwriter.commit();

			iwriter.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	private void addDocumentToIndex(Resource resource, String predicate, String object) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_SUBJECT, resource.getURI(), Store.YES));
		doc.add(new StringField(FIELD_NAME_PREDICATE, predicate, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, object, Store.YES));
		iwriter.addDocument(doc);
	}

	public static void main(String args[]) {
		IndexDBO_classes index = new IndexDBO_classes();
		System.out.println("king " + index.search("king").size());
		System.out.println("street basketball player " + index.search("street basketball player").size());
		System.out.println("basketball player " + index.search("basketball player").size());
		System.out.println("pope " + index.search("pope").size());
		System.out.println("island " + index.search("island").size());

	}
}
