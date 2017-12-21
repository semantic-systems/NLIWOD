package org.aksw.hawk.querybuilding;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.json.simple.parser.ParseException;

//FIXME RRicha this class should also be implemented by the old recursive query checking 
public interface ISparqlBuilder {

	List<Answer> build(HAWKQuestion q) throws ExecutionException, RuntimeException, ParseException;

}