package org.aksw.qa.commons.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

import com.google.common.base.Joiner;

/**
 *
 * @author ricardousbeck
 *
 */
public class Entity implements Serializable, Comparable<Entity> {

	// TODO make those things private
	private static final long serialVersionUID = 7859357081713774767L;
	public String label = "";
	public String type = "";
	public List<Resource> posTypesAndCategories = new ArrayList<>();
	public List<Resource> uris = new ArrayList<>();
	private int offset;

	/**
	 *
	 * @param label
	 * @param type
	 */
	public Entity(final String label, final String type) {
		this.label = label;
		this.type = type;
	}

	/**
	* 
	*/
	public Entity() {
	}

	/*
	 * 
	 * Currently, two entities are compared by their labels
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Entity o) {
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
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (uris.get(0) == null) {
			if (other.uris.get(0) != null)
				return false;
		} else if (!uris.get(0).equals(other.uris.get(0)))
			return false;
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
		return label + "(uri: "+ Joiner.on(", ").join(uris) + "; type: "+ type + ")";
	}

	public int getOffset() {
	    return offset;
    }

	public void setOffset(int offset) {
	    this.offset = offset;
    }

}
