package org.aksw.hawk.ranking;

import java.io.File;
import java.util.Set;

import org.aksw.hawk.cache.StorageHelper;
import org.aksw.hawk.datastructures.Question;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class FeatureBasedRankerDB {
	private Logger log = LoggerFactory.getLogger(FeatureBasedRankerDB.class);

	public FeatureBasedRankerDB() {
	}

	public Set<SPARQLQuery> readRankings() {
		Set<SPARQLQuery> set = Sets.newHashSet();
		for (File f : new File("c:/ranking/").listFiles()) {
			log.debug("Reading file for ranking: " + f);
			set.add((SPARQLQuery) StorageHelper.readFromFileSavely(f.toString()));
		}
		return set;

	}

	/**
	 * stores a question
	 * 
	 * @param queries
	 * 
	 */
	public void store(Question q, Set<SPARQLQuery> queries) {
		for (SPARQLQuery query : queries) {
			int hash = query.hashCode();
			String serializedFileName = getFileName(hash);
//			File tmp = new File(serializedFileName);
			StorageHelper.storeToFileSavely(query, serializedFileName);

		}
	}

	private String getFileName(int hash) {
		String serializedFileName = "c:/ranking/" + hash + ".question";
		return serializedFileName;
	}
}
