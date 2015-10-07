#!/bin/bash

if [ "$1" == "new" ]; then
  cd ~/cs224n/pa1-mt/java
  ant
  cd ~/cs224n/pa1-mt/system
  phrasal.sh cs224n.vars 2,4-5 myFeature.ini myfeature
else
  cd ~/cs224n/pa1-mt/system
fi

echo $'\nBLEU:'
head newstest2012.BLEU

echo $'\nFEATURES:'
java edu.stanford.nlp.mt.tools.PrintWeights newstest2011.myfeature.online.final.binwts
