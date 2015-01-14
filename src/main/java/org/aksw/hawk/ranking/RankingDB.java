package org.aksw.hawk.ranking;

import java.io.File;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.StorageHelper;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class RankingDB {
	private Logger log = LoggerFactory.getLogger(RankingDB.class);

	public RankingDB() {
	}

	public Set<SPARQLQuery> readRankings() {
		Set<SPARQLQuery> set = Sets.newHashSet();
		for (File f : new File("ranking/").listFiles()) {
			log.debug("Reading file for ranking: " + f);
			set.add((SPARQLQuery) StorageHelper.readFromFileSavely(f.toString()));
		}
		return set;

	}

	/**
	 * stores a question
	 * 
	 * @param query
	 * 
	 */
	public void store(Question q, SPARQLQuery query) {
		String question = q.languageToQuestion.get("en");
		int hash = question.hashCode();
		String serializedFileName = getFileName(hash);
		File tmp = new File(serializedFileName);
		StorageHelper.storeToFileSavely(query, serializedFileName);

	}

	private String getFileName(int hash) {
		String serializedFileName = "ranking/" + hash + ".question";
		return serializedFileName;
	}
}
