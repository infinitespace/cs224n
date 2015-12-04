package cs224n.deep;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.ejml.data.*;
import org.ejml.simple.*;

import java.text.*;

public class WindowModel {

	public int window_Size, word_Size, hidden_Size;
	public int label_Size, vocab_Size;
	public int left_shift, right_shift;
	public double lr, lambda;

	public static int LABEL_SIZE = 5;

	protected SimpleMatrix L, W, U;
	protected SimpleMatrix b_1, b_2;
	protected SimpleMatrix h, p, v, y, z;

	/**
	 * Set parameter
	 */
	public WindowModel(int _windowSize, int _wordSize, int _hiddenSize, int _vocabSize, double _lr, double _lambda){
		window_Size = _windowSize;
		word_Size = _wordSize;
		hidden_Size = _hiddenSize;
		
		label_Size = LABEL_SIZE;
		vocab_Size = _vocabSize;

		left_shift = (window_Size - 1) / 2;
		right_shift = window_Size - 1 - left_shift;

		lr = _lr;
		lambda = _lambda;
	}

	/**
	 * Initializes the weights randomly. 
	 */
	public void initWeights(){
		double ep_W = Math.sqrt(6.0 / (hidden_Size + word_Size * window_Size));
	    W = SimpleMatrix.random( hidden_Size, word_Size * window_Size, -ep_W, ep_W, new Random());
	    
	    double ep_U = Math.sqrt(6.0 / (label_Size + hidden_Size));
		U = SimpleMatrix.random( label_Size, hidden_Size, -ep_U, ep_U, new Random());
	    
	    b_2 = new SimpleMatrix(label_Size, 1);
		b_1 = new SimpleMatrix(hidden_Size, 1);
	}

	/**
	 * Initializes L randomly. 
	 */
	public void initRandomL() {
		double ep_L = Math.sqrt(6.0 / (vocab_Size + word_Size));
		L = SimpleMatrix.random(
			vocab_Size,
			word_Size,
	    	-ep_L, 
	    	ep_L,
	    	new Random()
		);
	}
	
	/**
	 * Initializes L from provided matrix
	 */
	public void initProvidedL(SimpleMatrix providedL) {
		if (providedL.numRows() == vocab_Size && providedL.numCols() == word_Size) {
			L = providedL;
		}
	}

	public SimpleMatrix feedForward(SimpleMatrix x) {
		// z = Wx + b_1
		z = (W.mult(x)).plus(b_1);
		// h = f(z)
		h = new SimpleMatrix(hidden_Size, 1);
		for (int j = 0; j < hidden_Size; j++) {
			h.set(j, 0, Math.tanh(z.get(j, 0)));
		}
		// v = Uh + b_2
		v = (U.mult(h)).plus(b_2);
		// softmax g(v)
		SimpleMatrix exp_v = new SimpleMatrix(label_Size, 1);
		for (int j = 0; j < label_Size; j++) {
			exp_v.set(j, 0, Math.exp(v.get(j, 0)));	
		}
		double sum = exp_v.elementSum();
		// p = g(v)
		p = exp_v.divide(sum);
		return p;
	}

	/**
	 * get x_i from word vector
	 */
	private SimpleMatrix getx(List<Datum> data, int i) {
		SimpleMatrix x = new SimpleMatrix(word_Size * window_Size, 1);
		int row = 0;
		for (int j = i - left_shift; j <= i + right_shift; ++j) {
			int idx = 0;
			Integer wordidx = FeatureFactory.wordToNum.get(data.get(j).word);

			if (wordidx == null) {
				try {
					Double.parseDouble(data.get(j).word);
					idx = FeatureFactory.wordToNum.get("NNNUMMM");
				} 
				catch(NumberFormatException e) {}	
			} 
			else idx = wordidx;	
			
			for (int k = 0; k < word_Size; k++) {
				x.set(row, 0, L.get(idx, k));
				row++;
			}
		}
		return x;
	}

	/**
	 * get regularized Cost function
	 */
	public void printCost(List<Datum> data){
		int m = 0;
		double cost = 0;
		double accuracy = 0;
		for (int i = left_shift; i < data.size() - right_shift; ++i) {
			String label = data.get(i).label;
			String word = data.get(i).word;
			if (word != "<s>" && word != "</s>") {
				SimpleMatrix xs = getx(data, i);
				p = feedForward(xs);
				double labelP = p.get(FeatureFactory.label2num.get(label));
				double inc = 1.0;
				for (int k = 0; k < label_Size; k++) {
					cost -= Math.log(p.get(FeatureFactory.label2num.get(label)));
					if (labelP < p.get(k)) {
						inc = 0.0;
					}
				}
				m++;
				accuracy += inc;
			}
		}
		accuracy /= m;
		double norm_W = W.normF();
		double norm_U = U.normF();
		double regularCost = cost + lambda / 2 * (norm_W * norm_W + norm_U * norm_U);
		System.out.println("Cost:"+ regularCost + " Accuracy:" + accuracy);
	}

	public double regularValue() {
		double normW = W.normF();
		double normU = U.normF();
		return lambda / 2 * (normW * normW + normU * normU);
	}

	/**
	 * Simplest SGD training 
	 */
	public void train(List<Datum> _trainData ){
		
	}

	
	public void test(List<Datum> testData){
		// TODO
	}
	
}
