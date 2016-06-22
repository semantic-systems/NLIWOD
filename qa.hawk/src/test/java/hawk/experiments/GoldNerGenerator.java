/**
 * 
 */
package hawk.experiments;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.aksw.autosparql.commons.qald.TriplePatternExtractor;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * @author Lorenz Buehmann
 *
 */
public class GoldNerGenerator {

	/**
	 * @param args
	 */
	public static void main(final String[] args) throws Exception {
		QALD_Loader datasetLoader = new QALD_Loader();
		for (Dataset dataset : Dataset.values()) {
			StringBuilder sb = new StringBuilder();
			sb.append("#id\tquestion\tentities\trewritten question\n");
			List<IQuestion> questions = datasetLoader.load(dataset);
			Collections.sort(questions, new Comparator<IQuestion>() {

				@Override
				public int compare(final IQuestion o1, final IQuestion o2) {
					return Integer.compare(o1.getId(), o2.getId());

				}
			});
			for (IQuestion question : questions) {
				sb.append(question.getId()).append("\t");// ID
				sb.append(question.getLanguageToQuestion().get("en")).append("\t");// question
				sb.append(extractEntities(rewritePseudoQuery(question.getPseudoSparqlQuery()))).append("\t");// entities
				sb.append(question.getLanguageToQuestion().get("en"));// rewritten
				                                                      // question
				                                                      // template
				sb.append("\n");
			}
			File targetFile = new File("resources/" + dataset.name() + ".tsv");
			Files.write(sb, targetFile, Charsets.UTF_8);
			System.out.println(sb);
		}
	}

	private static String rewritePseudoQuery(String query) {
		query = "PREFIX text:<http://hawk.aksw.org/built-in/> " + "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + "PREFIX res:<http://dbpedia.org/resource/> "
		        + "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + query;
		query = query.replaceAll("text:\".*?\"", "?var");
		return query;
	}

	private static SortedSet<String> extractEntities(final String query) {
		SortedSet<String> entities = new TreeSet<>();
		try {
			Query q = QueryFactory.create(query, Syntax.syntaxARQ);
			TriplePatternExtractor tpExtractor = new TriplePatternExtractor();
			Set<Triple> triplePatterns = tpExtractor.extractTriplePattern(q);
			for (Triple tp : triplePatterns) {
				if (tp.getSubject().isURI()) {
					entities.add(tp.getSubject().getURI());
				}
				if (tp.getPredicate().isURI() && !tp.getPredicate().getNameSpace().equals(RDFS.getURI()) && !tp.getPredicate().getNameSpace().equals(RDF.getURI())) {
					entities.add(tp.getPredicate().getURI());
				}
				if (tp.getObject().isURI()) {
					entities.add(tp.getObject().getURI());
				}
			}
		} catch (Exception e) {
			System.err.println(query);
			e.printStackTrace();
		}
		return entities;
	}

}
