package cs224n.deep;

import java.util.*;
import java.io.*;

import org.ejml.simple.SimpleMatrix;


public class NER {
    
    public static void main(String[] args) throws IOException {
		if (args.length < 2) {
		    System.out.println("USAGE: java -cp \"classes:extlib/*\" cs224n.deep.NER ../data/train ../data/dev");
		    return;
		}	    

		// this reads in the train and test datasets
		List<Datum> trainData = FeatureFactory.readTrainData(args[0]);
		List<Datum> testData = FeatureFactory.readTestData(args[1]);	
		
		// read the train and test data
		// TODO: Implement this function (just reads in vocab and word vectors)
		FeatureFactory.initializeVocab("../data/vocab.txt");
		SimpleMatrix allVecs= FeatureFactory.readWordVectors("../data/wordVectors.txt");

		// System.out.println("allVecs: numCols" + allVecs.numCols());
		// System.out.println("allVecs: numRows" + allVecs.numRows());
		// System.out.println(allVecs.extractMatrix(0,allVecs.numRows(),1,2));

		// initialize model 
		WindowModel model = new WindowModel();
		model.window_size = 3;
		model.word_size = allVecs.numRows();
		model.hidden_size = 100;
		model.label_size = 5;
		model.vocab_size = allVecs.numCols();
		model.alpha = 0.02;
		model.lambda = 0.001;
		model.iteration = 1;

		model.initWeights();
		model.initLWithProvided(allVecs);

		//TODO: Implement those two functions
		model.train(trainData);
		model.test(trainData, "test_train.out");
		model.test(testData, "test_test.out");
	}
}