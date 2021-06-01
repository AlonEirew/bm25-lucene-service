import data.Cluster;
import data.Mention;
import data.QueryPassagePair;
import data.WECSplit;
import jdk.jshell.execution.Util;
import utils.JsonUtils;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateTrainExpls {
    public static void main(String[] args) throws Exception {
        File wecQueiresFile = new File("search_jsons/Train_queries.json");
        File wecPassagesFile = new File("search_jsons/Train_passages.json");
        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);

        // queries now consist of all clusters that should be used for rotating queries (Dev/Test)
        // passages now consist of all clusters that should be used as passages only  (Train)
        List<Mention> forRemoval = new ArrayList<>();
        for(Mention pass : passages.getMentions()) {
            if(queries.addMentionToCluster(pass)) {
                forRemoval.add(pass);
            }
        }

        for(Mention pass : forRemoval) {
            if (!passages.removeMentionFromCluster(pass)) {
                throw new Exception("Mention not removed!");
            }
        }

        List<Mention> tmpList = new ArrayList<>(passages.getMentions());
        tmpList.addAll(queries.getMentions());
        Utils.reIndexMentionList(tmpList);

        List<QueryPassagePair> queryPassagePairs = generateTrainExamples(queries, passages);
        printColBertTrainFiles(queryPassagePairs, queries, passages);
    }

    private static void printColBertTrainFiles(List<QueryPassagePair> queryPassagePairs, WECSplit queries,
                                               WECSplit passages) throws IOException {
        List<String> trainExamples = new ArrayList<>();
        for (QueryPassagePair pair : queryPassagePairs) {
            StringBuilder sb = new StringBuilder();
            Mention query = pair.getQuery();
            Mention passage = pair.getPassage();
            int gold = pair.getGold();
            sb.append(query.getMention_id()).append("\t").append(passage.getMention_id()).append("\t").append(gold);
            trainExamples.add(sb.toString());
        }

        Path colberTrainFile = Paths.get("output/colbert/trainExamples50.tsv");
        Files.write(colberTrainFile, trainExamples, Charset.defaultCharset());

        List<Mention> passAndQuery = new ArrayList<>(passages.getMentions());
        passAndQuery.addAll(queries.getMentions().stream().map(Mention::new).collect(Collectors.toList()));

//        Utils.convertToEventWithNerQuery(queries.getMentions());
        List<String> queryColbert = ToColBertFormat.mentionsToColBertFormat(queries.getMentions());
        List<String> passageColbert = ToColBertFormat.mentionsToColBertFormat(passAndQuery);

        Path queriesOutFile = Paths.get("output/colbert/trainQueryColbert.tsv");
        Path passagesOutFile = Paths.get("output/colbert/trainPassageColbert.tsv");
        Files.write(queriesOutFile, queryColbert, Charset.defaultCharset());
        Files.write(passagesOutFile, passageColbert, Charset.defaultCharset());
    }

    private static List<QueryPassagePair> generateTrainExamples(WECSplit queries, WECSplit passages) {
        List<QueryPassagePair> queryPassagePairs = new ArrayList<>();
        for(Cluster cluster : queries.getClusters().values()) {
            queryPassagePairs.addAll(generatePosExamples(cluster));
            queryPassagePairs.addAll(generateNegExamples(cluster, passages));
        }

        return queryPassagePairs;
    }

    private static List<QueryPassagePair> generatePosExamples(Cluster cluster) {
        List<QueryPassagePair> posPairs = new ArrayList<>();
        for(Mention ment1 : cluster.getMentions()) {
            for(Mention ment2 : cluster.getMentions()) {
                if(!ment1.getMention_id().equals(ment2.getMention_id())) {
                    posPairs.add(new QueryPassagePair(ment1, ment2));
                }
            }
        }

        return posPairs;
    }

    private static List<QueryPassagePair> generateNegExamples(Cluster cluster, WECSplit passages) {
        List<QueryPassagePair> negPairs = new ArrayList<>();
        for(Mention ment : cluster.getMentions()) {
            List<Mention> passagesList = new ArrayList<>(cluster.getMentions());
            passagesList.removeIf(value -> value.getMention_id().equals(ment.getMention_id()));
            passagesList.addAll(passages.getMentions());
            Collections.shuffle(passagesList);

            List<Mention> subPassArr = passagesList.subList(0, 50);
            for(Mention negPass : subPassArr) {
                negPairs.add(new QueryPassagePair(ment, negPass));
            }
        }

        return negPairs;
    }
}
