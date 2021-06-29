package bm25.data;

public class QueryPassageTriplet {
    private final Mention query;
    private final Mention posPassage;
    private final Mention negPassage;

    public QueryPassageTriplet(Mention query, Mention posPassage, Mention negPassage) {
        this.query = query;
        this.posPassage = posPassage;
        this.negPassage = negPassage;
    }

    public Mention getQuery() {
        return query;
    }

    public Mention getPosPassage() {
        return posPassage;
    }

    public Mention getNegPassage() {
        return negPassage;
    }
}