import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import data.Mention;
import data.SplitType;
import data.WECSplit;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Utils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<WECSplit> readWECJsonFolder(String wecFolder) {
        List<WECSplit> wecSplits = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(wecFolder))) {
            paths.filter(Files::isRegularFile).forEach(file -> wecSplits.add(readWECJsonFile(file.toFile())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wecSplits;
    }

    public static WECSplit readWECJsonFile(File wecInput) {
        SplitType splitType = SplitType.NA;
        List<Mention> mentList = new ArrayList<>();
        try (InputStream in = new FileInputStream(wecInput)) {
            if (wecInput.getName().toLowerCase(Locale.ROOT).contains("dev")) {
                splitType = SplitType.Dev;
            } else if (wecInput.getName().toLowerCase(Locale.ROOT).contains("test")) {
                splitType = SplitType.Test;
            } else if (wecInput.getName().toLowerCase(Locale.ROOT).contains("train")) {
                splitType = SplitType.Train;
            } else {
                throw new IOException("SplitType cannot be indicated from file name!");
            }

            Type MENTION_TYPE = new TypeToken<List<Mention>>() {}.getType();
            JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            mentList = GSON.fromJson(reader, MENTION_TYPE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return new WECSplit(splitType, mentList);
    }

    public static void writeWECJsonFile(List<Mention> mentions, String wec_output) throws IOException {
        OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(wec_output), StandardCharsets.UTF_8);
        JsonWriter output = new JsonWriter(os);
        Type MENTION_TYPE = new TypeToken<List<Mention>>() {}.getType();
        GSON.toJson(mentions, MENTION_TYPE, output);
        output.flush();
        output.close();
    }
}
