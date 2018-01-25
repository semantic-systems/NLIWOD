package org.aksw.hawk.experiment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Made for testing a single pipeline
 * 
 * @author Lorenz Buehmann
 * @author ricardousbeck
 * 
 */
public class SingleQuestionPipeline {
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	public static void main(String args[])
			throws IOException, ParserConfigurationException, ExecutionException, RuntimeException, ParseException {
		log.info("Configuring controller");
		AbstractPipeline pipeline = new PipelineStanford();

		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Who is the president of the United States?");

		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);
		log.info(Joiner.on("\n\t").join(answers));

	}

}
