package org.aksw.qa.commons.load.json;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;

public class QaldJson {
	private EJDataset dataset;
	private Vector<QaldQuestionEntry> questions;

	public QaldJson() {
		questions = new Vector<>();
	}

	public EJDataset getDataset() {
		return dataset;
	}

	public Vector<QaldQuestionEntry> getQuestions() {
		return questions;
	}

	public QaldJson setDataset(final EJDataset dataset) {
		this.dataset = dataset;
		return this;
	}

	public QaldJson setQuestions(final Vector<QaldQuestionEntry> questions) {
		this.questions = questions;
		return this;
	}

	@Override
	public String toString() {
		return "Dataset: " + Objects.toString(dataset) + "\nQuestions: " + questions.toString().replaceAll(",", "\n");
	}

	public static void main(final String[] args) throws Exception {

		URL url = LoaderController.mapDatasetToPath(Dataset.QALD6_Train_Multilingual);
		QaldJson exJ = (QaldJson) ExtendedQALDJSONLoader.readJson(new File(url.getPath().replace("%20", " ")), QaldJson.class);
		System.out.println(exJ.toString());
		List<IQuestion> questions = EJQuestionFactory.getQuestionsFromQaldJson(exJ);
		// for (IQuestion q : questions) {
		// System.out.println(q);
		// }

		ExtendedQALDJSONLoader.writeJson(EJQuestionFactory.getQaldJson(questions), new File("C:/output/from_qald_to_question_to_qald.json"), true);

		ExtendedJson extendedJson = EJQuestionFactory.fromQaldToExtended(exJ);
		ExtendedQALDJSONLoader.writeJson(extendedJson, new File("C:/output/qald6_as_ExtendedJson.json"), true);
		QaldJson fromExtended = EJQuestionFactory.fromExtendedToQald(extendedJson);
		ExtendedQALDJSONLoader.writeJson(fromExtended, new File("C:/output/qald6_to_extended_to_qald.json"), true);

		List<IQuestion> questions2 = EJQuestionFactory.getQuestionsFromExtendedJson(extendedJson);

		ExtendedQALDJSONLoader.writeJson(EJQuestionFactory.getQaldJson(questions2), new File("C:/output/from_qald_to_extended_to_question_to_qald.json"), true);

		// for (IQuestion q : questions2) {
		// System.out.println(q);
		// }

	}

}
