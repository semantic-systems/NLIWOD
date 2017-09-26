package org.aksw.qa.commons.load.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic json load and store class using jackson.
 */
// TODO rename class and packages
public final class ExtendedQALDJSONLoader {
	static Logger LOGGER = LoggerFactory.getLogger(ExtendedQALDJSONLoader.class);

	/**
	 * Make this class non instatiable.
	 */
	private ExtendedQALDJSONLoader() {

	}

	/**
	 * Loads and saves Settings into and from json files /** Writes the given Object as JSON to location specified in File. If File already exists and overwrite ==false, AttemptedOverwriteException
	 * will be thrown
	 *
	 * @param o
	 *            The object you want to write.
	 * @param f
	 *            The File (-location) to save it in.
	 * @param overwrite
	 *            Set this true to overwrite existing file.
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
			LOGGER.error("", e);
		}
		LOGGER.info("File Written to " + f.getAbsolutePath());
	}

	/**
	 * Writes the json to an byte array
	 *
	 * @param json
	 * @return the given json as byte representation
	 * @throws JsonProcessingException
	 */
	public static byte[] writeJson(final Object json) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);

		return mapper.writer().writeValueAsBytes(json);

	}

	/**
	 * Parses Json file and returns an Object containing the results. You need to cast the return of this class to the class specified in type.
	 *
	 * @param inputJson
	 *            the json to parse
	 * @param type
	 *            The class type you want to read.
	 * @return An Object you should cast.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public static Object readJson(final byte[] inputJson, final Class<?> type) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);
		return mapper.readValue(inputJson, type);

	}

	/**
	 * Parses Json file and returns an Object containing the results. You need to cast the return of this class to the class specified in type.
	 *
	 * @param f
	 *            The file location you want to read from.
	 * @param type
	 *            The class type you want to read.
	 * @return An Object you should cast.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	public static Object readJson(final InputStream in, final Class<?> type) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);

		return mapper.readValue(in, type);
	}

	public static Object readJson(final InputStream in) throws JsonParseException, JsonMappingException, IOException {
		Object ret = null;
		// try to read ExtendedJson
		try {
			ret = readJson(in, ExtendedJson.class);
		} catch (JsonParseException | JsonMappingException e1) {
			LOGGER.debug("Parsing as ExtendedJson doesnt work, trying QaldJson", e1);
			ret = readJson(in, QaldJson.class);
		}

		return ret;
	}

	public static Object readJson(final File f) throws FileNotFoundException, IOException {
		Object ret = null;
		try {
			ret = readJson(f, ExtendedJson.class);
		} catch (JsonParseException | JsonMappingException e1) {
			LOGGER.debug("Parsing as ExtendedJson doesnt work, trying QaldJson", e1);
			ret = readJson(f, QaldJson.class);
		}
		return ret;
	}

	public static Object readJson(final File f, final Class<?> type) throws JsonParseException, JsonMappingException, FileNotFoundException, IOException {
		return readJson(new FileInputStream(f), type);
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
