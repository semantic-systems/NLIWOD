package org.aksw.mlqa.experiments;

import java.util.Arrays;
import java.util.List;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.analyzer.comparative.Comparative;
import org.aksw.mlqa.analyzer.dependencies.Dependencies;
import org.aksw.mlqa.analyzer.entitytype.EntityLocation;
import org.aksw.mlqa.analyzer.entitytype.EntityPerson;
import org.aksw.mlqa.analyzer.numberoftoken.NumberOfToken;
import org.aksw.mlqa.analyzer.partofspeechtags.PartOfSpeechTags;
import org.aksw.mlqa.analyzer.questiontype.QuestionTypeAnalyzer;
import org.aksw.qa.commons.load.Dataset;

import meka.classifiers.multilabel.BCC;
import meka.classifiers.multilabel.CC;
import meka.classifiers.multilabel.CCq;
import meka.classifiers.multilabel.FW;
import meka.classifiers.multilabel.LC;
import meka.classifiers.multilabel.PS;
import meka.classifiers.multilabel.RAkEL;
import meka.classifiers.multilabel.RT;

public class ExperimentExamples {
	
	/**
	 * Some example experiments. All files that are used here already exist.
	 */
	
	public static void main(String[] args) throws Exception {
		
		List<IAnalyzer> analyzers;
		
		// as default, the Analyzer uses all features
		Analyzer analyzer = new Analyzer();
		
		TableMaker maker = new TableMaker();
		CrossValidation cv = new CrossValidation();
		ARFFFromQALD arff = new ARFFFromQALD();
		
		//make arff file with all features from QALD-9
//		arff.makeARFF(Dataset.QALD9_Train_Multilingual, analyzer, "QALD9_all_features.arff");
		
		//make arff file with the best feature combination from QALD-9, gives a list of IAnalyzers to the Analyzer
//		analyzers = Arrays.asList(new PartOfSpeechTags(), new Dependencies(), new EntityLocation(), new EntityPerson());
//		analyzer = new Analyzer(analyzers);
//		arff.makeARFF(Dataset.QALD9_Train_Multilingual, analyzer, "QALD9_best_combination.arff");
		
		//make arff file with the best feature combination from QALD-8
//		analyzers = Arrays.asList(new PartOfSpeechTags(), new Dependencies(), new Comparative(), new EntityLocation(), new EntityPerson());
//		analyzer = new Analyzer(analyzers);
//		arff.makeARFF(Dataset.QALD8_Train_Multilingual, analyzer, "QALD8_best_combination.arff");
		
		
		
		// best feature combination and best classifier for QALD-9 trained on all questions	
		maker.makeTable(Dataset.QALD9_Train_Multilingual, new FW(), "QALD9_best_combination.arff");
			
		// best feature combination and best classifier for QALD-8 trained on all questions	
//		maker.makeTable(Dataset.QALD8_Train_Multilingual, new FW(), "QALD8_best_combination.arff");
		
			
		
		// 10 fold cross-validation for QALD-9, just need to insert different classifiers
//		cv.cVModel(new PS(), 10, "QALD9_all_features.arff");
				
		// 10 fold cross-validation for QALD-8
//		cv.cVModel(new FW(), 10, "QALD8_all_features.arff");
		
		
		// leave-one-out cross-validation for QALD-9, -1 for leave-one-out
//		cv.cVModel(new RAkEL(), -1, "QALD9_all_features.arff");
		
		// leave-one-out cross-validation for QALD-8, -1 for leave-one-out
//		cv.cVModel(new PS(), -1, "QALD8_all_features.arff");
		
	}
	
}
