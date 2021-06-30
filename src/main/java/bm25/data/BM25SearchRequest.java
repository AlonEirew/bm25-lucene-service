package bm25.data;

public class BM25SearchRequest {
    private final String indexId;
    private final String queryId;
    private final String queryText;
    private final int topK;

    public BM25SearchRequest(String indexId, String queryId, String queryText, int topK) {
        this.indexId = indexId;
        this.queryId = queryId;
        this.queryText = queryText;
        this.topK = topK;
    }

    public String getIndexId() {
        return indexId;
    }

    public String getQueryId() {
        return queryId;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getTopK() {
        return topK;
    }
}
