package org.fife.ui.rtextarea;

class RegExReplaceInfo
{
    private String matchedText;
    private int startIndex;
    private int endIndex;
    private String replacement;
    
    public RegExReplaceInfo(final String matchedText, final int start, final int end, final String replacement) {
        super();
        this.matchedText = matchedText;
        this.startIndex = start;
        this.endIndex = end;
        this.replacement = replacement;
    }
    
    public int getEndIndex() {
        return this.endIndex;
    }
    
    public String getMatchedText() {
        return this.matchedText;
    }
    
    public String getReplacement() {
        return this.replacement;
    }
    
    public int getStartIndex() {
        return this.startIndex;
    }
}
