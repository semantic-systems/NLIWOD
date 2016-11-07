package qa.seqa.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import qa.seqa.model.Pipeline;

@Repository
public class InMemoryPipelineDAO implements PipelineDAO {

	@Override
	public List<Pipeline> getPipelines() {
		// TODO Auto-generated method stub
		return null;
	}
}
