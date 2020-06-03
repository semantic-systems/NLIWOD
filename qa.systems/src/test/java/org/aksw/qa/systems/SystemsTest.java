package org.aksw.qa.systems;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemsTest {
    private Logger log = LoggerFactory.getLogger(SystemsTest.class);

    @Test
    public void testAllSystemsEN() throws Exception {
        String questionString = "What is the capital of Germany?";
        List<ASystem> systems = Arrays.asList(
//                (ASystem) new HAWK(), 
//                (ASystem) new QAKIS(),
//                (ASystem) new SINA(),
//                (ASystem) new START(), 
//                (ASystem) new OKBQA(),
                (ASystem) new AskNow(),
//                (ASystem) new SorokinQA(),
//                (ASystem) new YODA(),
                (ASystem) new QANARY(),
//                (ASystem) new GANSWER2(),
                (ASystem) new PLATYPUS(),
//               (ASystem) new QASystem(),
//                (ASystem) new QUEPY(),
                (ASystem) new TeBaQA(),
//                (ASystem) new FRANKENSTEIN(),
                (ASystem) new TEQUILA(),
                (ASystem) new KGQA()
                );
        for (ASystem system : systems) {
        	system.setSocketTimeOutMs(30000);
            try{
        		IQuestion question = system.search(questionString, "en", true);
            	log.debug(question.toString());
        	}catch(SocketTimeoutException e){
        		log.debug("Timeout in " + system.name());
        	}catch(Exception e){
        		log.debug("System "+system.name()+" does not work");
        		e.printStackTrace();
        	}
        }
    }
    
    @Test
    public void testAllSystemsDE() throws Exception {
        String questionString = "Was ist die Hauptstadt von Deutschland?";
        List<ASystem> systems = Arrays.asList(
//                (ASystem) new HAWK(), 
//                (ASystem) new QAKIS(),
//                (ASystem) new SINA(),
//                (ASystem) new START(), 
//                (ASystem) new OKBQA(),
//                (ASystem) new AskNow(),
//                (ASystem) new YODA(),
                (ASystem) new QANARY()
                );
        for (ASystem system : systems) {
        	system.setSocketTimeOutMs(30000);
        	try{
        		IQuestion question = system.search(questionString, "de", true);
            	log.debug(question.toString());
        	}catch(SocketTimeoutException e){
        		log.debug("Timeout in " + system.name());
        	}catch(Exception e){
        		System.out.println("System "+system.name()+" does not work");
        		e.printStackTrace();
        	}
        }
    }
}
