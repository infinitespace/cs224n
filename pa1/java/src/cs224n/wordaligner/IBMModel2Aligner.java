package cs224n.wordaligner;  

import cs224n.util.*;
import java.util.List;

/**
 * Simple word alignment baseline model that maps source positions to target 
 * positions along the diagonal of the alignment grid.
 * 
 * IMPORTANT: Make sure that you read the comments in the
 * cs224n.wordaligner.WordAligner interface.
 * 
 * @author Dan Klein
 * @author Spence Green
 */
public class IBMModel2Aligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> tranFE;
  private CounterMap<String,Integer> qILMj;

  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();
    int numSourceWords = sentencePair.getSourceWords().size();
    int numTargetWords = sentencePair.getTargetWords().size();

    for (int srcIdx = 0; srcIdx < numSourceWords; srcIdx++) {
      String sourceWord = sentencePair.getSourceWords().get(srcIdx);
      Counter<String> tranFE_F = tranFE.getCounter(sourceWord);
      Counter<Integer> qILMj_ILM = qILMj.getCounter(position(srcIdx, numTargetWords+1, numSourceWords));

      int alignIdx = -1;

      if (qILMj_ILM.containsKey(-1)){
        double maxP = tranFE_F.getCount(NULL_WORD) * qILMj_ILM.getCount(-1);

        for (int tgtIdx = 0; tgtIdx < numTargetWords; tgtIdx++) {
          String targetWord = sentencePair.getTargetWords().get(tgtIdx);
          double curP = tranFE_F.getCount(targetWord) * qILMj_ILM.getCount(tgtIdx);
          if (curP > maxP) {
            maxP = curP;
            alignIdx = tgtIdx;
          }
        }
      } else {
        double maxP = tranFE_F.getCount(NULL_WORD);

        for (int tgtIdx = 0; tgtIdx < numTargetWords; tgtIdx++) {
          String targetWord = sentencePair.getTargetWords().get(tgtIdx);
          double curP = tranFE_F.getCount(targetWord);
          if (curP > maxP) {
            maxP = curP;
            alignIdx = tgtIdx;
          }
        }
      }

      if (alignIdx >= 0)
      {
        alignment.addPredictedAlignment(alignIdx, srcIdx);
      }
    }
   
    return alignment;
  }

  public void train(List<SentencePair> trainingPairs) {
    
    trainTranFEWithM1(trainingPairs);

    // use tranFE as init value to start IBM_M2
    qILMj = new CounterMap<String,Integer>();

    double tranFE_total = 0;
    double qILMj_total = 0;

    for (int iter = 0; iter < 10; iter++) {
      CounterMap<String,String> counterEF = new CounterMap<String,String>();
      Counter<String> counterE = new Counter<String>();
      CounterMap<String,Integer> counterILMj = new CounterMap<String,Integer>();
      Counter<String> counterILM = new Counter<String>();

      for (SentencePair trainingPair : trainingPairs) {
        List<String> sourceWords = trainingPair.getSourceWords();
        List<String> targetWords = trainingPair.getTargetWords();
        for (int srcIdx = 0; srcIdx < sourceWords.size(); srcIdx++) {
          String sourceWord = sourceWords.get(srcIdx);

          // sum up q(j|i,l,m)t(fi|ej) for j= 1...l
          Counter<String> tranFE_F = tranFE.getCounter(sourceWord);
          Counter<Integer> qILMj_ILM = qILMj.getCounter(position(srcIdx, targetWords.size()+1, sourceWords.size()));
          double sumTtimesQ = 0;
          for (int tgtIdx = 0; tgtIdx < targetWords.size(); tgtIdx++) {
            String targetWord = targetWords.get(tgtIdx);
            if (!tranFE_F.containsKey(targetWord)) {
              tranFE_F.setCount(targetWord, 1.0);
            }
            if (!qILMj_ILM.containsKey(tgtIdx)) {
              qILMj_ILM.setCount(tgtIdx, 1.0);
            }
            sumTtimesQ += (tranFE_F.getCount(targetWord) * qILMj_ILM.getCount(tgtIdx));
          }
          if (!tranFE_F.containsKey(NULL_WORD)) {
            tranFE_F.setCount(NULL_WORD, 1.0);
          }
          if (!qILMj_ILM.containsKey(-1)) {
            qILMj_ILM.setCount(-1, 1.0);
          }
          sumTtimesQ += (tranFE_F.getCount(NULL_WORD) * qILMj_ILM.getCount(-1));
          
          // update counters
          for (int tgtIdx = 0; tgtIdx < targetWords.size(); tgtIdx++) {
            String targetWord = targetWords.get(tgtIdx);
            // get delta
            double delta = tranFE_F.getCount(targetWord) * qILMj_ILM.getCount(tgtIdx) / sumTtimesQ;
            counterEF.incrementCount(targetWord, sourceWord, delta);
            counterE.incrementCount(targetWord, delta);
            counterILMj.incrementCount(position(srcIdx, targetWords.size()+1, sourceWords.size()), tgtIdx, delta);
            counterILM.incrementCount(position(srcIdx, targetWords.size()+1, sourceWords.size()), delta);
          }
          double delta = tranFE_F.getCount(NULL_WORD) / sumTtimesQ;
          counterEF.incrementCount(NULL_WORD, sourceWord, delta);
          counterE.incrementCount(NULL_WORD, delta);
          counterILMj.incrementCount(position(srcIdx, targetWords.size()+1, sourceWords.size()), -1, delta);
          counterILM.incrementCount(position(srcIdx, targetWords.size()+1, sourceWords.size()), delta);
        }
      }

      // t(f|e) = c(e,f)/c(e)
      double t_diff = 0;
      for (String keyF : tranFE.keySet()) {
        Counter<String> tranFE_F = tranFE.getCounter(keyF);
        for (String keyE : tranFE_F.keySet()) {
          double newTranFE = counterEF.getCount(keyE, keyF)/counterE.getCount(keyE);
          t_diff += ((tranFE.getCount(keyF,keyE) - newTranFE) * (tranFE.getCount(keyF,keyE) - newTranFE));
          tranFE.setCount(keyF, keyE, newTranFE);
        }
      }

      // q(j|i,l,m) = c(j|i,l,m) / c(i,l,m)
      double q_diff = 0;
      for (String keyILM : qILMj.keySet()) {
        Counter<Integer> qILMj_ILM = qILMj.getCounter(keyILM);
        for (Integer keyJ : qILMj_ILM.keySet()) {
          double newQILMj = counterILMj.getCount(keyILM, keyJ)/counterILM.getCount(keyILM);
          q_diff += ((qILMj.getCount(keyILM, keyJ) - newQILMj) * (qILMj.getCount(keyILM, keyJ) - newQILMj));
          qILMj.setCount(keyILM, keyJ, newQILMj);
        }
      }

      // scale the diff with tranFE_total & qILMj_total
      if (tranFE_total == 0)
        tranFE_total = tranFE.totalCount();
      if (qILMj_total == 0)
        qILMj_total = qILMj.totalCount();

      t_diff = t_diff / (tranFE_total * tranFE_total);
      q_diff = q_diff / (qILMj_total * qILMj_total);

      if ((t_diff < 1e-6) && (q_diff < 1e-6)) {
        break;
      }
    }
  }

  public void trainTranFEWithM1(List<SentencePair> trainingPairs) {
    // run IBM_M1 for init t(f|e) value
    tranFE = new CounterMap<String,String>();
    
    double tranFE_total = 0;

    for (int iter = 0; iter < 50; iter++) {
      CounterMap<String,String> counterEF = new CounterMap<String,String>();
      Counter<String> counterE = new Counter<String>();
      for (SentencePair trainingPair : trainingPairs) {
        List<String> sourceWords = trainingPair.getSourceWords();
        List<String> targetWords = trainingPair.getTargetWords();
        for (int srcIdx = 0; srcIdx < sourceWords.size(); srcIdx++) {
          String sourceWord = sourceWords.get(srcIdx);
          
          // sum up t(fi|ej) for j = 1...l
          Counter<String> tranFE_F = tranFE.getCounter(sourceWord);
          double sumTranFE = 0;
          for (int tgtIdx = 0; tgtIdx < targetWords.size(); tgtIdx++) {
            String targetWord = targetWords.get(tgtIdx);
            if (!tranFE_F.containsKey(targetWord)) {
              tranFE_F.setCount(targetWord, 1.0);
            }
            sumTranFE += tranFE_F.getCount(targetWord);
          }
          if (!tranFE_F.containsKey(NULL_WORD)) {
            tranFE_F.setCount(NULL_WORD, 1.0);
          }
          sumTranFE += tranFE_F.getCount(NULL_WORD);

          // update counters
          for (int tgtIdx = 0; tgtIdx < targetWords.size(); tgtIdx++) {
            String targetWord = targetWords.get(tgtIdx);
            // get delta
            double delta = tranFE_F.getCount(targetWord) / sumTranFE;
            counterEF.incrementCount(targetWord, sourceWord, delta);
            counterE.incrementCount(targetWord, delta);
          }
          double delta = tranFE_F.getCount(NULL_WORD) / sumTranFE;
          counterEF.incrementCount(NULL_WORD, sourceWord, delta);
          counterE.incrementCount(NULL_WORD, delta);
        }
      }

      // t(f|e) = c(e,f)/c(e)
      double diff = 0;
      for (String keyF : tranFE.keySet()) {
        Counter<String> tranFE_F = tranFE.getCounter(keyF);
        for (String keyE : tranFE_F.keySet()) {
          double newTranFE = counterEF.getCount(keyE, keyF)/counterE.getCount(keyE);
          diff += ((tranFE.getCount(keyF,keyE) - newTranFE) * (tranFE.getCount(keyF,keyE) - newTranFE));
          tranFE.setCount(keyF, keyE, newTranFE);
        }
      }

      // scale the diff with tranFE_total
      if (tranFE_total == 0)
        tranFE_total = tranFE.totalCount();

      diff = diff / (tranFE_total * tranFE_total);
      if (diff < 1e-6)
        break;
    }
  }

  public String position(int i, int l, int m) {
    return String.format("%d,%d,%d", i, l, m);
  }
}
