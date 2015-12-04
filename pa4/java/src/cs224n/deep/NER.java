package cs224n.deep;

import java.util.*;
import java.io.*;

import org.ejml.simple.SimpleMatrix;


public class NER {
    
    public static void main(String[] args) throws IOException {
		if (args.length < 2) {
		    System.out.println("USAGE: java -cp classes cs224n.deep.NER ../data/train ../data/dev");
		    return;
		}	    

		// this reads in the train and test datasets
		List<Datum> trainData = FeatureFactory.readTrainData(args[0]);
		List<Datum> testData = FeatureFactory.readTestData(args[1]);	
		
		//	read the train and test data
		//TODO: Implement this function (just reads in vocab and word vectors)
		FeatureFactory.initializeVocab("../data/vocab.txt");
		SimpleMatrix allVecs= FeatureFactory.readWordVectors("../data/wordVectors.txt");

		// initialize model 
		WindowModel model = new WindowModel(
			7, /* window size */
		allVecs.numCols(), /* word size */
		100, /* hidden size */
		allVecs.numRows(), /* vocabulary size */
		0.02, /* learning rate */
		0.0001 /* regularization parameter */
		);
		model.initWeights();

		model.initProvidedL(allVecs);

		model.train(trainData);

		//TODO: Implement those two functions
		//model.train(trainData);
		//model.test(testData);
	    }
}