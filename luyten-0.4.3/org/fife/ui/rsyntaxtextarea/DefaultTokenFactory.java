package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;

class DefaultTokenFactory implements TokenFactory
{
    private int size;
    private int increment;
    private TokenImpl[] tokenList;
    private int currentFreeToken;
    protected static final int DEFAULT_START_SIZE = 30;
    protected static final int DEFAULT_INCREMENT = 10;
    
    public DefaultTokenFactory() {
        this(30, 10);
    }
    
    public DefaultTokenFactory(final int size, final int increment) {
        super();
        this.size = size;
        this.increment = increment;
        this.currentFreeToken = 0;
        this.tokenList = new TokenImpl[size];
        for (int i = 0; i < size; ++i) {
            this.tokenList[i] = new TokenImpl();
        }
    }
    
    private final void augmentTokenList() {
        final TokenImpl[] temp = new TokenImpl[this.size + this.increment];
        System.arraycopy(this.tokenList, 0, temp, 0, this.size);
        this.size += this.increment;
        this.tokenList = temp;
        for (int i = 0; i < this.increment; ++i) {
            this.tokenList[this.size - i - 1] = new TokenImpl();
        }
    }
    
    public TokenImpl createToken() {
        final TokenImpl token = this.tokenList[this.currentFreeToken];
        token.text = null;
        token.setType(0);
        token.setOffset(-1);
        token.setNextToken(null);
        ++this.currentFreeToken;
        if (this.currentFreeToken == this.size) {
            this.augmentTokenList();
        }
        return token;
    }
    
    public TokenImpl createToken(final Segment line, final int beg, final int end, final int startOffset, final int type) {
        return this.createToken(line.array, beg, end, startOffset, type);
    }
    
    public TokenImpl createToken(final char[] line, final int beg, final int end, final int startOffset, final int type) {
        final TokenImpl token = this.tokenList[this.currentFreeToken];
        token.set(line, beg, end, startOffset, type);
        ++this.currentFreeToken;
        if (this.currentFreeToken == this.size) {
            this.augmentTokenList();
        }
        return token;
    }
    
    public void resetAllTokens() {
        this.currentFreeToken = 0;
    }
}
