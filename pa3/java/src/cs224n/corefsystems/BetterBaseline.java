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
          List<Entity> clusters = pair.getSecond();
          for(Entity e : clusters){
            System.out.println(e);
            for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
                String key = getKeyFromMentionPair(mentionPair.getFirst(), mentionPair.getSecond());
                if(!handwriten.containsKey(key)){
                    // System.out.println("key:" + key);
                    handwriten.put(key, e);
                }
            }
          }
        }

	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {

    	ArrayList<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
        HashSet<Integer> clusteredMentions = new HashSet();

    	for (Mention m1 : doc.getMentions()) {
            for (Mention m2 : doc.getMentions()) {
                if(!m1.equals(m2)){
                    String key = getKeyFromMentionPair(m1, m2);
                    if(handwriten.containsKey(key)){
                        Entity e = handwriten.get(key);
                        if(!clusteredMentions.contains(m1.hashCode())){
                            mentions.add(m1.markCoreferent(e));
                            clusteredMentions.add(m1.hashCode());
                        }
                        if(!clusteredMentions.contains(m2.hashCode())){
                            mentions.add(m2.markCoreferent(e));
                            clusteredMentions.add(m2.hashCode());
                        }
                    }
                }
            }
        }

        // System.out.println(" org " + Integer.toString(doc.getMentions().size()));
        // System.out.println(" pre " + Integer.toString(clusteredMentions.size()));
        // for(ClusteredMention m : mentions){
        //     System.out.println(m.toString());
        // }
        
        for (Mention m : doc.getMentions()) {
            int hashcode = m.hashCode();
            if(!clusteredMentions.contains(hashcode)){
                ClusteredMention newCluster = m.markSingleton();
                mentions.add(newCluster);
                clusteredMentions.add(hashcode);
            }
        }
        // System.out.println(" aft " + Integer.toString(clusteredMentions.size()));
        // for(ClusteredMention m : mentions){
        //     System.out.println(m.toString());
        // }
    	return mentions;
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
