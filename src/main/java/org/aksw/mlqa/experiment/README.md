The Idea
===

Simple solution
==
For each system, we learn a classifier whether this system can answer the features of the class or not. 
We choose the one with the highest probability as the system that should answer.

Enhanced solution
==
We try to learn how to combine answer sets.
We use a multilayer perceptron for the following:
* input: system, confidence (from simple solution), feature of question, F-measure of the system
* output: classification value c

For each system with c>0.5 we merge the answers and analyze whether union or intersection is better

Action items: Implement various wrappers for webservices to train and test on. Store the results in files.
