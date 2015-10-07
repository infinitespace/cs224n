##### First, setup environment
cp -r /afs/ir/class/cs224n/cs224n-pa/pa1-mt .

source setup.sh

##### for the very first time to run
sh run.sh new

##### To modify My features, just play with 

vi ./java/src/cs224n/MyFeaturizer.java

##### To rebuild and train the system to get a new BLEU, just run

sh run.sh re

##### To show recent result, just run

sh run.sh n
