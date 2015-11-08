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

public class RuleBased implements CoreferenceSystem {

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		handwriten = new HashMap();
        for(Pair<Document, List<Entity>> pair : trainingData){
          List<Entity> clusters = pair.getSecond();
          for(Entity e : clusters){
            // System.out.println(e);
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
        HashSet<Mention> clusteredMentions = new HashSet();

        for (Mention m1 : doc.getMentions()) {
            for (Mention m2 : doc.getMentions()) {
                String key = getKeyFromMentionPair(m1, m2);
                if(handwriten.containsKey(key)){
                    if(!clusteredMentions.contains(m1)){
                        mentions.add(m1.markCoreferent(handwriten.get(key)));
                        clusteredMentions.add(m1);
                    }
                    if(!clusteredMentions.contains(m2)){
                        mentions.add(m2.markCoreferent(handwriten.get(key)));
                        clusteredMentions.add(m2);
                    }
                }
            }
        }
        System.out.println("before:" + Integer.toString(clusteredMentions.size()));
        for (Mention m : doc.getMentions()) {
            if(!clusteredMentions.contains(m)){
                ClusteredMention newCluster = m.markSingleton();
                mentions.add(newCluster);
                clusteredMentions.add(m);
            }
        }
        System.out.println("after:" + Integer.toString(clusteredMentions.size()));

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
