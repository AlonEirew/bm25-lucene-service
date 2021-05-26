import data.*;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.simple.Sentence;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static final String SPLIT = "Test";

    public static void main(String[] args) throws IOException {
        File wecQueiresFile = new File("search_jsons/" + SPLIT + "_queries.json");
        File wecPassagesFile = new File("search_jsons/" + SPLIT + "_passages.json");
        String indexDir = "tempIndex/" + SPLIT;
        WECSplit queries = Utils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = Utils.readWECJsonFile(wecPassagesFile);
        System.out.println("SPLIT=" + SPLIT);
        System.out.println("Number of queries=" + queries.getMentions().size());
        System.out.println("Number of passages=" + passages.getMentions().size());
        IndexSearcher indexSearcher = getIndexSearcher(indexDir);

//        int[] topKs = {10, 50, 100, 150, 200, 300, 400, 500};
        int[] topKs = {200};
        for (int topK : topKs) {
            BooleanQuery.setMaxClauseCount(4096);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser parser = new QueryParser(CreateOrDeleteIndex.PASSAGE, analyzer);
            runLuceneExpr(indexSearcher, parser, queries.getMentions(), topK, passages.getClusters());
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
//            String finalQuery = QueryParser.escape(String.join(" ", ment.getMention_context()));
            String mentQuery = QueryParser.escape(ment.getTokens_str());
//            String mentQuery = getQuerySentence(ment);
            String finalQuery = mentQuery + ". " + getQueryNERs(ment);
//            String querySentence = getQueryNERs(ment);

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

        Path colberTopKFile = Paths.get("output/colbert/" + SPLIT.toLowerCase(Locale.ROOT) + "Top" + topK + ".json");
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

    private static String getQuerySentence(Mention query) {
        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(
                String.join(" ", query.getMention_context()));

        for(Sentence sent : doc.sentences()) {
            for (int i = 0; i < sent.tokens().size(); i++) {
                CoreNLPProtos.Token.Builder builder = sent.rawToken(i);
                if (builder.getTokenBeginIndex() == query.getTokens_number().get(0)) {
                    return sent.text();
                }
            }
        }

        return null;
    }

    private static String getQueryNERs(Mention query) {
        Set<String> docNers = new HashSet<>();
        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(
                String.join(" ", query.getMention_context()));

        for(Sentence sent : doc.sentences()) {
            for (int i = 0; i < sent.tokens().size(); i++) {
                sent.nerTags();
                CoreNLPProtos.Token.Builder builder = sent.rawToken(i);
                String nerTag = builder.getNer();
                if (!nerTag.equals("O")) {
                    docNers.add(builder.getWord());
                }
            }
        }

        return String.join(" ", docNers);
    }
}
