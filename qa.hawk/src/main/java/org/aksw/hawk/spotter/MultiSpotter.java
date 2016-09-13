/**
 *
 */
package org.aksw.hawk.spotter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A wrapper class which can be used to get as much entities as possible by from
 * several added spotters.
 *
 * @author Lorenz Buehmann
 *
 */
public class MultiSpotter extends ASpotter {

	private static final Logger logger = LoggerFactory.getLogger(MultiSpotter.class);

	private List<ASpotter> spotters;

	private Map<String, List<Entity>> totalResult;

	public MultiSpotter(final ASpotter... spotters) {
		this.spotters = Arrays.asList(spotters);
	}

	/*
	 * (non-Javadoc)
	 * @see org.aksw.hawk.nlp.spotter.ASpotter#getEntities(java.lang.String)
	 */
	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		totalResult = Maps.newConcurrentMap();

		ExecutorService tp = Executors.newFixedThreadPool(spotters.size());
		CompletionService<Map<String, List<Entity>>> ecs = new ExecutorCompletionService<>(tp);

		for (final ASpotter spotter : spotters) {
			tp.submit(new Runnable() {

				@Override
				public void run() {
					// get result from current spotter
					Map<String, List<Entity>> tmp = spotter.getEntities(question);

					// merge into global result
					mergeResults(tmp);
				}
			});
		}

		tp.shutdown();
		try {
			tp.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return totalResult;
	}

	private synchronized void mergeResults(final Map<String, List<Entity>> result) {
		// merge into global result
		for (Entry<String, List<Entity>> entry : result.entrySet()) {
			String lang = entry.getKey();
			List<Entity> newEntities = entry.getValue();

			List<Entity> existingEntities = totalResult.get(lang);
			if (existingEntities != null) {
				List<Entity> tmp = Lists.newArrayList();
				for (Entity entity1 : newEntities) {
					boolean add = true;
					for (Entity entity2 : existingEntities) {
						if (entity1.getLabel().equals(entity2.getLabel())) {
							add = false;
							entity2.getUris().addAll(entity1.getUris());
							break;
						}
					}
					if (add) {
						tmp.add(entity1);
					}
				}
				existingEntities.addAll(tmp);
			} else {
				totalResult.put(lang, newEntities);
			}
		}
	}

	public static void main(final String[] args) throws Exception {
		String question = "Which building owned by the crown overlook the North Sea?";

		ASpotter fox = new Fox();
		System.out.println(fox.getEntities(question));

		ASpotter tagMe = new TagMe();
		System.out.println(tagMe.getEntities(question));

		// ASpotter wiki = new WikipediaMiner();
		// System.out.println(wiki.getEntities(question));

		ASpotter spot = new Spotlight();
		System.out.println(spot.getEntities(question));

		MultiSpotter multiSpotter = new MultiSpotter(fox, tagMe, spot);
		System.out.println(multiSpotter.getEntities(question));
	}

}
