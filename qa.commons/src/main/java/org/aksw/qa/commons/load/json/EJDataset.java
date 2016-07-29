package org.aksw.qa.commons.load.json;

public class EJDataset {
	private String id;
	private String metadata;

	public String getId() {
		return id;
	}

	public EJDataset setId(final String id) {
		this.id = id;
		return this;
	}

	public String getMetadata() {
		return metadata;
	}

	public EJDataset setMetadata(final String metadata) {
		this.metadata = metadata;
		return this;
	}

	@Override
	public String toString() {
		return "\n  ID: " + id + "\n  Metadata: " + metadata;
	}
}
