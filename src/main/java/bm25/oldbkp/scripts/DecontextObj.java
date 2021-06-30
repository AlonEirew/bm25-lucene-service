package bm25.oldbkp.scripts;

import java.util.List;

public class DecontextObj {
    private List<String> paragraph;
    private String page_title;
    private String section_title;
    private int target_sentence_idx;
    private String query_id;
    private String query_event;

    public List<String> getParagraph() {
        return paragraph;
    }

    public void setParagraph(List<String> paragraph) {
        this.paragraph = paragraph;
    }

    public String getPage_title() {
        return page_title;
    }

    public void setPage_title(String page_title) {
        this.page_title = page_title;
    }

    public String getSection_title() {
        return section_title;
    }

    public void setSection_title(String section_title) {
        this.section_title = section_title;
    }

    public int getTarget_sentence_idx() {
        return target_sentence_idx;
    }

    public void setTarget_sentence_idx(int target_sentence_idx) {
        this.target_sentence_idx = target_sentence_idx;
    }

    public String getQuery_id() {
        return query_id;
    }

    public void setQuery_id(String query_id) {
        this.query_id = query_id;
    }

    public String getQuery_event() {
        return query_event;
    }

    public void setQuery_event(String query_event) {
        this.query_event = query_event;
    }
}
