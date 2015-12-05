package cs224n.deep;
import java.io.*;
import java.lang.*;
import java.util.*;
import org.ejml.data.*;
import org.ejml.simple.*;

import java.text.*;

public class WindowModel {

	public int window_size, word_size, hidden_size;
	public int label_size, vocab_size;
	public int left_shift, right_shift;
	public double alpha, lambda;
	public int iteration;

	public SimpleMatrix L, W, U;
	public SimpleMatrix b_1, b_2;
	public SimpleMatrix h, p, z;

	public class LogLikelihoodRegularizedCostFunction implements ObjectiveFunction {
		
		public WindowModel model;

		public LogLikelihoodRegularizedCostFunction(WindowModel _model) {
			model = _model;
		}
		
		public double valueAt(SimpleMatrix label, SimpleMatrix input) {
	    	SimpleMatrix p = model.feedForward(input);
	    	double logLikelihood = 0;
	    	for (int i=0; i<p.numRows(); i++) {
	    		logLikelihood += (label.get(i) * Math.log(p.get(i)));
	    	}
			double W_norm = model.W.normF();
			double U_norm = model.U.normF();
			double regularization = model.lambda / 2 * (W_norm * W_norm + U_norm * U_norm);

	    	return regularization-logLikelihood;
	    }
	}

	public WindowModel(){
	}

	/**
	 * Initializes the weights randomly. 
	 */
	public void initWeights(){
		double ep_W = Math.sqrt(6.0 / (hidden_size + word_size * window_size));
	    W = SimpleMatrix.random( hidden_size, word_size * window_size, -ep_W, ep_W, new Random());
	    b_1 = SimpleMatrix.random (hidden_size, 1, -ep_W, ep_W, new Random());

	    double ep_U = Math.sqrt(6.0 / (label_size + hidden_size));
		U = SimpleMatrix.random( label_size, hidden_size, -ep_U, ep_U, new Random());
	    b_2 = SimpleMatrix.random (label_size, 1, -ep_U, ep_U, new Random());
	}

	/**
	 * Initializes L randomly. 
	 */
	public void initLRandomly() {
		double ep_L = Math.sqrt(6.0 / (vocab_size + word_size));
		L = SimpleMatrix.random(
			vocab_size,
			word_size,
	    	-ep_L, 
	    	ep_L,
	    	new Random()
		);
	}
	
	/**
	 * Initializes L from provided matrix
	 */
	public void initLWithProvided(SimpleMatrix providedL) {
		L = providedL;
	}

	public SimpleMatrix feedForward(SimpleMatrix x) {
		// z = Wx + b_1
		z = (W.mult(x)).plus(b_1);
		// h = f(z)
		h = new SimpleMatrix(hidden_size, 1);
		for (int i = 0; i < hidden_size; i++) {
			h.set(i, 0, Math.tanh(z.get(i, 0)));
		}
		// temp = Uh + b_2
		SimpleMatrix temp = (U.mult(h)).plus(b_2);
		// softmax g(temp)
		SimpleMatrix exp_temp = new SimpleMatrix(label_size, 1);
		for (int j = 0; j < label_size; j++) {
			exp_temp.set(j, 0, Math.exp(temp.get(j, 0)));	
		}
		double sum = exp_temp.elementSum();
		p = exp_temp.divide(sum);
		return p;
	}

