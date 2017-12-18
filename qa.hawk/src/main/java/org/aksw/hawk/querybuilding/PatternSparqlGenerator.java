package org.aksw.hawk.querybuilding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedSpanImpl;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.Annotater;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.qa.annotation.sparql.SimpleQuantityRanker;
import org.aksw.qa.annotation.util.NifEverything;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.sparql.SPARQL;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;

import com.google.common.collect.Lists;

public class PatternSparqlGenerator implements ISparqlBuilder {

	private final static String NOT_DEFINED = "No pattern for those quantities of classes / properties / named entities available";

	private static PatternSparqlGenerator instance;
	private Integer limit = null;
	private NifEverything nif = NifEverything.getInstance();
	private SimpleQuantityRanker ranker = new SimpleQuantityRanker();

	private List<String> classes;
	private List<String> properties;
	private List<String> namedEntities;
	private List<String> nounPhrases;

	private final static String CLASS_PREFIX = "a dbo:";
	private final static String PROPERTY_PREFIX = "dbo:";
	private final static String NAMED_ENTITY_PREFIX = "dbr:";
	private final static String URI_START_PREFIX = "http://dbpedia.org/";
	private final static String PROJ = "?proj ";
	private final static String PROJ2 = " ?proj2 ";

	public PatternSparqlGenerator() {

	}

	public static PatternSparqlGenerator getInstance() {
		if (instance == null) {
			instance = new PatternSparqlGenerator();
		}
		return instance;
	}

	public String nifToQuery(final Document doc) {
		List<Marking> markings = doc.getMarkings();
		List<String> classUri = new ArrayList<>();
		List<String> propertyUri = new ArrayList<>();
		List<String> namedUri = new ArrayList<>();
		List<String> nounpUri = new ArrayList<>();

		for (Marking marking : markings) {
			if (marking instanceof TypedSpanImpl) {
				// taclassref

				classUri.add(ranker.rank(((TypedSpanImpl) marking).getTypes()));
			} else if (marking instanceof TypedNamedEntity) {
				// annotated with TaClassRef AND taIdentRef

				TypedNamedEntity typednamed = (TypedNamedEntity) marking;
				boolean foundNamed = false;
				for (String uri : typednamed.getUris()) {
					if (uri.contains("dbpedia.org/resource")) {
						namedUri.add(uri);
						foundNamed = true;
						break;
					}
				}
				if (!foundNamed) {
					List<String> uris = new ArrayList<>();
					uris.addAll(typednamed.getUris());
					uris.addAll(typednamed.getTypes());
					String ranked = ranker.rank(uris);
					if (ranker.disambiguateOntologyIsProperty(ranked)) {
						propertyUri.add(ranked);
					} else {
						classUri.add(ranked);
					}
				}

				classUri.add(typednamed.getTypes().iterator().next());

			} else if (marking instanceof NamedEntity) {
				// taIdentRef properties
				NamedEntity named = (NamedEntity) marking;
				String uri = named.getUris().iterator().next();
				if (uri.contains("dbpedia.org/resource")) {
					namedUri.add(uri);
				} else {
					propertyUri.add(uri);
				}

			}

		}
		Boolean isASK = false;

		return generateQuery(classUri, propertyUri, namedUri, nounpUri, isASK);

	}

	public String nifStrigToQuery(final String nifString) {
		List<Document> parsedDocs = nif.parseNIF(nifString);
		if (parsedDocs.size() != 1) {
			return "Cannot convert more than one nif at a time";
		}
		return nifToQuery(parsedDocs.get(0));
	}

