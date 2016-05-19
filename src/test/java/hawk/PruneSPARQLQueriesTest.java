package hawk;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.BGPisConnected;
import org.aksw.hawk.pruner.NumberOfTypesPerVariable;
import org.aksw.hawk.pruner.PredicatesPerVariableEdge;
import org.aksw.hawk.pruner.UnboundTriple;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class PruneSPARQLQueriesTest {
	Logger log = LoggerFactory.getLogger(PruneSPARQLQueriesTest.class);
	BGPisConnected gSCCPruner = new BGPisConnected();
	UnboundTriple unboundTriple = new UnboundTriple();
	PredicatesPerVariableEdge predicatesPerVariableEdge = new PredicatesPerVariableEdge();
	NumberOfTypesPerVariable numberOfTypesPerVariable = new NumberOfTypesPerVariable();

	@Test
	public void numberOfTypesperVariable() {

		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Bird>.");
		query.addConstraint("?proj <http://dbpedia.org/ontology/date> ?const.");
		query.addFilterOverAbstractsContraint("?proj", "protected");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Bird>.");
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Car>.");
		query.addFilterOverAbstractsContraint("?proj", "protected");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Bird>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Car>.");
		query.addFilterOverAbstractsContraint("?proj", "protected");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = numberOfTypesPerVariable.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		log.debug(Joiner.on("\n").join(queries));
		Assert.assertTrue(queries.size() == 2);

	}

	@Test
	public void predicatesPerVariableEdge() {

		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Bird>.");
		query.addFilterOverAbstractsContraint("?proj", "protected");
		query.addFilterOverAbstractsContraint("?proj", "National Parks and Wildlife Act");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?const <http://dbpedia.org/ontology/Film> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "stage");
		log.debug(query.toString());
		queries.add(query);
		query = new SPARQLQuery("?const <http://dbpedia.org/ontology/Film> ?proj.");
		query.addConstraint("?const <http://dbpedia.org/ontology/date> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "stage");
		log.debug(query.toString());
		queries.add(query);
		query = new SPARQLQuery("?const <http://dbpedia.org/ontology/Film> ?proj.");
		query.addConstraint("?const <http://dbpedia.org/ontology/board> ?proj.");
		query.addConstraint("?const <http://dbpedia.org/ontology/date> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "stage");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/RaceHorse>.");
		query.addConstraint("?const <http://dbpedia.org/ontology/raceHorse> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Long Fellow");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = predicatesPerVariableEdge.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		log.debug(Joiner.on("\n").join(queries));
		Assert.assertTrue(queries.size() == 3);

	}

	@Test
	public void disjointness() {
		// TODO fix long runtime
		// DisjointnessBasedQueryFilter disjoint = new
		// DisjointnessBasedQueryFilter(new
		// QueryExecutionFactoryHttp("http://139.18.2.164:3030/ds/sparql"));
		// Set<SPARQLQuery> queries = Sets.newHashSet();
		// SPARQLQuery query = new
		// SPARQLQuery("?proj a <http://dbpedia.org/ontology/Bird>.");
		// query.addFilterOverAbstractsContraint("?proj", "protected");
		// query.addFilterOverAbstractsContraint("?proj",
		// "National Parks and Wildlife Act");
		// log.debug(query.toString());
		// queries.add(query);
		//
		// log.debug("Size before pruning: " + queries.size());
		// queries = disjoint.prune(queries, new HAWKQuestion());
		// log.debug("Size after pruning: " + queries.size());
		// Assert.assertTrue(queries.size() == 1);

	}

	@Test
	public void isSCC() {
		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/influencedBy> ?proj. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/abstract> ?abstractconst. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Country>.");
		query.addConstraint("?const  ?p ?proj. ");
		query.addFilterOverAbstractsContraint("?const", "first known fotographer of snowflakes");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?a0 a <http://dbpedia.org/ontology/City>.");
		query.addConstraint("?a1 <http://dbpedia.org/ontology/birthPlace> ?a0.");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?a0 a <http://dbpedia.org/ontology/City>.");
		query.addConstraint("?a0 <http://dbpedia.org/ontology/birthPlace> ?a2.");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = gSCCPruner.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		Assert.assertTrue(queries.size() == 4);

	}

	@Test
	public void isNotSCC() {
		Set<SPARQLQuery> queries = Sets.newHashSet();

		SPARQLQuery query = new SPARQLQuery("?const a <http://dbpedia.org/ontology/Philosopher>.");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?proj", "influenced");
		log.debug(query.toString());
		queries.add(query);

		query = new SPARQLQuery("?a0 a <http://dbpedia.org/ontology/City>.");
		query.addConstraint("?a1 <http://dbpedia.org/ontology/birthPlace> ?a2.");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = gSCCPruner.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		Assert.assertTrue(queries.size() == 0);

	}

	@Test
	public void unboundTripleTest() {
		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/influencedBy> ?proj. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/abstract> ?abstractconst. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		queries.add(query);
		log.debug(query.toString());

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const ?p ?proj. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/abstract> ?abstractconst. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		queries.add(query);
		log.debug(query.toString());

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const ?p ?proj. ");
		query.addConstraint("?const ?pp ?abstractconst. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		queries.add(query);
		log.debug(query.toString());

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const ?p ?proj. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		queries.add(query);
		log.debug(query.toString());

		query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Family>.");
		query.addConstraint("?proj <http://dbpedia.org/ontology/deathPlace> ?const.");
		query.addConstraint("?const <http://dbpedia.org/ontology/deathPlace> ?proj.");
		query.addFilterOverAbstractsContraint("?const", "accident");
		queries.add(query);
		log.debug(query.toString());

		log.debug("Size before pruning: " + queries.size());
		queries = unboundTriple.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		Assert.assertTrue(queries.size() == 4);
	}

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
	// FIXME disjointness killt die richtige antwort für philosopher
	// ohne die filter werden mehr fragen richtig beantwortet aber das
	// programm zerbricht!!!!!!!!!!!!
	// scc scheint die böse Query durchzulassen und disjointness auch
	// virtuoso ist halt kacke
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11
	// FIXME influence of pruner?

}
