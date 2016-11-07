package org.aksw.qa.annotation.index;

import java.util.ArrayList;

public interface IndexDBO {
	ArrayList<String> search(final String object);

	void close();
}