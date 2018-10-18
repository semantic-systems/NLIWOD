package org.aksw.mlqa.systems;

import java.util.ArrayList;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.systems.ASystem;
import org.aksw.qa.systems.HAWK;
import org.aksw.qa.systems.QAKIS;
import org.aksw.qa.systems.SINA;
import org.aksw.qa.systems.YODA;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemsTest {
	Logger log = LoggerFactory.getLogger(SystemsTest.class);

	@Test
	@Ignore
	public void testAllSystems() throws Exception {
		String question = "What is the capital of Germany?";
		ArrayList<ASystem> systems = Lists.newArrayList(new HAWK(), new QAKIS(), new SINA(), new YODA());
		for (ASystem system : systems) {
			IQuestion answer = system.search(question, "en");
			log.debug("Answers: "+ answer.toString());
			Assert.assertNotNull(answer);
		}
	}
}