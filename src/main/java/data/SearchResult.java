package data;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {
    private final Mention query;
    private final List<RetPassage> topKRetPassages;
    private final int firstCoveredRank;

    public SearchResult(Mention query, List<RetPassage> topKRetPassages, int firstCoveredRank) {
        this.query = query;
        this.topKRetPassages = topKRetPassages;
        this.firstCoveredRank = firstCoveredRank;
    }

    public Mention getQuery() {
        return query;
    }

    public List<RetPassage> getTopKRetPassages() {
        return topKRetPassages;
    }

    public int getConvertedMents() {
        int covered = 0;
        for(RetPassage pass : topKRetPassages) {
            if(pass.getRelQueryId() == this.query.getCoref_chain()) {
                covered++;
            }
        }
        return covered;
    }

    public List<String> getTriplets() {
        return new ArrayList<>();
    }

    public List<String> getQueryPassages() {
        List<String> coveredQueryPassage = new ArrayList<>();
        for (RetPassage pass : this.topKRetPassages) {
            int gold = this.query.getCoref_chain() == pass.getRelQueryId() ? 1 : 0;
            coveredQueryPassage.add(query.getMention_id() + "\t" + pass.getPassageId() + "\t" + pass.getPassageRank() +
                    "\t" + gold);
        }

        return coveredQueryPassage;
    }

    public int getFirstCoveredRank() {
        return firstCoveredRank;
    }

    public static int calcCoveredMentions(List<SearchResult> results) {
        int covered = 0;
        for(SearchResult result : results) {
            covered += result.getConvertedMents();
        }
        return covered;
    }

    public static List<String> getAllQueryPassages(List<SearchResult> results) {
        List<String> allQueriesPassagesPairs = new ArrayList<>();
        for(SearchResult result : results) {
            allQueriesPassagesPairs.addAll(result.getQueryPassages());
        }
        return allQueriesPassagesPairs;
    }
}
