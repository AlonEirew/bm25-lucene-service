import data.Mention;
import data.WECSplit;

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
        WECSplit queries = Utils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = Utils.readWECJsonFile(wecPassagesFile);

        List<String> queryColbert = queryToColBertFormat(queries.getMentions());
        List<String> passageColbert = passageToColBertFormat(passages.getMentions());

        Path queriesOutFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) +"queryColbert.txt");
        Path passagesOutFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) +"passageColbert.txt");
        Files.write(queriesOutFile, queryColbert, Charset.defaultCharset());
        Files.write(passagesOutFile, passageColbert, Charset.defaultCharset());
    }

    private static List<String> queryToColBertFormat(List<Mention> mentions) {
        List<String> convertedList = new ArrayList<>();
        for(Mention ment : mentions) {
            convertedList.add(ment.getCoref_chain() + "\t" + String.join(" ", ment.getMention_context()));
        }

        return convertedList;
    }

    private static List<String> passageToColBertFormat(List<Mention> mentions) {
        List<String> convertedList = new ArrayList<>();
        for(Mention ment : mentions) {
            convertedList.add(ment.getMention_id() + "\t" + String.join(" ", ment.getMention_context()));
        }

        return convertedList;
    }
}
