package qa.seqa.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import qa.seqa.model.Module;

@Repository
public class InMemoryModuleDAO implements ModuleDAO {

	@Override
	public void insertModule(Module m) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Module> getModules() {
		// TODO Auto-generated method stub
		return null;
	}

}
