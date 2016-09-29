package qa.seqa.owl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.IRIDocumentSource;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLOntologyMerger;
import org.springframework.stereotype.Service;

import qa.seqa.model.Module;

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
		DefaultPrefixManager prefixManager = new DefaultPrefixManager();

		OWLOntology ontologyNIF = null;
		OWLOntology ontologyOA = null;
		OWLOntology ontologyQANARY = null;

		try {
			URL nifURL = new URL("https://raw.githubusercontent.com/NLP2RDF/ontologies/master/nif-core/nif-core.ttl");
			InputStream nifis = nifURL.openStream();
			ontologyNIF = manager.loadOntologyFromOntologyDocument(nifis);
			OWLDocumentFormat format = ontologyNIF.getOWLOntologyManager().getOntologyFormat(ontologyNIF);
			prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());

			// FIXME when W3C fixed website https://www.w3.org/ns/oa/index.ttl
			// brokenhttps://github.com/w3c/web-annotation/raw/gh-pages/vocab/wd/ontology/index.xml
			URL OAURL = new URL("https://www.w3.org/ns/oa.owl");
			InputStream oais = OAURL.openStream();
			ontologyOA = manager.loadOntologyFromOntologyDocument(oais);
			format = ontologyOA.getOWLOntologyManager().getOntologyFormat(ontologyOA);
			prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());

			manager.getIRIMappers().clear();
			OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
			// FIXME replace the IRI below with
			// https://www.w3.org/ns/oa/index.owl as soon as bug is fixed within
			// QANARY
			IRI create = IRI.create("http://www.openannotation.org/spec/core/20130208/oa.owl");
			config = config.addIgnoredImport(create);
			IRI qanaryURL = IRI.create("https://raw.githubusercontent.com/WDAqua/QAOntology/master/qanary.ttl");
			ontologyQANARY = manager.loadOntologyFromOntologyDocument(new IRIDocumentSource(qanaryURL), config);
			format = ontologyQANARY.getOWLOntologyManager().getOntologyFormat(ontologyQANARY);
			prefixManager.copyPrefixesFrom(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());

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

		prefixManager.getPrefixName2PrefixMap().forEach((x, y) -> System.out.println(x + " - > " + y));

		OWLDataFactory owlDataFactory = manager.getOWLDataFactory();

		Module m1 = new Module();
		m1.setURL(new URL("http://spotlight.sztaki.hu:2222/rest/annotate"));

		OWLClass owlEntity = (OWLClass) owlDataFactory.getOWLEntity(EntityType.CLASS, prefixManager.getIRI("oa:Annotation"));
		m1.addRDFOutputClass(owlEntity);

		Module m2 = new Module();
		m2.setURL(new URL("http://139.18.2.164:8080/AGDISTIS"));
		owlEntity = (OWLClass) owlDataFactory.getOWLEntity(EntityType.CLASS, prefixManager.getIRI("oa:Annotation"));
		m2.addrdfInputClass(owlEntity);
		owlEntity = (OWLClass) owlDataFactory.getOWLEntity(EntityType.CLASS, prefixManager.getIRI("qa:AnnotationOfSpotEntity"));
		m2.addrdfInputClass(owlEntity);
		
		
		System.out.println("class tree");
		PelletReasoner r = PelletReasonerFactory.getInstance().createReasoner(merged);
		r.getKB().classify();
		// r.getKB().printClassTree();

		// check whether output of Module 1 is valid input of Module 2
		for (OWLClass cls : m1.getRdfOutput()) {
			Set<OWLClass> classes = r.getSubClasses(cls, false).getFlattened();
			for (OWLClass subclasses : classes) {
				for (OWLClass m2c : m2.getRdfInput()) {
					if (subclasses.equals(m2c)) {
						System.out.println("HEUREKA");
					}
				}
			}
		}
		for (OWLClass cls : m1.getRdfOutput()) {
			Node<OWLClass> classes = r.getEquivalentClasses(cls.getNNF());
			for (OWLClassExpression equi : classes) {
				for (OWLClass m2c : m2.getRdfInput()) {
					if (equi.asOWLClass().equals(m2c)) {
						System.out.println("HEUREKA2");
					}
				}
			}
		}

	}
}
