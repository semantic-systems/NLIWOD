package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.hawk.index.DBOIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public abstract class Module {
	public List<WhereClause> statementList = new ArrayList<>();
	public Logger log = LoggerFactory.getLogger(getClass());
	public DBOIndex dboIndex = new DBOIndex();
	
	public String toString(){
		return Joiner.on("\n").join(statementList);
	}
}
