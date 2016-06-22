package org.aksw.hawk.ranking;

import java.io.File;
import java.util.Set;

import org.aksw.hawk.cache.StorageHelper;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class FeatureBasedRankerDB {
	private Logger log = LoggerFactory.getLogger(FeatureBasedRankerDB.class);

	public FeatureBasedRankerDB() {
	}

	// TODO Christian make it independent of OS
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
	 */
	public void store(IQuestion q, Set<SPARQLQuery> queries) {
		for (SPARQLQuery query : queries) {
			int hash = query.hashCode();
			String serializedFileName = getFileName(hash);
			// File tmp = new File(serializedFileName);
			StorageHelper.storeToFileSavely(query, serializedFileName);

		}
	}

	private String getFileName(int hash) {
		String serializedFileName = "c:/ranking/" + hash + ".question";
		return serializedFileName;
	}
}
