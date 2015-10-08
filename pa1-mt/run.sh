#!/bin/bash

set -e

postfix=$(date +"%y%m%d_%R")
featurename=myfeature_$2_$postfix

case $1 in
   "new")
	cd ~/cs224n/pa1-mt/java;
	ant;
	cd ~/cs224n/pa1-mt/system;
	phrasal.sh cs224n.vars 1-5 cs224n.ini baseline; ;;
   "re")
	cd ~/cs224n/pa1-mt/java;
        ant;
	cd ~/cs224n/pa1-mt/system;
        for i in `seq 1 $3`; do
            phrasal.sh cs224n.vars 2,4-5 myFeature.ini $featurename; 
        done
	echo $'\nFEATURES:';
	java edu.stanford.nlp.mt.tools.PrintWeights newstest2011.$featurename.online.final.binwts; ;;
   "clean")
        cd ~/cs224n/pa1-mt/system;	
esac; 

#if [ "$1" == "re" ]; then
#  cd ~/cs224n/pa1-mt/java
#  ant
#  cd ~/cs224n/pa1-mt/system
#  phrasal.sh cs224n.vars 2,4-5 myFeature.ini myfeature_$postfix
#else 
#  if [ "$1" == "new" ]; then
#  cd ~/cs224n/pa1-mt/java
#  ant
#  cd ~/cs224n/pa1-mt/system
#  phrasal.sh cs224n.vars 1-5 cs224n.ini baseline
#  else 
#  if [ "$1" == "clean" ]; then
#  cd ~/cs224n/pa1-mt/system
#  
#  fi
#fi

cd ~/cs224n/pa1-mt/system

echo $'\nBLEU:'
head newstest2012.BLEU
