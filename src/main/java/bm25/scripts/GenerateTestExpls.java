package bm25.scripts;

import bm25.data.Mention;
import bm25.data.WECSplit;
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

public class GenerateTestExpls {

    private static final String SPLIT = "Train";

    public static void main(String[] args) throws IOException {
        File wecQueiresFile = new File("search_jsons/" + SPLIT + "_queries.json");
        File wecPassagesFile = new File("search_jsons/" + SPLIT + "_passages.json");
        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);

        List<Mention> allQueryMentions = queries.getAllClustersMentions();
        List<Mention> allPassageMentions = passages.getAllClustersMentions();
        if (!Utils.assertNoDups(allQueryMentions) || !Utils.assertNoDups(allPassageMentions)) {
            throw new IllegalStateException("Queries of passages contains duplicate id's");
        }

        Set<String> queryIds = allQueryMentions.stream().map(Mention::getMention_id).collect(Collectors.toSet());
        Set<String> passageIds = allPassageMentions.stream().map(Mention::getMention_id).collect(Collectors.toSet());
        queryIds.retainAll(passageIds);
        if(!queryIds.isEmpty()) {
            throw new IllegalStateException("Queries of passages contains shared id's");
        }

        NLPUtils.convertToEventSentenceQuery(allQueryMentions);
        List<String> queryColbert = mentionsToColBertFormat(allQueryMentions);
        List<String> passageColbert = mentionsToColBertFormat(allPassageMentions);

        Path queriesOutFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) +"QueryColbert.tsv");
        Path passagesOutFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) +"PassageColbert.tsv");
        Files.write(queriesOutFile, queryColbert, Charset.defaultCharset());
        Files.write(passagesOutFile, passageColbert, Charset.defaultCharset());
    }

    public static List<String> mentionsToColBertFormat(List<Mention> mentions) {
        List<String> convertedList = new ArrayList<>();
        for(Mention mention : mentions) {
            convertedList.add(mention.getMention_id() + "\t" + String.join(" ", mention.getMention_context()));
        }

        return convertedList;
    }
}
