# NLIWOD - Natural Language Interfaces for the Web of Data
Collection of tools, utilities, datasets and approaches towards realizing natural language interfaces for the Web of Data. 

Especially, this repository contains 
* QA Systems: A set of existing online webservices of QA systems all executable via a simple Java interface.
* QA Datasets: A collection of existing Question Answering datasets

This repository will soon include the following repositories as submodules:
* https://github.com/aksw/qa-ml
* https://github.com/aksw/qa-commons 

More interesting Question Answering and Natural Language Generation projects can be found here:
* https://github.com/aksw/hawk 
* https://github.com/aksw/watodo
* https://github.com/aksw/semweb2nl

We aim at providing a fast entrance to the field of natural language interfaces (search, question answering, ranking). Thus, we will offer here Maven dependencies and source code for using many available datasets, systems and techniques. 

Foreseen modules:
* QA Features: Features calculated on a NL question to train ML algorithms.

If you are interested in standardization efforts join or W3C Commmunity Group https://www.w3.org/community/nli/ !

## For developers
To deploy a new version increase the according versions in the pom.xml and execute ```mvn clean deploy``` after setting your ~.m2/settings.xml in accordance to https://wiki.aksw.org/private/infrastructure/aksw-responsibilities/maven .

## Maven Dependency
This library is available as snapshot here: http://maven.aksw.org/archiva/#artifact~snapshots/org.aksw.qa/datasets

Add the following repository to your project:

Artificats are described in the sub-modules.

Look for more interesting libraries here: http://maven.aksw.org/archiva/#browse/org.aksw.qa 