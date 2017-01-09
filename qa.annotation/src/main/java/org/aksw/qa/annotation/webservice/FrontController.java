package org.aksw.qa.annotation.webservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
