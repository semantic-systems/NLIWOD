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

import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.TriplePatternExtractor;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author Lorenz Buehmann
 *
 */
public class GoldNerGenerator {
	
	static final List<String> datasets = Lists.newArrayList("resources/qald-4_hybrid_train.xml");

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		QALD_Loader datasetLoader = new QALD_Loader();
		for (String dataset : datasets) {
			StringBuilder sb = new StringBuilder();
			sb.append("#id\tquestion\tentities\trewritten question\n");
			List<Question> questions = datasetLoader.load(dataset);
			Collections.sort(questions, new Comparator<Question>() {

				@Override
				public int compare(Question o1, Question o2) {
					return Integer.compare(o1.id, o2.id);
				}
			});
			for (Question question : questions) {
				sb.append(question.id).append("\t");//ID
				
				sb.append(question.languageToQuestion.get("en")).append("\t");// question
				
				sb.append(extractEntities(rewritePseudoQuery(question.pseudoSparqlQuery))).append("\t");// entities

				sb.append(question.languageToQuestion.get("en"));// rewritten question template
				sb.append("\n");
			}
			File datasetFile = new File(dataset);
			File targetFile = new File("resources/" + datasetFile.getName().replace("." + Files.getFileExtension(dataset), "") + ".tsv");
			Files.write(sb, targetFile, Charsets.UTF_8);
			System.out.println(sb);
		}
	}
	
	private static String rewritePseudoQuery(String query) {
		query = "PREFIX text:<http://hawk.aksw.org/built-in/> "
				+ "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX res:<http://dbpedia.org/resource/> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + query;
		query = query.replaceAll("text:\".*?\"", "?var");
		return query;
	}
	
	private static SortedSet<String> extractEntities(String query) {
		SortedSet<String> entities = new TreeSet<String>();
		try {
			Query q = QueryFactory.create(query, Syntax.syntaxARQ);
			TriplePatternExtractor tpExtractor = new TriplePatternExtractor();
			Set<Triple> triplePatterns = tpExtractor.extractTriplePattern(q);
			for (Triple tp : triplePatterns) {
				if(tp.getSubject().isURI()) {
					entities.add(tp.getSubject().getURI());
				}
				if(tp.getPredicate().isURI() 
						&& !tp.getPredicate().getNameSpace().equals(RDFS.getURI()) 
						&& !tp.getPredicate().getNameSpace().equals(RDF.getURI())) {
					entities.add(tp.getPredicate().getURI());
				}
				if(tp.getObject().isURI()) {
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
