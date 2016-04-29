package org.aksw.qa.systems;

import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemsTest {
    Logger log = LoggerFactory.getLogger(SystemsTest.class);

    @Test
    public void testAllSystems() {
        String questionString = "What is the capital of Germany?";
        List<ASystem> systems = Arrays.asList(
//                (ASystem) new HAWK(), 
//                (ASystem) new QAKIS(),
                (ASystem) new SINA()
//                (ASystem) new START(), 
//                (ASystem) new YODA()
                );
        for (ASystem system : systems) {
            IQuestion question = system.search(questionString);
            log.debug(question.toString());
        }
    }
}
