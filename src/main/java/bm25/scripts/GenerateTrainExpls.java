package bm25.scripts;

import bm25.data.Mention;
import bm25.data.QueryPassageTriplet;
import bm25.data.WECSplit;
import bm25.generators.AGenerator;
import bm25.generators.GenerateTripletsExamplesUtils;
import bm25.utils.JsonUtils;
import bm25.utils.NLPUtils;
import bm25.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateTrainExpls {
    public static void main(String[] args) throws Exception {
        File wecQueiresFile = new File("search_jsons/Train_queries.json");
        File wecPassagesFile = new File("search_jsons/Train_passages.json");
        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);

//        AGenerator<QueryPassagePair> generator = new GeneratePairsExamplesUtils();
        AGenerator<QueryPassageTriplet> generator = new GenerateTripletsExamplesUtils();

        // queries now consist of all clusters that should be used for rotating queries (Dev/Test)
        // passages now consist of all clusters that should be used as passages only  (Train)
        List<Mention> forRemoval = new ArrayList<>();
        for(Mention pass : passages.getAllClustersMentions()) {
            if(queries.addMentionToCluster(pass)) {
                forRemoval.add(pass);
            }
        }

        for(Mention pass : forRemoval) {
            if(!passages.removeMentionFromCluster(pass)) {
                throw new IllegalStateException("Passage not removed from collection!");
            }
        }

        List<QueryPassageTriplet> queryPassage = generator.generateTrainExamples(queries, passages);
        List<String> trainExamples = generator.toPrintFormat(queryPassage);
        printColBertTrainFiles(trainExamples, queries, passages);
    }

    private static void printColBertTrainFiles(List<String> trainExamples, WECSplit queries,
                                               WECSplit passages) throws IOException {
        Path colberTrainFile = Paths.get("output/colbert/trainExamples50.tsv");
        Files.write(colberTrainFile, trainExamples, Charset.defaultCharset());

        List<Mention> passAndQuery = new ArrayList<>(passages.getAllClustersMentions());
        passAndQuery.addAll(queries.getAllClustersMentions().stream().map(Mention::new).collect(Collectors.toList()));

        NLPUtils.convertToEventSentenceQuery(queries.getAllClustersMentions());
        if (!Utils.assertNoDups(queries.getAllClustersMentions()) || !Utils.assertNoDups(passAndQuery)) {
            throw new IllegalStateException("Queries of passages contains duplicate id's");
        }

        List<Integer> querySizes = queries.getAllClustersMentions().stream().map(value -> value.getMention_context().size()).collect(Collectors.toList());
        List<Integer> passSizes = passAndQuery.stream().map(value -> value.getMention_context().size()).collect(Collectors.toList());
        int queryMax = querySizes.stream().max(Integer::compare).get();
        double queryAvg = querySizes.stream().mapToInt(Integer::intValue).average().getAsDouble();
        int passMax = passSizes.stream().max(Integer::compare).get();
        double passAvg = passSizes.stream().mapToInt(Integer::intValue).average().getAsDouble();

        System.out.println("Maximum query length = " + queryMax);
        System.out.println("Average query length = " + queryAvg);
        System.out.println("Maximum passage length = " + passMax);
        System.out.println("Average passage length = " + passAvg);

        List<String> queryColbert = GenerateTestExpls.mentionsToColBertFormat(queries.getAllClustersMentions());
        List<String> passageColbert = GenerateTestExpls.mentionsToColBertFormat(passAndQuery);

        Path queriesOutFile = Paths.get("output/colbert/trainQueryColbert.tsv");
        Path passagesOutFile = Paths.get("output/colbert/trainPassageColbert.tsv");
        Files.write(queriesOutFile, queryColbert, Charset.defaultCharset());
        Files.write(passagesOutFile, passageColbert, Charset.defaultCharset());
    }
}
