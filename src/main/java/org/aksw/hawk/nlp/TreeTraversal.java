package org.aksw.hawk.nlp;

import com.clearnlp.dependency.DEPNode;

public class TreeTraversal {
	public static String inorderTraversal(DEPNode depNode, int i, StringBuilder sb) {
		if (sb == null) {
			sb = new StringBuilder();
			sb.append("\n");
		}
		sb.append(printTabs(i) + depNode.lemma + "\n");
		++i;
		for (DEPNode node : depNode.getDependentNodeList()) {
			inorderTraversal(node, i, sb);
		}
		return sb.toString();
	}

	public static String inorderTraversal(MutableTreeNode depNode, int i, StringBuilder sb) {
		if (sb == null) {
			sb = new StringBuilder();
			sb.append("\n");
		}
		int size = depNode.getAnnotations().size();
		sb.append(printTabs(i) + depNode.label + " (" + depNode.nodeNumber + "|" + depNode.posTag + "|" + size + ")\n");
		++i;
		for (MutableTreeNode node : depNode.getChildren()) {
			inorderTraversal(node, i, sb);
		}
		return sb.toString();
	}

	private static String printTabs(int i) {
		String tabs = "";
		if (i > 0) {
			tabs = "|";
		}
		for (int j = 0; j < i; ++j) {
			tabs += "=";
		}
		tabs += ">";
		return tabs;
	}
}