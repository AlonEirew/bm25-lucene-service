package data;

import java.util.ArrayList;
import java.util.List;

public class SearchSplit {
    private final List<Mention> passages = new ArrayList<>();
    private final List<Mention> queries = new ArrayList<>();
    private final SplitType splitType;

    public SearchSplit(List<Cluster> clusters, SplitType splitType) {
        this(splitType);
        addPassagesAndQueriesFromClusters(clusters);
    }

    public SearchSplit(SplitType splitType) {
        this.splitType = splitType;
    }

    public List<Mention> getPassages() {
        return passages;
    }

    public List<Mention> getQueries() {
        return queries;
    }

    public void addPassagesAndQueriesFromClusters(List<Cluster> clusters) {
        for(Cluster cluster : clusters) {
            addPassageAndQueryFromCluster(cluster);
        }
    }

    public void addPassageAndQueryFromCluster(Cluster cluster) {
        if(cluster.isSingleton()) {
            this.passages.addAll(cluster.getMentions());
        } else {
            if (cluster.getSplitType() == SplitType.Dev || cluster.getSplitType() == SplitType.Test) {
                queries.add(cluster.getMentions().get(0));
                for(int i = 1 ; i < cluster.getMentions().size() ; i++) {
                    passages.add(cluster.getMentions().get(i));
                }
            } else if (cluster.getSplitType() == SplitType.Train) {
                passages.addAll(cluster.getMentions());
            } else {
                System.out.println("Nothing to add cluster type is not marked");
            }
        }
    }

    public SplitType getSplitType() {
        return splitType;
    }
}
