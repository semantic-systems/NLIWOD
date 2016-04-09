//package org.aksw.asknow.nqs;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.io.*;
//import java.nio.charset.Charset;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.stream.XMLOutputFactory;
//import javax.xml.stream.XMLStreamWriter;
//import org.apache.commons.csv.*;
//import org.apache.jena.query.QuerySolution;
//import org.apache.jena.rdf.model.RDFNode;
//import org.apache.log4j.Level;
//import org.w3c.dom.*;
//import edu.stanford.nlp.parser.nndep.Config;
//import lombok.*;
//import lombok.extern.log4j.Log4j;
//
///** Benchmark class with the evaluate function that is used for the papers.
// * Use {@link Benchmark#fromCsv(String)} and {@link Benchmark#fromQald(String)} to load a benchmark from a file.
// * CSV files contain questions and correct SPARQL queries.
// * QALD XML files also contain the results of the correct SPARQL queries so they are faster to evaluate.
// * CSV can be converted to QALD by using {@link Benchmark#fromCsv(String)}  and then {@link Benchmark#saveAsQald()} or {@link Benchmark#saveAsQald(File)}.
// * Call {@link Benchmark#evaluate(Algorithm)} to execute CubeQA and write precision and recall to the log.*/
//@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
//@Slf4j public class Benchmark
//{
//	/**Identifier used as filename for serialization.*/
//	public final String name;
//	public final List<Question> questions;
//	/**True, iff the answers to the correct SPARQL queries are precomputed.*/
//	public final boolean isComplete;
//
//
//	static Question completeQuestion(CubeSparql sparql,String string, String query)
//	{
//		Set<Map<String,String>> answers = new HashSet<>();
//		Map<String,DataType> tagTypes  = new HashMap<>();
//
//		if(query.startsWith("ask"))
//		{
//			tagTypes.put("",DataType.BOOLEAN);
//			Map<String,String> answer = new HashMap<>();
//			answer.put("",String.valueOf(sparql.ask(query)));
//			answers.add(Collections.unmodifiableMap(answer));
//		}
//		else if(query.startsWith("select"))
//		{
//			ResultSet rs = sparql.select(query);
//			Set<String> varNames = new TreeSet<>();
//			while(rs.hasNext())
//			{
//				Map<String,String> answer = new HashMap<>();
//				QuerySolution qs = rs.nextSolution();
//				// TODO: how to deal with unions where one part does not exist?
//				varNames.addAll(stream(qs.varNames()).collect(Collectors.toList()));
//				if(varNames.size()==1)
//				{
//					RDFNode node = qs.get(varNames.iterator().next());
//					tagTypes.put("",DataType.typeOf(node));
//					String ns = nodeString(qs.get(varNames.iterator().next()));
//					if(!ns.isEmpty()) {answer.put("", ns);}
//				} else
//				{
//					for(String var: varNames)
//					{
//						RDFNode node = qs.get(var);
//						if(node!=null)
//						{
//							tagTypes.put(var,DataType.typeOf(node));
//							String ns = nodeString(qs.get(var));
//							if(!ns.isEmpty()) {answer.put(var, ns);}
//						}
//					}
//				}
//				if(!answer.isEmpty()) {answers.add(Collections.unmodifiableMap(answer));}
//
//			}
//		}
//		return new Question(sparql.cubeUri, string, query, answers, tagTypes);
//	}
//
//	public void evaluate(Algorithm algorithm)
//	{
//		evaluate(algorithm, 1, questions.size());
//	}
//
//	@SneakyThrows
//	public void evaluate(Algorithm algorithm,int startQuestionNumber,int endQuestionNumber)
//	{
//		int emptyCount = 0;
//		int count = 0;
//		List<Performance> performances = new ArrayList<>();
//		log.info("Evaluating benchmark "+name+" with "+questions.size()+" questions, ["+startQuestionNumber+","+endQuestionNumber+"]");
//		try(CSVPrinter out = new CSVPrinter(new PrintWriter("benchmark/"+this.name+System.currentTimeMillis()+".csv"), CSVFormat.DEFAULT))
//		{
//			//		int unionCount = 0;
//			//		int subqueryCount = 0;
//			//		int askCount = 0;
//			for(int i=startQuestionNumber;i<=endQuestionNumber;i++)
//			{
//				//			if(i==74) {performances.add(new Performance(0,0,true));continue;} // q 74 gets wrongly positively evaluated // removed as for old benchmark
//				Question q = questions.get(i-1);
//				//			// remove questions with unions
//				//			if(q.query.toLowerCase().contains("union")) {unionCount++;continue;}
//				//			//			// remove questions with subqueries
//				//			if(q.query.toLowerCase().substring(5).contains("select")) {subqueryCount++;continue;}
//				//			//			// remove ask queries
//				//			if(q.query.toLowerCase().startsWith("ask")) {askCount++;continue;}
//				count++;
//				Performance p = evaluate(algorithm,i);
//				performances.add(p);
//				if(p.empty) {emptyCount++;}
//				out.printRecord(i,Cube.linkedSpendingCubeName(q.cubeUri),q.string,q.query,p.query,p.precision,p.recall,p.fscore());
//			}
//			log.info(count+" questions processed, "+emptyCount+" with no answers");
//			//		System.out.println(unionCount+ "union queries");
//			//		System.out.println(subqueryCount+ "sub queries");
//			//		System.out.println(askCount+ "ask queries");
//			log.info("Average precision "+ performances.stream().filter(p->!p.isEmpty()).mapToDouble(Performance::getPrecision).average());
//			log.info("Average recall "+ performances.stream().mapToDouble(Performance::getRecall).average());
//			//		log.info("f score")
//			log.info("Average f score "+ performances.stream().filter(p->!p.isEmpty()).mapToDouble(Performance::fscore).average());
//			log.info(performances.stream().filter(Performance::isEmpty).count()+" empty datasets");
//			log.info("Runtime: "+StopWatches.INSTANCE.elapsedTimesMs());
//		}
//	}
//
//	public Performance evaluate(Algorithm algorithm, int questionNumber)
//	{
//		log.setLevel(Level.ALL);
//		Question question = questions.get(questionNumber-1);
//		log.info("Question Number "+questionNumber+": Answering "+question.string);
//		log.debugebug("correct query: "+question.query);
//		log.debugebug("correct answer: "+question.answers);
//
//		String cubeName;
//		if(!Config.INSTANCE.givenDataSets)
//		{
//			List<String> uris = CubeIndex.INSTANCE.getCubeUris(question.string);;
//			if(uris.isEmpty()) {return new Performance(0, 0, true);}
//			cubeName = Cube.linkedSpendingCubeName(uris.get(0));
//		} else
//		{
//			cubeName = Cube.linkedSpendingCubeName(question.cubeUri);
//		}
//		Cube cube = Cube.getInstance(cubeName);
//		String query = algorithm.answer(cube.name,question.string).sparqlQuery();
//		Question found;
//		try
//		{
//			found = completeQuestion(cube.sparql, question.string, query);
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			return new Performance(0, 0, true);
//		}
//		log.debugebug("found query: "+found.query);
//		log.debugebug("found answer: "+found.answers);
//		Performance p = Performance.performance(question.answers, found.answers);
//		p.query = found.query;
//		log.info(p);
//		return p;
//	}
//
//	/** CSV does not contain answers. file gets loaded from benchmark/name.csv. */
//	public static Benchmark fromCsv(String name) throws IOException
//	{
//		List<Question> questions = new LinkedList<>();
//		try(CSVParser parser = CSVParser.parse(new File(new File("benchmark"),name+".csv"),Charset.defaultCharset(),CSVFormat.DEFAULT))
//		{
//			for(CSVRecord record: parser)
//			{
//				if(record.size()<3)
//				{
//					questions.add(new Question(Cube.getInstance(name).uri, record.get(0),record.get(1)));
//				} else
//				{
//					String uri = record.get(2);
//					if(!uri.startsWith("http")) {uri=Cube.linkedSpendingUri(uri);}
//					questions.add(new Question(uri, record.get(0),record.get(1)));
//				}
//
//			}
//		}
//		return new Benchmark(name,questions,false);
//	}
//
//	static String nodeString(RDFNode node)
//	{
//		if(node.isLiteral()) {
//			return node.asLiteral().getLexicalForm();
//		}
//		if(node.isResource()) {
//			return node.asResource().getURI();
//		}
//		throw new IllegalArgumentException();
//	}
//
//	/**Load a benchmark from a QALD 5 XML format slightly modified for statistical questions and multi-dimensional answers.
//	 * Queries with empty answers are assumed to result from empty SPARQL result sets and are not executed again.*/
//	@SneakyThrows
//	public static Benchmark fromQald(String name)
//	{
//		File file= new File("benchmark/"+name+".xml");
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		// only works with xsd not dtd?
//		//		dbFactory.setValidating(true);
//		//		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//		//			factory.setSchema(schemaFactory.newSchema(
//		//			    new Source[] {new StreamSource("benchmark/qaldcube.dtd")}));
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//		Document doc = dBuilder.parse(file);
//		doc.getDocumentElement().normalize();
//		List<Question> questions = new ArrayList<>();
//		NodeList questionNodes = doc.getElementsByTagName("question");
//		for(int i=0; i<questionNodes.getLength();i++)
//		{
//			Map<String,DataType> tagTypes = new HashMap<>();
//			Set<Map<String,String>> answers = new HashSet<>();
//			Element questionElement = (Element) questionNodes.item(i);
//			String string = questionElement.getElementsByTagName("string").item(0).getTextContent().trim();
//			String cubeUri = questionElement.getAttribute("cubeuri");
//			String query = Nodes.directTextContent(questionElement.getElementsByTagName("query").item(0)).trim();
//
//			Element answersElement = (Element) questionElement.getElementsByTagName("answers").item(0);
//			List<Element> answerElements = Nodes.childElements(answersElement, "answer");
//			for(Element answerElement: answerElements)
//			{
//				Map<String,String> answer = new HashMap<>();
//				String direct = Nodes.directTextContent(answerElement).trim();
//				if(direct.isEmpty())
//				{
//					for(Element var: Nodes.childElements(answerElement))
//					{
//						tagTypes.put(var.getTagName(), DataType.valueOf(var.getAttribute("answerType").toUpperCase()));
//						answer.put(var.getTagName(), var.getTextContent());
//					}
//				} else
//				{
//					tagTypes.put("", DataType.valueOf(answerElement.getAttribute("answerType").toUpperCase()));
//					answer.put("", direct);
//				}
//				answers.add(Collections.unmodifiableMap(answer));
//			}
//			Question question = new Question(cubeUri,string, query, answers,tagTypes);
//			questions.add(question);
//		}
//		return new Benchmark(name, questions,true);
//	}
//
//	/** {@link #} */
//	public void saveAsQald() {saveAsQald(new File (new File("benchmark"),name+".xml"));}
//
//	/**	 */
//	@SneakyThrows
//	public void saveAsQald(File file)
//	{
//		int id = 0;
//		try(FileWriter fw = new FileWriter(file))
//		{
//			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fw);
//			writer.writeStartDocument();
//			writer.writeStartElement("dataset");
//			writer.writeAttribute("id",name);
//			for(Question question: questions)
//			{
//				writer.writeStartElement("question");
//				writer.writeAttribute("id",String.valueOf(++id));
//				writer.writeAttribute("cubeuri",question.cubeUri);
//				writer.writeAttribute("hybrid","false");
//				writer.writeAttribute("statistical","true");
//				writer.writeCharacters("\n");
//				writer.writeStartElement("string");
//				writer.writeCharacters(question.string);
//				writer.writeEndElement();
//				writer.writeCharacters("\n");
//				writer.writeStartElement("query");
//				writer.writeCharacters("\n");
//				writer.writeCharacters(question.query);
//				writer.writeStartElement("answers");
//				writer.writeCharacters("\n");
//				if(question.answers==null)
//				{
//					question = completeQuestion(CubeSparql.getLinkedSpendingInstanceForUri(question.cubeUri), question.string, question.query);
//					////					if(true) throw new IllegalArgumentException("answers are null");
//					//					log.warn("Benchmark contains no answers, querying SPARQL endpoint");
//					//					if(question.query.startsWith("ask"))
//					//					{
//					//						writer.writeStartElement("answer");
//					//						writer.writeAttribute("answerType","boolean");
//					//						writer.writeCharacters(String.valueOf(sparql.ask(question.query)));
//					//						writer.writeEndElement();
//					//
//					//					} else if(question.query.startsWith("select"))
//					//					{
//					//						ResultSet rs = sparql.select(question.query);
//					//						List<String> varNames = null;
//					//						while(rs.hasNext())
//					//						{
//					//							writer.writeStartElement("answer");
//					//							QuerySolution qs = rs.nextSolution();
//					//							//						if(varNames==null) // unions may have empty parts so recalculate
//					//							{varNames = stream(qs.varNames()).collect(Collectors.toList());}
//					//							if(varNames.size()==1)
//					//							{
//					//								writer.writeAttribute("answerType",AnswerType.typeOf(qs.get(varNames.get(0))).toString().toLowerCase());
//					//								writer.writeCharacters(nodeString(qs.get(varNames.get(0))));
//					//							} else
//					//							{
//					//								for(String var: varNames)
//					//								{
//					//									writer.writeStartElement(var);
//					//									writer.writeAttribute("answerType",AnswerType.typeOf(qs.get(var)).toString().toLowerCase());
//					//									writer.writeCharacters(nodeString(qs.get(var)));
//					//									writer.writeEndElement();
//					//								}
//					//							}
//					//							writer.writeEndElement();
//					//							writer.writeCharacters("\n");
//					//						}
//					//					} else throw new IllegalArgumentException("unsupported SPARQL query type (neither ASK nor SELECT): "+question.query);
//					//				} else
//				}
//				{
//					for(Map<String,String> answer: question.answers)
//					{
//						writer.writeStartElement("answer");
//						if(answer.containsKey(""))
//						{
//							writer.writeAttribute("answerType",question.dataTypes.get("").toString().toLowerCase());
//							writer.writeCharacters(answer.get("").toString());
//
//						} else
//						{
//							for(String tag: answer.keySet())
//							{
//								writer.writeStartElement(tag);
//								writer.writeAttribute("answerType",question.dataTypes.get(tag).toString().toLowerCase());
//								writer.writeCharacters(answer.get(tag).toString());
//								writer.writeEndElement();
//								writer.writeCharacters("\n");
//							}
//						}
//						writer.writeEndElement();
//						writer.writeCharacters("\n");
//					}
//				}
//				writer.writeEndElement();
//				writer.writeCharacters("\n");
//				writer.writeEndElement();
//				writer.writeCharacters("\n");
//				writer.writeEndElement();
//				writer.writeCharacters("\n");
//			}
//			writer.writeEndElement();
//			writer.writeEndDocument();
//			writer.close();
//		}
//	}
//
//}