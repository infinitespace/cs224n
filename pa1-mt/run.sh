#!/bin/bash

if [ "$1" == "re" ]; then
  cd ~/cs224n/pa1-mt/java
  ant
  cd ~/cs224n/pa1-mt/system
  phrasal.sh cs224n.vars 2,4-5 myFeature.ini myfeature
else if [ "$1" == "new" ]; then
  cd ~/cs224n/pa1-mt/java
  ant
  cd ~/cs224n/pa1-mt/system
  phrasal.sh cs224n.vars 1-5 cs224n.ini baseline
  fi
fi

cd ~/cs224n/pa1-mt/system

echo $'\nBLEU:'
head newstest2012.BLEU

echo $'\nFEATURES:'
java edu.stanford.nlp.mt.tools.PrintWeights newstest2011.myfeature.online.final.binwts
