package org.aksw.hawk.nlp;

import java.util.List;
import java.util.Map;

import org.aksw.autosparql.commons.qald.uri.Entity;

public interface NERD_module {

	public abstract Map<String, List<Entity>> getEntities(String question);

}