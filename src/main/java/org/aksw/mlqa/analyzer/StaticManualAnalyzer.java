package org.aksw.mlqa.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;

public class StaticManualAnalyzer {

    private Attribute fmeasureAtt = new Attribute("fmeasure");
    public ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
    private HashMap<String, Instance> map = new HashMap<String, Instance>();

    /**
     *
     * @param ClassAttribute
     *            classes to be differentiated ArrayList<Attribute> fvClassVal = new
     *            ArrayList<Attribute>(2); fvClassVal.add("positive");
     *            fvClassVal.add("negative");
     */
    public StaticManualAnalyzer() {
        // declare attributes
        // Question Type: List, Resource, Boolean, Number

        ArrayList<String> fvClassValQuestionType = new ArrayList<String>(4);
        fvClassValQuestionType.add("List");
        fvClassValQuestionType.add("Resource");
        fvClassValQuestionType.add("Boolean");
        fvClassValQuestionType.add("Number");
        Attribute questionTypeAtt = new Attribute("QuestionType", fvClassValQuestionType, fvWekaAttributes.size());
        fvWekaAttributes.add(questionTypeAtt);

        // Answer Resource Type: Person, Organization, Location, Misc, Boolean,
        // Number, Date
        ArrayList<String> fvClassValAnswerResourceType = new ArrayList<String>(7);
        fvClassValAnswerResourceType.add("Misc");
        fvClassValAnswerResourceType.add("Date");
        fvClassValAnswerResourceType.add("Boolean");
        fvClassValAnswerResourceType.add("Number");
        fvClassValAnswerResourceType.add("Person");
        fvClassValAnswerResourceType.add("Organization");
        fvClassValAnswerResourceType.add("Location");
        Attribute answerResourceTypeAtt = new Attribute("AnswerResourceType", fvClassValAnswerResourceType,
                fvWekaAttributes.size());
        fvWekaAttributes.add(answerResourceTypeAtt);

        // Wh-type: Command, Who, Which, Ask, How, In which, What, When,
        // Where
        ArrayList<String> fvClassValWhtype = new ArrayList<String>(9);
        fvClassValWhtype.add("Command");
        fvClassValWhtype.add("Who");
        fvClassValWhtype.add("Which");
        fvClassValWhtype.add("Ask");
        fvClassValWhtype.add("How");
        fvClassValWhtype.add("In which");
        fvClassValWhtype.add("What");
        fvClassValWhtype.add("When");
        fvClassValWhtype.add("Where");
        Attribute whType = new Attribute("WhType", fvClassValWhtype);
        fvWekaAttributes.add(whType);

        // #Token numeric
        Attribute tokenAtt = new Attribute("token", fvWekaAttributes.size());
        fvWekaAttributes.add(tokenAtt);

        // Limit (includes order by and offset): Boolean
        ArrayList<String> fvClassValLimit = new ArrayList<String>(2);
        fvClassValLimit.add("TRUE");
        fvClassValLimit.add("FALSE");
        Attribute limitAtt = new Attribute("Limit", fvClassValLimit, fvWekaAttributes.size());
        fvWekaAttributes.add(limitAtt);

        // Comparative : Boolean
        ArrayList<String> fvClassValComparative = new ArrayList<String>(2);
        fvClassValComparative.add("TRUE");
        fvClassValComparative.add("FALSE");
        Attribute comparativeAtt = new Attribute("Comparative", fvClassValComparative, fvWekaAttributes.size());
        fvWekaAttributes.add(comparativeAtt);

        // Superlative : Boolean
        ArrayList<String> fvClassValSuperlative = new ArrayList<String>(2);
        fvClassValSuperlative.add("TRUE");
        fvClassValSuperlative.add("FALSE");
        Attribute superlativeAtt = new Attribute("Superlative", fvClassValSuperlative, fvWekaAttributes.size());
        fvWekaAttributes.add(superlativeAtt);

        // Person : Boolean
        ArrayList<String> fvClassValPerson = new ArrayList<String>(2);
        fvClassValPerson.add("TRUE");
        fvClassValPerson.add("FALSE");
        Attribute personAtt = new Attribute("Person", fvClassValPerson, fvWekaAttributes.size());
        fvWekaAttributes.add(personAtt);

        // Location: Boolean
        ArrayList<String> fvClassValLocation = new ArrayList<String>(2);
        fvClassValLocation.add("TRUE");
        fvClassValLocation.add("FALSE");
        Attribute locationAtt = new Attribute("Location", fvClassValLocation, fvWekaAttributes.size());
        fvWekaAttributes.add(locationAtt);

        // Organization: Boolean
        ArrayList<String> fvClassValOrganization = new ArrayList<String>(2);
        fvClassValOrganization.add("TRUE");
        fvClassValOrganization.add("FALSE");
        Attribute organizationAtt = new Attribute("Organization", fvClassValOrganization, fvWekaAttributes.size());
        fvWekaAttributes.add(organizationAtt);

        // Misc: Boolean
        ArrayList<String> fvClassValMisc = new ArrayList<String>(2);
        fvClassValMisc.add("TRUE");
        fvClassValMisc.add("FALSE");
        Attribute miscAtt = new Attribute("Misc", fvClassValMisc, fvWekaAttributes.size());
        fvWekaAttributes.add(miscAtt);

        // put the fmeasure/class attribute
        fvWekaAttributes.add(fmeasureAtt);

        // load file
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    StaticManualAnalyzer.class.getClassLoader().getResourceAsStream("manual_features.tsv"), "UTF-8"));
            while (br.ready()) {
                String line = br.readLine();
                String split[] = line.split("\t");
                // If this is a question line
                if ((split.length >= 13) && (!split[1].isEmpty())) {
                    Instance tmpInstance = new DenseInstance(fvWekaAttributes.size());
                    tmpInstance.setValue(questionTypeAtt, split[2]);
                    tmpInstance.setValue(answerResourceTypeAtt, split[3]);
                    tmpInstance.setValue(whType, split[4]);
                    tmpInstance.setValue(tokenAtt, Double.parseDouble(split[5]));
                    tmpInstance.setValue(limitAtt, split[6]);
                    tmpInstance.setValue(comparativeAtt, split[7]);
                    tmpInstance.setValue(superlativeAtt, split[8]);
                    tmpInstance.setValue(personAtt, split[9]);
                    tmpInstance.setValue(locationAtt, split[10]);
                    tmpInstance.setValue(organizationAtt, split[11]);
                    tmpInstance.setValue(miscAtt, split[12]);
                    map.put(split[1], tmpInstance);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     *
     * @param q
     * @return feature vector leaving out a slot for the class variable, i.e.,
     *         the QA system that can answer this feature vector
     */
    public Instance analyze(String q) {
        return map.get(q);
    }

    public Attribute getClassAttribute() {
        return fmeasureAtt;
    }

    public ArrayList<Attribute> getAttributes() {
        return fvWekaAttributes;
    }

}