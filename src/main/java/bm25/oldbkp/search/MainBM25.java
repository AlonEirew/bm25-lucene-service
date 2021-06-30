package bm25.oldbkp.search;

import bm25.oldbkp.data.*;
import bm25.oldbkp.index.CreateOrDeleteIndex;
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
import bm25.oldbkp.utils.JsonUtils;
import bm25.oldbkp.utils.NLPUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainBM25 {

    private static final String SPLIT = "Dev";

    public static void main(String[] args) throws IOException {
        File wecQueiresFile = new File("search_jsons/" + SPLIT + "_queries.json");
        File wecPassagesFile = new File("search_jsons/" + SPLIT + "_passages.json");
        String indexDir = "tempIndex/" + SPLIT;
        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);
        System.out.println("SPLIT=" + SPLIT);
        List<Mention> allQueries = queries.getAllClustersMentions();
        List<Mention> allPassages = passages.getAllClustersMentions();
        System.out.println("Number of queries=" + allQueries.size());
        System.out.println("Number of passages=" + allPassages.size());
        IndexSearcher indexSearcher = getIndexSearcher(indexDir);

//        int[] topKs = {10, 50, 100, 150, 200, 300, 400, 500};
        int[] topKs = {200};
        for (int topK : topKs) {
            BooleanQuery.setMaxClauseCount(4096);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser(CreateOrDeleteIndex.PASSAGE, analyzer);
            runLuceneExpr(indexSearcher, parser, allQueries, topK, passages.getClusters());
        }
        closeIndexSearcher(indexSearcher);
    }

    private static void runLuceneExpr(IndexSearcher idxSearcher, QueryParser parser, List<Mention> queries,
                                      int topK, Map<Integer, Cluster> passagesClusters) throws IOException {
        // Parse a simple query that searches for "text":
        float totalMents = 0;
        float mrr = 0;
        List<SearchResult> results = new ArrayList<>();
        for(Mention ment : queries) {
            totalMents += passagesClusters.get(ment.getCoref_chain()).getMentions().size();
//            String finalQuery = String.join(" ", ment.getMention_context());
//            String mentQuery = ment.getTokens_str();
//            String finalQuery = mentQuery + " . " + NLPUtils.getQueryNERs(ment);
            String finalQuery = NLPUtils.getQuerySentence(ment);

            assert finalQuery != null;
            String queryText = QueryParser.escape(finalQuery);
            ScoreDoc[] hits = searchIndex(idxSearcher, parser, queryText, topK);
            SearchResult searchResult = getSearchResult(idxSearcher, ment, hits);
            results.add(searchResult);
            float firstCoveredRank = searchResult.getFirstCoveredRank();
            if (firstCoveredRank != 0) {
                mrr += (1 / firstCoveredRank);
            }
        }

        int covered = SearchResult.calcCoveredMentions(results);

        System.out.println("Total of-" + queries.size() + " queries, " + covered + " out of " + totalMents + " mentions are covered with k=" + topK);
        System.out.println("Coverage percentage=" + (covered / totalMents));
        System.out.println("MRR=" + (mrr / queries.size()));

        Path colberTopKFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) + "Top" + topK + ".tsv");
        Files.write(colberTopKFile, SearchResult.getAllQueryPassages(results), Charset.defaultCharset());
    }

    public static SearchResult getSearchResult(IndexSearcher idxSearcher, Mention mentQuery, ScoreDoc[] hits) throws IOException {
        int firstCoveredRank = 0;
        List<RetPassage> topKPass = new ArrayList<>();
        if(hits != null) {
            for (int i = 0, hitsLength = hits.length; i < hitsLength; i++) {
                int rank = i+1;
                ScoreDoc hit = hits[i];
                org.apache.lucene.document.Document hitDoc = idxSearcher.doc(hit.doc);
                String passage = hitDoc.get(CreateOrDeleteIndex.PASSAGE);
                int passageId = Integer.parseInt(hitDoc.get(CreateOrDeleteIndex.PASSAGE_ID));
                int retCorefId = Integer.parseInt(hitDoc.get(CreateOrDeleteIndex.PASSAGE_CORE_ID));
                topKPass.add(new RetPassage(passageId, retCorefId, passage, rank));
                if (firstCoveredRank == 0 && retCorefId == mentQuery.getCoref_chain()) {
                    firstCoveredRank = rank;
                }
            }
        }

        return new SearchResult(mentQuery, topKPass, firstCoveredRank);
    }

    public static ScoreDoc[] searchIndex(IndexSearcher isearcher, QueryParser parser, String queryText, int topK) {
        try {
            Query query = parser.parse(queryText);
            return isearcher.search(query, topK).scoreDocs;
        } catch (ParseException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static IndexSearcher getIndexSearcher(String indexDir) throws IOException {
        Path indexPath = Paths.get(indexDir);
        Directory directory = FSDirectory.open(indexPath);

        // Now search the index:
        DirectoryReader ireader = DirectoryReader.open(directory);
        return new IndexSearcher(ireader);
    }

    public static void closeIndexSearcher(IndexSearcher indexSearcher) throws IOException {
        if (indexSearcher != null)
            indexSearcher.getIndexReader().close();
    }
}
