package org.aksw.hawk.nlp;

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

public class NounCombiner {

	public void combineNouns(Question q) {
		// go through sentence and
		// put all next to each other pos = NN(.*) words to
		// entities
		AbstractTokenizer tokenizer = NLPGetter.getTokenizer(AbstractReader.LANG_EN);
		String sentence = q.languageToQuestion.get("en");
		if (!q.languageToNamedEntites.isEmpty()) {
			for (Entity entity : q.languageToNamedEntites.get("en")) {
				if (!entity.label.equals("")) {
					// " " inserted so punctuation gets seperated correctly from
					// URIs
					sentence = sentence.replace(entity.label, entity.uris.get(0).getURI() + " ");
				}
			}
		}
		List<String> posList = Lists.newArrayList();
		List<String> tokens = tokenizer.getTokens(sentence);
		for (String token : tokens) {
			// look for each token in the tree and find its POS tag
			String findPos = findPos(token, q.depTree.getFirstRoot());
			posList.add(findPos);
		}
		System.out.println();
		for (int i = 0; i < posList.size() - 1; i++) {
			if (posList.get(i).matches("NN(.)*") && posList.get(i + 1).matches("NN(.)*")) {
				Set<String> tokenSet = Sets.newHashSet();
				String entLabel = "";
				int j = i;
				while (i < posList.size()) {
					if (posList.get(j).matches("NN(.)*")) {
						entLabel += tokens.get(j) + " ";
						tokenSet.add(tokens.get(j));
						j++;
						i++;
					} else {
						break;
					}
				}
				// mutate tree q.tree
				MutableTreeNode top = findTopMostNode(q.tree.getRoot(), tokenSet);
				top.label = entLabel;
				top.posTag = "CombinedNN";
				List<MutableTreeNode> toBeDeleted = Lists.newArrayList();
				for (MutableTreeNode child : top.getChildren()) {
					for (String token : tokenSet) {
						MutableTreeNode findDeletableNodes = findDeletableNodes(child, token);
						if (findDeletableNodes != null) {
							toBeDeleted.add(findDeletableNodes);
						}
					}
				}
				for (int x = 0; x < toBeDeleted.size(); x++) {
					q.tree.remove(toBeDeleted.get(x));
				}
			}
		}

	}

	// use breadth first search to find the deletable node
	private MutableTreeNode findDeletableNodes(MutableTreeNode root, String token) {
		Queue<MutableTreeNode> queue = Queues.newLinkedBlockingQueue();
		queue.add(root);
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			if (token.equals(tmp.label)) {
				return tmp;
			}
			for (MutableTreeNode n : tmp.getChildren()) {
				queue.add(n);
			}

		}
		return null;
	}

	// use breadth first search to find the top most node
	private MutableTreeNode findTopMostNode(MutableTreeNode root, Set<String> tokenSet) {
		Queue<MutableTreeNode> queue = Queues.newLinkedBlockingQueue();
		queue.add(root);
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			for (String token : tokenSet) {
				if (token.equals(tmp.label)) {
					return tmp;
				}
			}
			for (MutableTreeNode n : tmp.getChildren()) {
				queue.add(n);
			}

		}
		return null;
	}

	// use depth first search to find the POS tag
	private String findPos(String token, DEPNode node) {
		Stack<DEPNode> stack = new Stack<>();
		stack.push(node);
		while (!stack.isEmpty()) {
			DEPNode tmp = stack.pop();
			if (token.equals(tmp.form)) {
				return tmp.pos;
			} else {
				for (DEPNode n : tmp.getDependentNodeList()) {
					stack.push(n);
				}
			}
		}
		return null;
	}
}
