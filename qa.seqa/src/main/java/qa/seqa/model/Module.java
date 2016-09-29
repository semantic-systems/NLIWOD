package qa.seqa.model;

import java.net.URL;
import java.util.List;

import org.apache.jena.ext.com.google.common.collect.Lists;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * A module is a unit used to generate a pipeline. It can serve as a
 * fine-grained function or serve as a whole pipeline taking just a question and
 * returning a set of answers
 * 
 * @author ricardousbeck
 *
 */
public class Module {
	private URL URL;
	private List<OWLClass> rdfInput = Lists.newArrayList();
	private List<OWLClass> rdfOutput = Lists.newArrayList();

	public URL getURL() {
		return URL;
	}

	public void setURL(URL uRL) {
		URL = uRL;
	}

	public List<OWLClass> getRdfInput() {
		return rdfInput;
	}

	public void setRdfInput(List<OWLClass> rdfInput) {
		this.rdfInput = rdfInput;
	}

	public List<OWLClass> getRdfOutput() {
		return rdfOutput;
	}

	public void setRdfOutput(List<OWLClass> rdfOutput) {
		this.rdfOutput = rdfOutput;
	}

	public void addRDFOutputClass(OWLClass owlClass) {
		rdfOutput.add(owlClass);
	}

	public void addrdfInputClass(OWLClass owlEntity) {
		rdfInput.add(owlEntity);
	}
}
