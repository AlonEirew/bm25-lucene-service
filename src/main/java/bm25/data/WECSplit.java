package bm25.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WECSplit {
    private final SplitType splitType;
    private final Map<Integer, Cluster> clusters;

    public WECSplit(SplitType splitType, List<Mention> mentions) {
        this.splitType = splitType;
        this.clusters = fromMentionsToCluster(mentions);
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Mention> getAllClustersMentions() {
        List<Mention> mentions = new ArrayList<>();
        this.clusters.values().stream().map(Cluster::getMentions).forEach(mentions::addAll);
        return mentions;
    }

    public Map<Integer, Cluster> getClusters() {
        return clusters;
    }

    private Map<Integer, Cluster> fromMentionsToCluster(List<Mention> mentions) {
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
            return true;
        }
        return false;
    }

    public boolean removeMentionFromCluster(Mention mention) {
        if(mention != null && this.clusters.containsKey(mention.getCoref_chain())) {
            return this.clusters.get(mention.getCoref_chain()).removeMention(mention);
        }
        return false;
    }
}
