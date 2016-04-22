package org.aksw.qa.systems;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

public class SystemsTest {
    Logger log = LoggerFactory.getLogger(SystemsTest.class);

    @Test
    public void testAllSystems() {
        String question = "What is the capital of Germany?";
        List<ASystem> systems = Arrays.asList((ASystem) new HAWK(), (ASystem) new QAKIS(), (ASystem) new SINA(),
                (ASystem) new START(), (ASystem) new YODA());
        for (ASystem system : systems) {
            HashSet<String> answer = system.search(question);
            log.debug("Answers: " + answer.size());
            Assert.assertNotNull(answer);
        }
    }
}
