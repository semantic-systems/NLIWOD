package org.aksw.hawk.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.aksw.hawk.visualization.SVGForTextInBoxTree;
import org.aksw.hawk.visualization.TextInBox;
import org.aksw.hawk.visualization.TextInBoxNodeExtentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineController_QALD4 {
	static Logger log = LoggerFactory.getLogger(PipelineController_QALD4.class);
	private String dataset;
	private QaldLoader datasetLoader;
	private ASpotter nerdModule;
	private ParseTree parseTree;
	private ModuleBuilder moduleBuilder;
	private PseudoQueryBuilder pseudoQueryBuilder;
	private Pruner pruner;
	private TreeTransformer treeTransform;
	private SystemAnswerer systemAnswerer;
	private QueryVariableHomomorphPruner queryVariableHomomorphPruner;
	private GraphNonSCCPruner graphNonSCCPruner;

	public static void main(String args[]) throws IOException {
		PipelineController_QALD4 controller = new PipelineController_QALD4();

		log.info("Configuring controller");

		controller.dataset = ClassLoader.getSystemResource("qald-4_hybrid_train.xml").getFile();
		controller.datasetLoader = new QaldLoader();
		controller.nerdModule = new Fox();
		controller.parseTree = new ParseTree();
		controller.treeTransform = new TreeTransformer();
		controller.pruner = new Pruner();
		controller.moduleBuilder = new ModuleBuilder();
		controller.pseudoQueryBuilder = new PseudoQueryBuilder();
		controller.queryVariableHomomorphPruner = new QueryVariableHomomorphPruner();
		controller.graphNonSCCPruner = new GraphNonSCCPruner();
		String endpoint = "http://dbpedia.org/sparql";
		controller.systemAnswerer = new SystemAnswerer(endpoint,controller.nerdModule);

		log.info("Run controller");
		controller.run();

	}

	private void run() throws IOException {
		// 1. read in Questions from QALD 1,2,3,4
		List<Question> questions = datasetLoader.load(dataset);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("trees.html")));

		for (Question q : questions) {
			log.info("->" + q.languageToQuestion);
			// 2. Disambiguate parts of the query
			q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
			if (!q.languageToNamedEntites.isEmpty()) {
				log.debug("\t" + Joiner.on("\n").join(q.languageToNamedEntites.get("en")));
			}
			// 3. Build trees from questions and cache them
			q.depTree = this.parseTree.process(q);

			q.tree = this.treeTransform.DEPtoMutableDEP(q.depTree);

			// visualize the tree
			String svg = visTree(q);
			bw.write(svg);

			// 4. Apply pruning rules
			q.tree = this.pruner.prune(q);

			// visualize the tree
			vis(bw, q);

			// 5. Build modules
			q.modules = this.moduleBuilder.build(q);

			// 8. Build pseudo queries
			List<ParameterizedSparqlString> tmp = this.pseudoQueryBuilder.buildQuery(q);

			log.info("Before : " + tmp.size());
			// homogenize variables in queries
			tmp = this.queryVariableHomomorphPruner.prune(tmp);
			log.info("After homogenizing queries:  " + tmp.size());

			// check whether clauses are connected
			tmp = this.graphNonSCCPruner.prune(tmp);
			log.info("After SCC check number of queries:  " + tmp.size());

			// TODO Eliminate invalid queries and find top ranked query
			// 10. Execute queries to generate system answers
			if (tmp == null) {
				log.info("\tP=" + 0.0 + " R=" + 0.0 + " F=" + 0.0);
			} else {
				log.info("Number of PseudoQueries: " + tmp.size());
				for (ParameterizedSparqlString pseudoQuery : tmp) {
					log.debug(pseudoQuery.toString());
					Set<RDFNode> systemAnswers = this.systemAnswerer.answer(pseudoQuery);

					// 11. Compare to set of resources from benchmark
					double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
					double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
					double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);

					if (fMeasure > 0) {
						log.info(pseudoQuery.toString());
						log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
					}
				}
			}
			bw.write("<hr/>");
			break;
		}
		bw.close();
	}

	private void vis(BufferedWriter bw, Question q) throws IOException {
		String svg;
		svg = visTree(q);
		bw.write(svg);
		bw.write("<div style=\"float:right\">");
		bw.write("Query: " + q.languageToQuestion.get("en") + " <br/>");
		bw.write("Spotter: " + nerdModule.toString() + " <br/>");
		for (Entity ent : q.languageToNamedEntites.get("en")) {
			bw.write("=>&nbsp" + ent.toString() + " <br/>");
		}
		bw.write("PseudoQuery: " + q.pseudoSparqlQuery.replace("\n", "<br/>") + " <br/>");
		bw.write("</div>");
	}

	private static String visTree(Question q) {
		// setup the tree layout configuration
		double gapBetweenLevels = 50;
		double gapBetweenNodes = 10;
		DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(gapBetweenLevels, gapBetweenNodes);

		TreeForTreeLayout<TextInBox> tree = createTree(q.tree);

		// create the NodeExtentProvider for TextInBox nodes
		TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();

		// create the layout
		TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(tree, nodeExtentProvider, configuration);

		// Generate the SVG and write it to System.out
		SVGForTextInBoxTree generator = new SVGForTextInBoxTree(treeLayout);
		String oldChar = "<?xml version=\"1.0\" standalone=\"no\" ?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">";
		String svg = generator.getSVG().replace(oldChar, "");
		return svg;
	}

	private static TreeForTreeLayout<TextInBox> createTree(MutableTree origTree) {
		MutableTreeNode tmp = origTree.getRoot();
		int width = 80;
		int height = 40;
		TextInBox parent = new TextInBox(tmp.label + "\n" + tmp.posTag, width, height);
		tmp.setTextNode(parent);
		DefaultTreeForTreeLayout<TextInBox> tree = new DefaultTreeForTreeLayout<TextInBox>(parent);

		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tmp);
		while (!stack.isEmpty()) {
			tmp = stack.pop();
			for (MutableTreeNode child : tmp.getChildren()) {
				if (child.getTextNode() == null) {
					child.setTextNode(new TextInBox(child.label + "\n" + child.posTag, width, height));
				}
				tree.addChild(tmp.getTextNode(), child.getTextNode());
				stack.push(child);
			}
		}
		return tree;
	}
}
