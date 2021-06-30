package bm25.data;

import java.util.List;

public class BM25SearchResponse {
    private final String queryId;
    private final List<String> rankedPassageIds;
    private final String message;

    public BM25SearchResponse(String queryId, List<String> rankedPassageIds, String message) {
        this.queryId = queryId;
        this.rankedPassageIds = rankedPassageIds;
        this.message = message;
    }

    public String getQueryId() {
        return queryId;
    }

    public List<String> getRankedPassageIds() {
        return rankedPassageIds;
    }

    public String getMessage() {
        return message;
    }
}
