package org.fife.ui.rsyntaxtextarea;

public abstract class AbstractTokenMaker extends TokenMakerBase
{
    protected TokenMap wordsToHighlight;
    
    public AbstractTokenMaker() {
        super();
        this.wordsToHighlight = this.getWordsToHighlight();
    }
    
    public abstract TokenMap getWordsToHighlight();
    
    public void removeLastToken() {
        if (this.previousToken == null) {
            final TokenImpl loc_0 = null;
            this.currentToken = loc_0;
            this.firstToken = loc_0;
        }
        else {
            (this.currentToken = this.previousToken).setNextToken(null);
        }
    }
}
