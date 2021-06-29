package bm25.generators;

import bm25.data.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GenerateTripletsExamplesUtils extends AGenerator<QueryPassageTriplet> {

    @Override
    public List<QueryPassageTriplet> generateTrainExamples(WECSplit queries, WECSplit passages) {
        List<QueryPassageTriplet> queryPassageTriplets = new ArrayList<>();

        List<Mention> passagesList = new ArrayList<>(passages.getAllClustersMentions());
        Collections.shuffle(passagesList);

        for(Cluster cluster : queries.getClusters().values()) {
            queryPassageTriplets.addAll(generateExamples(cluster, passagesList));
        }

        return queryPassageTriplets;
    }

    @Override
    public List<String> toPrintFormat(List<QueryPassageTriplet> queryPassagePairs) {
        List<String> trainExamples = new ArrayList<>();
        for (QueryPassageTriplet triplet : queryPassagePairs) {
            StringBuilder sb = new StringBuilder();
            Mention query = triplet.getQuery();
            Mention posPass = triplet.getPosPassage();
            Mention negPass = triplet.getNegPassage();
            sb.append(query.getMention_id()).append("\t").append(posPass.getMention_id()).append("\t").append(negPass.getMention_id());
            trainExamples.add(sb.toString());
        }

        return trainExamples;
    }

    private List<QueryPassageTriplet> generateExamples(Cluster cluster, List<Mention> passagesList) {
        List<QueryPassageTriplet> triplets = new ArrayList<>();
        for(Mention query : cluster.getMentions()) {
            for(Mention posPass : cluster.getMentions()) {
                if(!query.getMention_id().equals(posPass.getMention_id())) {
                    int randomNum = ThreadLocalRandom.current().nextInt(0, passagesList.size());
                    Mention negPass = passagesList.get(randomNum);
                    triplets.add(new QueryPassageTriplet(query, posPass, negPass));
                }
            }
        }

        return triplets;
    }
}
