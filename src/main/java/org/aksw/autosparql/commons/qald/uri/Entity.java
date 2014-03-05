/**
 * 
 */
package org.aksw.autosparql.commons.qald.uri;

import java.util.ArrayList;
import java.util.List;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * @author gerb
 * 
 */
public class Entity {
	public String label = "";
	public String type = "";
	public List<Resource> uris = new ArrayList<Resource>();
	public List<Resource> types = new ArrayList<Resource>();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}

	public Entity() {
	}

	/**
	 * 
	 * @param label
	 * @param type
	 */
	public Entity(String label, String type) {

		this.label = label;
		this.type = type;
	}

	public String toString() {

		return label + "(" +uris.get(0) + ")";
	}

}
