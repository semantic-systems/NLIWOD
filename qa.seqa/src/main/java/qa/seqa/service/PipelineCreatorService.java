package qa.seqa.service;

import java.util.List;

import qa.seqa.model.Pipeline;

public interface PipelineCreatorService {

	public List<Pipeline> createAllPossiblePipelines();

}