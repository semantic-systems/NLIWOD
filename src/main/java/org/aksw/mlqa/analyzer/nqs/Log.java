package org.aksw.mlqa.analyzer.nqs;


public class Log {

	public static void d(String tag, String value){
		System.out.println(tag+":"+value);
	}
	
	public static void e(String tag, String value){
		System.err.println(tag+":"+value);
	}

	public static void d(String tag, int value) {
		System.out.println(tag+":"+value);		
	}
}
