package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;

public class TokenMap
{
    private int size;
    private TokenMapToken[] tokenMap;
    private boolean ignoreCase;
    private static final int DEFAULT_TOKEN_MAP_SIZE = 52;
    
    public TokenMap() {
        this(52);
    }
    
    public TokenMap(final int size) {
        this(size, false);
    }
    
    public TokenMap(final boolean ignoreCase) {
        this(52, ignoreCase);
    }
    
    public TokenMap(final int size, final boolean ignoreCase) {
        super();
        this.size = size;
        this.tokenMap = new TokenMapToken[size];
        this.ignoreCase = ignoreCase;
    }
    
    private void addTokenToBucket(final int bucket, final TokenMapToken token) {
        final TokenMapToken old = this.tokenMap[bucket];
        token.nextToken = old;
        this.tokenMap[bucket] = token;
    }
    
    public int get(final Segment text, final int start, final int end) {
        return this.get(text.array, start, end);
    }
    
    public int get(final char[] array1, final int start, final int end) {
        final int length1 = end - start + 1;
        final int hash = this.getHashCode(array1, start, length1);
        TokenMapToken token = this.tokenMap[hash];
        if (!this.ignoreCase) {
        Label_0033:
            while (token != null) {
                if (token.length == length1) {
                    final char[] array2 = token.text;
                    int offset2 = token.offset;
                    offset2 = start;
                    int length2 = length1;
                    while (length2-- > 0) {
                        if (array1[offset2++] != array2[offset2++]) {
                            token = token.nextToken;
                            continue Label_0033;
                        }
                    }
                    return token.tokenType;
                }
                token = token.nextToken;
            }
        }
        else {
        Label_0123:
            while (token != null) {
                if (token.length == length1) {
                    final char[] array2 = token.text;
                    int offset2 = token.offset;
                    offset2 = start;
                    int length2 = length1;
                    while (length2-- > 0) {
                        if (RSyntaxUtilities.toLowerCase(array1[offset2++]) != array2[offset2++]) {
                            token = token.nextToken;
                            continue Label_0123;
                        }
                    }
                    return token.tokenType;
                }
                token = token.nextToken;
            }
        }
        return -1;
    }
    
    private final int getHashCode(final char[] text, final int offset, final int length) {
        return (RSyntaxUtilities.toLowerCase(text[offset]) + RSyntaxUtilities.toLowerCase(text[offset + length - 1])) % this.size;
    }
    
    protected boolean isIgnoringCase() {
        return this.ignoreCase;
    }
    
    public void put(final String string, final int tokenType) {
        if (this.isIgnoringCase()) {
            this.put(string.toLowerCase().toCharArray(), tokenType);
        }
        else {
            this.put(string.toCharArray(), tokenType);
        }
    }
    
    private void put(final char[] string, final int tokenType) {
        final int hashCode = this.getHashCode(string, 0, string.length);
        this.addTokenToBucket(hashCode, new TokenMapToken(string, tokenType));
    }
    
    private static class TokenMapToken
    {
        char[] text;
        int offset;
        int length;
        int tokenType;
        TokenMapToken nextToken;
        
        TokenMapToken(final char[] text, final int tokenType) {
            super();
            this.text = text;
            this.offset = 0;
            this.length = text.length;
            this.tokenType = tokenType;
        }
        
        public String toString() {
            return "[TokenMapToken: " + new String(this.text, this.offset, this.length) + "]";
        }
    }
}
