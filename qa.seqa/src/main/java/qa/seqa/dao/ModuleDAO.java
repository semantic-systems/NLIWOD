package qa.seqa.dao;

import java.util.List;

import qa.seqa.model.Module;

public interface ModuleDAO {

	void insertModule(Module m);

	List<Module> getModules();
}
