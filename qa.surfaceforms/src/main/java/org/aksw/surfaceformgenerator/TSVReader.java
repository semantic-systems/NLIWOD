package org.aksw.surfaceformgenerator;
//package org.aksw.agdistis.surfaceformgenerator;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Scanner;
//
///**
// * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
// * @author Diego Moussallem
// * @author Ricardo Usbeck <usbeck@informatik.uni-leipzig.de> Reader for the tab
// *         separated values format (a basic table format without escapings or
// *         anything where the rows are separated by tabulators).
// **/
//
//public class TSVReader implements Closeable {
//	final Scanner in;
//	String peekLine = null;
//
//	/**
//	 * Constructs a new TSVReader which produces values scanned from the
//	 * specified input stream.
//	 */
//	public TSVReader(File f) throws FileNotFoundException {
//		in = new Scanner(f);
//	}
//
//	public TSVReader(InputStream stream) throws FileNotFoundException {
//		in = new Scanner(stream);
//	}
//
//	@Override
//	public void close() throws IOException {
//		in.close();
//	}
//
//	public boolean hasNextTokens() {
//		if (peekLine != null)
//			return true;
//		if (!in.hasNextLine()) {
//			return false;
//		}
//		String line = in.nextLine().trim();
//		if (line.isEmpty()) {
//			return hasNextTokens();
//		}
//		this.peekLine = line;
//		return true;
//	}
//
//	public String[] nextTokens() {
//		if (!hasNextTokens())
//			return null;
//		String[] tokens = peekLine.split("[\\s\t]+");
//		// System.out.println(Arrays.toString(tokens));
//		peekLine = null;
//		return tokens;
//	}
//}