package qa.seqa.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import qa.seqa.dao.InMemoryModuleDAO;
import qa.seqa.model.Module;
import qa.seqa.model.Pipeline;

@Service
public class GreedyPipelineCreatorService implements PipelineCreatorService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private InMemoryModuleDAO moduleDAO;

	@Override
	public List<Pipeline> createAllPossiblePipelines() {
		// get list of all available modules from registry
		logger.info("Get all modules from registry");
		List<Module> modules = moduleDAO.getModules();

		
		// TODO Auto-generated method stub
		return null;
	}

}
