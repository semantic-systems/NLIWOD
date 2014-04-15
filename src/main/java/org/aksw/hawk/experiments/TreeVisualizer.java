package org.aksw.hawk.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.Fox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class TreeVisualizer {
	static Logger log = LoggerFactory.getLogger(TreeVisualizer.class);

	public static void main(String args[]) throws IOException {
		String dataset = ClassLoader.getSystemResource("qald-4_hybrid_train.xml").getFile();
		Fox nerdModule = new Fox();
		ParseTree parseTree = new ParseTree();
		QaldLoader datasetLoader = new QaldLoader();
		TreeTransformer treeTransform = new TreeTransformer();
		Pruner pruner = new Pruner();

		List<Question> questions = datasetLoader.load(dataset);
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("trees.html")));

		for (Question q : questions) {
			log.info("->" + q.languageToQuestion);
			q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
			if (!q.languageToNamedEntites.isEmpty()) {
				log.debug("\t" + Joiner.on("\n").join(q.languageToNamedEntites.get("en")));
			}
			q.depTree = parseTree.process(q);
			q.tree = treeTransform.DEPtoMutableDEP(q.depTree);

			String svg = visTree(q);
			bw.write(svg);

			q.tree = pruner.prune(q);
			svg = visTree(q);
			bw.write(svg);
			bw.write("<div style=\"float:right\">");
			bw.write("Query: " + q.languageToQuestion.get("en") + " <br/>");
			bw.write("Spotter: " + nerdModule.toString()+ " <br/>");
			for (Entity ent : q.languageToNamedEntites.get("en")) {
				bw.write("=>&nbsp" + ent.toString()+ " <br/>");
			}
			bw.write("</div>");
			bw.write("<hr/>");
		}
		bw.close();

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
