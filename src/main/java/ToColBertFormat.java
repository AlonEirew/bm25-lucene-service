import data.Mention;
import data.WECSplit;
import utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ToColBertFormat {

    private static final String SPLIT = "Test";

    public static void main(String[] args) throws IOException {
        File wecQueiresFile = new File("search_jsons/" + SPLIT + "_queries.json");
        File wecPassagesFile = new File("search_jsons/" + SPLIT + "_passages.json");
        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);

        List<String> queryColbert = mentionsToColBertFormat(queries.getMentions());
        List<String> passageColbert = mentionsToColBertFormat(passages.getMentions());

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
