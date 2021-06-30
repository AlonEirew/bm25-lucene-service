package bm25.oldbkp.scripts;

import bm25.oldbkp.data.WECSplit;
import bm25.oldbkp.utils.JsonUtils;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class MentionSpanPlace {
    private static final String SPLIT = "Dev";

    public static void main(String[] args) {
        File wecPassagesFile = new File("search_jsons/" + SPLIT + "_passages.json");
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);
        List<Integer> maxMentIndexs = passages.getAllClustersMentions().stream().map(value -> value.getTokens_number()
                .get(value.getTokens_number().size() - 1)).filter(value -> value > 180).collect(Collectors.toList());

        List<Integer> maxPassIndexs = passages.getAllClustersMentions().stream().map(value -> value.getMention_context().size())
                .filter(value -> value > 180).collect(Collectors.toList());

        System.out.println("Total passages in " + SPLIT + " = " + passages.getAllClustersMentions().size());
        System.out.println("Num of mentions with indexes > 180 = " + maxMentIndexs.size());
        System.out.println("Num of passages with tok_num > 180 = " + maxPassIndexs.size());
    }
}
