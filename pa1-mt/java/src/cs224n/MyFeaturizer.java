package edu.stanford.nlp.mt.decoder.feat;

import java.util.List;
import java.util.Collections;
import edu.stanford.nlp.mt.util.FeatureValue;
import edu.stanford.nlp.mt.util.Featurizable;
import edu.stanford.nlp.mt.util.IString;
import edu.stanford.nlp.mt.decoder.feat.RuleFeaturizer;
import edu.stanford.nlp.util.Generics;
import edu.stanford.nlp.mt.util.PhraseAlignment;
/**
 * A rule featurizer.
 */
public class MyFeaturizer implements RuleFeaturizer<IString, String> {
  
  
  private static final String FEATURE_PREFIX = "ACNT";
 
  @Override
  public void initialize() {
    // Do any setup here.
  }

  @Override
  public List<FeatureValue<String>> ruleFeaturize(
      Featurizable<IString, String> f) {
  PhraseAlignment alignment = f.rule.abstractRule.alignment;
    boolean[] sourceCoverage = new boolean[f.sourcePhrase.size()];
    
    int nUnalignedTarget = 0;
    for (int i = 0, max = f.targetPhrase.size(); i < max; ++i) {
      int[] alignments = alignment.t2s(i);
      if (alignments == null) {
        // Unaligned target token
        ++nUnalignedTarget;
      } else {
        for (int j : alignments) {
          sourceCoverage[j] = true;
        }
      }
    }
    int nUnalignedSource = 0;
    for (boolean covered : sourceCoverage) {
      // Unaligned source token
      if (! covered) ++nUnalignedSource;
    }
    
    List<FeatureValue<String>> features = Generics.newLinkedList();
    features.add(new FeatureValue<String>(FEATURE_PREFIX + "src", nUnalignedSource));
    features.add(new FeatureValue<String>(FEATURE_PREFIX + "tgt", nUnalignedTarget));
    /*
    // TODO: Return a list of features for the rule. Replace these lines
    // with your own feature.
    List<FeatureValue<String>> features = Generics.newLinkedList();
    // features.add(new FeatureValue<String>("MyFeature", 1.0));
    */
    // features.add(new FeatureValue<String>(String.format("%s:%d","TGTD", f.targetPhrase.size()), 1.0));

    double ratio = (double) f.targetPhrase.size() / (double) f.sourceSentence.size();
    features.add(new FeatureValue<String>("RATIO", ratio));
    return features;
  }

  @Override
  public boolean isolationScoreOnly() {
    return false;
  }
}
