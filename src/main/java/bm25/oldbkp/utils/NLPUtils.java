package bm25.oldbkp.utils;

import bm25.oldbkp.data.Mention;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;
import java.util.stream.Collectors;

public class NLPUtils {

    private static final StanfordCoreNLP pipeline;

    static {
        // set up pipeline properties
        Properties props = new Properties();
        // set the list of annotators to run
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        // build pipeline
        pipeline = new StanfordCoreNLP(props);
    }

    public static void convertToEventWithNerQuery(List<Mention> queries) {
        if (queries != null) {
            for (Mention query : queries) {
                String mentQuery = query.getTokens_str();
                String finalQuery = mentQuery + " . " + NLPUtils.getQueryNERs(query);
                query.setMention_context(Arrays.asList(finalQuery.split(" ")));
            }
        }
    }

    public static void convertToEventSentenceQuery(List<Mention> queries) {
        if (queries != null) {
            for (Mention query : queries) {
                String finalQuery = getQuerySentence(query);
                assert finalQuery != null;
                query.setMention_context(Arrays.asList(finalQuery.split(" ")));
            }
        }
    }

    public static String getQueryNERs(Mention query) {
        String querySentence = getQuerySentence(query);
        CoreDocument doc = new CoreDocument(querySentence);
        pipeline.annotate(doc);

        Set<String> docNers = doc.tokens().stream().filter(token -> !token.ner().equals("O") &&
                !token.ner().equals("NUMBER") && !token.ner().equals("MONEY"))
                .map(CoreLabel::lemma).collect(Collectors.toSet());

        return String.join(" ", docNers);
    }

    public static String getQuerySentence(Mention query) {
        Sentence sentence = getSentence(query);
        if(sentence != null) {
            return sentence.text();
        }
        return null;
    }

    public static int getQuerySentenceInx(Mention query) {
        Sentence sentence = getSentence(query);
        if(sentence != null) {
            return sentence.sentenceIndex();
        }
        return -1;
    }

    private static Sentence getSentence(Mention query) {
        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(
                String.join(" ", query.getMention_context()));

        for(Sentence sent : doc.sentences()) {
            for (int i = 0; i < sent.tokens().size(); i++) {
                CoreNLPProtos.Token.Builder builder = sent.rawToken(i);
                if (builder.getTokenBeginIndex() == query.getTokens_number().get(0)) {
                    return sent;
                }
            }
        }

        return null;
    }
}
