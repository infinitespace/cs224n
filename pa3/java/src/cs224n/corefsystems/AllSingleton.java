package cs224n.corefsystems;

import java.util.Collection;
import java.util.List;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Mention;
import cs224n.util.Pair;

import java.util.ArrayList;
import java.util.*;

public class AllSingleton implements CoreferenceSystem {

	@Override
	public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ClusteredMention> runCoreference(Document doc) {
		//(variables)
        List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
        Map<String,Entity> clusters = new HashMap<String,Entity>();
        //(for each mention...)
        for(Mention m : doc.getMentions()){
          //(...get its text)
          String mentionString = m.gloss();
          // System.out.println("Mention String:");
          // System.out.println(mentionString);
          ClusteredMention newCluster = m.markSingleton();
          mentions.add(newCluster);
          clusters.put(mentionString, newCluster.entity);
        }
        //(return the mentions)
        return mentions;
	}

}
