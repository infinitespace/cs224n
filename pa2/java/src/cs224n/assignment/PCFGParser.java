package cs224n.assignment;

import cs224n.ling.Tree;
import java.util.*;

/**
 * The CKY PCFG Parser you will implement.
 */
public class PCFGParser implements Parser {
    private Grammar grammar;
    private Lexicon lexicon;

    public void train(List<Tree<String>> trainTrees) {
        // binarize training trees
        List<Tree<String>> binarizedTrees = new LinkedList<Tree<String>>();
        for (Tree<String> tree : trainTrees) {
            binarizedTrees.add(TreeAnnotations.annotateTree(tree));
        }
        lexicon = new Lexicon(binarizedTrees);
        grammar = new Grammar(binarizedTrees);
        // System.out.println("Finished Training");
    }

    public Tree<String> getBestParse(List<String> sentence) {
        int numWords = sentence.size();
        TriMap<Double> score = new TriMap<Double>(numWords);
        TriMap<Tree<String>> back = new TriMap<Tree<String>>(numWords);

        Set<String> tags = lexicon.getAllTags();

        for (int i = 0; i < numWords; i++) {
            String word = sentence.get(i);
            for (String tag : tags) {
                score.put(i, i+1, tag, lexicon.scoreTagging(word, tag));
                back.put(i, i+1, tag, new Tree<String>(tag, Collections.singletonList(new Tree<String>(word))));
            }
            // handle unaries
            boolean added = true;
            while (added) {
                added = false;
                Set<String> tagSet = score.getKeySet(i, i+1);
                List<String> tagList = new ArrayList<String>(tagSet);
                for (String tag : tagList) {
                    for (Grammar.UnaryRule rule : grammar.getUnaryRulesByChild(tag)) {
                        double prob = rule.getScore() * score.get(i, i+1, tag);
                        if ((score.get(i, i+1, rule.getParent()) == null) || (prob > score.get(i, i+1, rule.getParent()))) {
                            score.put(i, i+1, rule.getParent(), prob);
                            back.put(i, i+1, rule.getParent(), new Tree<String>(rule.getParent(), Collections.singletonList(back.get(i, i+1, tag))));
                            added = true;
                        }
                    }
                }
            }
        }

        // System.out.println(score);
        // System.out.println(back);

        for (int span = 2; span <= numWords; span++) {
            for (int begin = 0; begin <= numWords - span; begin++) {
                int end = begin + span;
                for (int split = begin+1; split <= end-1; split++) {
                    for (String tagB : score.getKeySet(begin, split)) {
                        // tagB in A->BC
                        for (Grammar.BinaryRule rule : grammar.getBinaryRulesByLeftChild(tagB)) {
                            // tagA, tagC in A->BC
                            String tagA = rule.getParent();
                            String tagC = rule.getRightChild();
                            double prob = 0;
                            if (score.get(split, end, tagC) == null) {
                                continue;
                            }
                            prob = score.get(begin, split, tagB) * score.get(split, end, tagC) * rule.getScore();
                            if ((score.get(begin, end, tagA) == null) || (prob > score.get(begin, end, tagA))) {
                                score.put(begin, end, tagA, prob);
                                List<Tree<String>> children = new ArrayList<Tree<String>>(2);
                                children.add(back.get(begin, split, tagB));
                                children.add(back.get(split, end, tagC));
                                back.put(begin, end, tagA, new Tree<String>(tagA, children));
                            }
                        }
                    }
                }  

                // handle unaries
                boolean added = true;
                while (added) {
                    added = false;
                    Set<String> tagSet = score.getKeySet(begin, end);
                    List<String> tagList = new ArrayList<String>(tagSet);
                    for (String tagB : tagList) {
                        for (Grammar.UnaryRule rule : grammar.getUnaryRulesByChild(tagB)) {
                            double prob = rule.getScore() * score.get(begin, end, tagB);
                            String tagA = rule.getParent();
                            if ((score.get(begin, end, tagA) == null) || (prob > score.get(begin, end, tagA))) {
                                score.put(begin, end, tagA, prob);
                                back.put(begin, end, tagA, new Tree<String>(tagA, Collections.singletonList(back.get(begin, end, tagB))));
                                added = true;
                            }
                        }
                    }
                }
            }
        }

        // System.out.println(score);
        // System.out.println(back);

        Tree<String> parseTree = back.get(0, numWords, "ROOT");
        Tree<String> resultTree = TreeAnnotations.unAnnotateTree(parseTree);
        
        // System.out.println(parseTree);

        return resultTree;
    }

    private class TriMap<T> {

        private ArrayList<HashMap<String,T>> storeArray;
        private int numWords = 0;

        public TriMap (int _numWords) {
            numWords = _numWords;
            storeArray = new ArrayList<HashMap<String,T>>((numWords+1)*(numWords+1));
            for (int i = 0; i < (numWords+1)*(numWords+1); i++) {
                storeArray.add(new HashMap<String, T>());
            }
        }

        public T get (int i, int j, String nonTerm) {
            HashMap<String, T> map = storeArray.get(i*(numWords+1)+j);
            return map.get(nonTerm);
        }

        public void put (int i, int j, String nonTerm, T value) {
            HashMap<String, T> map = storeArray.get(i*(numWords+1)+j);
            map.put(nonTerm, value);
        }

        public Set<String> getKeySet (int i, int j) {
            HashMap<String, T> map = storeArray.get(i*(numWords+1)+j);
            return map.keySet();
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numWords + 1; i++) {
                for (int j = 0; j < numWords + 1; j++) {
                    HashMap<String, T> map = storeArray.get(i*(numWords+1)+j);
                    for (String key : map.keySet())
                    {
                        sb.append("(" + i + "," + j + ")" + key + ":" + map.get(key).toString() + '\n');
                    }
                }
            }
            return sb.toString();
        }
    }
}
