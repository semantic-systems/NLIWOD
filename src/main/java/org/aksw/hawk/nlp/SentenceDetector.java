package org.aksw.hawk.nlp;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class SentenceDetector {
	Logger log = LoggerFactory.getLogger(SentenceDetector.class);
	private SentenceDetectorME sentenceDetector;

	public SentenceDetector() {
		InputStream modelIn = null;
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("en-sent.bin");
			if (in == null) {
				log.error("Something went horribly wrong loading en-sent.bin");
			}
			SentenceModel model = new SentenceModel(in);
			sentenceDetector = new SentenceDetectorME(model);
		} catch (IOException e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public String[] detectSentences(String text) {
		String sentences[] = sentenceDetector.sentDetect(text);
		return sentences;
	}

	public static void main(String[] args) {
		SentenceDetector sd = new SentenceDetector();
		String text = "  First sentence. Second sentence. ";
		String[] sentences = sd.detectSentences(text);
		System.out.println(Joiner.on("\n").join(sentences));
	}
}
