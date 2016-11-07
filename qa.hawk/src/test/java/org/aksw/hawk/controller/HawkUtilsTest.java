package org.aksw.hawk.controller;

import java.util.Arrays;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Assert;
import org.junit.Test;

public class HawkUtilsTest {

	@Test
	public void test() {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?");
		Entity ww2 = new Entity();
		ww2.setLabel("World War II");
		ww2.setOffset(103);
		ww2.getUris().add(new ResourceImpl("http://dbpedia.org/resource/World_War_II"));
		Entity japan = new Entity();
		japan.setLabel("Japan");
		japan.setOffset(90);
		japan.getUris().add(new ResourceImpl("http://dbpedia.org/resource/Japan"));

		q.getLanguageToNamedEntites().put("en", Arrays.asList(ww2, japan));

		Entity vPre = new Entity();
		vPre.setLabel("vice president");
		vPre.setOffset(8);
		vPre.getUris().add(new ResourceImpl("http://aksw.org/combinedNN/vice_president"));
		q.getLanguageToNounPhrases().put("en", Arrays.asList(vPre));

		String retString = HAWKUtils.replaceNamedEntitysWithURL(q);
		System.out.println(retString);
		Assert.assertTrue(retString.equals(
		        "Who was http://aksw.org/combinedNN/vice_president under the president who approved the use of atomic weapons against http://dbpedia.org/resource/Japan during http://dbpedia.org/resource/World_War_II?"));

	}

}
