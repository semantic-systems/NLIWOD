package org.aksw.qa.commons.load.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic json load and store class using jackson.
 */
// TODO rename class and packages
public final class ExtendedQALDJSONLoader {
	private static final Logger LOGGER = LogManager.getLogger(ExtendedQALDJSONLoader.class);

	/**
	 * Make this class non instatiable.
	 */
	private ExtendedQALDJSONLoader() {

	}

	/**
	 * Loads and saves Settings into and from json files
	 *
	 *
	 *
	 * /** Writes the given Object as JSON to location specified in File. If
	 * File already exists and overwrite ==false, AttemptedOverwriteException
	 * will be thrown
	 *
	 * @param o The object you want to write.
	 * @param f The File (-location) to save it in.
	 * @param overwrite Set this true to overwrite existing file.
	 *
	 */
	public static void writeJson(final Object o, final File f, final boolean overwrite) throws IOException {
		if (f.exists() && !overwrite) {
			throw new IllegalArgumentException("File already exists. Set overwrite flag if you want to overwrite current file");
		}

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		try {
			mapper.writerWithDefaultPrettyPrinter().writeValue(f, o);
		} catch (IOException e) {
			LOGGER.debug(Level.ERROR, e);
		}
		LOGGER.info("File Written to " + f.getAbsolutePath());
	}

	/**
	 * Parses Json file and returns an Object containing the results. You need
	 * to cast the return of this class to the class specified in type.
	 *
	 * @param f The file location you want to read from.
	 * @param type The class type you want to read.
	 * @return An Object you should cast.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public static Object readJson(final InputStream in, final Class<?> type) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		try {
			return mapper.readValue(in, type);
		} catch (Exception e) {
			return null;
		}
	}

	public static Object readJson(final InputStream in) throws JsonParseException, JsonMappingException, IOException {
		Object ret = null;
		if ((ret = readJson(in, ExtendedJson.class)) == null) {
			ret = readJson(in, QaldJson.class);
		}
		return ret;
	}

	public static Object readJson(final File f) {
		Object ret = null;
		if ((ret = readJson(f, ExtendedJson.class)) == null) {
			ret = readJson(f, QaldJson.class);
		}
		return ret;
	}

	public static Object readJson(final File f, final Class<?> type) {
		try (InputStream in = new FileInputStream(f)) {
			return readJson(in, type);
		} catch (FileNotFoundException e) {
			LOGGER.error("Could not find File :" + f.getAbsolutePath());
			LOGGER.error(Level.ERROR, e);

		} catch (IOException e) {
			LOGGER.error(Level.ERROR, e);

		}
		return null;
	}

	// TODO transform to unit test
	public static void main(final String[] args) throws Exception {
		HashMap<String, EJBinding> hash = new HashMap<>();
		hash.put("myVar", new EJBinding().setType("myType").setValue("myValue"));

		ExtendedJson ej = new ExtendedJson();
		EJQuestionEntry entry = new EJQuestionEntry();
		EJAnswers answers = new EJAnswers();
		entry.getQuestion().addAnsweritemtype("answeritemtype").setId(5 + "").setAnswertype("someAnswertype").setConfidence("very confident").setAnswers(answers);
		answers.setHead(new EJHead());
		answers.getHead().getVars().add("myVariable");
		answers.getHead().getLink().add("http://myli.n" + "k");
		EJResults results = new EJResults();
		results.getBindings().add((hash));
		answers.setResults(results);
		answers.setConfidence("so confident, very satisfied, such unafraid").setBoolean(true);
		EJDataset dataset = new EJDataset();
		dataset.setId("5").setMetadata("MetadataString");
		ej.setDataset(dataset);

		ej.addQuestions(entry);

		ExtendedQALDJSONLoader.writeJson(ej, new File("C:/output/ExtendedJson2.json"), true);
		System.out.println(ExtendedQALDJSONLoader.readJson(new File("C:/output/ExtendedJson2.json"), ExtendedJson.class).toString());

	}
}
