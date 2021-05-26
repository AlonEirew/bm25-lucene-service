package data;

public class RetPassage {
    private final int passageId;
    private final int relQueryId;
    private final String text;
    private final int passageRank;

    public RetPassage(int passageId, int relQueryId, String text, int passageRank) {
        this.passageId = passageId;
        this.relQueryId = relQueryId;
        this.text = text;
        this.passageRank = passageRank;
    }

    public int getPassageRank() {
        return passageRank;
    }

    public int getPassageId() {
        return passageId;
    }

    public int getRelQueryId() {
        return relQueryId;
    }

    public String getText() {
        return text;
    }
}
