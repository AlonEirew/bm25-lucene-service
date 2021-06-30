package bm25.data;

public class DeleteIndexResponse {
    private final String deleted;
    private final String message;

    public DeleteIndexResponse(String deleted, String message) {
        this.deleted = deleted;
        this.message = message;
    }

    public String getDeleted() {
        return deleted;
    }

    public String getMessage() {
        return message;
    }
}
