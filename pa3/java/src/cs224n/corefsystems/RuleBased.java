package cs224n.corefsystems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cs224n.coref.ClusteredMention;
import cs224n.coref.Document;
import cs224n.coref.Entity;
import cs224n.coref.Gender;
import cs224n.coref.Mention;
import cs224n.coref.Name;
import cs224n.coref.Pronoun;
import cs224n.coref.Util;
import cs224n.coref.Pronoun.Speaker;
import cs224n.util.CounterMap;
import cs224n.util.Pair;

public class RuleBased implements CoreferenceSystem {

    public void train(Collection<Pair<Document, List<Entity>>> trainingData) {
    }

    @Override
    public List<ClusteredMention> runCoreference(Document doc) {

        List<ClusteredMention> mentions = new ArrayList<ClusteredMention>();
        Map<String, Entity> clusters = new HashMap<String, Entity>();
        List<ClusteredMention> tmp_mentions = new ArrayList<ClusteredMention>();

        for (Mention m : doc.getMentions()) {
            boolean flag = false;
            for (ClusteredMention cm : tmp_mentions) {
                for (Mention m2 : cm.entity.mentions) {
                    if (sameMention(m, m2)) {
                        mentions.add(m.markCoreferent(cm.entity));
                        flag = true;
                        break;
                    }
                }
                if (flag) break;
            }
            if (flag == false) {
                ClusteredMention clm = m.markSingleton();
                tmp_mentions.add(clm);
                mentions.add(clm);
            }

        }      
        return mentions;

    }

    boolean sameMention(Mention m1, Mention m2) {
        if (exactMatch(m1, m2))
            return true;
        if (headMatch(m1, m2))
            return true;

        if (!sameGenderOrUnknown(m1, m2))
            return false;
        if (!sameNumberOrUnknown(m1, m2))
            return false;
        if (Pronoun.isSomePronoun(m2.gloss()) && m1.sentence.equals(m2.sentence)
                && ((m2.headWordIndex - m1.headWordIndex) > 0) && ((m2.headWordIndex - m1.headWordIndex) < 5))
            return true;
        if (!sameNE(m1, m2)) {
            return false;
        }

        if (pronouAndNameAgree(m1, m2)) {
            return true;
        }

        if (!sameLemma(m1, m2)) {
            return false;
        }

        if (contains(m1, m2)) {
            return true;
        }
        return true;

    }

    boolean pronouAndNameAgree(Mention m1, Mention m2) {
        Pronoun p1 = Pronoun.valueOrNull(m1.gloss());// getPronoun(m1.gloss());

        // if (p1 == null && m2.gloss().equals("it"))
        // return true;

        Pronoun p2 = Pronoun.valueOrNull(m2.gloss());// getPronoun(m2.gloss());
        Name n1 = Name.get(m1.gloss());
        Name n2 = Name.get(m2.gloss());

        if (n1 != null && n2 != null && n1.gloss.equals(n2.gloss))
            return true;

        if (p1 == null || p2 == null)
            return false;
        if (p1.speaker == p2.speaker && p1.plural == p2.plural) {
            // System.out.println(p1.speaker);
            return true;
        }

        return false;
    }

    boolean contains(Mention m1, Mention m2) {
        if (m1.gloss().contains(m2.gloss()) || m2.gloss().contains(m1.gloss()))
            return true;
        return false;
    }

    boolean exactMatch(Mention m1, Mention m2) {
        if (m1.gloss().equals(m2.gloss()))
            return true;
        return false;
    }

    boolean headMatch(Mention m1, Mention m2) {
        if (m1.headWord().equals(m2.headWord()))
            return true;
        return false;
    }

    boolean sameLemma(Mention m1, Mention m2) {
        if (m1.headToken().lemma().equals(m2.headToken().lemma()))
            return true;
        return false;
    }

    boolean sameNE(Mention m1, Mention m2) {
        if (m1.headToken().nerTag().equals(m2.headToken().nerTag()))
            return true;
        return false;
    }

    boolean sameGenderOrUnknown(Mention m1, Mention m2) {
        Pair<Boolean, Boolean> pair = Util.haveGenderAndAreSameGender(m1, m2);
        if (pair.getSecond() && pair.getFirst())
            return true;
        if (!pair.getFirst())
            return true;
        return false;
    }

    boolean sameNumberOrUnknown(Mention m1, Mention m2) {
        Pair<Boolean, Boolean> pair = Util.haveNumberAndAreSameNumber(m1, m2);
        if (pair.getSecond() && pair.getFirst())
            return true;
        if (!pair.getFirst())
            return true;
        return false;
    }

}