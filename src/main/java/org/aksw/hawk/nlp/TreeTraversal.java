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