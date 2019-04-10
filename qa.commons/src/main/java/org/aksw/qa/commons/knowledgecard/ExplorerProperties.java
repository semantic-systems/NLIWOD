package org.aksw.qa.commons.knowledgecard;

public class ExplorerProperties {
	private String className;
	private String property;
	private String score;

	public ExplorerProperties(String className, String property, String score) {
		super();
		this.className = className;
		this.property = property;
		this.score = score;
	}

	public String getClassName() {
		return className;
	}

	public ExplorerProperties setClassName(String className) {
		this.className = className;
		return this;
	}

	public String getProperty() {
		return property;
	}

	public ExplorerProperties setProperty(String property) {
		this.property = property;
		return this;
	}

	public String getScore() {
		return score;
	}

	public ExplorerProperties setScore(String score) {
		this.score = score;
		return this;
	}
}
