# NLIWOD
Collection of tools, utilities, datasets and approaches towards realizing natural language interfaces for the Web of Data. 

This repository will soon include the following repositories as submodules:
* https://github.com/aksw/qa-ml
* https://github.com/aksw/qa-datasets
* https://github.com/aksw/qa-commons 
* https://github.com/aksw/hawk (?)

We aim at providing a fast entrance to the field of natural language interfaces (search, question answering, ranking). Thus, we will offer here Maven dependencies and source code for using many available datasets, systems and techniques. 

Foreseen modules:
* QA Features: Features calculated on a NL question to train ML algorithms.
* QA Systems: A set of existing online webservices of QA systems all executable via a simple Java interface.

If you are interested in standardization efforts join or W3C Commmunity Group https://www.w3.org/community/nli/ !

## For developers
To deploy a new version increase the according versions in the pom.xml and execute ```mvn clean deploy``` after setting your ~.m2/settings.xml in accordance to https://wiki.aksw.org/private/infrastructure/aksw-responsibilities/maven .

