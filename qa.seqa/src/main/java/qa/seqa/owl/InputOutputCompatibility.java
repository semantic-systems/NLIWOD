package qa.seqa.owl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.stereotype.Service;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * 
 * Finding out whether the output of one module is suitable as input for the
 * other
 * 
 * @author ricardousbeck
 *
 */
@Service
public class InputOutputCompatibility {

	public static void main(String[] args) throws Exception {
		// load the importing ontology
		OWLOntology ontology = null;
		try {
			URL dbpediaURL = new URL("http://downloads.dbpedia.org/2015-10/dbpedia_2015-10.owl");
			InputStream is = dbpediaURL.openStream();
			ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(is);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		PelletReasoner r = PelletReasonerFactory.getInstance().createReasoner( ontology );
		System.out.println("done.");
		r.getKB().realize();
		r.getKB().printClassTree();
		
		OWLClass person = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create("http://dbpedia.org/ontology/Person"));
		Set<OWLClass> classes = r.getSubClasses(person, false).getFlattened();
		System.out.println(classes);
	}

}
