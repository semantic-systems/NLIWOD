package org.aksw.surfaceformgenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Diego Moussallem
 * @author Ricardo Usbeck <usbeck@informatik.uni-leipzig.de>
 */
public class NtripleUtil {

	private static Logger log = LoggerFactory.getLogger(NtripleUtil.class);

	public static List<String[]> getSubjectAndObjectsFromNTriple(String filename, String replacePrefix) {

		List<String[]> results = new ArrayList<String[]>();

		PipedRDFIterator<Triple> iter = fileToStreamIterator(filename);

		while (iter.hasNext()) {
			Triple statement = iter.next();
			results.add(new String[] {
					replacePrefix == null || "".equals(replacePrefix) ? statement.getSubject().getURI()
							: statement.getSubject().getURI().replace(replacePrefix, ""),
					replacePrefix == null || "".equals(replacePrefix) ? statement.getObject().getURI()
							: statement.getObject().getURI().replace(replacePrefix, ""), });

		}

		iter.close();
		return null;

		// return results;
	}

	public static List<String> getSubjectsFromNTriple(String filename, String replacePrefix) {

		List<String> results = new ArrayList<String>();

		PipedRDFIterator<Triple> iter = fileToStreamIterator(filename);
		int i = 0;
		while (iter.hasNext()) {
			Triple statement = iter.next();
			i++;
			if (i % 10000 == 0) {
				log.debug("still reading " + filename);
			}
			results.add(replacePrefix == null || replacePrefix.equals("") ? statement.getSubject().getURI()
					: statement.getSubject().getURI().replace(replacePrefix, ""));

			
		}
		iter.close();

		return results;
	}

	private static PipedRDFIterator<Triple> fileToStreamIterator(String filename) {
		PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
		final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);

		// PipedRDFStream and PipedRDFIterator need to be on different threads
		ExecutorService executor = Executors.newSingleThreadExecutor();

		// Create a runnable for our parser thread
		Runnable parser = new Runnable() {

			@Override
			public void run() {
				RDFDataMgr.parse(inputStream, filename);
			}
		};

		// Start the parser on another thread
		executor.submit(parser);
		// We will consume the input on the main thread here
		// We can now iterate over data as it is parsed, parsing only runs as
		// far ahead of our consumption as the buffer size allows
		return iter;
	}

}