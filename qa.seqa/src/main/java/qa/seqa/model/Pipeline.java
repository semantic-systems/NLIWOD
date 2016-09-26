package qa.seqa.model;

import java.util.List;

/**
 * A module is a unit used to generate a pipeline. It can serve as a
 * fine-grained function or serve as a whole pipeline taking just a question and
 * returning a set of answers
 * 
 * @author ricardousbeck
 *
 */
public class Pipeline {
	private List<Module> modules;

	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
	}

}
