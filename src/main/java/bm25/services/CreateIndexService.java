package bm25.services;

import bm25.data.CreateIndexRequest;
import bm25.data.CreateIndexResponse;
import bm25.utils.Utils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
public class CreateIndexService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateIndexService.class);

    @PutMapping(value = "/createLuceneIndex",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public CreateIndexResponse createLuceneIndex(@RequestBody CreateIndexRequest createIndexRequest) {
        LOGGER.info("Got createLuceneIndex request " + Utils.getGSON().toJson(createIndexRequest));
        try {
            boolean isIndexExist = Files.exists(Paths.get(createIndexRequest.getIndexPath()));
            if(!Utils.getKeyToIndexMap().containsValue(createIndexRequest.getIndexPath())) {
                if (!isIndexExist) {
                    return createResources(createIndexRequest);
                } else {
                    return new CreateIndexResponse(0, "NA", "Failed, folder " +
                            createIndexRequest.getIndexPath() + " already exists");
                }
            } else {
                String uuid = Utils.getKeyToIndexMap().entrySet().stream()
                        .filter(entry -> Objects.equals(entry.getValue(), createIndexRequest.getIndexPath()))
                        .map(Map.Entry::getKey).findFirst().get();

                if(isIndexExist) {
                    return new CreateIndexResponse(0, uuid, "Index already exists, returning indexId");
                } else {
                    Utils.getKeyToIndexMap().remove(uuid);
                    return createResources(createIndexRequest);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed parsing the request");
            return new CreateIndexResponse(0, "NA", e.toString());
        }
    }

    private CreateIndexResponse createResources(CreateIndexRequest createIndexRequest) throws IOException {
        Map<String, String> inputKeyValues = Utils.readPassageQueryFileFormat(createIndexRequest.getInputPath());
        Path indexPath = Files.createDirectory(Paths.get(createIndexRequest.getIndexPath()));
        Directory directory = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter iwriter = new IndexWriter(directory, config);
        for (Map.Entry<String, String> entry : inputKeyValues.entrySet()) {
            Document doc = new Document();
            doc.add(new Field(Utils.TEST_FIELD_DOC_TEXT, entry.getValue(), TextField.TYPE_STORED));
            doc.add(new Field(Utils.TEST_FIELD_DOC_ID, entry.getKey(), TextField.TYPE_STORED));
            iwriter.addDocument(doc);
        }

        iwriter.close();
        directory.close();

        String uuid = UUID.randomUUID().toString();
        Utils.getKeyToIndexMap().put(uuid, createIndexRequest.getIndexPath());

        return new CreateIndexResponse(inputKeyValues.size(), uuid, "Index created successfully");
    }
}
