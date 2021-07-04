package bm25.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String TEST_FIELD_DOC_TEXT = "doc_text";
    public static final String TEST_FIELD_DOC_ID = "doc_id";

    private static final Gson GSONPrettyPrint = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private static String keyToIndexMapFile;
    private static Map<String, String> keyToIndexMap;

    public static Map<String, String> readPassageFileFormat(String inputFile) throws IOException {
        Map<String, String> results = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(inputFile));
        for(int line_index = 0 ; line_index < lines.size() ; line_index++) {
            String[] split = lines.get(line_index).split("\t");
            if (split.length < 2) {
                LOGGER.info("Illegal line format in line No-" + line_index +", " +
                        "line text=\"" + lines.get(line_index) + "\", in file-" + inputFile);
            }
            results.put(split[0].strip(), split[1].trim());
        }
        return results;
    }

    public static void setKeyToIndexMapFile(String keyToIndexMapFile) {
        Utils.keyToIndexMapFile = keyToIndexMapFile;
        LOGGER.debug("keyToIndexMapFile set to-" + keyToIndexMapFile);
    }

    public static Map<String, String> getKeyToIndexMap() throws IOException {
        if(keyToIndexMap == null) {
            if (Files.exists(Paths.get(keyToIndexMapFile))) {
                keyToIndexMap = GSONPrettyPrint.fromJson(Files.readString(Paths.get(keyToIndexMapFile)),
                        new TypeToken<Map<String, String>>() {
                        }.getType());

                keyToIndexMap.entrySet().removeIf(entry -> Files.notExists(Paths.get(entry.getValue())));
                LOGGER.debug("keyToIndexMapFile loaded successfully-" + GSON.toJson(keyToIndexMap));
            } else {
                keyToIndexMap = new HashMap<>();
            }
        }

        return Collections.synchronizedMap(keyToIndexMap);
    }

    public static void saveKeyToIndexMap() throws IOException {
        String csq = GSONPrettyPrint.toJson(keyToIndexMap);
        LOGGER.debug("Prepare to save key to index map-" + csq);
        if(keyToIndexMap != null) {
            keyToIndexMap.entrySet().removeIf(entry -> Files.notExists(Paths.get(entry.getValue())));
            Files.writeString(Paths.get(Utils.keyToIndexMapFile), csq);
        }
    }

    public static Gson getGSONPrettyPrint() {
        return GSONPrettyPrint;
    }

    public static Gson getGSON() {
        return GSON;
    }
}
