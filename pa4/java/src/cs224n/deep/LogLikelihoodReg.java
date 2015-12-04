package cs224n.deep;
import org.ejml.data.*;
import org.ejml.simple.*;

public class LogLikelihoodReg implements ObjectiveFunction {
	private WindowModel model;
	public LogLikelihoodReg(WindowModel _model) {
		model = _model;
	}
	
	public double valueAt(SimpleMatrix label, SimpleMatrix input) {
    	SimpleMatrix p = model.feedForward(input);
    	double rs = 0;
    	for (int i=0; i<p.numRows(); i++) {
    		rs += (label.get(i) * Math.log(p.get(i)));
    	}
    	return model.regularValue()-rs;
    }
}
