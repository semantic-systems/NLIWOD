package org.aksw.qa.commons.load.extended;

public class EJDataset {
	private Integer id;
	private String metadata;

	public Integer getId() {
		return id;
	}

	public EJDataset setId(final Integer id) {
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

}
