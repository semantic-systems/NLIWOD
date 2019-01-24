package org.aksw.qa.systems;

import static org.junit.Assert.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TimeoutTest {

    @Test
    public void testAllSystems() throws Exception {
        String questionString = "Was ist die Hauptstadt von Deutschland?";
        List<ASystem> systems = Arrays.asList(
                (ASystem) new HAWK(), 
                (ASystem) new QAKIS(),
                (ASystem) new SINA(),
                (ASystem) new START(), 
                (ASystem) new YODA(),
                (ASystem) new QANARY(),
                (ASystem) new AskNow(),
                (ASystem) new SorokinQA(),
                (ASystem) new OKBQA(),
                (ASystem) new GANSWER2(),
                (ASystem) new PLATYPUS(),
                (ASystem) new QASystem(),
                (ASystem) new QUEPY(),
                (ASystem) new TeBaQA()
                );
        for (ASystem system : systems) {
        	system.setSocketTimeOutMs(10);
        	long a = System.currentTimeMillis();
        	try{
        		system.search(questionString, "de", true);
            	assertTrue(false);
        	}catch(SocketTimeoutException e){
        		long b = System.currentTimeMillis();
        		System.out.println(b-a);
        		assertTrue(b-a<3000);
        	}
        	catch(Exception e){
        		System.out.println("System "+system.name()+" does not work");
        		e.printStackTrace();
        	}
        }
    }

}
