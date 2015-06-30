package org.aksw.hawk.cache;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageHelper.class);

	/**
	 * Reads and deserializes the object from the file with the given name. Note
	 * that only the first object will be read from file.
	 * 
	 * @param filename
	 * @return The read object.
	 * @throws IOException
	 *             if an IO error occurs
	 * @throws ClassNotFoundException
	 *             if the class inside the file can't be found
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T readFromFile(String filename) throws IOException, ClassNotFoundException {
		T object = null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(filename);
			ois = new ObjectInputStream(fis);
			object = (T) ois.readObject();
		} finally {
			try {
				ois.close();
				fis.close();
			} catch (Exception e) {
				// nothing to do
			}
		}
		return object;
	}

	/**
	 * Reads and deserializes the object from the file with the given name. Note
	 * that only the first object will be read from file.
	 * 
	 * @param filename
	 * @return The read object or null if an error occurred.
	 */
	public static <T extends Serializable> T readFromFileSavely(String filename) {
		T object = null;
		try {
			object = readFromFile(filename);
		} catch (Exception e) {
			LOGGER.error("Couldn't load object from file (\"" + filename + "\").", e);
		}
		return object;
	}

	/**
	 * Serializes and stores the given object in the file with the given
	 * filename.
	 * 
	 * @param object
	 * @param filename
	 * @throws IOException
	 *             if an IO error occurs
	 */
	public static <T extends Serializable> void storeToFile(T object, String filename) throws IOException {
		ObjectOutputStream oout = null;
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(filename);
			oout = new ObjectOutputStream(fout);
			oout.writeObject(object);
		} finally {
			try {
				oout.close();
				fout.close();
			} catch (Exception e) {
				LOGGER.error(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * Serializes and stores the given object in the file with the given
	 * filename.
	 * 
	 * @param object
	 * @param filename
	 * @return false if an error occurred, else true
	 */
	public static <T extends Serializable> boolean storeToFileSavely(T object, String filename) {
		if (object == null) {
			LOGGER.error("Can't serialize null.");
			return false;
		}
		try {
			storeToFile(object, filename);
			return true;
		} catch (Exception e) {
			LOGGER.error("Couldn't store " + object.getClass().getSimpleName() + " object to file.", e);
			return false;
		}
	}
}