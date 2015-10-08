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
public class IBMModel1Aligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> tranFE;

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

      int alignIdx = -1;
      double maxP = tranFE_F.getCount(NULL_WORD);

      for (int tgtIdx = 0; tgtIdx < numTargetWords; tgtIdx++) {
        String targetWord = sentencePair.getTargetWords().get(tgtIdx);
        double curP = tranFE_F.getCount(targetWord);
        if (curP > maxP) {
          maxP = curP;
          alignIdx = tgtIdx;
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
    // YOUR CODE HERE
    tranFE = new CounterMap<String,String>();
    
    double totalCount = 0;

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

      // scale the diff with totalCount()
      if (totalCount == 0)
        totalCount = tranFE.totalCount();

      diff = diff / (totalCount * totalCount);
      if (diff < 1e-6)
        break;
    }

  }
}
