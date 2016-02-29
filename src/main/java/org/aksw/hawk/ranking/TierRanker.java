package org.aksw.hawk.ranking;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.Question;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.MutableTreeNodeIterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.ElementWalker;

public class TierRanker implements Ranking {

	private MutableTree tree = new MutableTree(); // initial empty tree
	private MutableTreeNodeIterator it;

	public TierRanker() {

	}

	// TODO write proper tests

	public TierRanker(MutableTree tree) {
		init(tree);
	}

	public void init(MutableTree tree) {
		this.tree = tree;
		it = new MutableTreeNodeIterator(this.tree.getRoot());

	}

	protected double getScore(Query query) {
		double ret = 0.0;
		Set<String> clause = queryToNodeList(query);
		while (it.hasNext()) {
			MutableTreeNode cur = it.next();
			int nodeTier = it.getTier();
			double curScore = getScoreForNode(cur, nodeTier, clause);
			ret += curScore;
		}
		it.reset();
		return ret;
	}

	protected static double getScoreForNode(MutableTreeNode node, int nodeTier, Set<String> clause) {
		double ret = 0;
		// String clause =resolvedClause(q);

		if (clause.contains(node.label)) {
			ret = 1;
		}
		double tier = 1.0 / Math.pow(2, nodeTier);
		return ret * tier;
	}

	private static Set<String> queryToNodeList(Query q) {
		// Remember distinct subjects in this
		final Set<String> subjects = new HashSet<String>();

		// This will walk through all parts of the query
		ElementWalker.walk(q.getQueryPattern(),
		// For each element...
		        new ElementVisitorBase() {
			        // ...when it's a block of triples...
			        public void visit(ElementPathBlock el) {
				        // ...go through all the triples...
				        Iterator<TriplePath> triples = el.patternElts();
				        while (triples.hasNext()) {
					        // ...and grab the subject
					        TriplePath t = triples.next();
					        subjects.add(getString(t.getSubject()));
					        subjects.add(getString(t.getObject()));
					        subjects.add(getString(t.getPredicate()));
				        }
			        }
		        });
		return subjects;
	}

	private static String getString(Node n) {
		// String ret= "";
		if (n.isURI()) {
			// n.get
			return n.getURI();
		}
		if (n.isVariable()) {
			return n.toString();
		}
		if (n.isBlank()) {
			return n.getBlankNodeLabel();
		}
		if (n.isLiteral()) {
			return n.toString(false);
		}
		return "";
		// return ret;
	}

	protected static String resolvedClause(Query q) {
		Map<String, String> map = q.getPrefixMapping().getNsPrefixMap();
		for (String key : map.keySet()) {
			q.getPrefixMapping().removeNsPrefix(key);
		}
		return q.getQueryPattern().toString();
	}

	@Override
	public List<Answer> rank(List<Answer> answers, Question q) {
		init(q.tree);
		for (Answer answer : answers) {
			double rank = getScore(QueryFactory.create(answer.queryString));
			 System.out.println(answer.queryString+" ranked with score: "+rank);
			answer.score = rank;
		}
		Collections.sort(answers);
		Collections.reverse(answers);
		return answers;
	}

}
