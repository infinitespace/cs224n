package cs224n.corefsystems;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;

import cs224n.coref.Document;
import cs224n.coref.ClusteredMention;
import cs224n.util.Pair;
import cs224n.coref.Gender;
import cs224n.util.CounterMap;
import cs224n.coref.Name;
import cs224n.coref.Entity;
import cs224n.coref.Pronoun;
import cs224n.coref.Util;
import cs224n.coref.Mention;
import cs224n.coref.Pronoun.Speaker;

public class RuleBased implements CoreferenceSystem {

    public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
    }

    @Override
    public List<ClusteredMention> runCoreference(Document doc) {

        List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
        List<ClusteredMention> known_mentions = new ArrayList<ClusteredMention>();
        Map<String, Entity> clusters = new HashMap<String, Entity>();

        for (Mention m : doc.getMentions()) {
            boolean found_known = false;
            for (ClusteredMention clustered_m : known_mentions) {
                for (Mention m2 : clustered_m.entity.mentions) {
                    if (isEqualByRules(m, m2)) {
                        mentions.add(m.markCoreferent(clustered_m.entity));
                        found_known = true;
                        break;
                    }
                }
                if (found_known) break;
            }
            if (!found_known) {
                ClusteredMention clustered_m = m.markSingleton();
                known_mentions.add(clustered_m);
                mentions.add(clustered_m);
            }

        }      
        return mentions;
    }

    boolean isEqualByRules(Mention m1, Mention m2) {
        if (MentionStringExactMatch(m1, m2)) return true;
        if (HeadExactMatch(m1, m2)) return true;
        if (!GenderMatchOrUnknown(m1, m2)) return false;
        if (!NumberMatchOrUnknown(m1, m2)) return false;
        if (Pronoun.isSomePronoun(m2.gloss()) && m1.sentence.equals(m2.sentence)
                && ((m2.headWordIndex - m1.headWordIndex) > 0) && ((m2.headWordIndex - m1.headWordIndex) < 5))
            return true;
        if (!NerTagMatch(m1, m2)) return false;
        if (PronouAndNameMatch(m1, m2)) return true;
        if (!LemmaMatch(m1, m2)) return false;
        if (StringSubset(m1, m2)) return true;
        return true;
    }

    boolean MentionStringExactMatch(Mention m1, Mention m2) {
        return m1.gloss().equals(m2.gloss()) ? true : false;
    }

    boolean HeadExactMatch(Mention m1, Mention m2) {
        return m1.headWord().equals(m2.headWord()) ? true : false;
    }

    boolean GenderMatchOrUnknown(Mention m1, Mention m2) {
        Pair<Boolean, Boolean> pair = Util.haveGenderAndAreSameGender(m1, m2);
        if ((pair.getSecond() && pair.getFirst()) || !pair.getFirst()) return true;
        else return false;
    }

    boolean NumberMatchOrUnknown(Mention m1, Mention m2) {
        Pair<Boolean, Boolean> pair = Util.haveNumberAndAreSameNumber(m1, m2);
        if ((pair.getSecond() && pair.getFirst()) || !pair.getFirst()) return true;
        else return false;
    }

    boolean NerTagMatch(Mention m1, Mention m2) {
        return m1.headToken().nerTag().equals(m2.headToken().nerTag()) ? true : false;
    }

    boolean PronouAndNameMatch(Mention m1, Mention m2) {
        Pronoun pron1 = Pronoun.valueOrNull(m1.gloss()); // getPronoun(m1.gloss());
        Pronoun pron2 = Pronoun.valueOrNull(m2.gloss()); // getPronoun(m2.gloss());
        Name name1 = Name.get(m1.gloss());
        Name name2 = Name.get(m2.gloss());

        if (name1 != null && name2 != null && name1.gloss.equals(name2.gloss)) return true;
        
        if (pron1 == null || pron2 == null) return false;
        
        if (pron1.speaker == pron2.speaker && pron1.plural == pron2.plural) return true;
        
        return false;
    } 

    boolean LemmaMatch(Mention m1, Mention m2) {
        return m1.headToken().lemma().equals(m2.headToken().lemma()) ? true: false;
    }

    boolean StringSubset(Mention m1, Mention m2) {
        return m1.gloss().contains(m2.gloss()) || m2.gloss().contains(m1.gloss()) ? true : false;
    }

}