package qa.seqa.owl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.springframework.stereotype.Service;

import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

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
		OWLOntology ontologyQANARY = null;

		try {
			URL nifURL = new URL("https://raw.githubusercontent.com/NLP2RDF/ontologies/master/nif-core/nif-core.ttl");
			InputStream nifis = nifURL.openStream();
			ontologyNIF = manager.loadOntologyFromOntologyDocument(nifis);

			// FIXME when W3C fixed website https://www.w3.org/ns/oa/index.ttl
			// broken
			URL OAURL = new URL("https://github.com/w3c/web-annotation/raw/gh-pages/vocab/wd/ontology/index.xml");
			InputStream oais = OAURL.openStream();
			ontologyOA = manager.loadOntologyFromOntologyDocument(oais);

			manager.getIRIMappers().clear();
			OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
			//FIXME replace the IRI below with https://www.w3.org/ns/oa/index.owl as soon as bug is fixed within QANARY
			IRI create = IRI.create("http://www.openannotation.org/spec/core/20130208/oa.owl");
			config = config.addIgnoredImport(create);
			IRI qanaryURL = IRI.create("https://raw.githubusercontent.com/WDAqua/QAOntology/master/qanary.ttl");
			ontologyQANARY = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(qanaryURL), config);
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

		System.out.println("class tree");
		PelletReasoner r = PelletReasonerFactory.getInstance().createReasoner(merged);
		r.getKB().classify();
		r.getKB().printClassTree();

		System.out.println("subclasses of owl:Thing");
		Set<OWLClass> classes = r.getSubClasses(manager.getOWLDataFactory().getOWLThing(), false).getFlattened();
		System.out.println(classes);

		System.out.println("Equivalent classes");
		NodeSet<OWLClass> clss = r.getSubClasses(manager.getOWLDataFactory().getOWLThing(),false);
		clss.forEach(System.out::println);

		//EntitySearcher.getEquivalentClasses(cls, merged);
	}

}
