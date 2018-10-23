# NLIWOD - Natural Language Interfaces for the Web of Data
[![Build Status](https://travis-ci.org/dice-group/NLIWOD.svg?branch=master)](https://travis-ci.org/dice-group/NLIWOD)
[![Project Stats](https://www.openhub.net/p/NLIWOD/widgets/project_thin_badge.gif)](https://www.openhub.net/p/NLIWOD)
[![BCH compliance](https://bettercodehub.com/edge/badge/AKSW/NLIWOD)](https://bettercodehub.com/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/d20dadb8431c4b93befabff1c5858127)](https://www.codacy.com/app/lukasbluebaumb94/NLIWOD?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=dice-group/NLIWOD&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/dice-group/NLIWOD/branch/master/graph/badge.svg)](https://codecov.io/gh/dice-group/NLIWOD)


Collection of tools, utilities, datasets and approaches towards realizing natural language interfaces for the Web of Data. Currently, we are focusing on Question Answering (QA) utilities.

Especially, this repository contains 
* QA Systems: A set of existing online webservices of QA systems all executable via a simple Java interface.
* QA Datasets: A collection of existing Question Answering datasets
* QA Machine Learning: This projects aims at learning a ML-based algorithm to combine multiple QA systems into one
* QA Commons: A collection to ease handling of QA datasets. It allows to load, store and evaluate datasets and systems. 

We aim at providing a fast entrance to the field of natural language interfaces (search, question answering, ranking). Thus, we will offer here Maven dependencies and source code for using many available datasets, systems and techniques. 

More interesting Question Answering and Natural Language Generation projects can be found here:
* SemWeb2NL https://github.com/aksw/semweb2nl: This repository provides means to verbalize triples, entities, SPARQL queries and many more RDF dataset. This is especially useful to allow users understand Web of Data content.

Foreseen modules:
* QA Features: Features calculated on a NL question to train ML algorithms.

If you are interested in standardization efforts join or W3C Commmunity Group https://www.w3.org/community/nli/ !

## For developers
To deploy a new version increase the according versions in the pom.xml and execute ```mvn clean deploy``` after setting your ~.m2/settings.xml in accordance to https://wiki.aksw.org/private/infrastructure/aksw-responsibilities/maven .

## Maven Dependency
This library is available as snapshot here: http://maven.aksw.org/archiva/#artifact~snapshots/org.aksw.qa/datasets

Add the following repository to your project:

Artifacts are described in the sub-modules.

Look for more interesting libraries here: http://maven.aksw.org/archiva/#browse/org.aksw.qa 
