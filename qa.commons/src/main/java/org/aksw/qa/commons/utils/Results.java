package org.aksw.qa.commons.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Results implements Iterator<List<String>> {

	private int row = 0;
	public List<String> header = new LinkedList<String>();
	public List<List<String>> table = new LinkedList<List<String>>();

	@Override
	public boolean hasNext() {
		if (row < table.size()) {
			return true;
		}
		return false;
	}

	@Override
	public List<String> next() {
		return table.get(row++);
	}

	public List<String> getHeader() {
		return header;
	}

	public Set<String> getStringSet() {
		Set<String> ret = CollectionUtils.newHashSet();
		for (int i = 0; i < table.size(); i++) {
			for (int j = 0; j < table.get(i).size(); j++) {
				ret.add(table.get(i).get(j));
			}
		}
		return ret;
	}

	@Override
    public void remove() {
	    // TODO Auto-generated method stub
	    
    }
}
