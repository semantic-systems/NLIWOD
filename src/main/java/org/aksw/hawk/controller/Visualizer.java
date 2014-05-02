package org.aksw.hawk.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;

import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.TreeLayout;
import org.abego.treelayout.util.DefaultConfiguration;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.visualization.SVGForTextInBoxTree;
import org.aksw.hawk.visualization.TextInBox;
import org.aksw.hawk.visualization.TextInBoxNodeExtentProvider;

public class Visualizer {
	private BufferedWriter bw;

	public Visualizer() {
		try {
			bw = new BufferedWriter(new FileWriter(new File("trees.html")));
			bw.write("<?xml version=\"1.0\" standalone=\"no\" ?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void vis(Question q, ASpotter nerdModule) throws IOException {
		double gapBetweenLevels = 50;
		double gapBetweenNodes = 10;
		DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(gapBetweenLevels, gapBetweenNodes);
		TreeForTreeLayout<TextInBox> tree = createTree(q.tree);
		TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();
		TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(tree, nodeExtentProvider, configuration);
		SVGForTextInBoxTree generator = new SVGForTextInBoxTree(treeLayout);

		String oldChar = "<?xml version=\"1.0\" standalone=\"no\" ?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">";
		String svg = generator.getSVG().replace(oldChar, "");
		
		bw.write(svg);
		bw.write("<div style=\"float:right\">");
		bw.write("Query: " + q.languageToQuestion.get("en") + " <br/>");
		bw.write("Spotter: " + nerdModule.toString() + " <br/>");
		for (Entity ent : q.languageToNamedEntites.get("en")) {
			bw.write("=>&nbsp" + ent.toString() + " <br/>");
		}
		if (q.pseudoSparqlQuery != null) {
			bw.write("PseudoQuery: " + q.pseudoSparqlQuery.replace("\n", "<br/>") + " <br/>");
		} else {
			bw.write("SPARQL: " + q.sparqlQuery.replace("\n", "<br/>") + " <br/>");
		}

		bw.write("</div>");
	}

	void visTree(Question q) {
		// setup the tree layout configuration
		double gapBetweenLevels = 50;
		double gapBetweenNodes = 10;
		DefaultConfiguration<TextInBox> configuration = new DefaultConfiguration<TextInBox>(gapBetweenLevels, gapBetweenNodes);
		TreeForTreeLayout<TextInBox> tree = createTree(q.tree);
		TextInBoxNodeExtentProvider nodeExtentProvider = new TextInBoxNodeExtentProvider();
		TreeLayout<TextInBox> treeLayout = new TreeLayout<TextInBox>(tree, nodeExtentProvider, configuration);
		SVGForTextInBoxTree generator = new SVGForTextInBoxTree(treeLayout);

		String oldChar = "<?xml version=\"1.0\" standalone=\"no\" ?>\n<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" \"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">";
		String svg = generator.getSVG().replace(oldChar, "");
		try {
			bw.write(svg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private TreeForTreeLayout<TextInBox> createTree(MutableTree origTree) {
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

	public void horRule() {
		try {
			bw.write("<hr/>");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
