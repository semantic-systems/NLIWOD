package org.aksw.qa.commons.qald;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.json.EJDataset;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QALDWriter {
	private static Logger LOGGER = LoggerFactory.getLogger(QALDWriter.class);
	
	
	/**
	 * Can be used to write QALD JSON files like 
	 * <a href="https://github.com/ag-sc/QALD/blob/master/9/data/qald-9-train-multilingual.json">https://github.com/ag-sc/QALD/blob/master/9/data/qald-9-train-multilingual.json</a>.
	 * @param questions List of IQuestions that should be written to the file.
	 * @param fileName The name of the file. (without the file name extension)
	 */
	public void writeQALDJsonFile(List<IQuestion> questions, String fileName) {
		int newID = 1;
		for (IQuestion q : questions) {
			q.setId("" + newID++);
		}
		
		File qaldjsonFile = new File(fileName + ".json ");
		qaldjsonFile.delete();

		QaldJson qaldJson = EJQuestionFactory.getQaldJson(questions);

		EJDataset header = new EJDataset();
		header.setId(fileName);
		qaldJson.setDataset(header);

		try {
			ExtendedQALDJSONLoader.writeJson(qaldJson, qaldjsonFile, true);
		} catch (IOException e) {
			LOGGER.error("Could not write the file: ", e);
		}
	}
}
