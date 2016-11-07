package qa.seqa.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import qa.seqa.model.Pipeline;
import qa.seqa.service.PipelineCreatorService;

@RestController
@SpringBootApplication
@ComponentScan("qa.seqa")
public class FrontController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PipelineCreatorService pipelineCreatorService;

	@RequestMapping("/generatePipeline")
	public List<Pipeline> generatePipeline() {

		// generate possible pipelines via strategy service
		logger.info("Create all possible pipelines");
		List<Pipeline> pipelines = pipelineCreatorService.createAllPossiblePipelines();

		// return pipeline URL
		return pipelines;
	}

	public static void main(String[] args) {
		SpringApplication.run(FrontController.class, args);

	}
}
