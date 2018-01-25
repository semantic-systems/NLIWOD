package org.aksw.qa.annotation.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class IndexDBO_properties extends IndexDBO {

	private static final Version LUCENE_VERSION = Version.LUCENE_46;
	private org.slf4j.Logger log = LoggerFactory.getLogger(IndexDBO_properties.class);
	public String FIELD_NAME_SUBJECT = "subject";
	public String FIELD_NAME_PREDICATE = "predicate";
	public String FIELD_NAME_OBJECT = "object";
	private int numberOfDocsRetrievedFromIndex = 100;

	private Directory directory;
	private IndexSearcher isearcher;
	private DirectoryReader ireader;
	private IndexWriter iwriter;
	private SimpleAnalyzer analyzer;

	public IndexDBO_properties() {
		try {
			File index = new File("resources/ontologyProperties");
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

	@Override
	public List<String> search(final String object) {
		if (stopwords.contains(object.toLowerCase())) {
			log.debug("\t Stopword detected: |" + object + "|");
			System.out.println("returning immutable empty");
			return ImmutableList.of();
		}
		ArrayList<String> uris = Lists.newArrayList();
		try {
			log.debug("\t start asking index for |" + object + "|");

			Query q = new FuzzyQuery(new Term(FIELD_NAME_OBJECT, object), 0);
			TopScoreDocCollector collector = TopScoreDocCollector.create(numberOfDocsRetrievedFromIndex, true);

			isearcher.search(q, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			for (ScoreDoc hit : hits) {
				Document hitDoc = isearcher.doc(hit.doc);
				log.debug(object + "->" + hitDoc.get(FIELD_NAME_SUBJECT) + ", " + hitDoc.get(FIELD_NAME_OBJECT));
				uris.add(hitDoc.get(FIELD_NAME_SUBJECT));
			}
			log.debug("\t finished asking index...");
		} catch (Exception e) {
			log.error(e.getLocalizedMessage() + " -> " + object, e);
		}
		return uris;
	}

	@Override
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
			InputStream res = this.getClass().getResourceAsStream("/matoll_EN_surface_forms.ttl");
			Model model = ModelFactory.createDefaultModel();
			model.read(res, "http://dbpedia.org/", "TTL");
			StmtIterator stmts = model.listStatements(null, RDFS.label, (RDFNode) null);
			while (stmts.hasNext()) {
				final Statement stmt = stmts.next();
				RDFNode label = stmt.getObject();
				addDocumentToIndex(stmt.getSubject(), "rdfs:label", label.asLiteral());
			}

			iwriter.commit();
			iwriter.close();
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	private void addDocumentToIndex(final Resource resource, final String predicate, final RDFNode next) throws IOException {
		Document doc = new Document();
		doc.add(new StringField(FIELD_NAME_SUBJECT, resource.getURI(), Store.YES));
		doc.add(new StringField(FIELD_NAME_PREDICATE, predicate, Store.YES));
		doc.add(new TextField(FIELD_NAME_OBJECT, next.asLiteral().getString(), Store.YES));
		iwriter.addDocument(doc);
	}

	public static void main(final String[] args) {
		IndexDBO_properties classes = new IndexDBO_properties();
		Scanner sc = new Scanner(System.in);
		do {
			System.out.println("Search: ");
			String in = sc.next();
			List<String> out = classes.search(in);
			for (String it : out) {
				System.out.println("Result: " + it);
			}

		} while (true);

	}

}
