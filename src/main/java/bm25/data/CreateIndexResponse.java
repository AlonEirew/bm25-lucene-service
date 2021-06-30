package bm25.data;

public class CreateIndexResponse {
    private final int added;
    private final String indexId;
    private final String message;

    public CreateIndexResponse(int added, String indexId, String message) {
        this.added = added;
        this.message = message;
        this.indexId = indexId;
    }

    public int getAdded() {
        return added;
    }

    public String getMessage() {
        return message;
    }

    public String getIndexId() {
        return indexId;
    }
}
