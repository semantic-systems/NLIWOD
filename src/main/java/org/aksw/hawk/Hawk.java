/**
 * 
 */
package org.aksw.hawk;

import java.util.Map;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.TrainPipeline;

/**
 * @author Lorenz Buehmann
 * 
 */
public class Hawk {

	public static void main(String[] args) throws Exception {
		TrainPipeline controller = new TrainPipeline();

		Question q = new Question();
		q.languageToQuestion.put("en", "Which recipients of the Victoria Cross died in the Battle of Arnhem?");

		Map<String, Answer> sparqll = controller.calculateSPARQLRepresentation(q,null);

		System.out.println(sparqll);
	}

}
