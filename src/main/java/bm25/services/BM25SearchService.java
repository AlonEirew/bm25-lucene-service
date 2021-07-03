package bm25.services;

import bm25.data.BM25SearchRequest;
import bm25.data.BM25SearchResponse;
import bm25.utils.Utils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RestController
public class BM25SearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BM25SearchService.class);

    @PostMapping(value = "/bm25Search",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public BM25SearchResponse bm25Search(@RequestBody BM25SearchRequest searchRequest) {
        LOGGER.info("Got bm25Search request " + Utils.getGSON().toJson(searchRequest));
        BM25SearchResponse response;
        try {
            response = runSearch(searchRequest);
        } catch (Exception e) {
            LOGGER.error("Failed parsing the request", e);
            response = new BM25SearchResponse(searchRequest.getQueryId(), new ArrayList<>(), e.toString());
        }

        LOGGER.info("Search response-" + Utils.getGSON().toJson(response));
        return response;
    }

    private BM25SearchResponse runSearch(BM25SearchRequest searchRequest) throws IOException, ParseException {
        if(Utils.getKeyToIndexMap().containsKey(searchRequest.getIndexId())) {
            IndexSearcher indexSearcher = getIndexSearcher(Utils.getKeyToIndexMap().get(searchRequest.getIndexId()));
            BooleanQuery.setMaxClauseCount(4096);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser(Utils.TEST_FIELD_DOC_TEXT, analyzer);

            String queryText = QueryParser.escape(searchRequest.getQueryText());
            Query query = parser.parse(queryText);
            List<String> topKPassIds = runLuceneExpr(indexSearcher, query, searchRequest.getTopK());
            return new BM25SearchResponse(searchRequest.getQueryId(), topKPassIds, "Done successfully");
        } else {
            return new BM25SearchResponse(searchRequest.getQueryId(), new ArrayList<>(),
                    "Failed to search, index with id=" + searchRequest.getIndexId() + " doesnt exist");
        }
    }

    private List<String> runLuceneExpr(IndexSearcher idxSearcher, Query query, int topK) throws IOException {
        ScoreDoc[] hits = idxSearcher.search(query, topK).scoreDocs;
        List<String> topKPass = new ArrayList<>();
        if(hits != null) {
            for (ScoreDoc hit : hits) {
                org.apache.lucene.document.Document hitDoc = idxSearcher.doc(hit.doc);
                String passageId = hitDoc.get(Utils.TEST_FIELD_DOC_ID);
                topKPass.add(passageId);
            }
        }

        return topKPass;
    }

    private IndexSearcher getIndexSearcher(String indexDir) throws IOException {
        Path indexPath = Paths.get(indexDir);
        Directory directory = FSDirectory.open(indexPath);

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        return new IndexSearcher(ireader);
    }
}
