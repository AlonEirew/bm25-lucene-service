package bm25.oldbkp.index;

import com.google.common.collect.Lists;
import bm25.oldbkp.data.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;
import bm25.oldbkp.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CreateOrDeleteIndex {

    public static final String PASSAGE = "passage";
    public static final String PASSAGE_ID = "passage_id";
    public static final String PASSAGE_CORE_ID = "passage_coref_id";

    public static void main(String[] args) throws IOException {
        System.out.println("[C]reate or [D]elete?");
        Scanner in = new Scanner(System.in);
        String ans = in.nextLine();
        in.close();
        Path tempIndex = Paths.get("tempIndex");
        Path outFolder = Paths.get("search_jsons");
        String wecFolder = "input/WEC-Eng";
        if(ans.equalsIgnoreCase("C")) {
            System.out.println("Creating elastic index");
            createResources(wecFolder, outFolder, tempIndex);
        } else if (ans.equalsIgnoreCase("D")) {
            System.out.println("deleting elastic index");
            IOUtils.rm(tempIndex, outFolder);
        } else {
            System.out.println("Notion was done");
        }

        System.out.println("Done!");
    }

    public static void createResources(String wecFolder, Path outFolder, Path tempIndex) throws IOException {
        Path indexPath = Files.createDirectory(tempIndex);
        Files.createDirectory(outFolder);
        try {
            Map<SplitType, SearchSplit> splitParts = createQueriesAndMentionsSplit(wecFolder);
            persistToLuceneIndex(splitParts, indexPath);
            writeJsonToFile(splitParts, outFolder.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            IOUtils.rm(tempIndex);
        }
    }

    private static Map<SplitType, SearchSplit> createQueriesAndMentionsSplit(String wecFolder) throws IOException {
        List<WECSplit> splits = JsonUtils.readWECJsonFolder(wecFolder);
        Map<SplitType, SearchSplit> splitParts = new HashMap<>();
        splitParts.put(SplitType.Train, new SearchSplit(SplitType.Train));
        splitParts.put(SplitType.Dev, new SearchSplit(SplitType.Dev));
        splitParts.put(SplitType.Test, new SearchSplit(SplitType.Test));
        for(WECSplit split : splits) {
            List<Cluster> splitClusters = new ArrayList<>(split.getClusters().values());
            List<List<Cluster>> partitions = Lists.partition(splitClusters, (splitClusters.size() / 3) + 1);
            splitParts.get(SplitType.Train).addPassagesAndQueriesFromClusters(partitions.get(0));
            splitParts.get(SplitType.Dev).addPassagesAndQueriesFromClusters(partitions.get(1));
            splitParts.get(SplitType.Test).addPassagesAndQueriesFromClusters(partitions.get(2));
        }

        return splitParts;
    }

    private static void writeJsonToFile(Map<SplitType, SearchSplit> splitParts, String outFolder) throws IOException {
        for(SplitType st : splitParts.keySet()) {
            JsonUtils.writeJsonObjListToFile(splitParts.get(st).getQueries(),
                    outFolder + File.separator + st.name() + "_queries.json");
            JsonUtils.writeJsonObjListToFile(splitParts.get(st).getPassages(),
                    outFolder + File.separator + st.name() + "_passages.json");
        }
    }

    private static void persistToLuceneIndex(Map<SplitType, SearchSplit> splitParts, Path tempIndex) throws IOException {
        for (SearchSplit split : splitParts.values()) {
            Path currPath = Paths.get(tempIndex + File.separator + split.getSplitType().name());
            Files.createDirectory(currPath);
            Directory directory = FSDirectory.open(currPath);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter iwriter = new IndexWriter(directory, config);
            for(Mention passage : split.getPassages()) {
                Document doc = new Document();
                String text = String.join(" ", passage.getMention_context());
                doc.add(new Field(PASSAGE, text, TextField.TYPE_STORED));
                doc.add(new Field(PASSAGE_ID, passage.getMention_id(), TextField.TYPE_STORED));
                doc.add(new Field(PASSAGE_CORE_ID, String.valueOf(passage.getCoref_chain()), TextField.TYPE_STORED));
                iwriter.addDocument(doc);
            }

            iwriter.close();
            directory.close();
        }
    }
}
