package bm25.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Utils {
    public static final String TEST_FIELD_DOC_TEXT = "doc_text";
    public static final String TEST_FIELD_DOC_ID = "doc_id";

    private static final Gson GSONPrettyPrint = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static String keyToIndexMapFile;
    private static Map<String, String> keyToIndexMap = new ConcurrentHashMap<>();

    public static Map<String, String> readPassageQueryFileFormat(String inputFile) throws IOException {
        Map<String, String> results = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        for(String line : lines) {
            String[] split = line.split("\t");
            results.put(split[0].strip(), split[1].trim());
        }
        return results;
    }

    public static void setKeyToIndexMapFile(String keyToIndexMapFile) {
        Utils.keyToIndexMapFile = keyToIndexMapFile;
    }

    public static Map<String, String> getKeyToIndexMap() throws IOException {
        if(keyToIndexMap == null) {
            keyToIndexMap = GSONPrettyPrint.fromJson(Files.readString(Paths.get(keyToIndexMapFile)),
                    new TypeToken<Map<String, String>>() {}.getType());
        }

        return keyToIndexMap;
    }

    public static void saveKeyToIndexMap() throws IOException {
        if(keyToIndexMap != null) {
            Files.writeString(Paths.get(Utils.keyToIndexMapFile), GSONPrettyPrint.toJson(keyToIndexMap));
        }
    }

    public static Gson getGSONPrettyPrint() {
        return GSONPrettyPrint;
    }
}
