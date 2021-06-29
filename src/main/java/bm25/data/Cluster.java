package bm25.data;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private final Integer clusterId;
    private final SplitType splitType;
    private List<Mention> mentions = new ArrayList<>();

    public Cluster(Integer clusterId, SplitType splitType) {
        this.clusterId = clusterId;
        this.splitType = splitType;
    }

    public Cluster(Integer clusterId, List<Mention> mentions, SplitType splitType) {
        this.clusterId = clusterId;
        this.mentions = mentions;
        this.splitType = splitType;
    }

    public boolean isSingleton() {
        return this.mentions.size() == 1;
    }

    public void addMention(final Mention mention) {
        if(mention != null) {
            if(this.mentions.stream().filter(current -> current.getMention_id().equals(mention.getMention_id()))
                    .findFirst().orElse(null) != null) {
                throw new IllegalStateException("Mention with this ID already in collection!");
            }
            this.mentions.add(mention);
        }
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public SplitType getSplitType() {
        return splitType;
    }

    public List<Mention> getMentions() {
        return mentions;
    }

    public boolean removeMention(Mention mention) {
        return this.mentions.removeIf(value -> value.getMention_id().equals(mention.getMention_id()));
    }
}