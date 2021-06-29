package bm25.scripts;

import bm25.data.Mention;
import bm25.data.SplitType;
import bm25.data.WECSplit;
import edu.stanford.nlp.pipeline.CoreNLPProtos;
import edu.stanford.nlp.simple.Sentence;
import bm25.utils.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportToDecontext {
    public static void main(String[] args) throws IOException {
        String wecFolder = "input/WEC-Eng";
        List<WECSplit> splits = JsonUtils.readWECJsonFolder(wecFolder);
        List<DecontextObj> queries = new ArrayList<>();
        for (WECSplit split : splits) {
            if (split.getSplitType() != SplitType.Train) {
                for(Mention ment : split.getAllClustersMentions()) {
                    queries.add(genDecontextObj(ment));
                }
            }
        }

        JsonUtils.writeJsonObjListToFile(queries, "output/decontext_queries/queries.json");
        System.out.println("Total queries=" + queries.size());

    }

    private static DecontextObj genDecontextObj(Mention query) {
        edu.stanford.nlp.simple.Document doc = new edu.stanford.nlp.simple.Document(
                String.join(" ", query.getMention_context()));

        DecontextObj decontextObj = new DecontextObj();
        decontextObj.setPage_title(query.getDoc_id());
        decontextObj.setQuery_event(query.getTokens_str());
        decontextObj.setQuery_id(query.getMention_id());
        List<String> paragraph = new ArrayList<>();
        boolean keepSearch = true;
        for(Sentence sent : doc.sentences()) {
            paragraph.add(sent.text());
            if (keepSearch) {
                for (int i = 0; i < sent.tokens().size(); i++) {
                    CoreNLPProtos.Token.Builder builder = sent.rawToken(i);
                    if (builder.getTokenBeginIndex() == query.getTokens_number().get(0)) {
                        decontextObj.setTarget_sentence_idx(sent.sentenceIndex());
                        keepSearch = false;
                    }
                }
            }
        }

        decontextObj.setParagraph(paragraph);
        return decontextObj;
    }
}
