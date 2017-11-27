package org.aksw.hawk.querybuilding;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;

public interface ISparqlBuilder {

	List<Answer> build(HAWKQuestion q) throws ExecutionException;

}