package org.aksw.qa.commons.load.extended;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Basic json load and store class using jackson.
 */
//TODO rename class and packages
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
	 */
	public static Object readJson(final File f, final Class<?> type) {
		Object object = null;
		try {
			InputStream inp = new FileInputStream(f);

			ObjectMapper mapper = new ObjectMapper();
			mapper.disable(MapperFeature.USE_GETTERS_AS_SETTERS);

			object = mapper.readValue(f, type);
			inp.close();

		} catch (FileNotFoundException e) {
			LOGGER.error("Could not find File :" + f.getAbsolutePath());
			LOGGER.debug(Level.ERROR, e);

		} catch (IOException e) {
			LOGGER.debug(Level.ERROR, e);

		}

		return object;
	}
//TODO transform to unit test
	public static void main(final String[] args) throws Exception {
		HashMap<String, EJBinding> hash = new HashMap<>();
		hash.put("myVar", new EJBinding().setType("myType").setValue("myValue"));

		EJQuestionEntry que = new EJQuestionEntry().setId(5)
		        .setMetadata(new EJMetadata().addAnswerItemType("some Answer Item Type").addAnswerItemType("answer item type 2").setAnswertype("some Answertype"));
		que.setQuery(new EJQuery().setSPARQL("sparql query").setSchemaless("schemaless?"));
		que.getAnswers().addBindings(hash).setConfidence("very confident").getHead().addVar("myVar");
		que.getQuestion().addAnnotation(new EJAnnotation().setType("myType").setURI("myURI").setChar_end(45).setChar_begin(11));
		que.getQuestion().setKeywords("all my keywords").setLanguage("all of them").setString("such String ");

		ExtendedJson ej = new ExtendedJson().addQuestions(que).setDataset(new EJDataset().setId(4565).setMetadata("myMetadata"));

		// Loader.writeJson(ej, new File("C:/output/ExtendedJson.json"), true);

	}
}
