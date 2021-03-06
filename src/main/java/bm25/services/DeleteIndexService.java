package bm25.services;

import bm25.data.DeleteIndexResponse;
import bm25.utils.Utils;
import org.apache.lucene.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Paths;

@RestController
public class DeleteIndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteIndexService.class);

    @DeleteMapping(value = "/deleteLuceneIndex")
    public DeleteIndexResponse deleteLuceneIndex(@RequestBody String deleteIndexRequest) {
        LOGGER.info("Got deleteLuceneIndex request " + Utils.getGSON().toJson(deleteIndexRequest));
        DeleteIndexResponse response;
        try {
            if(Utils.getKeyToIndexMap().containsKey(deleteIndexRequest)) {
                IOUtils.rm(Paths.get(Utils.getKeyToIndexMap().get(deleteIndexRequest)));
                Utils.getKeyToIndexMap().remove(deleteIndexRequest);
                response = new DeleteIndexResponse("true", "Index deleted successfully");
            } else {
                response = new DeleteIndexResponse("false", "Index with uuid=" + deleteIndexRequest + " not found");
            }
        } catch (Exception e) {
            LOGGER.error("Failed parsing the request", e);
            response = new DeleteIndexResponse("false", e.toString());
        }

        LOGGER.info("Delete response-" + Utils.getGSON().toJson(response));
        return response;
    }
}
