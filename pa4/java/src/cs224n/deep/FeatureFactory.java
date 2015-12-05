package cs224n.deep;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import org.ejml.simple.*;


public class FeatureFactory {	

	public static int STARTEND_TOKEN_CNT = 1;

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
	public static HashMap<Integer, String> num2label = new HashMap<Integer, String>();
	public static HashMap<String, Integer> label2num = new HashMap<String, Integer>(); 

	private static List<Datum> read(String filename)
			throws FileNotFoundException, IOException {

		BufferedReader input = new BufferedReader(new FileReader(filename));
		List<Datum> res = new ArrayList<Datum>();
		
		String l = input.readLine();
		while(l != null){
			if (l.equals("-DOCSTART-\tO")) {
				// skip the next blank line
				l = input.readLine();
				continue;
			}
			if (l.trim().length() == 0) {
				for (int i = 0; i < STARTEND_TOKEN_CNT; i++) {
					res.add(new Datum("</s>", ""));
				}
				for (int i = 0; i < STARTEND_TOKEN_CNT; i++) {
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
			if (rowLen == 0) {
        		colLen = 0;
				rowLen = all_bits.length;
			} 
			else if (rowLen != all_bits.length){
				line = input.readLine();
				continue;
			}
			colLen++;
			line = input.readLine();
		}
		
		input = new BufferedReader(new FileReader(vecFilename));
		allVecs = new SimpleMatrix(rowLen, colLen);
   		int col = 0;
   		line = input.readLine();
   		while(line != null){
   			if (line.length() == 0){
   				line = input.readLine();
   				continue;
   			}
			String[] all_bits = line.split("\\s+");
			if (rowLen == all_bits.length) {
				for (int row = 0; row < rowLen; row++) {
					allVecs.set(row, col, Double.parseDouble(all_bits[row]));
				}
				col++;
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
