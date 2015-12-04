package org.aksw.qa.commons.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

/**
 *
 * @author ricardousbeck
 *
 */
public class Entity implements Serializable, Comparable<Entity> {

	private static final long serialVersionUID = 7859357081713774767L;
	public String label = "";
	public String type = "";

	public List<Resource> uris = new ArrayList<Resource>();

	/**
	 *
	 * @param label
	 * @param type
	 */
	public Entity(String label, String type) {
		this.label = label;
		this.type = type;
	}

	/*
	 * 
	 * Currently, two entities are compared by their labels
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return label + "(" + type + ")";
	}

}
