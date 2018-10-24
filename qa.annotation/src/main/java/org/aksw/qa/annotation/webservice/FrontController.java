package org.aksw.qa.annotation.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @RestController
//@Configuration
//@ComponentScan
//@EnableAutoConfiguration
@SpringBootApplication
public class FrontController {

	// final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static void main(final String[] args) {
		SpringApplication.run(FrontController.class, args);

	}

}
