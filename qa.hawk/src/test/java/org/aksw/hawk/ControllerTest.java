package org.aksw.hawk;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.aksw.hawk.webservice.WebController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@RunWith(SpringRunner.class)
@SpringBootTest(classes=WebController.class)
public class ControllerTest {

	 private MockMvc mockMvc;

	    @Before
	    public void setup() {
	        this.mockMvc = MockMvcBuilders.standaloneSetup(new WebController()).build();
	    }

	    @Test
	    public void testSayHelloWorld() throws Exception {
	        this.mockMvc.perform(get("/simple-search?query=\"\"").accept(MediaType.parseMediaType("application/json;charset=UTF-8")))
	                .andExpect(status().isOk());

	    }

}