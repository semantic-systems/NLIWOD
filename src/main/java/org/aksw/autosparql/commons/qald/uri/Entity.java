package org.aksw.autosparql.commons.qald.uri;

import java.util.ArrayList;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Resource;

public class Entity implements Comparable<Entity> {
	public String label = "";
	public String type = "";
	public List<Resource> uris = new ArrayList<Resource>();
	public List<Resource> types = new ArrayList<Resource>();

	public String toString() {
		return label + "(" + uris.get(0) + ")";
	}

	@Override
	public int compareTo(Entity o) {
		int thisLength = label.length();
		int otherLength = o.label.length();
		if (thisLength < otherLength) {
			return -1;
		}
		if (thisLength > otherLength) {
			return 1;
		}
		return 0;
	}

}
