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

public class OneCluster implements CoreferenceSystem {

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

        boolean hasentity = false;
        Entity oneEntity = new Entity(doc.getMentions());
        for(Mention m : doc.getMentions()){
          mentions.add(m.markCoreferent(oneEntity));
        }
        //(return the mentions)
        return mentions;
	}

}
