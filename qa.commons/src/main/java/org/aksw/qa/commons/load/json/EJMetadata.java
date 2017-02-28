package org.aksw.qa.commons.load.json;

public class EJMetadata {
	private Boolean aggregation;
	private Boolean onlydbo;
	private Boolean hybrid;

	public EJMetadata() {
		this.aggregation = null;
		this.onlydbo = null;
		this.hybrid = null;

	}

	public Boolean getAggregation() {
		return aggregation;
	}

	public Boolean getOnlydbo() {
		return onlydbo;
	}

	public Boolean getHybrid() {
		return hybrid;
	}

	public void setAggregation(final Boolean aggregation) {
		this.aggregation = aggregation;
	}

	public void setOnlydbo(final Boolean onlydbo) {
		this.onlydbo = onlydbo;
	}

	public void setHybrid(final Boolean hybrid) {
		this.hybrid = hybrid;
	}

}