	public String generateQuery(final List<String> classesIn, final List<String> propertiesIn,
			final List<String> namedEntitiesIn, List<String> nounPhrasesIn, Boolean isASK) {
		classes = emptyIfNull(classesIn);
		properties = emptyIfNull(propertiesIn);
		namedEntities = emptyIfNull(namedEntitiesIn);
		nounPhrases = emptyIfNull(nounPhrasesIn);
		Querytype type;
		if (isASK != null && isASK == true)
			type = Querytype.ASK;
		else
			type = Querytype.SELECT;

		switch (classes.size()) {

		case 0:

			switch (properties.size()) {
			case 0:
				switch (namedEntities.size()) {
				case 0:
					switch (nounPhrases.size()) {
					case 0:
						return NOT_DEFINED;
					case 1:
						return "SELECT ?uri { ?uri <http://jena.apache.org/text#query> " + nounp(0) + " }";
					default:
						return NOT_DEFINED;
					}
				case 1:
					// return "SELECT " + named(0).get() + " WHERE {}";
					return NOT_DEFINED;
				case 2:
					// return "SELECT * WHERE { dbr:" + named(0) + " ?proj dbr:"
					// + named(1) + " . }";

					return construct(type, named(0), var(PROJ), named(1));

				default:
					return NOT_DEFINED;
				}
			case 1:
				switch (namedEntities.size()) {
				case 0:
					/**
					 * having only one property and nothing else doesnt make sense, does it?
					 */
					// return "SELECT * WHERE{ ?proj dbo:" + prop(0) + " ?proj2
					// . } ";

					return construct(type, var(PROJ), prop(0), var(PROJ2));
				case 1:
					switch (nounPhrases.size()) {
					case 0:
						// return "SELECT * WHERE{ ?proj dbo:" + prop(0) + " dbr:" +
						// named(0) + " . } ";
						return construct(type, named(0), prop(0), var(PROJ));
					case 1:
						return "SELECT ?uri { ?uri <" + properties.get(0) + ">  <" + namedEntities.get(0)
								+ "> . ?uri <http://jena.apache.org/text#query> " + nounp(0) + " }";
					default:
						return NOT_DEFINED;
					}
					// return "SELECT * WHERE{ ?proj dbo:" + prop(0) + " dbr:" +
					// named(0) + " . } ";
					// return construct(type, named(0), prop(0), var(PROJ));
				case 2:
					return construct(type, named(0), prop(0), var(PROJ), named(1), prop(0), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), prop(0), var(PROJ2), var(PROJ), prop(1), var(PROJ2));
				case 1:
					return construct(type, named(0), prop(0), var(PROJ), named(0), prop(1), var(PROJ));
				case 2:
					return construct(type, named(0), prop(0), var(PROJ), named(1), prop(1), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			default:
				return NOT_DEFINED;

			}
		case 1:
			switch (properties.size()) {
			case 0:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0));
				case 1:
					return construct(type, named(0), clazz(0));
				case 2:
					return construct(type, named(0), clazz(0), named(1), clazz(0));
				default:
					return NOT_DEFINED;
				}

			case 1:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0), var(PROJ), prop(0), var(PROJ2));
				case 1:
					return construct(type, var(PROJ), clazz(0), named(0), prop(0), var(PROJ));
				case 2:
					return construct(type, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(1), prop(0),
							var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0), var(PROJ), prop(0), var(PROJ2), var(PROJ), prop(1),
							var(PROJ2));
				case 1:
					return construct(type, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(0), prop(1),
							var(PROJ));
				case 2:
					return construct(type, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(1), prop(1),
							var(PROJ));
				default:
					return NOT_DEFINED;
				}
			default:
				return NOT_DEFINED;
			}
		case 2:
			switch (properties.size()) {
			case 0:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1));
				case 1:
					return construct(type, named(0), clazz(0), named(0), clazz(1));
				case 2:
					return construct(type, named(0), clazz(0), named(1), clazz(1));
				default:
					return NOT_DEFINED;
				}
			case 1:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), var(PROJ), prop(0), var(PROJ2));
				case 1:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ));
				case 2:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ),
							named(1), prop(0), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), var(PROJ), prop(0), var(PROJ2),
							var(PROJ), prop(1), var(PROJ2));
				case 1:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ),
							named(0), prop(1), var(PROJ));
				case 2:
					return construct(type, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ),
							named(1), prop(1), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			default:
				return NOT_DEFINED;
			}

		default:
			return NOT_DEFINED;
		}

	}

	private <T> List<T> emptyIfNull(final List<T> coll) {
		if (coll == null) {
			return new ArrayList<>();
		}
		return coll;
	}

	private Querypart clazz(final int index) {
		String uri = classes.get(index);
		if (uri.startsWith(URI_START_PREFIX)) {
			return new Clazz(" a <" + uri + ">");
		}
		return new Clazz(CLASS_PREFIX + uri);
	}

	private Querypart prop(final int index) {
		String uri = properties.get(index);
		if (uri.startsWith(URI_START_PREFIX)) {
			return new Property("<" + uri + ">");
		}
		return new Property(PROPERTY_PREFIX + uri);
	}

	private Querypart named(final int index) {
		String uri = namedEntities.get(index);
		if (uri.startsWith(URI_START_PREFIX)) {
			return new Named("<" + uri + ">");
		}
		return new Named(NAMED_ENTITY_PREFIX + uri);
	}

	private String nounp(final int index) {
		String uri = "\'" + nounPhrases.get(index) + "\'";
		return uri;
	}

	private Querypart var(final String var) {
		return new Variable(var);
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

	private String construct(final Querytype type, final Querypart... queryparts) {
		List<Querypart> input = Arrays.asList(queryparts);

		int partCntr = 1;
		String query = "";
		for (Querypart it : input) {

			query += " " + it.get();

			if ((partCntr == 3) || (it instanceof Clazz)) {
				query += " . ";
				partCntr = 0;
			}
			partCntr++;

		}
		String out = "";
		switch (type) {
		case SELECT:
			out = "SELECT ?proj WHERE{ " + query + " }";
			break;

		case ASK:
			out = "ASK { " + query + " } ";
			break;
		default:
			return NOT_DEFINED;
		}
		if (limit != null) {
			out = out + " LIMIT " + limit;
		}

		return out;

	}

	enum Querytype {
		SELECT, ASK
	}

	class Querypart {
		String part;

		public Querypart(final String part) {
			this.part = part;
		}

		public String get() {
			return part;
		}

	}

	class Clazz extends Querypart {
		public Clazz(final String part) {
			super(part);

		}
	}

	class Property extends Querypart {
		public Property(final String part) {
			super(part);
		}
	}

	class Named extends Querypart {
		public Named(final String part) {
			super(part);
		}
	}

	class Variable extends Querypart {
		public Variable(final String part) {
			super(part);
		}
	}

	public String getLink(String str) {
		str = str.replaceAll(".*(?=http://)", "");
		str = str.replaceAll("\\;.*$", "");
		return str;
	}

	public String getWordAfterLastSlash(String str) {
		str = getLink(str);
		str = str.replaceAll(".*/", "");
		return str;
	}

	@Override
	public List<Answer> build(HAWKQuestion q) throws ExecutionException, RuntimeException {
		SPARQL sparql = new SPARQL("http://131.234.28.52:3030/ds/sparql");
		Annotater annotator = new Annotater(sparql);
		annotator.annotateTree(q);
		
		List<String> namedEntitiesIn = new ArrayList<String>();
		List<String> nounPhrasesIn = new ArrayList<String>();
		List<String> classes = annotator.classesIn;
		List<String> properties = annotator.propertiesIn;
		List<String> classesIn = new ArrayList<>();
		List<String> propertiesIn = new ArrayList<>();
		
		//Extract shortest distinct strings from the list of properties
		//String smallest = "";
		//Collections.sort(properties);
		//Collections.sort(classes);
		//TODO Rricha write test case if zero properties, classes or entities found, that this class still works
		String base = null ;
		int smallestIndex =0;
			if(properties!=null&&properties.size()>0) {
				base =  properties.get(0);
			if (properties.size() > 1 && properties.get(0).isEmpty()) {
				smallestIndex = 1;
				base = properties.get(1);
			}
			for (int i = smallestIndex + 1; i <properties.size(); i++) {
				if (properties.get(i).length() < base.length() ) {
					smallestIndex = i;
					base = properties.get(i);
				}
			}
			if (!base.isEmpty())
				propertiesIn.add(base);
		}
			
//		if(!properties.isEmpty()) {
//			base = properties.get(0);
//		}
//		for (int i = 1; i < properties.size(); i++) {
//			String bstr = base, pstr = properties.get(i);
//			bstr= getWordAfterLastSlash(bstr);
//			pstr = getWordAfterLastSlash(pstr); 
//			
//			if (!base.equals("") && !pstr.toLowerCase().contains(bstr.toLowerCase())) {
//				propertiesIn.add(base);
//				base = properties.get(i);
//			}
//			else if(base.equals(""))
//				base = properties.get(i);
//		}
//		
//		if (base != null && !base.equals("")) {
//			propertiesIn.add(base);
//			properties = annotator.rank(propertiesIn);
//			if (!properties.isEmpty() && properties.get(0) != "")
//				propertiesIn = properties; 
//		}
		
		//Extract shortest distinct string from the list of classes
		base = null; 
		if (classes.size() != 0)
			base = classes.get(0);
		smallestIndex =0;
		if (classes.size() > 1 && classes.get(0).isEmpty()) {
			smallestIndex = 1;
			base = classes.get(1);
		}
		for (int i = smallestIndex + 1; i <classes.size(); i++) {
			if (classes.get(i).length() < base.length() ) {
				smallestIndex = i;
				base = classes.get(i);
			}
		}
		if ( base != null &&!base.isEmpty() )
			classesIn.add(base);
//		base = null;
//		if (classes.size() != 0) {
//			base = classes.get(0);
//		}
//		for (int i = 1; i < classes.size(); i++) {
//			String bstr = base, cstr = classes.get(i);
//			bstr= getWordAfterLastSlash(bstr);
//			cstr = getWordAfterLastSlash(cstr); 
//			
//			if (!base.equals("") && !cstr.toLowerCase().contains(bstr.toLowerCase())) {
//				classesIn.add(base);
//				base = classes.get(i);
//			}
//			else if(base.equals(""))
//				base = classes.get(i);
//		}
//		if (base != null)
//			classesIn.add(base);
		
		
		//if a label appears in both properties and classes, keep the label in properties
		for (int i=0, k=0; i < propertiesIn.size() && k < classesIn.size() ; ) {
			if (propertiesIn.get(i).toLowerCase().equals(classesIn.get(k).toLowerCase())) {
				classesIn.remove(classesIn.get(k));
				i++;
			}
			else {i++; k++;}
		}
		
		
		//Extract NamedEntitiesIn and nounPhrases
		List<Entity> namedEntity =  q.getLanguageToNamedEntites().get("en");
		List<Entity> nounPhrases = q.getLanguageToNounPhrases().get("en");
		
		if (namedEntity != null) {
			for (Iterator<Entity> litr = namedEntity.iterator(); litr.hasNext(); ) {
				Entity ent = litr.next();
				String str = ent.toString();
				namedEntitiesIn.add(getLink(str));
			}
			
		}
		
		if (nounPhrases != null) {
			for (Iterator<Entity> litr = nounPhrases.iterator(); litr.hasNext(); ) {
				Entity ent = litr.next();
				String str = ent.toString();
				nounPhrasesIn.add(getWordAfterLastSlash(str));
			}
			
		}
		
		Boolean isASK = q.getIsClassifiedAsASKQuery();
		
		String queryString = generateQuery(classesIn, propertiesIn, namedEntitiesIn, nounPhrasesIn, isASK);
		List<Answer> answer = Lists.newArrayList();
			// build sparql queries

			// identify the cardinality of the answers
		//	FIXME implement cardinality check		
		//		int cardinality = cardinality(q, queryStrings);
		Answer a = new Answer();
		if (queryString == "No pattern for those quantities of classes / properties / named entities available")
			return answer;
		//System.out.println(queryString);
		
		//FIXME Rricha, here goes the output as json, best would be to return this or add this to Answer object
		CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend("./sparql", true, 1000000);
		QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql").config().withCache(cacheFrontend).end().create();
		QueryExecution qe = qef.createQueryExecution(queryString);
		ResultSet resultSet = qe.execSelect();
		ResultSetFormatter.outputAsJSON(resultSet);
		
		a.answerSet = sparql.sparql(queryString);
		a.queryString = queryString;
		q.setSparqlQuery("en", queryString);
		a.question_id = q.getId();
		a.question = q.getLanguageToQuestion().get("en").toString();
		if (!a.answerSet.isEmpty()) {
			answer.add(a);
		}
		//System.out.println(answer);
		return answer;
	}

	// public static void main(final String[] args) {
	// PatternSparqlGenerator gen = PatternSparqlGenerator.getInstance();
	// List<String> cStr = new ArrayList<>(Arrays.asList("class1", "class2",
	// "dummy"));
	// List<String> pStr = new ArrayList<>(Arrays.asList("property1",
	// "property2", "dummy"));
	// List<String> nStr = new ArrayList<>(Arrays.asList("namedEntity1",
	// "namedEntity2", "dummy"));
	//
	// List<String> classes = new ArrayList<>();
	// List<String> properties = new ArrayList<>();
	// List<String> namedEntities = new ArrayList<>();
	// for (int i = 0; i < 3; i++) {
	// for (int j = 0; j < 3; j++) {
	// for (int k = 0; k < 3; k++) {
	// properties, " + k + " namedEntitites");
	// System.out.println(gen.generateQuery(classes, properties,
	// namedEntities));
	// System.out.println("\r\n");
	//
	// namedEntities.add(nStr.get(0));
	// nStr.remove(0);
	// }
	// nStr.addAll(namedEntities);
	// namedEntities.clear();
	//
	// properties.add(pStr.get(0));
	// pStr.remove(0);
	// }
	// pStr.addAll(properties);
	// properties.clear();
	//
	// classes.add(cStr.get(0));
	// cStr.remove(0);
	// }System.out.println("index: " + annotator.index);
	//
	// }
	// public static void main(final String[] args) {
	// List<IQuestion> questions =
	// LoaderController.load(Dataset.QALD6_Train_Multilingual);
	// IndexDBO_classes classIndex = new IndexDBO_classes();
	// IndexDBO_properties propertyindex = new IndexDBO_properties();
	// NifEverything nif = NifEverything.getInstance();
	// Spotlight spotlight = new Spotlight();
	// for (IQuestion question : questions) {
	// String q = question.getLanguageToQuestion().get("en");
	// System.out.println("classesSearch");
	// String classNif = nif.createNIFResultFromIndexDBO(q, classIndex,
	// NifProperty.TACLASSREF);
	// System.out.println("PropertySearch");
	// String propNif = nif.appendNIFResultFromIndexDBO(classNif, propertyindex,
	// NifProperty.TAIDENTREF);
	// System.out.println("Spotlight");
	// String nerNif = nif.appendNIFResultFromSpotters(propNif, spotlight);
	//
	// System.out.println("Ranking and generation");
	// System.out.println(new PatternSparqlGenerator().nifStrigToQuery(nerNif));
	//
	// }
	//
	// }

}