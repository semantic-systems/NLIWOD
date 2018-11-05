package org.aksw.qa.annotation.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedSpanImpl;
import org.aksw.qa.annotation.index.IndexDBO;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NifEverything {
	final Logger logger = LoggerFactory.getLogger(this.getClass());
	/**
	 * Make this Singleton
	 */
	private static NifEverything nif = null;
	/**
	 * Will be returned when given NIF via POST is not parsable or empty
	 */
	public static final String INPUT_NOT_PARSABLE = "Given input not parsable or nothing found";

	/**
	 * Singleton
	 */
	private NifEverything() {

	}

	/**
	 * @return the only possible instance (Singleton)
	 */
	public static NifEverything getInstance() {
		if (nif == null) {
			nif = new NifEverything();
		}
		return nif;
	}

	/**
	 * Splits Sentence on whitespace (inserts whitespace before Punctuation).
	 * Words appear in List in same order as they are in given Sentence. The
	 * Integer-Part of ImmutablePair stores the offset of this word relative to
	 * begin of the sentence. (first word has offset 0)
	 *
	 * @param q a string to split and calculate Offset on.
	 * @return Words with their respective offset.
	 */
	private List<ImmutablePair<String, Integer>> extractSplitQuestion(String q) {
		List<ImmutablePair<String, Integer>> ret = new ArrayList<>();
		q = q.trim().replaceAll("(\\p{Punct})(\\s)*(\\z)", " $1");
		int wordIndex = 0;
		for (String s : q.split(" ")) {
			ret.add(new ImmutablePair<>(s, wordIndex));
			wordIndex += s.length() + 1;
		}
		return ret;

	}

	/**
	 * Creates a NIF with annotations respective to given {@link NifProperty}
	 * respective to given {@link IndexDBO}
	 *
	 * @param q The sentence to run IdexDBO on
	 * @param indexDBO Choose implementation of @link {@link IndexDBO} to find
	 *            properties(classes) to store in nif
	 * @param nif The kind of Annotation how found properties(classes) sould be
	 *            represented in NIF (e.g. taIdentRef,taClassRef)
	 * @return Created NIF
	 */
	public String createNIFResultFromIndexDBO(final String q, final IndexDBO indexDBO, final NifProperty nif) {
		Document doc = new DocumentImpl(q);
		addAllMarkingsToDoc(doc, stringToMarkingsIndexDBO(q, indexDBO, nif));
		return writeNIF(doc);
	}

	/**
	 * Appends found properties(classes) annoatation to given NIF.
	 *
	 * @param documentsString The NIF as String
	 * @param indexDBO Choose implementation of {@link IndexDBO} to find
	 *            properties(classes) to store in nif.
	 * @param nif The kind of Annotation how found properties(classes) sould be
	 *            represented in NIF (e.g. taIdentRef,taClassRef)
	 * @return The given NIF, with added annoataions.
	 */
	public String appendNIFResultFromIndexDBO(final String documentsString, final IndexDBO indexDBO, final NifProperty nif) {
		List<Document> docs = null;
		try {
			docs = parseNIF(documentsString);
		} catch (IllegalArgumentException e) {
			logger.debug("Given input results in empty document list - check input");
			return INPUT_NOT_PARSABLE;
		}

		for (Document doc : docs) {
			addAllMarkingsToDoc(doc, stringToMarkingsIndexDBO(doc.getText(), indexDBO, nif));
		}

		return writeNIF(docs);
	}

	/**
	 * Adds all given {@link Marking}s to a given {@link Document} Needed,
	 * because {@link Document} does not have something like
	 * Document.addAll(Collection&lt;Marking&gt;)
	 *
	 * @param doc The {@link Document} you wish to add {@link Marking}s to
	 * @param markings The {@link Marking}s you want to add.
	 */
	public void addAllMarkingsToDoc(final Document doc, final Collection<Marking> markings) {
		for (Marking marking : markings) {
			doc.addMarking(marking);
		}
	}

	/**
	 * Here the actual annoataion takes place. Takes some String, annotates it,
	 * and returns a List of {@link Marking}s you can add to a {@link Document}
	 *
	 * @param q String to annotate
	 * @param indexDBO Sone Annotator
	 * @param nif Specify here which Style the Annotation should have:
	 *            {@link NifProperty}
	 * @return All found Annotations, ready to be added to a NIF
	 *         {@link Document}
	 */
	private List<Marking> stringToMarkingsIndexDBO(final String q, final IndexDBO indexDBO, final NifProperty nif) {
		List<Marking> markings = new ArrayList<>();
		for (ImmutablePair<String, Integer> it : extractSplitQuestion(q)) {
			List<String> foundURIs = indexDBO.search(it.getLeft());
			if (CollectionUtils.isEmpty(foundURIs)) {
				continue;
			}
			markings.add(nif.getInstanceWith(it.getRight(), it.getLeft().length(), foundURIs));
		}
		return markings;
	}

	/**
	 * Appends found NEs as annotation to given NIF. Annotation will have the
	 * form {@link NifProperty#TAIDENTREF}
	 *
	 *
	 * @param documentsString The NIF as String
	 * @param spotter A NER tool to use to find named entities.
	 * @return The given NIF, with added annoataions.
	 */
	public String appendNIFResultFromSpotters(final String documentsString, final ASpotter spotter) {
		List<Document> docs = null;
		try {
			docs = parseNIF(documentsString);
		} catch (IllegalArgumentException e) {
			logger.debug("Given input results in empty document list - check input");
			return INPUT_NOT_PARSABLE;
		}

		for (Document doc : docs) {
			addAllMarkingsToDoc(doc, stringToMarkingsSpotters(doc.getText(), spotter));
		}

		return writeNIF(docs);

	}

	/**
	 * Creates a NIF String from given NIF {@link Document} list.
	 *
	 * @param docs List of {@link Document}s to convert
	 * @return NIF as String
	 */
	public String writeNIF(final List<Document> docs) {
		NIFWriter writer = new TurtleNIFWriter();
		return writer.writeNIF(docs);
	}

	/**
	 * Creates a NIF with annotations respective to given {@link NifProperty}
	 * respective to given {@link ASpotter} Annotation will have the form
	 * {@link NifProperty#TAIDENTREF}
	 *
	 * @param q The sentence to run IdexDBO o
	 * @param spotter A spotter to find named entities
	 * @return Created NIF
	 */
	public String createNIFResultFromSpotters(final String q, final ASpotter spotter) {
		Document doc = new DocumentImpl(q);

		addAllMarkingsToDoc(doc, stringToMarkingsSpotters(q, spotter));

		return writeNIF(doc);
	}

	/**
	 * Here the actual annoataion takes place. Takes some String, annotates it,
	 * and returns a List of {@link Marking}s you can add to a {@link Document}.
	 * Annotation will have the form {@link NifProperty#TAIDENTREF}
	 *
	 * @param q String to annotate
	 * @param spotter Some NER tool
	 *
	 * @return All found Annotations, ready to be added to a NIF
	 *         {@link Document}
	 */
	private List<Marking> stringToMarkingsSpotters(final String q, final ASpotter spotter) {
		List<Marking> markings = new ArrayList<>();
		List<Entity> allEntities = new ArrayList<>();

		for (List<Entity> it : spotter.getEntities(q).values()) {
			allEntities.addAll(it);
		}
		for (Entity entityIt : allEntities) {
			ArrayList<String> uris = new ArrayList<>();
			for (Resource res : entityIt.getUris()) {
				uris.add(res.getURI());
			}

			markings.add(NifProperty.TAIDENTREF.getInstanceWith(entityIt.getOffset(), entityIt.getLabel().length(), uris));
		}
		return markings;
	}

	/**
	 * @param doc A {@link Document} to vonvert to an NIF
	 * @return NIF as String
	 */
	public String writeNIF(final Document doc) {
		List<Document> documents = new ArrayList<>();
		documents.add(doc);
		NIFWriter writer = new TurtleNIFWriter();
		String nifString = writer.writeNIF(documents);
		return nifString;
	}

	/**
	 * Wraps implementations of {@link Marking}. For the annotations we do here,
	 * we only need annotations with (offset, length and uriList) This Enum
	 * helps to get some means of generalization.
	 *
	 * @author Jonathan
	 *
	 */
	public enum NifProperty {
		/**
		 * Will annotate in NIF with:
		 *
		 * <pre>
		 * itsrdf:taIdentRef
		 * </pre>
		 */
		TAIDENTREF {
			@Override
			public Marking getInstanceWith(final int offset, final int len, final List<String> uris) {
				return new NamedEntity(offset, len, new HashSet<>(uris));
			}
		},
		/**
		 * Will annotate in NIF with:
		 *
		 * <pre>
		 * itsrdf:taClassRef
		 * </pre>
		 */
		TACLASSREF {
			@Override
			public Marking getInstanceWith(final int offset, final int len, final List<String> uris) {
				return new TypedSpanImpl(offset, len, new HashSet<>(uris));
			}
		};
		/**
		 *
		 * @param offset Offset of the annotated word respective to sentence
		 *            start
		 * @param len length of annotated word
		 * @param uris list of annotations
		 * @return a new instance of {@link Marking}
		 */
		public abstract Marking getInstanceWith(final int offset, final int len, final List<String> uris);
	}

	/**
	 * Extracts a List of NIF {@link Document}s from given String.
	 *
	 * @param input String to be parsed
	 * @return NIF as {@link Document} list
	 * @throws IllegalArgumentException When input is empty or not parsable.
	 */
	public List<Document> parseNIF(final String input) throws IllegalArgumentException {
		NIFParser parser = new TurtleNIFParser();
		List<Document> docs = parser.parseNIF(input);
		/**
		 * Had machine-dependent paths here
		 */
		for (Document doc : docs) {
			doc.setDocumentURI(null);
		}
		if (CollectionUtils.isEmpty(docs)) {
			logger.debug("Recieved empty or not parsable POST body");
			throw new IllegalArgumentException("Nothing parsed");
		}
		return docs;

	}
}
