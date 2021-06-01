package utils;

import data.Mention;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.simple.Sentence;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Utils {

    public static void reIndexMentionList(List<Mention> mentions) {
        // Reindex queries and passages
        for(int i = 0 ; i < mentions.size() ; i++) {
            mentions.get(i).setMention_id(String.valueOf(i));
        }
    }

    public static void convertToEventWithNerQuery(List<Mention> queries) {
        if (queries != null) {
            for (Mention query : queries) {
                String mentQuery = query.getTokens_str();
                String finalQuery = mentQuery + " . " + Utils.getQueryNERs(query);
                query.setMention_context(Arrays.asList(finalQuery.split(" ")));
            }
        }
    }

    public static String getQueryNERs(Mention query) {
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

    public static String getQuerySentence(Mention query) {
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
}
