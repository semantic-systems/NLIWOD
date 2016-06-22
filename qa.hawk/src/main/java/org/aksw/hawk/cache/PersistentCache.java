package org.aksw.hawk.cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentCache {
	public static Logger log = LoggerFactory.getLogger(PersistentCache.class);
	public String charset = "UTF-8";
	public static String cacheLocation;
	public HashMap<String, String> cache;
	int i = 0;

	public PersistentCache() {
		cacheLocation = new File("cache/spotterCache").getAbsolutePath();
		log.debug("cacheLocation: " + cacheLocation);
		readCache();
	}

	/**
	 * Read the cache to a file
	 */
	public void readCache() {
		cache = new HashMap<String, String>();
		try {
			if (new File(cacheLocation).exists()) {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(cacheLocation), "UTF8"));
				String s = reader.readLine();
				while (s != null) {
					String input = s.split("\t")[0];
					String output = s.split("\t")[1];
					cache.put(input, output);
					s = reader.readLine();
				}
				reader.close();
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Write the cache to a file
	 */
	public void writeCache() {
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(cacheLocation)));
			for (String input : cache.keySet()) {
				writer.println(input + "\t" + cache.get(input));
			}
			writer.close();
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
	}

	public boolean containsKey(String input) {
		return cache.containsKey(input);
	}

	public String get(String input) {
		return cache.get(input);
	}

	public void put(String input, String output) {
		cache.put(input, output);
	}

	public int size() {
		return cache.size();
	}

}
