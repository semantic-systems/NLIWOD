package org.aksw.qa.commons.load.extended;

public class EJAnnotation {
	private Integer char_begin;
	private Integer char_end;
	private String URI;
	private String type;

	public Integer getChar_begin() {
		return char_begin;
	}

	public EJAnnotation setChar_begin(final Integer char_begin) {
		this.char_begin = char_begin;
		return this;
	}

	public Integer getChar_end() {
		return char_end;
	}

	public EJAnnotation setChar_end(final Integer char_end) {
		this.char_end = char_end;
		return this;
	}

	public String getURI() {
		return URI;
	}

	public EJAnnotation setURI(final String uRI) {
		URI = uRI;
		return this;
	}

	public String getType() {
		return type;
	}

	public EJAnnotation setType(final String type) {
		this.type = type;
		return this;
	}

}
