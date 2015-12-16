package org.aksw.mlqa.analyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class StaticManualAnalyzer {

    private Attribute fmeasureAtt = new Attribute("fmeasure");
    public FastVector fvWekaAttributes = new FastVector();
    private HashMap<String, Instance> map = new HashMap<String, Instance>();

    /**
     *
     * @param ClassAttribute
     *            classes to be differentiated FastVector fvClassVal = new
     *            FastVector(2); fvClassVal.addElement("positive");
     *            fvClassVal.addElement("negative");
     */
    public StaticManualAnalyzer() {
        // declare attributes
        // Question Type: List, Resource, Boolean, Number

        FastVector fvClassValQuestionType = new FastVector(4);
        fvClassValQuestionType.addElement("List");
        fvClassValQuestionType.addElement("Resource");
        fvClassValQuestionType.addElement("Boolean");
        fvClassValQuestionType.addElement("Number");
        Attribute questionTypeAtt = new Attribute("QuestionType", fvClassValQuestionType, fvWekaAttributes.size());
        fvWekaAttributes.addElement(questionTypeAtt);

        // Answer Resource Type: Person, Organization, Location, Misc, Boolean,
        // Number, Date
        FastVector fvClassValAnswerResourceType = new FastVector(7);
        fvClassValAnswerResourceType.addElement("Misc");
        fvClassValAnswerResourceType.addElement("Date");
        fvClassValAnswerResourceType.addElement("Boolean");
        fvClassValAnswerResourceType.addElement("Number");
        fvClassValAnswerResourceType.addElement("Person");
        fvClassValAnswerResourceType.addElement("Organization");
        fvClassValAnswerResourceType.addElement("Location");
        Attribute answerResourceTypeAtt = new Attribute("AnswerResourceType", fvClassValAnswerResourceType,
                fvWekaAttributes.size());
        fvWekaAttributes.addElement(answerResourceTypeAtt);

        // Wh-type: Command, Who, Which, Ask, How, In which, What, When,
        // Where
        FastVector fvClassValWhtype = new FastVector(9);
        fvClassValWhtype.addElement("Command");
        fvClassValWhtype.addElement("Who");
        fvClassValWhtype.addElement("Which");
        fvClassValWhtype.addElement("Ask");
        fvClassValWhtype.addElement("How");
        fvClassValWhtype.addElement("In which");
        fvClassValWhtype.addElement("What");
        fvClassValWhtype.addElement("When");
        fvClassValWhtype.addElement("Where");
        Attribute whType = new Attribute("WhType", fvClassValWhtype, fvWekaAttributes.size());
        fvWekaAttributes.addElement(whType);

        // #Token numeric
        Attribute tokenAtt = new Attribute("token", fvWekaAttributes.size());
        fvWekaAttributes.addElement(tokenAtt);

        // Limit (includes order by and offset): Boolean
        FastVector fvClassValLimit = new FastVector(2);
        fvClassValLimit.addElement("TRUE");
        fvClassValLimit.addElement("FALSE");
        Attribute limitAtt = new Attribute("Limit", fvClassValLimit, fvWekaAttributes.size());
        fvWekaAttributes.addElement(limitAtt);

        // Comparative : Boolean
        FastVector fvClassValComparative = new FastVector(2);
        fvClassValComparative.addElement("TRUE");
        fvClassValComparative.addElement("FALSE");
        Attribute comparativeAtt = new Attribute("Comparative", fvClassValComparative, fvWekaAttributes.size());
        fvWekaAttributes.addElement(comparativeAtt);

        // Superlative : Boolean
        FastVector fvClassValSuperlative = new FastVector(2);
        fvClassValSuperlative.addElement("TRUE");
        fvClassValSuperlative.addElement("FALSE");
        Attribute superlativeAtt = new Attribute("Superlative", fvClassValSuperlative, fvWekaAttributes.size());
        fvWekaAttributes.addElement(superlativeAtt);

        // Person : Boolean
        FastVector fvClassValPerson = new FastVector(2);
        fvClassValPerson.addElement("TRUE");
        fvClassValPerson.addElement("FALSE");
        Attribute personAtt = new Attribute("Person", fvClassValPerson, fvWekaAttributes.size());
        fvWekaAttributes.addElement(personAtt);

        // Location: Boolean
        FastVector fvClassValLocation = new FastVector(2);
        fvClassValLocation.addElement("TRUE");
        fvClassValLocation.addElement("FALSE");
        Attribute locationAtt = new Attribute("Location", fvClassValLocation, fvWekaAttributes.size());
        fvWekaAttributes.addElement(locationAtt);

        // Organization: Boolean
        FastVector fvClassValOrganization = new FastVector(2);
        fvClassValOrganization.addElement("TRUE");
        fvClassValOrganization.addElement("FALSE");
        Attribute organizationAtt = new Attribute("Organization", fvClassValOrganization, fvWekaAttributes.size());
        fvWekaAttributes.addElement(organizationAtt);

        // Misc: Boolean
        FastVector fvClassValMisc = new FastVector(2);
        fvClassValMisc.addElement("TRUE");
        fvClassValMisc.addElement("FALSE");
        Attribute miscAtt = new Attribute("Misc", fvClassValMisc, fvWekaAttributes.size());
        fvWekaAttributes.addElement(miscAtt);

        // put the fmeasure/class attribute
        fvWekaAttributes.addElement(fmeasureAtt);

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
                    Instance tmpInstance = new Instance(fvWekaAttributes.size());
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

    public FastVector getAttributes() {
        return fvWekaAttributes;
    }

}