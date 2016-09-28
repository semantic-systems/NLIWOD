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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
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
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontologyNIF = null;
		OWLOntology ontologyOA = null;

		try {
			URL nifURL = new URL("https://raw.githubusercontent.com/NLP2RDF/ontologies/master/nif-core/nif-core.ttl");
			InputStream nifis = nifURL.openStream();
			ontologyNIF = manager.loadOntologyFromOntologyDocument(nifis);

			// FIXME when W3C fixed website https://www.w3.org/ns/oa/index.ttl
			// broken
			URL OAURL = new URL("https://github.com/w3c/web-annotation/raw/gh-pages/vocab/wd/ontology/index.xml");
			InputStream oais = OAURL.openStream();
			ontologyOA = manager.loadOntologyFromOntologyDocument(oais);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// merge relevant ontologies
		OWLOntologyMerger merger = new OWLOntologyMerger(manager);
		IRI mergedOntologyIRI = IRI.create("http://aksw.org/SEQA");
		OWLOntology merged = merger.createMergedOntology(manager, mergedOntologyIRI);

		PelletReasoner r = PelletReasonerFactory.getInstance().createReasoner(merged);
		System.out.println("done.");
		r.getKB().realize();
		r.getKB().printClassTree();

		OWLClass thing = merged.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create("owl:Thing"));
		Set<OWLClass> classes = r.getSubClasses(thing, false).getFlattened();
		System.out.println(classes);
	}

}
