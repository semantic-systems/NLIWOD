package org.aksw.hawk.util;

import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONStatusBuilder {

	@SuppressWarnings("unchecked")
	public static JSONObject status(Question question) {
		JSONObject document = new JSONObject();
		document.put("input_question", question.languageToQuestion.get("en"));

		// named entities
		if (!question.languageToNamedEntites.isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (Entity key : question.languageToNamedEntites.get("en")) {
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", key.label);
				tmpobj.put("value", key.uris.get(0));
				tmp.add(tmpobj);
			}
			document.put("named_entities", tmp);
		}
		// combined nouns
		if (!question.languageToNounPhrases.isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (Entity key : question.languageToNounPhrases.get("en")) {
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", key.label);
				tmpobj.put("value", key.uris.get(0));
				tmp.add(tmpobj);
			}
			document.put("combined_nouns", tmp);
		}
		// POS tags
		if (question.tree != null) {
			Stack<MutableTreeNode> stack = new Stack<MutableTreeNode>();
			stack.push(question.tree.getRoot());
			JSONArray tmp = new JSONArray();
			while (!stack.isEmpty()) {
				MutableTreeNode node = stack.pop();
				JSONObject tmpobj = new JSONObject();
				tmpobj.put("key", node.label);
				tmpobj.put("POS", node.posTag);
				tmp.add(tmpobj);
				for (MutableTreeNode child : node.children) {
					stack.push(child);
				}
			}
			document.put("POS_tags", tmp);
		}

		// full tree
		if (question.tree_full != null) {
			document.put("tree_full", question.tree_full);
		}
		// pruned tree
		if (question.tree_pruned != null) {
			document.put("tree_pruned", question.tree_pruned);
		}
		// final tree
		if (question.tree_final != null) {
			document.put("tree_pruned", question.tree_final);
		}

		// annotation
		if (question.tree != null) {
			Stack<MutableTreeNode> stack = new Stack<MutableTreeNode>();
			stack.push(question.tree.getRoot());
			JSONArray tmp = new JSONArray();
			while (!stack.isEmpty()) {
				MutableTreeNode node = stack.pop();

				JSONObject tmpobj = new JSONObject();
				tmpobj.put("label", node.label);
				JSONArray tmpArray = new JSONArray();
				tmpArray.addAll(node.getAnnotations());
				tmpobj.put("annotations", tmpArray);

				tmp.add(tmpobj);
				for (MutableTreeNode child : node.children) {
					stack.push(child);
				}
			}
			document.put("annotation", tmp);
		}

		// pruning
		if (question.pruning_messages != null) {
			document.put("pruning_messages", question.pruning_messages);
		}
		// final sparql
		if (question.finalAnswer != null && !question.finalAnswer.isEmpty()) {
			document.put("final_sparql_base64", new String(Base64.encodeBase64(question.finalAnswer.get(0).queryString.getBytes())));
			// final answer FIXME schwachsinnige struktur hier
			JSONObject obj = new JSONObject();
			JSONArray array = new JSONArray();
			array.add(question.finalAnswer.get(0).answerSet);
			obj.put("value", array);
			document.put("answer", obj);
		}

		return document;
	}

	public static JSONObject treeToJSON(MutableTree tree) {
		if (tree != null) {
			JSONObject document = recursiveNodeLabel(tree.getRoot(), new JSONObject());
			return document;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private static JSONObject recursiveNodeLabel(MutableTreeNode node, JSONObject document) {
		document.put("label", node.label);
		if (!node.children.isEmpty()) {
			JSONArray tmp = new JSONArray();
			for (MutableTreeNode child : node.children) {
				JSONObject object = new JSONObject();
				recursiveNodeLabel(child, object);
			}
			document.put("children", tmp);
		}
		return document;
	}
}
