package qa.commons.nlp.ner;

import java.util.List;

public interface NER {
	
	List<String> getNamedEntitites(String sentence);

}
