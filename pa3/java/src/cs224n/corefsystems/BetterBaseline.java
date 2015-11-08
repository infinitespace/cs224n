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
import cs224n.util.*;

public class BetterBaseline implements CoreferenceSystem {

    private HashSet<String> handwriten;
    private String getKeyFromMentionPair(Mention m1, Mention m2){
        String token1 = m1.headToken().word();
        String token2 = m2.headToken().word();
        String[] tokens = {token1, token2};
        Arrays.sort(tokens);
        return tokens[0] + tokens[1];
    }

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
        handwriten = new HashSet<String>();
        for(Pair<Document, List<Entity>> pair : trainingData){
          List<Entity> clusters = pair.getSecond();
          for(Entity e : clusters){
            // System.out.println(e);
            for(Pair<Mention, Mention> mentionPair : e.orderedMentionPairs()){
                handwriten.add(getKeyFromMentionPair(mentionPair.getFirst(), mentionPair.getSecond()));
            }
          }
        }
	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {

    	ArrayList<ClusteredMention> mentions = new ArrayList<ClusteredMention>(); // result
        
        HashMap<String, Entity> mentionHeadPair = new HashMap<String, Entity>(); // header pair from training set
        HashMap<String, Entity> head2Entity = new HashMap<String, Entity>(); // direct match by head

    	for (Mention m1 : doc.getMentions()) {
            String token1 = m1.headToken().word();
            // direct match by head
            if(head2Entity.containsKey(token1)){
                mentions.add(m1.markCoreferent(head2Entity.get(token1)));
            }
            else{
                boolean findPair = false;
                String key = "";
                for (Mention m2 : doc.getMentions()) {
                    key = getKeyFromMentionPair(m1, m2);
                    if(handwriten.contains(key)){
                        findPair = true;
                        break;
                    }
                }
                // header pair from training set
                if(findPair){
                    if(mentionHeadPair.containsKey(key)){
                        mentions.add(m1.markCoreferent(mentionHeadPair.get(key)));
                        head2Entity.put(token1, mentionHeadPair.get(key));
                    }
                    else{
                        ClusteredMention newCluster = m1.markSingleton();
                        mentions.add(newCluster);
                        mentionHeadPair.put(key, newCluster.entity);
                        head2Entity.put(token1, newCluster.entity);
                    }
                }
                else{
                    ClusteredMention newCluster = m1.markSingleton();
                    mentions.add(newCluster);
                    head2Entity.put(token1, newCluster.entity);
                }
            }
        }
    	return mentions;
	} 
}
