package org.aksw.mlqa.analyzer.nqs;

import java.util.List;
import java.util.Properties;
import java.io.PrintStream;
import edu.stanford.nlp.io.NullOutputStream;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/** Stanford Core NLP utility class. */
@SuppressWarnings("resource")
@Slf4j public class StanfordNlp
{
	static private final StanfordCoreNLP treeParser;
	//	static private final StanfordCoreNLP lemmatizer;

	@RequiredArgsConstructor
	@ToString
	static public class ParseResult
	{
		final Tree parseTree;
		final String pos;
	}

	static
	{
		// stanford doesn't use a logging framework so we have to disable syserr
		PrintStream err = System.err;
		System.setErr(new PrintStream(new NullOutputStream()));
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse");
		treeParser = new StanfordCoreNLP(props);

		System.setErr(err);
	}

	public static Tree parse(String sentence)
	{
		log.trace("parsing sentence: '"+sentence+"' as tree");
		Annotation document = new Annotation(sentence);
		treeParser.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		return sentences.get(0).get(TreeAnnotation.class);
		//		return new ParseResult(sentences.get(0).get(TreeAnnotation.class),document.get(PartOfSpeechAnnotation.class));
	}

	public static String PosTag(String sentence)
	{
		// TODO: fix this
		log.trace("parsing sentence: '"+sentence+"' as tree");
		//		new POSTaggerAnnotator().
		Annotation document = new Annotation(sentence);
		treeParser.annotate(document);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		return sentences.get(0).get(TreeAnnotation.class).toString();
		//		return new ParseResult(sentences.get(0).get(TreeAnnotation.class),document.get(PartOfSpeechAnnotation.class));
	}
	//	public static String lemmatize(String text)
	//	{
	//		Annotation document = lemmatizer.process(text);
	//StringBuilder sb = new StringBuilder();
	//		for(CoreMap sentence: document.get(SentencesAnnotation.class))
	//		{
	//			for(CoreLabel token: sentence.get(TokensAnnotation.class))
	//			{
	//				String word = token.get(TextAnnotation.class);
	//				String lemma = token.get(LemmaAnnotation.class);
	////				System.out.println("lemmatized version :" + lemma);
	//				sb.append(" "+lemma);
	//			}
	//		}
	//		return sb.toString().substring(1);
	//	}
}