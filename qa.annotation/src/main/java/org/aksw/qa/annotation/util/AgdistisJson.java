package org.aksw.qa.annotation.util;

public class AgdistisJson {

	private String disambiguatedURL;
	private int offset;
	private String namedEntity;
	private int start;

	public String getDisambiguatedURL() {
		return disambiguatedURL;
	}

	public int getOffset() {
		return offset;
	}

	public String getNamedEntity() {
		return namedEntity;
	}

	public int getStart() {
		return start;
	}

	public void setDisambiguatedURL(final String disambiguatedURL) {
		this.disambiguatedURL = disambiguatedURL;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public void setNamedEntity(final String namedEntity) {
		this.namedEntity = namedEntity;
	}

	public void setStart(final int start) {
		this.start = start;
	}
}