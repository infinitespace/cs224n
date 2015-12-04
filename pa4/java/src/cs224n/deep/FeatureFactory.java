package cs224n.deep;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.ejml.simple.*;


public class FeatureFactory {	

	private FeatureFactory() {

	}
	 
	static List<Datum> trainData;
	/** Do not modify this method **/
	public static List<Datum> readTrainData(String filename) throws IOException {
		System.out.println("Start loading training data");  
        if (trainData==null) trainData= read(filename);
        return trainData;
	}
	
	static List<Datum> testData;
	/** Do not modify this method **/
	public static List<Datum> readTestData(String filename) throws IOException {
		System.out.println("Start loading test data");  
        if (testData==null) testData= read(filename);
        return testData;
	}
	
	static int labelIndex = 0;
	public static int PADDING_LEN = 3;
	public static HashMap<Integer, String> num2label = new HashMap<Integer, String>();
	public static HashMap<String, Integer> label2num = new HashMap<String, Integer>(); 

	private static List<Datum> read(String filename)
			throws FileNotFoundException, IOException {

		BufferedReader input = new BufferedReader(new FileReader(filename));
		List<Datum> res = new ArrayList<Datum>();
	    int paddingLen = PADDING_LEN;
		
		String l = input.readLine();
		while(l != null){
			if (l.trim().length() == 0) {
				for (int i = 0; i < paddingLen; i++) {
					res.add(new Datum("</s>", ""));
				}
				for (int i = 0; i < paddingLen; i++) {
					res.add(new Datum("<s>", ""));
				}
				l = input.readLine();
				continue;
			}

			String[] all_bits = l.split("\\s+");
			String word = all_bits[0].toLowerCase();
			String label = all_bits[1];

			Datum datum = new Datum(word, label);

			res.add(datum);
			
			if (!label2num.containsKey(label)) {
				num2label.put(labelIndex, label);
				label2num.put(label, labelIndex);
				labelIndex++;
			}
			l = input.readLine();
		}

		return res;
	}
 
 
	// Look up table matrix with all word vectors as defined in lecture with dimensionality n x |V|
	static SimpleMatrix allVecs; //access it directly in WindowModel
	public static SimpleMatrix readWordVectors(String vecFilename) throws IOException {
		if (allVecs != null) return allVecs;

		BufferedReader input = new BufferedReader(new FileReader(vecFilename));
		int rowLen = 0;
		int colLen = 0;

		String line = input.readLine();
		while(line != null){
			if (line.length() == 0){
				line = input.readLine();
				continue;
			}
			String[] all_bits = line.split("\\s+");
			if (colLen == 0) {
        		rowLen = 0;
				colLen = all_bits.length;
			} 
			else if (colLen != all_bits.length){
				line = input.readLine();
				continue;
			}
			rowLen++;
			line = input.readLine();
		}
		
		input = new BufferedReader(new FileReader(vecFilename));
		allVecs = new SimpleMatrix(rowLen, colLen);
   		int row = 0;
   		line = input.readLine();
   		while(line != null){
   			if (line.length() == 0){
   				line = input.readLine();
   				continue;
   			}
			String[] all_bits = line.split("\\s+");
			if (colLen == all_bits.length) {
				for (int col = 0; col < colLen; col++) {
					allVecs.set(row, col, Double.parseDouble(all_bits[col]));
				}
				row++;
			}
   			line = input.readLine();
   		}

		return allVecs;

	}
	// might be useful for word to number lookups, just access them directly in WindowModel
	public static HashMap<String, Integer> wordToNum = new HashMap<String, Integer>(); 
	public static HashMap<Integer, String> numToWord = new HashMap<Integer, String>();

	public static HashMap<String, Integer> initializeVocab(String vocabFilename) throws IOException {
		BufferedReader input = new BufferedReader(new FileReader(vocabFilename));
   		int id = 0;
		for (String line = input.readLine(); line != null; line = input.readLine()) {
			if (line.length() == 0) {
				continue;
			}
			wordToNum.put(line, id);
			numToWord.put(id, line);
			id++;
		}
		return wordToNum;
	}
 
}
