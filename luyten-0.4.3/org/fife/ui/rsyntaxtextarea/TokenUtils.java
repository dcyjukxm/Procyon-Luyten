package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;

public class TokenUtils
{
    public static TokenSubList getSubTokenList(final Token tokenList, final int pos, final TabExpander e, final RSyntaxTextArea textArea, final float x0) {
        return getSubTokenList(tokenList, pos, e, textArea, x0, null);
    }
    
    public static TokenSubList getSubTokenList(final Token tokenList, final int pos, final TabExpander e, final RSyntaxTextArea textArea, float x0, TokenImpl tempToken) {
        if (tempToken == null) {
            tempToken = new TokenImpl();
        }
        Token t;
        for (t = tokenList; t != null && t.isPaintable() && !t.containsPosition(pos); t = t.getNextToken()) {
            x0 += t.getWidth(textArea, e, x0);
        }
        if (t == null || !t.isPaintable()) {
            return new TokenSubList(tokenList, x0);
        }
        if (t.getOffset() != pos) {
            final int difference = pos - t.getOffset();
            x0 += t.getWidthUpTo(t.length() - difference + 1, textArea, e, x0);
            tempToken.copyFrom(t);
            tempToken.makeStartAt(pos);
            return new TokenSubList(tempToken, x0);
        }
        return new TokenSubList(t, x0);
    }
    
    public static class TokenSubList
    {
        public Token tokenList;
        public float x;
        
        public TokenSubList(final Token tokenList, final float x) {
            super();
            this.tokenList = tokenList;
            this.x = x;
        }
    }
}
