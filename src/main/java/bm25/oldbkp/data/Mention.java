package bm25.oldbkp.data;

import java.util.List;

public class Mention {
    private int coref_chain;
    private String coref_link;
    private String doc_id;
    private List<String> mention_context;
    private String mention_id;
    private String tokens_str;
    private List<Integer> tokens_number;

    public Mention(Mention mention) {
        this.coref_chain = mention.coref_chain;
        this.coref_link = mention.coref_link;
        this.doc_id = mention.doc_id;
        this.mention_context = mention.mention_context;
        this.mention_id = mention.mention_id;
        this.tokens_str = mention.tokens_str;
        this.tokens_number = mention.tokens_number;
    }

    public int getCoref_chain() {
        return coref_chain;
    }

    public void setCoref_chain(int coref_chain) {
        this.coref_chain = coref_chain;
    }

    public String getCoref_link() {
        return coref_link;
    }

    public void setCoref_link(String coref_link) {
        this.coref_link = coref_link;
    }

    public String getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(String doc_id) {
        this.doc_id = doc_id;
    }

    public List<String> getMention_context() {
        return mention_context;
    }

    public void setMention_context(List<String> mention_context) {
        this.mention_context = mention_context;
    }

    public String getMention_id() {
        return mention_id;
    }

    public void setMention_id(String mention_id) {
        this.mention_id = mention_id;
    }

    public String getTokens_str() {
        return tokens_str;
    }

    public void setTokens_str(String tokens_str) {
        this.tokens_str = tokens_str;
    }

    public List<Integer> getTokens_number() {
        return tokens_number;
    }

    public void setTokens_number(List<Integer> tokens_number) {
        this.tokens_number = tokens_number;
    }
}
