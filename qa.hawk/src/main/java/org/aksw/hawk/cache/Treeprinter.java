package org.aksw.hawk.cache;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.apache.jena.atlas.logging.Log;

public class Treeprinter {
	public boolean ready;
	BufferedWriter bwStanford;
	BufferedWriter bwClearnlp;

	public Treeprinter() {
		ready = initialize();
	}

	public String printTreeStanford(HAWKQuestion q) {

		String treeString = q.getTree().toString();
		if (ready) {
			try {

				bwStanford.write(q.getLanguageToQuestion().toString());
				bwStanford.newLine();
				bwStanford.write(treeString);
				bwStanford.newLine();
				bwStanford.newLine();
				bwStanford.flush();

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else
			Log.debug(Treeprinter.class, "Printer not ready!");
		return treeString;

	}

	public String printTreeStanford(HAWKQuestion q, String s) {

		String treeString = s;
		if (ready) {
			try {

				bwStanford.write(q.getLanguageToQuestion().toString());
				bwStanford.newLine();
				bwStanford.write(treeString);
				bwStanford.newLine();
				bwStanford.newLine();
				bwStanford.flush();

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else
			Log.debug(Treeprinter.class, "Printer not ready!");
		return treeString;

	}

	public String printTreeClearnlp(HAWKQuestion q) {
		String treeString = q.getTree().toString();
		if (ready) {
			try {

				bwClearnlp.write(q.getLanguageToQuestion().toString());
				bwClearnlp.newLine();
				bwClearnlp.write(q.getTree().toString());
				bwClearnlp.newLine();
				bwClearnlp.newLine();
				bwClearnlp.flush();

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} else
			Log.debug(Treeprinter.class, "Printer not ready!");
		return treeString;
	}

	boolean initialize() {
		try {

			SimpleDateFormat sdfDate = new SimpleDateFormat("dd.MM_HH.mm");// dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			bwStanford = new BufferedWriter(new FileWriter("stanford" + strDate + ".txt", true));
			bwClearnlp = new BufferedWriter(new FileWriter("clearnlp" + strDate + ".txt", true));
		} catch (IOException ioe) {
			ioe.printStackTrace();
			return false;
		}
		return true;

	}

	public void close() {
		closeStanford();
		closeClearnlp();
	}

	public void closeStanford() {
		if (bwStanford != null)
			try {
				bwStanford.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
	}

	public void closeClearnlp() {
		if (bwClearnlp != null)
			try {
				bwClearnlp.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
	}

}
