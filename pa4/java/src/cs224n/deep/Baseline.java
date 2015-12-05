package cs224n.deep;

import java.util.*;
import java.io.*;
import org.ejml.simple.SimpleMatrix;



public class Baseline {

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("USAGE: java -cp classes cs224n.deep.Baseline ../data/train ../data/dev");
            return;
        }       
        
        // reads in the train and test datasets
        System.out.println("Start reading data");  
        List<Datum> train = FeatureFactory.readTrainData(args[0]);
        List<Datum> test = FeatureFactory.readTestData(args[1]);
        System.out.println("Finished reading data");   
        
        // map word to label
        Map<String, String> word2label = new HashMap<String, String>();
        for (Datum datum: train) {
            word2label.put(datum.word, datum.label);
        }
        
        Writer output = null;
        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Baseline.out"), "utf-8"));
            for (Datum datum: test) {
                String word = datum.word;
                if (word.equals("<s>") || word.equals("</s>")) {
                    continue;
                }

                String label = word2label.get(datum.word);
                if(label == null) label = "O";
                output.write(datum.word + "\t" + datum.label + "\t" + label + "\n");
                // System.out.println(datum.word + "\t" + datum.label + "\t" + label + "\n");
            }
            System.out.println("Finished Baseline");  
            output.close(); 
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }   
}