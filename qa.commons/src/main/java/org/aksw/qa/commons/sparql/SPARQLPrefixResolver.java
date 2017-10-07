package org.aksw.qa.commons.sparql;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.util.FmtUtils;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SPARQLPrefixResolver {
	private static Logger LOGGER = LoggerFactory.getLogger(SPARQLPrefixResolver.class);
	/**
	 * Refers to resource "prefixes.properties". Left had side is a prefix, right hand side is a corresponding URI.
	 * <p>
	 * Prefixes will be loaded during loading of enclosing class, via static constructor.
	 */
	private static Properties globalPrefixes = new Properties();
	/**
	 * This mapping will be used to refer to, if the local mapping (e.g.PREFIX declarations in the query itself) cannot resolve a prefix. Will be initialized with all prefixes in
	 * {@link #globalPrefixes}
	 */
	private static PrefixMapping globalPrefixMapping;

	static {
		try {
			globalPrefixes.load(SPARQLPrefixResolver.class.getClassLoader().getResourceAsStream("prefixes.properties"));

			globalPrefixMapping = new PrefixMappingImpl();

			if (globalPrefixes != null) {

				globalPrefixMapping.setNsPrefixes((Map) globalPrefixes);
			}

			LOGGER.debug("Loaded prefixes.properties. Key count: " + globalPrefixes.size());
		} catch (IOException e) {
			LOGGER.error("Couldn't find resource file: prefixes.properties", e);
		}
	}

	/**
	 * If you want to add/remove a prefix, this is your best bet.
	 *
	 * @return The global prefix mapping, possibly containing all from resource "prefixes.properties"
	 */
	public static PrefixMapping getGlobalPrefixMapping() {
		return globalPrefixMapping;
	}

	/**
	 * Adds the declaration of a prefix, if its used in the sparql query, but not declared. As global prefix source {@link #globalPrefixMapping} is used, which is initialized with data from resource
	 * "prefixes.properties"
	 * <p>
	 * In query defined prefixes override.
	 * <p>
	 * If uris are given in the query body which can be represented with a prefix, the uris are replaced with a prefix and a prefix declaration will be added. E.g.
	 * "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" is present, it will be replaced with "rdf:type", and prefix declaration for "rdf" will be added.
	 * <p>
	 * This will not remove unused prefix declarations.
	 * <p>
	 * if you wonder why keyword "a" is replaced, this is correct behavior. This is, because mapping for rdf: is set in a PrefixMap(its default in global).
	 *
	 * @param sparqlQuery
	 *            a sparql query
	 * @return the same sparql query, but with missing PREFIX declarations added.
	 */
	public static String addMissingPrefixes(final String sparqlQuery) {

		/**
		 * Create a custom two stage prefix mapping. Global mappings are those defined in prefixes.properties, local mappings are those present in the querystring. Single global mappings will be added
		 * to local mappings if 1) local cannot resolve it 2) global can resolve it, 3) it is deemed as a correct mapping for given uri by the parser.
		 */
		PrefixMapping2 pmap = new TwoStagePrefixMapping(globalPrefixMapping);

		/**
		 * Create empty query, set custom mapping
		 */
		Query q1 = QueryFactory.create();
		q1.setPrefixMapping(pmap);

		/**
		 * Parse the string. if e.g. rdf:type is present in query, but there was no ""PREFIX rdf:" , they will be resolved durig this process.
		 */
		q1 = QueryFactory.parse(q1, sparqlQuery, null, null);

		/**
		 * If uris are given in the actual query which can be represented with a prefix, the uris are replaced with a prefix and a prefix declaration will be added. E.g.
		 * "<http://www.w3.org/1999/02/22-rdf-syntax-ns#>" is present, it will be replaced with "rdf:", and prefix declaration for "rdf" will be added.
		 * <p>
		 * We have to call toString twice, because the serialization of the query {@Link org.apache.jena.sparql.core.Prologue} is done before serializing the query body (thus replacing URIS and
		 * setting prefixes in mapping). so, query gets replaced, but the already done prefix declaration strings are not modified. After first toString call, all prefixes are set in the mapping,
		 * allowing for adequate serialization now.
		 */
		q1.toString();

		return q1.toString();

	}

	/**
	 * Consists of two, a local and a global request mapping. If something is requested, the request goes first to the local mapping. If the local mapping cant respond, the global mapping is asked. If
	 * the global mapping has a valid answer, it will be written to the local mapping
	 */
	static class TwoStagePrefixMapping extends PrefixMapping2 {
		/**
		 * references to {@link FmtUtils.checkValidPrefixName(String)} Checks, if a prefixed string is valid. Unfortunately, this method is private. Well, a java hack aint a java hack without a little
		 * reflection ¯\_(ツ)_/¯
		 */
		private static Method checkValidPrefixMethod;
		static {
			try {
				checkValidPrefixMethod = FmtUtils.class.getDeclaredMethod("checkValidPrefixName", String.class);
				checkValidPrefixMethod.setAccessible(true);
			} catch (NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}
		}

		public TwoStagePrefixMapping(final PrefixMapping globalPrefixes) {
			super(globalPrefixes);

		}

		@Override
		public String getNsPrefixURI(final String prefix) {
			String s = super.getLocalPrefixMapping().getNsPrefixURI(prefix);
			if (s != null) {
				return s;
			}

			PrefixMapping pmapGlobal = super.getGlobalPrefixMapping();
			s = pmapGlobal.getNsPrefixURI(prefix);
			if (s != null) {
				super.getLocalPrefixMapping().setNsPrefix(prefix, s);
				return s;
			}
			return null;

		}

		@Override
		public String getNsURIPrefix(final String uri) {
			String s = super.getLocalPrefixMapping().getNsURIPrefix(uri);
			if (s != null) {
				return s;
			}
			PrefixMapping pmapGlobal = super.getGlobalPrefixMapping();
			if (pmapGlobal == null) {
				return null;
			}
			if (pmapGlobal != null) {
				s = pmapGlobal.getNsURIPrefix(uri);
			}
			super.getLocalPrefixMapping().setNsPrefix(s, uri);
			return null;
		}

		@Override
		public PrefixMapping removeNsPrefix(final String prefix) {
			super.getLocalPrefixMapping().removeNsPrefix(prefix);
			return this;
		}

		@Override
		public String expandPrefix(final String prefixed) {
			String s = super.getLocalPrefixMapping().expandPrefix(prefixed);
			PrefixMapping pmapGlobal = super.getGlobalPrefixMapping();
			if (pmapGlobal == null) {
				return s;
			}

			if (s == null || s.equals(prefixed)) {
				if (pmapGlobal != null) {
					s = pmapGlobal.expandPrefix(prefixed);
				}
				if (s != null) {
					int colon = prefixed.indexOf(':');
					String prefix = prefixed.substring(0, colon);
					String uri = pmapGlobal.getNsPrefixURI(prefix);
					super.getLocalPrefixMapping().setNsPrefix(prefix, uri);
				}
			}
			return s;
		}

		/** @see org.apache.jena.shared.PrefixMapping#shortForm(java.lang.String) */
		@Override
		public String shortForm(final String uri) {

			PrefixMapping pmapLocal = super.getLocalPrefixMapping();
			PrefixMapping pmapGlobal = super.getGlobalPrefixMapping();
			String s = pmapLocal.shortForm(uri);
			if (pmapGlobal == null) {
				return s;
			}

			if (s == null || s.equals(uri)) {
				s = pmapGlobal.shortForm(uri);
				if (s != null && !s.equals(uri)) {
					boolean b = false;
					try {
						b = (boolean) checkValidPrefixMethod.invoke(null, s);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (b) {
						String prefix = s.substring(0, s.indexOf(":"));
						pmapLocal.setNsPrefix(prefix, pmapGlobal.getNsPrefixURI(prefix));
					}
				}
			}

			return s;
		}

		/** @see org.apache.jena.shared.PrefixMapping#qnameFor(java.lang.String) */
		@Override
		public String qnameFor(final String uri) {
			PrefixMapping pmapLocal = super.getLocalPrefixMapping();
			PrefixMapping pmapGlobal = super.getGlobalPrefixMapping();
			String s = pmapLocal.qnameFor(uri);

			if (s != null) {
				return s;
			}

			if (pmapGlobal != null) {
				s = pmapGlobal.qnameFor(uri);
				if (s != null) {
					boolean b = false;
					try {
						b = (boolean) checkValidPrefixMethod.invoke(null, s);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (b) {
						String prefix = s.substring(0, s.indexOf(":"));
						pmapLocal.setNsPrefix(prefix, pmapGlobal.getNsPrefixURI(prefix));
					}
				}
			}
			return s;
		}

		@Override
		public Map<String, String> getNsPrefixMap() {
			return getNsPrefixMap(false);
		}

		@Override
		public Map<String, String> getNsPrefixMap(final boolean includeGlobalMap) {
			return super.getNsPrefixMap(false);
		}

		@Override
		public String toString() {
			return "LocalMapping: " + super.getLocalPrefixMapping().toString();
		}
	}

	public static void main(final String[] args) throws Exception {

		String prefixQuery = "SELECT DISTINCT ?uri WHERE { ?uri a dbo:Musical . ?uri dbo:musicBy <http://dbpedia.org/resource/Category:Critically_endangered_animals> .}";

		System.out.println(SPARQLPrefixResolver.addMissingPrefixes(prefixQuery));
	}
}
