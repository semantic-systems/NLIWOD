package org.aksw.surfaceformgenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Diego Moussallem
 * @author Ricardo Usbeck <usbeck@informatik.uni-leipzig.de>
 */
public class DBpediaSpotlightSurfaceFormGenerator {

	private static final Logger logger = LoggerFactory.getLogger(DBpediaSpotlightSurfaceFormGenerator.class);

	private static int MAXIMUM_SURFACE_FORM_LENGHT = 50;

	private static final List<String> STOPWORDS = Arrays.asList("but", "i", "a", "about", "an", "and", "are", "as",
			"at", "be", "by", "com", "for", "from", "how", "in", "is", "it", "of", "on", "or", "that", "the", "this",
			"to", "what", "when", "where", "who", "will", "with", "the", "www", "before", ",", "after", ";", "like",
			"and", "such");

	private static Set<String> addNonAccentVersion(Set<String> surfaceForms) {
		// remove all the accents in the surface forms and add that new label
		Set<String> normalizedLabels = new HashSet<String>();
		for (String surfaceForm : surfaceForms) {
			String normalized = Normalizer.normalize(surfaceForm, Normalizer.Form.NFD);
			normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			if (!normalized.equals(surfaceForm)) {
				normalizedLabels.add(normalized);
			}
		}
		surfaceForms.addAll(normalizedLabels);
		return surfaceForms;
	}

	private static void addSurfaceForm(Map<String, Set<String>> surfaceForms, String key, String value) {
		// clean and URL decode, whitespace removal
		String newSurfaceForm = createCleanSurfaceForm(value);
		if (newSurfaceForm != null) {

			if (surfaceForms.containsKey(key)) {
				surfaceForms.get(key).add(newSurfaceForm);
			} else {
				Set<String> sfList = new HashSet<String>();
				sfList.add(newSurfaceForm);
				surfaceForms.put(key, sfList);
			}
		}
	}

	private static void addSurfaceFormAnchor(Map<String, Set<String>> surfaceForms, String key, String value) {
		// do not clean and URL decode, whitespace removal
		String newSurfaceForm = value;
		if (newSurfaceForm != null) {

			if (surfaceForms.containsKey(key)) {
				surfaceForms.get(key).add(newSurfaceForm);
			} else {
				Set<String> sfList = new HashSet<String>();
				sfList.add(newSurfaceForm);
				surfaceForms.put(key, sfList);
			}
		}
	}

	private static String createCleanSurfaceForm(String label) {
		try {
			String newLabel = URLDecoder.decode(label, "UTF-8");
			newLabel = newLabel.replaceAll("_", " ").replaceAll(" +", " ").trim();
			newLabel = newLabel.replaceAll(" \\(.+?\\)$", "");
			return isGoodSurfaceForm(newLabel) ? newLabel : null;
		} catch (Exception e) {
			return null;
		}
	}

	private static boolean isGoodSurfaceForm(String surfaceForm) {
		if (surfaceForm.length() > MAXIMUM_SURFACE_FORM_LENGHT || surfaceForm.matches("^[\\W\\d]+$")) {
			logger.info(
					"Surfaceform: " + surfaceForm + " is not a good surface form because its too long or regex match.");
			return false;
		}

		for (String token : surfaceForm.toLowerCase().split(" ")) {
			// current token is not a stopword
			if (!STOPWORDS.contains(token)) {
				// at least one non stop word found
				return true;
			}
		}
		return false;
	}

	private static boolean isGoodUri(String uri) {
		if (uri.startsWith("Liste_") || uri.contains("(Begriffsklärung)") || uri.startsWith("List_of_")
				|| uri.contains("(Disambiguation)")||uri.contains("%23")||uri.matches("^[\\W\\d]+$")) {
						logger.info("Uri: <" + uri + "> is not a good uri! / or %23 or regex");
						return false;
					}
		return true;
	}

	public Map<String, Set<String>> createSurfaceFormFile() throws IOException {
		System.out.println("Creating: " + SurfaceFormGenerator.SURFACE_FORMS_FILE);

		Set<String> conceptUris = new HashSet<String>();
		Set<String> badUris = new HashSet<String>();
		badUris.addAll(NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_REDIRECTS_FILE, ""));
		badUris.addAll(NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_DISAMBIGUATIONS_FILE, ""));
		System.gc();
		logger.info("Finished reading redirects and disambiguations file for bad uri detection!");

		// every uri which looks like a good uri and is not in the
		// disambiguations or redirect files is a concept uri
		// TODO check this
		List<String> labelURIs = NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_LABELS_FILE, "");
		for (String subjectUri : labelURIs) {

			String subjectUriWihtoutPrefix = subjectUri.substring(labelURIs.lastIndexOf("/") + 1);

			if (isGoodUri(subjectUriWihtoutPrefix) && !badUris.contains(subjectUri)) {
				conceptUris.add(subjectUri);
			}
		}
		logger.info("Concept Uris construction complete! Total of: " + conceptUris.size() + " concept URIs found!");

		Map<String, Set<String>> surfaceForms = new HashMap<String, Set<String>>();

		// first add all uris of the concept uris
		for (String uri : conceptUris) {
			addSurfaceForm(surfaceForms, uri, uri.substring(uri.lastIndexOf("/") + 1));
		}

		logger.info("Finished adding all conceptUris: " + surfaceForms.size());
		
		

		List<String> subjectToObject =  NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_DISAMBIGUATIONS_FILE, "");
		subjectToObject.addAll(NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_REDIRECTS_FILE,""));

		for (String subjectAndObject: subjectToObject) {

			String subject = subjectAndObject;
			String object = subjectAndObject;
		
		
			if (conceptUris.contains(object) && !object.contains("%")) {
				addSurfaceForm(surfaceForms, object, subject.substring(subject.lastIndexOf("/") + 1));
			}
		}

		List<String> subjectToObject2 = NtripleUtil.getSubjectsFromNTriple(SurfaceFormGenerator.DBPEDIA_INTER_LANGUAGE_LINKS_FILE, "");

		for (String subjectAndObject : subjectToObject2) {

			String subject = subjectAndObject;
			String object = subjectAndObject;

			String tempSubject = subject.substring(subject.lastIndexOf("/") + 1);
			String tempObject = object.substring(object.lastIndexOf("/") + 1);

			if (!tempSubject.equals(tempObject)) {
				if (conceptUris.contains(subject) && !subject.contains("%")) {
					addSurfaceFormAnchor(surfaceForms, subject, object);
				}
			}
		}

		logger.info("Finished generation of surface forms.");

		// write the file
		// TODO auf streamwriter mit encoding umschreiben
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(SurfaceFormGenerator.SURFACE_FORMS_FILE, false), "UTF-8");

		for (Map.Entry<String, Set<String>> entry : surfaceForms.entrySet()) {
			writer.write(entry.getKey() + "\t" + StringUtils.join(addNonAccentVersion(entry.getValue()), "\t"));
			writer.write("\n");
		}

		writer.close();
		logger.info("Finished writing of surface forms to disk.");

		return surfaceForms;
	}

}
