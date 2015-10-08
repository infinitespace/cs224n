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
public class PMIAligner implements WordAligner {

  private static final long serialVersionUID = 1315751943476440515L;
  
  // TODO: Use arrays or Counters for collecting sufficient statistics
  // from the training data.
  private CounterMap<String,String> sourceTargetCounts;

  public Alignment align(SentencePair sentencePair) {
    // Placeholder code below. 
    // TODO Implement an inference algorithm for Eq.1 in the assignment
    // handout to predict alignments based on the counts you collected with train().
    Alignment alignment = new Alignment();
    int numSourceWords = sentencePair.getSourceWords().size();
    int numTargetWords = sentencePair.getTargetWords().size();

    // YOUR CODE HERE
    for (int srcIdx = 0; srcIdx < numSourceWords; srcIdx++) {
      String sourceWord = sentencePair.getSourceWords().get(srcIdx);

      int alignIdx = -1;
      double maxPMI = sourceTargetCounts.getCount(NULL_WORD, sourceWord);

      for (int tgtIdx = 0; tgtIdx < numTargetWords; tgtIdx++) {
        String targetWord = sentencePair.getTargetWords().get(tgtIdx);
        double curPMI = sourceTargetCounts.getCount(targetWord, sourceWord);
        if (curPMI > maxPMI) {
          maxPMI = curPMI;
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
    sourceTargetCounts = new CounterMap<String,String>();

    for (SentencePair trainingPair : trainingPairs) {
      List<String> sourceWords = trainingPair.getSourceWords();
      List<String> targetWords = trainingPair.getTargetWords();
      for (String sourceWord : sourceWords)
      {
        for (String targetWord : targetWords)
        {
          sourceTargetCounts.incrementCount(targetWord, sourceWord, 1.0);
        }
        sourceTargetCounts.incrementCount(NULL_WORD, sourceWord, 1.0);
      }
    }

    sourceTargetCounts = Counters.conditionalNormalize(sourceTargetCounts);
  }
}
