package org.aksw.qa.commons.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

	
	public static String formatDate(String date){
		SimpleDateFormat parser1 = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat parser2 = new SimpleDateFormat("yyyy-MM-ddX");
		Date format; 
		try{
			format = parser1.parse(date);
			return parser1.format(format);
		}
		catch(Exception e){}
		try{
			format = parser2.parse(date);
			return parser1.format(format);
		}catch(Exception e1){}
		return date.trim();
		
	}
}
