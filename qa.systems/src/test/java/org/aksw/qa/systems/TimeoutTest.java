package org.aksw.qa.systems;

import static org.junit.Assert.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
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
                (ASystem) new QANARY()
                );
        for (ASystem system : systems) {
        	system.setSocketTimeOutMs(10);
        	long a = System.currentTimeMillis();
        	try{
        		IQuestion question = system.search(questionString, "de", true);
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
