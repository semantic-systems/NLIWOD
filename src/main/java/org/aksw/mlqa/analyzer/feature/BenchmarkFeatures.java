package org.aksw.mlqa.analyzer.feature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
//import java.util.Set;
//import java.util.regex.Pattern;
//import org.aksw.asknow.feature.Nqs;
//import org.aksw.asknow.feature.Parser;
//import org.aksw.asknow.query.*;
//import org.apache.jena.rdf.model.RDFNode;
//import lombok.extern.slf4j.Slf4j;

//@Slf4j 
public class BenchmarkFeatures {

	public static void main(String[] args)
	{//Create array list 
		File file = new File("computed-feature.tsv");
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write("query \t questionType \t answerType \t whType \t tokenCount \t QuestionType\t isComparative()\t"+
			 "isSuperlative\t  isPerson \t isLocation \t isLocation \t isOrganization \t Misc");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Nqs> templates = Parser.parse();
		//Nqs q1 = templates.get(queryid);
		for(int i=0;i<49;i++)
		{
			try {
				bw.write(benchmark(templates.get(i)));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			bw.close();
			System.out.println("features saved");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//print array list
	}	


	static String benchmark(Nqs q1)
	{
		
		return "\n"+q1.nlQuery +"\t"+Feature.feature(q1);
	}
}