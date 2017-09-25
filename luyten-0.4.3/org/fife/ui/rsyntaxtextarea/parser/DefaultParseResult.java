package org.fife.ui.rsyntaxtextarea.parser;

import java.util.*;

public class DefaultParseResult implements ParseResult
{
    private Parser parser;
    private int firstLineParsed;
    private int lastLineParsed;
    private List<ParserNotice> notices;
    private long parseTime;
    private Exception error;
    
    public DefaultParseResult(final Parser parser) {
        super();
        this.parser = parser;
        this.notices = new ArrayList<ParserNotice>();
    }
    
    public void addNotice(final ParserNotice notice) {
        this.notices.add(notice);
    }
    
    public void clearNotices() {
        this.notices.clear();
    }
    
    public Exception getError() {
        return this.error;
    }
    
    public int getFirstLineParsed() {
        return this.firstLineParsed;
    }
    
    public int getLastLineParsed() {
        return this.lastLineParsed;
    }
    
    public List<ParserNotice> getNotices() {
        return this.notices;
    }
    
    public long getParseTime() {
        return this.parseTime;
    }
    
    public Parser getParser() {
        return this.parser;
    }
    
    public void setError(final Exception e) {
        this.error = e;
    }
    
    public void setParseTime(final long time) {
        this.parseTime = time;
    }
    
    public void setParsedLines(final int first, final int last) {
        this.firstLineParsed = first;
        this.lastLineParsed = last;
    }
}
