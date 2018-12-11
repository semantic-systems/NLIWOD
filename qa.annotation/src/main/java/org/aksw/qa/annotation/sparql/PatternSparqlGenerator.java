package org.aksw.qa.annotation.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedNamedEntity;
import org.aksw.gerbil.transfer.nif.data.TypedSpanImpl;
import org.aksw.qa.annotation.util.NifEverything;

public class PatternSparqlGenerator {

	private final static String NOT_DEFINED = "No pattern for those quantities of classes / properties / named entities available";

	private static PatternSparqlGenerator instance;
	private Integer limit = null;
	private NifEverything nif = NifEverything.getInstance();
	private SimpleQuantityRanker ranker = new SimpleQuantityRanker();

	private List<String> classes;
	private List<String> properties;
	private List<String> namedEntities;

	private final static String CLASS_PREFIX = "a dbo:";
	private final static String PROPERTY_PREFIX = "dbo:";
	private final static String NAMED_ENTITY_PREFIX = "dbr:";
	private final static String URI_START_PREFIX = "http://dbpedia.org/";
	private final static String PROJ = " ?proj ";
	private final static String PROJ2 = " ?proj2 ";

	private PatternSparqlGenerator() {

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

		return generateQuery(classUri, propertyUri, namedUri);

	}

	public String nifStrigToQuery(final String nifString) {
		List<Document> parsedDocs = nif.parseNIF(nifString);
		if (parsedDocs.size() != 1) {
			return "Cannot convert more than one nif at a time";
		}
		return nifToQuery(parsedDocs.get(0));
	}

	public String generateQuery(final List<String> classesIn, final List<String> propertiesIn, final List<String> namedEntitiesIn) {
		classes = emptyIfNull(classesIn);
		properties = emptyIfNull(propertiesIn);
		namedEntities = emptyIfNull(namedEntitiesIn);

		switch (classes.size()) {

		case 0:

			switch (properties.size()) {
			case 0:
				switch (namedEntities.size()) {
				case 0:
					return NOT_DEFINED;
				case 1:
					return "SELECT " + named(0).get() + " WHERE {}";
				case 2:
					// return "SELECT * WHERE { dbr:" + named(0) + " ?proj dbr:"
					// + named(1) + " . }";
					return construct(Querytype.SELECT, named(0), var(PROJ), named(1));

				default:
					return NOT_DEFINED;
				}
			case 1:
				switch (namedEntities.size()) {
				case 0:
					/**
					 * having only one property and nothing else doesnt make
					 * sense, does it?
					 */
					// return "SELECT * WHERE{ ?proj dbo:" + prop(0) + " ?proj2
					// . } ";
					return construct(Querytype.SELECT, var(PROJ), prop(0), var(PROJ2));
				case 1:
					// return "SELECT * WHERE{ ?proj dbo:" + prop(0) + " dbr:" +
					// named(0) + " . } ";
					return construct(Querytype.SELECT, named(0), prop(0), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, named(0), prop(0), var(PROJ), named(1), prop(0), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(Querytype.SELECT, var(PROJ), prop(0), var(PROJ2), var(PROJ), prop(1), var(PROJ2));
				case 1:
					return construct(Querytype.SELECT, named(0), prop(0), var(PROJ), named(0), prop(1), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, named(0), prop(0), var(PROJ), named(1), prop(1), var(PROJ));
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
					return construct(Querytype.SELECT, var(PROJ), clazz(0));
				case 1:
					return construct(Querytype.ASK, named(0), clazz(0));
				case 2:
					return construct(Querytype.ASK, named(0), clazz(0), named(1), clazz(0));
				default:
					return NOT_DEFINED;
				}

			case 1:
				switch (namedEntities.size()) {
				case 0:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), prop(0), var(PROJ2));
				case 1:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), named(0), prop(0), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(1), prop(0), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), prop(0), var(PROJ2), var(PROJ), prop(1), var(PROJ2));
				case 1:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(0), prop(1), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), named(0), prop(0), var(PROJ), named(1), prop(1), var(PROJ));
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
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1));
				case 1:
					return construct(Querytype.ASK, named(0), clazz(0), named(0), clazz(1));
				case 2:
					return construct(Querytype.ASK, named(0), clazz(0), named(1), clazz(1));
				default:
					return NOT_DEFINED;
				}
			case 1:
				switch (namedEntities.size()) {
				case 0:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), var(PROJ), prop(0), var(PROJ2));
				case 1:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ), named(1), prop(0), var(PROJ));
				default:
					return NOT_DEFINED;
				}
			case 2:
				switch (namedEntities.size()) {
				case 0:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), var(PROJ), prop(0), var(PROJ2), var(PROJ), prop(1), var(PROJ2));
				case 1:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ), named(0), prop(1), var(PROJ));
				case 2:
					return construct(Querytype.SELECT, var(PROJ), clazz(0), var(PROJ), clazz(1), named(0), prop(0), var(PROJ), named(1), prop(1), var(PROJ));
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
			out = "SELECT * WHERE{ " + query + " }";
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
		SELECT,
		ASK
	}

	class Querypart {
		private String part;

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
}