package bm25.data;

public class CreateIndexRequest {
    private final String inputPath;
    private final String indexPath;

    public CreateIndexRequest(String inputPath, String indexPath) {
        this.inputPath = inputPath;
        this.indexPath = indexPath;
    }

    public String getInputPath() {
        return inputPath;
    }

    public String getIndexPath() {
        return indexPath;
    }
}