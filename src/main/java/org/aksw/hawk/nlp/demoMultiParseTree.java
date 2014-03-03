/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, 2013/11-Present, University of Massachusetts Amherst
 * Copyright 2013/05-2013/10, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.aksw.hawk.nlp;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.List;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.component.dep.AbstractDEPParser;
import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.segmentation.AbstractSegmenter;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.clearnlp.util.UTInput;
import com.clearnlp.util.UTOutput;
import com.clearnlp.util.pair.ObjectDoublePair;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class demoMultiParseTree {
	final String language = AbstractReader.LANG_EN;

	public demoMultiParseTree(String modelType, String inputFile, String outputFile) throws Exception {
		AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);
		AbstractComponent tagger = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
		AbstractComponent parser = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
		AbstractComponent identifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
		AbstractComponent classifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
		AbstractComponent labeler = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);

		AbstractComponent[] preComponents = { tagger }; // components used
														// before parsing
		AbstractComponent[] postComponents = { identifier, classifier, labeler }; // components
																					// used
																					// after
																					// parsing

		String sentence = "I know you know who I know.";
		// process(tokenizer, (AbstractDEPParser) parser, preComponents,
		// postComponents, sentence);
		process(tokenizer, (AbstractDEPParser) parser, preComponents, postComponents, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile));
	}

	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, String sentence) {
		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
		List<ObjectDoublePair<DEPTree>> trees = getParses(parser, preComponents, postComponents, tree);

		for (ObjectDoublePair<DEPTree> p : trees) {
			tree = (DEPTree) p.o;
			System.out.println("Score: " + p.d);
			// System.out.println(tree.toStringSRL() + "\n");
			System.out.println(tree.toStringCoNLL());
		}
	}

	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, BufferedReader reader, PrintStream fout) {
		AbstractSegmenter segmenter = NLPGetter.getSegmenter(language, tokenizer);
		List<ObjectDoublePair<DEPTree>> trees;
		DEPTree tree;

		for (List<String> tokens : segmenter.getSentences(reader)) {
			tree = NLPGetter.toDEPTree(tokens);
			trees = getParses(parser, preComponents, postComponents, tree);

			for (ObjectDoublePair<DEPTree> p : trees) {
				tree = (DEPTree) p.o;
				fout.println("Score: " + p.d);
				inorderTraversal(tree.getFirstRoot(), 0);
				fout.println(tree.toStringSRL() + "\n");
			}
		}

		fout.close();
	}

	private void inorderTraversal(DEPNode depNode, int i) {
		System.out.println(printTabs(i) + depNode.lemma );
		++i;
		for (DEPNode node : depNode.getDependentNodeList()) {
			inorderTraversal(node, i);
		}
	}

	private String printTabs(int i) {
		String tabs = "";
		if (i > 0) {
			tabs = "|";
		}
		for (int j = 0; j < i; ++j) {
			tabs += "=";
		}
		tabs+=">";
		return tabs;
	}

	private List<ObjectDoublePair<DEPTree>> getParses(AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, DEPTree tree) {
		List<ObjectDoublePair<DEPTree>> trees;
		boolean uniqueOnly = true; // return only unique trees given a sentence

		for (AbstractComponent component : preComponents)
			component.process(tree);

		trees = parser.getParsedTrees(tree, uniqueOnly);

		// parses are already sorted by their scores in descending order
		for (ObjectDoublePair<DEPTree> p : trees) {
			tree = (DEPTree) p.o;

			for (AbstractComponent component : postComponents)
				component.process(tree);
		}

		return trees;
	}

	public static void main(String[] args) {
		String modelType = args[0]; // "general-en" or "medical-en"
		String inputFile = args[1];
		String outputFile = args[2];

		try {
			new demoMultiParseTree(modelType, inputFile, outputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}