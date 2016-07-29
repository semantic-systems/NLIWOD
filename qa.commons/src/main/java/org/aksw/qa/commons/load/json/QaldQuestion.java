package org.aksw.qa.commons.load.json;

public class QaldQuestion {

	private String language;
	private String string;
	private String keywords;

	public String getLanguage() {
		return language;
	}

	public String getString() {
		return string;
	}

	public String getKeywords() {
		return keywords;
	}

	public QaldQuestion setLanguage(final String language) {
		this.language = language;
		return this;
	}

	public QaldQuestion setString(final String string) {
		this.string = string;
		return this;
	}

	public QaldQuestion setKeywords(final String keywords) {
		this.keywords = keywords;
		return this;
	}

	@Override
	public String toString() {
		return "\n    " + language + " : " + string + "\n    Keywords: " + keywords;
	}

}
