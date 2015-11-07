package cs224n.corefsystems;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.*;


import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.*;
import cs224n.util.Pair;

public class BetterBaseline implements CoreferenceSystem {

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
        handwriten = new HashMap();
        for(Pair<Document, List<Entity>> pair : trainingData){
          //--Get Variables
          // Document doc = pair.getFirst();
          List<Entity> clusters = pair.getSecond();
          // List<Mention> mentions = doc.getMentions();
          //--Print the Document
          // System.out.println("============ doc ==============");
          // System.out.println(doc.prettyPrint(clusters));
          // System.out.println("========== mentions ===========");
          //--Iterate over mentions
          // for(Mention m : mentions){
          //  // System.out.println(m);
          // }
          // System.out.println("======== mention pairs ========");
          //--Iterate Over Coreferent Mention Pairs
          for(Entity e : clusters){
            for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
                List <Mention> mlist = new ArrayList<Mention>();
                mlist.add(mentionPair.getFirst());
                mlist.add(mentionPair.getSecond());
                String key = getKeyFromMentionPair(mlist.get(0), mlist.get(1));

                System.out.println(""+mentionPair.getFirst() + " and " + mentionPair.getSecond() + " are coreferent");
                System.out.println("key:" + key);

                if(!handwriten.containsKey(key)){
                    handwriten.put(key, e);
                }
            }
          }
        }

	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {
		// TODO Auto-generated method stub
        HashSet clusteredMentions = new HashSet();
    	ArrayList<ClusteredMention> clusters = new ArrayList<ClusteredMention>();
    	for (Mention m1 : doc.getMentions()) {
            for (Mention m2 : doc.getMentions()) {
                if(!m1.equals(m2)){
                    String key = getKeyFromMentionPair(m1, m2);
                    if(handwriten.containsKey(key)){
                        Entity e = new Entity(handwriten.get(key));
                        if(!clusteredMentions.contains(m1.hashCode())){
                            clusters.add(m1.markCoreferent(e));
                            clusteredMentions.add(m1.hashCode());
                        }
                        if(!clusteredMentions.contains(m2.hashCode())){
                            clusters.add(m2.markCoreferent(e));
                            clusteredMentions.add(m2.hashCode());
                        }
                    }
                }
            }
        }
        for (Mention m : doc.getMentions()) {
            int hashcode = m.hashCode();
            if(!clusteredMentions.contains(hashcode)){
                ClusteredMention newCluster = m.markSingleton();
                clusters.add(newCluster);
                clusteredMentions.add(hashcode);
            }
        }
    	return clusters;
	}

    private HashMap<String, Entity> handwriten;
    private String getKeyFromMentionPair(Mention m1, Mention m2){
        String token1 = m1.headToken().word();
        String token2 = m2.headToken().word();
        String[] tokens = {token1, token2};
        Arrays.sort(tokens);
        return tokens[0] + tokens[1];
    }

}
