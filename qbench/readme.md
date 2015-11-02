# qbench

qbench is a set of benchmarks for QA on statistical Linked Data modelled using the RDF Data Cube vocabulary.
The target datacubes are part of the [LinkedSpending](http://aksw.org/Projects/LinkedSpending.html) project, which contains open government data from around the world.
The benchmarks arose from the evaluation of the [CubeQA](http://aksw.org/Projects/CubeQA) system and their publication aims to motivate other systems for this new subfield of semantic QA.
For comparison, intended performance indicators are average precision, recall and micro f-score (f-score for nonempty answers only).

## qbench1

The first CubeQA benchmark contains 100 questions on the finland-aid (foreign aid by Finland) dataset of LinkedSpending ([download](http://linkedspending.aksw.org/extensions/page/page/export/finland-aid.nt.zip)).

## qbench2

The second CubeQA benchmark contains 100 questions on 50 different LinkedSpending datasets.
It presents two different challenges: (1) with the datacube given and (2) with unspecified datacube, which presents the additional challenge of determining the correct datacube. 

## Future Work
Future benchmarks will have training and test parts.
One of the benchmarks will be included in QALD-6.
