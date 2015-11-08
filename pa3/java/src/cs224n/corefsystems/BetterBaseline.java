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

    	ArrayList<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
        HashMap<String, Entity> token2Entity = new HashMap<String, Entity>();

    	for (Mention m1 : doc.getMentions()) {
            boolean findPair = false;
            for (Mention m2 : doc.getMentions()) {
                String key = getKeyFromMentionPair(m1, m2);
                if(handwriten.contains(key)){
                    findPair = true;
                    break;
                }
            }
            String token1 = m1.headToken().word();
            if(token2Entity.containsKey(token1)){
                // System.out.print(token1);
                mentions.add(m1.markCoreferent(token2Entity.get(token1)));
            }
            else{
                ClusteredMention newCluster = m1.markSingleton();
                mentions.add(newCluster);
                token2Entity.put(token1, newCluster.entity);
            }
        }
    	return mentions;
	} 
}
