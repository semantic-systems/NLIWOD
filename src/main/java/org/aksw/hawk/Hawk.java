/**
 * 
 */
package org.aksw.hawk;

import java.util.Map;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.TrainPipeline;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author Lorenz Buehmann
 * 
 */
public class Hawk {

	public static void main(String[] args) throws Exception {
		TrainPipeline controller = new TrainPipeline();

		Question q = new Question();
		q.languageToQuestion.put("en", "Which actress starring in the TV series Friends owns the production company Coquette Productions?");

		Map<String, Answer> sparqll = controller.calculateSPARQLRepresentation(q, null);

		for (String key : sparqll.keySet()) {
			System.out.println(key);
			for (RDFNode answer : sparqll.get(key).answerSet) {
				System.out.println("\t" + answer);
			}
		}
	}

}
