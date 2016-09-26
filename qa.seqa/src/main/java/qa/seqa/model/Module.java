package qa.seqa.model;

import java.net.URI;

import org.apache.jena.rdf.model.Model;

/**
 * A module is a unit used to generate a pipeline. It can serve as a
 * fine-grained function or serve as a whole pipeline taking just a question and
 * returning a set of answers
 * 
 * @author ricardousbeck
 *
 */
public class Module {
	private URI URL;
	private Model rdfInput;
	private Model rdfOutput;
	public URI getURL() {
		return URL;
	}
	public void setURL(URI uRL) {
		URL = uRL;
	}
	public Model getRdfInput() {
		return rdfInput;
	}
	public void setRdfInput(Model rdfInput) {
		this.rdfInput = rdfInput;
	}
	public Model getRdfOutput() {
		return rdfOutput;
	}
	public void setRdfOutput(Model rdfOutput) {
		this.rdfOutput = rdfOutput;
	}
}
