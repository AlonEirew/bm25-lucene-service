package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WECSplit {
    private final SplitType splitType;
    private final List<Mention> mentions;
    private final Map<Integer, Cluster> clusters;

    public WECSplit(SplitType splitType, List<Mention> mentions) {
        this.splitType = splitType;
        this.mentions = mentions;
        this.clusters = fromMentionsToCluster(this.mentions);
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public Map<Integer, Cluster> getClusters() {
        return clusters;
    }

    public Map<Integer, Cluster> fromMentionsToCluster(List<Mention> mentions) {
        Map<Integer, Cluster> clusters = new HashMap<>();
        for (Mention ment : mentions) {
            if(!clusters.containsKey(ment.getCoref_chain())) {
                clusters.put(ment.getCoref_chain(), new Cluster(ment.getCoref_chain(), splitType));
            }
            clusters.get(ment.getCoref_chain()).addMention(ment);
        }

        return clusters;
    }

    public boolean addMentionToCluster(Mention mention) {
        if(mention != null && this.clusters.containsKey(mention.getCoref_chain())) {
            this.clusters.get(mention.getCoref_chain()).addMention(mention);
            this.mentions.add(mention);
            return true;
        }
        return false;
    }

    public boolean removeMentionFromCluster(Mention mention) {
        if(mention != null && this.clusters.containsKey(mention.getCoref_chain())) {
            boolean mentRm = this.mentions.removeIf(value -> value.getMention_id().equals(mention.getMention_id()));
            boolean clustRm = this.clusters.get(mention.getCoref_chain()).removeMention(mention);
            return mentRm && clustRm;
        }
        return false;
    }
}
