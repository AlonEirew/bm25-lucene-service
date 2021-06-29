package bm25.search;

import bm25.data.Mention;
import bm25.data.WECSplit;
import bm25.index.CreateOrDeleteIndex;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import bm25.utils.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class UISearchExpr {
    public static void main(String[] args) throws IOException {
        File wecQueiresFile = new File("search_jsons/Dev_queries.json");
        File wecPassagesFile = new File("search_jsons/Dev_passages.json");
        String indexDir = "tempIndex/Dev";
        int topK = 1000;
        Scanner in = new Scanner(System.in);

        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser = new QueryParser(CreateOrDeleteIndex.PASSAGE, analyzer);

        WECSplit queries = JsonUtils.readWECJsonFile(wecQueiresFile);
        WECSplit passages = JsonUtils.readWECJsonFile(wecPassagesFile);

        IndexSearcher idxSearcher = MainBM25.getIndexSearcher(indexDir);
        for(int i = 0 ; i < 5 ; i++) {
            int rand = ThreadLocalRandom.current().nextInt(0, queries.getAllClustersMentions().size());
            Mention query = queries.getAllClustersMentions().get(rand);
            int inQueryClust = passages.getClusters().get(query.getCoref_chain()).getMentions().size();
            String originQueryText = QueryParser.escape(String.join(" ", query.getMention_context()));
            ScoreDoc[] origHits = MainBM25.searchIndex(idxSearcher, parser, originQueryText, topK);
            System.out.println("QueryText=" + getMentionContextForPrint(query));
            System.out.println("Results Original:");
            float originalCount = MainBM25.getSearchResult(idxSearcher, query, origHits).getConvertedMents();
            System.out.println("Original query=" + originalCount + ":" + inQueryClust);
            boolean ret = true;
            while(ret) {
                String inputQuery = in.nextLine();
                if (inputQuery != null && !inputQuery.isEmpty()) {
                    ScoreDoc[] inputHits = MainBM25.searchIndex(idxSearcher, parser, inputQuery, topK);
                    System.out.println("Results Input:");
                    float inputCount = MainBM25.getSearchResult(idxSearcher, query, inputHits).getConvertedMents();
                    System.out.println("Input query=" + inputCount + ":" + inQueryClust);
                    System.out.println("Retry? [y/n]");
                    String retStr = in.nextLine();
                    if (retStr.equalsIgnoreCase("n")) {
                        ret = false;
                    }
                }
            }
            System.out.println("--------------------------------");
        }

        in.close();
        MainBM25.closeIndexSearcher(idxSearcher);
    }

    private static String getMentionContextForPrint(Mention query) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < query.getMention_context().size() ; i++) {
            if (i == query.getTokens_number().get(0)) {
                sb.append("<");
            }
            sb.append(query.getMention_context().get(i));
            if(i == query.getTokens_number().get(query.getTokens_number().size() - 1)) {
                sb.append(">");
            }
            sb.append(" ");
        }
        return sb.toString();
    }
}