	private static boolean isNumber (String str) {
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	/**
	 * get [x_i-1, x_i, x_i+1] from word vector
	 */
	private SimpleMatrix getX(List<Datum> data, int i) {
		SimpleMatrix x = new SimpleMatrix(word_size * window_size, 1);
		int start_idx = i - window_size/2;
		for (int cnt = 0; cnt < window_size; cnt++) {
			String word = data.get(start_idx).word;
			Integer wordIdx = FeatureFactory.wordToNum.get(word);
			if (wordIdx == null) {
				// check if number => NNNUMMM
				if (isNumber(word)) {
					wordIdx = FeatureFactory.wordToNum.get("NNNUMMM");
				} else {
					// unknown => UUUNKKK
					wordIdx = FeatureFactory.wordToNum.get("UUUNKKK");
				}
			}
			int start_row = cnt * word_size;
			for (int j = 0; j < word_size; j++) {
				x.set(start_row, 0, L.get(j, wordIdx));
				start_row ++;
			}
			start_idx++;
		}
		return x;
	}

	/**
	 * Simplest SGD training 
	 */
	public void train(List<Datum> _trainData ){
		// TODO	
		System.out.println("Data size: " + _trainData.size());

		for (int iter = 0; iter < iteration; iter++) {
			for (int i = 0; i < _trainData.size(); i++) {
				Datum datum = _trainData.get(i);
				String word = datum.word; 
				String label = datum.label;
				// skip the start and end symbols
				if (word.equals("<s>") || word.equals("</s>")) {
					continue;
				}
				SimpleMatrix x = getX(_trainData, i);	// x -> (150 * 1)
				
				// FEED FORWARD
				feedForward(x);

				// BACK PROPAGATION
				// --- calculate gradients
				SimpleMatrix y = new SimpleMatrix(label_size, 1);
				y.set(FeatureFactory.label2num.get(label), 0, 1);
				
				SimpleMatrix delta2 = p.minus(y); // delta2 = p-y = dJ/db2, (5*1)
				SimpleMatrix dJ_dU = delta2.mult(h.transpose()).plus(U.scale(lambda)); // dJ_dU = delta2*hT + lambda*U, (5*100)
				SimpleMatrix h_deriv = new SimpleMatrix(hidden_size, 1); // h'
				for (int h_i = 0; h_i < hidden_size; h_i++) {
					double d = h.get(h_i) * h.get(h_i);
					h_deriv.set(h_i, 0, 1-d);
				} 
				
				SimpleMatrix delta1 = U.transpose().mult(delta2).elementMult(h_deriv);	// delta1 = UT*delta2.*h' = dJ/db1, (100*1)
				SimpleMatrix dJ_dW = delta1.mult(x.transpose()).plus(W.scale(lambda)); // dJ_dW = delta1*xT + lambda*W
				SimpleMatrix dJ_dx = W.transpose().mult(delta1);

				// --- gradient check
				boolean gradient_check = false;
				if (gradient_check) {
					List<SimpleMatrix> matrix_in = new LinkedList<SimpleMatrix>();
					List<SimpleMatrix> matrix_out = new LinkedList<SimpleMatrix>();
					matrix_in.add(U);
					matrix_out.add(dJ_dU);
					matrix_in.add(W);
					matrix_out.add(dJ_dW);
					matrix_in.add(b_2);
					matrix_out.add(delta2);
					matrix_in.add(b_1);
					matrix_out.add(delta1);
					matrix_in.add(x);
					matrix_out.add(dJ_dx);
					if (!GradientCheck.check(y, matrix_in, matrix_out, new LogLikelihoodRegularizedCostFunction(this))) {
						System.out.println("Gradient check not passed!");	
					}
				}

				// --- gradient descent
				U = U.minus(dJ_dU.scale(alpha));
				b_2 = b_2.minus(delta2.scale(alpha));
				W = W.minus(dJ_dW.scale(alpha));
				b_1 = b_1.minus(delta1.scale(alpha));

				int start_idx = i - window_size/2;
				for (int cnt = 0; cnt < window_size; cnt++) {
					String cur_word = _trainData.get(start_idx).word;
					Integer wordIdx = FeatureFactory.wordToNum.get(cur_word);
					if (wordIdx == null) {
						// check if number => NNNUMMM
						if (isNumber(cur_word)) {
							wordIdx = FeatureFactory.wordToNum.get("NNNUMMM");
						} else {
							// unknown => UUUNKKK
							wordIdx = FeatureFactory.wordToNum.get("UUUNKKK");
						}
					}
					int start_row = cnt * word_size;
					for (int j = 0; j < word_size; j++) {
						double updated_value = L.get(j, wordIdx) - alpha*dJ_dx.get(start_row, 0);
						L.set(j, wordIdx, updated_value);
						start_row ++;
					}
					start_idx++;
				}

				if (i % 10000 == 0) {
					System.out.println("iteration: " + iter + " data: " + i);
					monitorLearning(_trainData);
				}	
			}
		}
	}

	public void test(List<Datum> testData, String filename){
		// TODO
		Writer output = null;
        try {
            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "utf-8"));
            for (int i = 0; i < testData.size(); i++) {
            	Datum datum = testData.get(i);
            	String word = datum.word;
            	String label = datum.label;
                if (datum.word.equals("<s>") || datum.word.equals("</s>")) {
                	continue;
                }
                SimpleMatrix x = getX(testData, i);
                int maxLabelId = -1;
                double maxValue = Double.MIN_VALUE;
                for (int row = 0; row < x.numRows(); row++) {
                	if (x.get(row, 0) > maxValue) {
                		maxValue = x.get(row, 0);
                		maxLabelId = row;
                	}
                }
                String predictedLabel = FeatureFactory.num2label.get(maxLabelId);
                output.write(datum.word + "\t" + label + "\t" + predictedLabel + "\n");
                output.close();
            }  
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
	}

	/**
	 * get regularized Cost function
	 */
	public void monitorLearning(List<Datum> data){
		int m = 0;
		double cost = 0;
		double accuracy = 0;
		for (int i = 0; i < data.size(); i++) {
			String label = data.get(i).label;
			String word = data.get(i).word;
			if (word.equals("<s>") || word.equals("</s>")) {
				continue;
			}
			SimpleMatrix x = getX(data, i);
			p = feedForward(x);
			double value = p.get(FeatureFactory.label2num.get(label));
			double increment = 1.0;
			for (int k = 0; k < label_size; k++) {
				cost -= Math.log(p.get(FeatureFactory.label2num.get(label)));
				if (value < p.get(k)) {
					increment = 0.0;
				}
			}
			m++;
			accuracy += increment;
		}
		accuracy /= m;
		double norm_W = W.normF();
		double norm_U = U.normF();
		double regularCost = cost + lambda / 2 * (norm_W * norm_W + norm_U * norm_U);
		System.out.println("Cost:"+ regularCost + " Accuracy:" + accuracy);
	}
	
}
