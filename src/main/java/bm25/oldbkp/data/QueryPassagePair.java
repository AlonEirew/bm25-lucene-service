package bm25.oldbkp.data;

public class QueryPassagePair {
    private final Mention query;
    private final Mention passage;
    private final int gold;

    public QueryPassagePair(Mention query, Mention passage) {
        this.query = query;
        this.passage = passage;
        this.gold = query.getCoref_chain() == passage.getCoref_chain() ? 1 : 0;
    }

    public Mention getQuery() {
        return query;
    }

    public Mention getPassage() {
        return passage;
    }

    public int getGold() {
        return gold;
    }
}
