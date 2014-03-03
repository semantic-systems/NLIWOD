package org.aksw.autosparql.commons.qald.uri;

public class GoldEntity {

	/**
	 * 
	 */
	public String uri;
	public String label;

	public GoldEntity(String uri, String label) {

		this.uri = uri;
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
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
		GoldEntity other = (GoldEntity) obj;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return uri;
	}

}
