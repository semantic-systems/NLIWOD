package org.aksw.qa.commons.KnowledgeCard;


import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.utils.JsonUtils;

import org.junit.Assert;


@RunWith(MockitoJUnitRunner.class)
public class KnowledgeCardCreatorTest {	
	
	 private String realCard;
	
	 @InjectMocks
	 private KnowledgeCardCreator knowledgeCardCreator = new KnowledgeCardCreator();
	 
	 @Mock
	 private ResourceLoader resourceLoader;
	
	 @Before 
	 public void createCard() throws JsonGenerationException, IOException {
		 LinkedHashSet<Field> realAnswer = new LinkedHashSet<Field>();
		 
		 Field f1 = new Field("doctoral advisor", null);
		 LinkedHashMap<String, String> values = new LinkedHashMap<String, String>();
		 values.put("http://dbpedia.org/resource/Alfred_Kleiner", "Alfred Kleiner");
		 f1.setValues(values);
		 realAnswer.add(f1);
		 	 
		 Field f2 = new Field("birth place", null , true);
		 values = new LinkedHashMap<String, String>();
		 values.put("http://dbpedia.org/resource/German_Empire", "German Empire");
		 values.put("http://dbpedia.org/resource/Kingdom_of_Württemberg", "Kingdom of Württemberg");
		 values.put("http://dbpedia.org/resource/Ulm", "Ulm");
		 f2.setValues(values);
		 realAnswer.add(f2);
		 
		 Field f3 = new Field("death date", "1955-04-18");
		 f3.setValues(null);
		 realAnswer.add(f3);
		 
		 Field f4 = new Field("birth date", "1879-03-14");
		 f4.setValues(null);
		 realAnswer.add(f4);
		 		 
		 Field f5 = new Field("death place", null);
		 values = new LinkedHashMap<String, String>();
		 values.put("http://dbpedia.org/resource/Princeton,_New_Jersey", "Princeton, New Jersey");
		 f5.setValues(values);
		 realAnswer.add(f5);
		 
		 realCard = JsonUtils.toPrettyString(realAnswer);
	 }
	 
	 @Test
	 public void knowledgeCardTest() throws JsonGenerationException, IOException {
		 String uri = "http://dbpedia.org/resource/Albert_Einstein";
		 DefaultResourceLoader loader = new DefaultResourceLoader();
		 Resource resource = loader.getResource("classpath:db.csv");
		 Mockito.when(resourceLoader.getResource("classpath:db.csv")).thenReturn(resource);

		 HashSet<Field> answer = knowledgeCardCreator.process(uri);
		 String answerCard = JsonUtils.toPrettyString(answer);

		 Assert.assertTrue(answerCard.equals(realCard));
	 }
}