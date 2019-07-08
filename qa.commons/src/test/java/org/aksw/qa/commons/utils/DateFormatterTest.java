package org.aksw.qa.commons.utils;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DateFormatterTest {
	
	@Test
	public void testDateFormat() {
		String date = "1993-6-8";
		String result = "1993-06-08";
		assertTrue(result.equals(DateFormatter.formatDate(date)));
		
		date = "1993-13-8";
		result = "1994-01-08";
		assertTrue(result.equals(DateFormatter.formatDate(date)));
		
		date = "1993-10-67";
		result = "1993-12-06";
		assertTrue(result.equals(DateFormatter.formatDate(date)));
	}
}
