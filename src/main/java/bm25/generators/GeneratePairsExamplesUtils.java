package bm25.generators;

import bm25.data.Cluster;
import bm25.data.Mention;
import bm25.data.QueryPassagePair;
import bm25.data.WECSplit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneratePairsExamplesUtils extends AGenerator<QueryPassagePair> {
    @Override
    public List<QueryPassagePair> generateTrainExamples(WECSplit queries, WECSplit passages) {
        List<QueryPassagePair> queryPassagePairs = new ArrayList<>();
        for(Cluster cluster : queries.getClusters().values()) {
            queryPassagePairs.addAll(generatePosExamples(cluster));
            queryPassagePairs.addAll(generateNegExamples(cluster, passages));
        }

        return queryPassagePairs;
    }

    @Override
    public List<String> toPrintFormat(List<QueryPassagePair> queryPassagePairs) {
        List<String> trainExamples = new ArrayList<>();
        for (QueryPassagePair pair : queryPassagePairs) {
            StringBuilder sb = new StringBuilder();
            Mention query = pair.getQuery();
            Mention passage = pair.getPassage();
            int gold = pair.getGold();
            sb.append(query.getMention_id()).append("\t").append(passage.getMention_id()).append("\t").append(gold);
            trainExamples.add(sb.toString());
        }

        return trainExamples;
    }

    private List<QueryPassagePair> generatePosExamples(Cluster cluster) {
        List<QueryPassagePair> posPairs = new ArrayList<>();
        for(Mention query : cluster.getMentions()) {
            for(Mention posPass : cluster.getMentions()) {
                if(!query.getMention_id().equals(posPass.getMention_id())) {
                    posPairs.add(new QueryPassagePair(query, posPass));
                }
            }
        }

        return posPairs;
    }

    private List<QueryPassagePair> generateNegExamples(Cluster cluster, WECSplit passages) {
        List<QueryPassagePair> negPairs = new ArrayList<>();
        for(Mention query : cluster.getMentions()) {
            List<Mention> passagesList = new ArrayList<>(cluster.getMentions());
            passagesList.removeIf(value -> value.getMention_id().equals(query.getMention_id()));
            passagesList.addAll(passages.getAllClustersMentions());
            Collections.shuffle(passagesList);

            List<Mention> subPassArr = passagesList.subList(0, 50);
            for(Mention negPass : subPassArr) {
                negPairs.add(new QueryPassagePair(query, negPass));
            }
        }

        return negPairs;
    }
}
