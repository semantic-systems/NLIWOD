package org.aksw.qa.commons.load.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.utils.SPARQLExecutor;
import org.apache.jena.ext.com.google.common.base.Joiner;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public final class EJQuestionFactory {
	public static final String SPLIT_KEYWORDS_ON = ",";
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private EJQuestionFactory() {

	}

	public static List<IQuestion> getQuestionsFromExtendedJson(final ExtendedJson json) {
		return getQuestionsFromExtendedJson(json, null);
	}

	public static List<IQuestion> getQuestionsFromExtendedJson(final ExtendedJson json, final String deriveUri) {
		List<IQuestion> out = new ArrayList<>();
		for (EJQuestionEntry it : json.getQuestions()) {
			IQuestion question = new Question();
			out.add(question);
			question.setId(it.getQuestion().getId() + "");
			question.setAnswerType(it.getQuestion().getAnswertype());
			if (it.getQuestion().getMetadata() != null) {
				EJMetadata meta = it.getQuestion().getMetadata();
				question.setAggregation(meta.getAggregation());
				question.setHybrid(meta.getHybrid());
				question.setOnlydbo(meta.getOnlydbo());

			}

			for (EJLanguage lang : it.getQuestion().getLanguage()) {

				question.setSparqlQuery(lang.getSparql());

				HashMap<String, String> langToQuestion = new HashMap<>();
				HashMap<String, List<String>> langToKeywords = new HashMap<>();

				langToQuestion.put(lang.getLanguage(), lang.getQuestion());
				question.setLanguageToQuestion(langToQuestion);

				langToKeywords.put(lang.getLanguage(), Arrays.asList(lang.getKeywords().split(SPLIT_KEYWORDS_ON)));
				question.setLanguageToKeywords(langToKeywords);
			}
			if ((deriveUri != null) && (question.getSparqlQuery() != null)) {
				HashSet<String> set = new HashSet<>();
				Set<RDFNode> answers = SPARQLExecutor.sparql(deriveUri, question.getSparqlQuery());

				for (RDFNode answ : answers) {
					set.add(answ.toString());
				}
				question.setGoldenAnswers(set);
			} else {
				EJAnswers answers = it.getQuestion().getAnswers();

				getAnswersFromAnswerObject(answers, question);
			}
		}

		return out;
	}

	public static ExtendedJson getExtendedJson(final List<IQuestion> questions) {

		ExtendedJson ex = new ExtendedJson();

		for (IQuestion question : questions) {
			EJQuestionEntry entry = new EJQuestionEntry();

			if ((question.getHybrid() != null) || (question.getOnlydbo() != null) || (question.getAggregation() != null) || question.getOutOfScope()) {
				EJMetadata metadata = new EJMetadata();
				metadata.setAggregation(question.getAggregation());
				metadata.setOnlydbo(question.getOnlydbo());
				metadata.setHybrid(question.getHybrid());
				entry.getQuestion().setMetadata(metadata);
			}

			if (!Strings.isNullOrEmpty(question.getAnswerType())) {
				entry.getQuestion().setAnswertype(question.getAnswerType());
			}
			if (!Strings.isNullOrEmpty(question.getId())) {
				entry.getQuestion().setId(question.getId());
			} else {
				entry.getQuestion().setId("undefined");
			}

			for (String langStr : question.getLanguageToQuestion().keySet()) {
				EJLanguage language = new EJLanguage();
				entry.getQuestion().getLanguage().add(language);
				if ((question.getLanguageToKeywords().get(langStr) != null) && !question.getLanguageToKeywords().get(langStr).isEmpty()) {
					language.setKeywords(Joiner.on(",").join(question.getLanguageToKeywords().get(langStr)));
				}
				language.setLanguage(langStr);
				language.setQuestion(question.getLanguageToQuestion().get(langStr));
				language.setSparql(question.getSparqlQuery());

			}

			EJAnswers answers = new EJAnswers();
			entry.getQuestion().setAnswers(answers);
			setAnswersInJson(answers, question);
			ex.addQuestions(entry);

		}

		return ex;

	}

	public static QaldJson getQaldJson(final List<IQuestion> questions) {
		QaldJson json = new QaldJson();
		for (IQuestion question : questions) {
			QaldQuestionEntry questionEntry = new QaldQuestionEntry();

			for (Entry<String, String> entry : question.getLanguageToQuestion().entrySet()) {
				QaldQuestion q = new QaldQuestion();

				q.setLanguage(entry.getKey()).setString(entry.getValue());

				if ((question.getLanguageToKeywords() != null) && (question.getLanguageToKeywords().get(entry.getKey()) != null)) {
					q.setKeywords(Joiner.on(", ").join(question.getLanguageToKeywords().get(entry.getKey())));
				}

				questionEntry.getQuestion().add(q);
			}
			questionEntry.setId(question.getId());
			questionEntry.setAnswertype(question.getAnswerType());
			questionEntry.setAggregation(question.getAggregation());
			questionEntry.setOnlydbo(question.getOnlydbo());
			questionEntry.setHybrid(question.getHybrid());

			QaldQuery language = new QaldQuery();
			language.setSparql(question.getSparqlQuery());
			language.setPseudo(question.getPseudoSparqlQuery());
			questionEntry.setQuery(language);

			EJAnswers answers = new EJAnswers();
			questionEntry.getAnswers().add(answers);

			EJHead head = new EJHead();
			answers.setHead(head);

			setAnswersInJson(answers, question);
			json.getQuestions().add(questionEntry);
		}

		return json;

	}

	public static List<IQuestion> getQuestionsFromJson(final Object json) {
		return getQuestionsFromJson(json, null);
	}

	public static List<IQuestion> getQuestionsFromJson(final Object json, final String deriveUri) {
		if (json instanceof ExtendedJson) {
			return getQuestionsFromExtendedJson((ExtendedJson) json, deriveUri);
		} else if (json instanceof QaldJson) {
			return getQuestionsFromQaldJson((QaldJson) json, deriveUri);
		} else {
			return null;
		}
	}

	public static List<IQuestion> getQuestionsFromQaldJson(final QaldJson json) {
		return getQuestionsFromQaldJson(json, null);
	}

	public static List<IQuestion> getQuestionsFromQaldJson(final QaldJson json, final String deriveUri) {
		List<IQuestion> questions = new ArrayList<>();

		for (QaldQuestionEntry it : json.getQuestions()) {
			Question question = new Question();
			question.setId(it.getId());
			question.setAnswerType(it.getAnswertype());
			question.setAggregation(it.getAggregation());
			question.setOnlydbo(it.getOnlydbo());
			question.setHybrid(it.getHybrid());

			if (it.getQuery() != null) {
				question.setPseudoSparqlQuery(it.getQuery().getPseudo());
				question.setSparqlQuery(it.getQuery().getSparql());
			}

			for (QaldQuestion qQuestion : it.getQuestion()) {
				question.getLanguageToQuestion().put(qQuestion.getLanguage(), qQuestion.getString());
				if ((qQuestion.getKeywords() != null) && !qQuestion.getKeywords().isEmpty()) {
					question.getLanguageToKeywords().put(qQuestion.getLanguage(), Arrays.asList(qQuestion.getKeywords().split(",\\s*")));
				}

			}

			if ((deriveUri != null) && (question.getSparqlQuery() != null)) {
				HashSet<String> set = new HashSet<>();
				Set<RDFNode> answers = SPARQLExecutor.sparql(deriveUri, question.getSparqlQuery());

				for (RDFNode answ : answers) {
					set.add(answ.toString());
				}
				question.setGoldenAnswers(set);
			} else {
				for (EJAnswers answerObject : it.getAnswers()) {
					getAnswersFromAnswerObject(answerObject, question);
				}
			}

			questions.add(question);
		}

		return questions;

	}

	private static void getAnswersFromAnswerObject(final EJAnswers answerObject, final IQuestion question) {
		if (answerObject == null) {
			return;
		}
		if (answerObject.getBoolean() != null) {
			question.getGoldenAnswers().add(answerObject.getBoolean() + "");
		}
		if (answerObject.getResults() != null) {
			Vector<HashMap<String, EJBinding>> answerVector = answerObject.getResults().getBindings();
			for (HashMap<String, EJBinding> answerMap : answerVector) {
				for (EJBinding bind : answerMap.values()) {
					question.getGoldenAnswers().add(bind.getValue());
				}
			}
		}
	}

	private static void setAnswersInJson(final EJAnswers answers, final IQuestion question) {

		EJHead head = answers.getHead();

		String ansType = question.getAnswerType();
		if ((ansType == null) || ansType.isEmpty()) {
			ansType = "not set";
		}

		answers.setResults(new EJResults());

		switch (ansType.toLowerCase()) {
		case "boolean":
			answers.setBoolean(Boolean.TRUE.equals(Joiner.on(" ").join(question.getLanguageToQuestion().values()).toLowerCase().contains("true")));
			break;
		case "date":
			head.getVars().add("date");

			for (String golden : question.getGoldenAnswers()) {
				EJBinding binding = new EJBinding();
				HashMap<String, EJBinding> bindMap = new HashMap<>();
				binding.setType("literal");
				binding.setValue(golden);
				bindMap.put("date", binding);
				answers.getResults().getBindings().add(bindMap);
			}
			break;
		case "resource":
			head.getVars().add("uri");
			for (String golden : question.getGoldenAnswers()) {
				EJBinding binding = new EJBinding();
				HashMap<String, EJBinding> bindMap = new HashMap<>();
				binding.setType("uri");
				binding.setValue(golden);
				bindMap.put("uri", binding);
				answers.getResults().getBindings().add(bindMap);
			}
			break;
		case "number":
			head.getVars().add("c");
			for (String golden : question.getGoldenAnswers()) {
				EJBinding binding = new EJBinding();
				HashMap<String, EJBinding> bindMap = new HashMap<>();
				binding.setType("literal");
				binding.setValue(golden);
				bindMap.put("c", binding);
				answers.getResults().getBindings().add(bindMap);
			}
			break;
		case "string":
			head.getVars().add("string");
			for (String golden : question.getGoldenAnswers()) {
				EJBinding binding = new EJBinding();
				HashMap<String, EJBinding> bindMap = new HashMap<>();
				binding.setType("literal");
				binding.setValue(golden);
				bindMap.put("string", binding);
				answers.getResults().getBindings().add(bindMap);
			}
			break;

		default:
			head.getVars().add(ansType);
			for (String golden : question.getGoldenAnswers()) {
				EJBinding binding = new EJBinding();
				HashMap<String, EJBinding> bindMap = new HashMap<>();
				binding.setType(ansType);
				binding.setValue(golden);
				bindMap.put(ansType, binding);
				answers.getResults().getBindings().add(bindMap);
			}
			break;

		}
	}

	public static ExtendedJson fromQaldToExtended(final QaldJson json) {
		if (json == null) {
			return null;
		}
		ExtendedJson exJson = new ExtendedJson();

		exJson.setDataset(json.getDataset());

		for (QaldQuestionEntry qaldQuestionEntry : json.getQuestions()) {
			EJQuestionEntry exEntry = new EJQuestionEntry();
			exEntry.getQuestion().setId(qaldQuestionEntry.getId());
			exEntry.getQuestion().setAnswertype(qaldQuestionEntry.getAnswertype());

			if (!qaldQuestionEntry.getAnswers().isEmpty()) {
				exEntry.getQuestion().setAnswers(qaldQuestionEntry.getAnswers().get(0));
			}
			EJMetadata meta = new EJMetadata();
			meta.setAggregation(qaldQuestionEntry.getAggregation());
			meta.setHybrid(qaldQuestionEntry.getHybrid());
			meta.setOnlydbo(qaldQuestionEntry.getOnlydbo());
			exEntry.getQuestion().setMetadata(meta);

			for (QaldQuestion qqIt : qaldQuestionEntry.getQuestion()) {

				EJLanguage lang = new EJLanguage();
				lang.setLanguage(qqIt.getLanguage());
				lang.setQuestion(qqIt.getString());
				lang.setKeywords(qqIt.getKeywords());
				lang.setSparql(qaldQuestionEntry.getQuery().getSparql());
				exEntry.getQuestion().getLanguage().add(lang);
			}
			exJson.getQuestions().add(exEntry);

		}
		return exJson;

	}

	public static QaldJson fromExtendedToQald(final ExtendedJson exJson) {
		if (exJson == null) {
			return null;
		}
		QaldJson json = new QaldJson();

		json.setDataset(exJson.getDataset());

		for (EJQuestionEntry exEntry : exJson.getQuestions()) {
			QaldQuestionEntry qEntry = new QaldQuestionEntry();

			if (exEntry.getQuestion().getMetadata() != null) {
				EJMetadata meta = exEntry.getQuestion().getMetadata();
				qEntry.setAggregation(meta.getAggregation());
				qEntry.setHybrid(meta.getHybrid());
				qEntry.setOnlydbo(meta.getOnlydbo());
			}
			qEntry.setId(exEntry.getQuestion().getId());
			qEntry.setAnswertype(exEntry.getQuestion().getAnswertype());

			QaldQuery query = new QaldQuery();

			qEntry.setQuery(query);
			if (!exEntry.getQuestion().getLanguage().isEmpty()) {
				query.setSparql(exEntry.getQuestion().getLanguage().get(0).getSparql());
			}

			for (EJLanguage lang : exEntry.getQuestion().getLanguage()) {
				QaldQuestion qQuestion = new QaldQuestion();
				qQuestion.setLanguage(lang.getLanguage());
				qQuestion.setString(lang.getQuestion());
				qQuestion.setKeywords(lang.getKeywords());

				qEntry.getQuestion().add(qQuestion);
			}
			qEntry.getAnswers().add(exEntry.getQuestion().getAnswers());
			json.getQuestions().add(qEntry);

		}

		return json;
	}

}
