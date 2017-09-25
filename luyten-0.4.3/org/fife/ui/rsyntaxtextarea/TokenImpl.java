package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import javax.swing.text.*;

public class TokenImpl implements Token
{
    public char[] text;
    public int textOffset;
    public int textCount;
    private int offset;
    private int type;
    private boolean hyperlink;
    private Token nextToken;
    private int languageIndex;
    
    public TokenImpl() {
        super();
        this.text = null;
        this.textOffset = -1;
        this.textCount = -1;
        this.setType(0);
        this.setOffset(-1);
        this.hyperlink = false;
        this.nextToken = null;
    }
    
    public TokenImpl(final Segment line, final int beg, final int end, final int startOffset, final int type) {
        this(line.array, beg, end, startOffset, type);
    }
    
    public TokenImpl(final char[] line, final int beg, final int end, final int startOffset, final int type) {
        this();
        this.set(line, beg, end, startOffset, type);
    }
    
    public TokenImpl(final Token t2) {
        this();
        this.copyFrom(t2);
    }
    
    public StringBuilder appendHTMLRepresentation(final StringBuilder sb, final RSyntaxTextArea textArea, final boolean fontFamily) {
        return this.appendHTMLRepresentation(sb, textArea, fontFamily, false);
    }
    
    public StringBuilder appendHTMLRepresentation(final StringBuilder sb, final RSyntaxTextArea textArea, final boolean fontFamily, final boolean tabsToSpaces) {
        final SyntaxScheme colorScheme = textArea.getSyntaxScheme();
        final Style scheme = colorScheme.getStyle(this.getType());
        final Font font = textArea.getFontForTokenType(this.getType());
        if (font.isBold()) {
            sb.append("<b>");
        }
        if (font.isItalic()) {
            sb.append("<em>");
        }
        if (scheme.underline || this.isHyperlink()) {
            sb.append("<u>");
        }
        sb.append("<font");
        if (fontFamily) {
            sb.append(" face=\"").append(font.getFamily()).append("\"");
        }
        sb.append(" color=\"").append(getHTMLFormatForColor(scheme.foreground)).append("\">");
        this.appendHtmlLexeme(textArea, sb, tabsToSpaces);
        sb.append("</font>");
        if (scheme.underline || this.isHyperlink()) {
            sb.append("</u>");
        }
        if (font.isItalic()) {
            sb.append("</em>");
        }
        if (font.isBold()) {
            sb.append("</b>");
        }
        return sb;
    }
    
    private final StringBuilder appendHtmlLexeme(final RSyntaxTextArea textArea, final StringBuilder sb, final boolean tabsToSpaces) {
        boolean lastWasSpace = false;
        int lastI;
        int i = lastI = this.textOffset;
        String tabStr = null;
        while (i < this.textOffset + this.textCount) {
            final char ch = this.text[i];
            switch (ch) {
                case ' ': {
                    sb.append(this.text, lastI, i - lastI);
                    lastI = i + 1;
                    sb.append(lastWasSpace ? "&nbsp;" : " ");
                    lastWasSpace = true;
                    break;
                }
                case '\t': {
                    sb.append(this.text, lastI, i - lastI);
                    lastI = i + 1;
                    if (tabsToSpaces && tabStr == null) {
                        tabStr = "";
                        for (int j = 0; j < textArea.getTabSize(); ++j) {
                            tabStr += "&nbsp;";
                        }
                    }
                    sb.append(tabsToSpaces ? tabStr : "&#09;");
                    lastWasSpace = false;
                    break;
                }
                case '<': {
                    sb.append(this.text, lastI, i - lastI);
                    lastI = i + 1;
                    sb.append("&lt;");
                    lastWasSpace = false;
                    break;
                }
                case '>': {
                    sb.append(this.text, lastI, i - lastI);
                    lastI = i + 1;
                    sb.append("&gt;");
                    lastWasSpace = false;
                    break;
                }
                default: {
                    lastWasSpace = false;
                    break;
                }
            }
            ++i;
        }
        if (lastI < this.textOffset + this.textCount) {
            sb.append(this.text, lastI, this.textOffset + this.textCount - lastI);
        }
        return sb;
    }
    
    public char charAt(final int index) {
        return this.text[this.textOffset + index];
    }
    
    public boolean containsPosition(final int pos) {
        return pos >= this.getOffset() && pos < this.getOffset() + this.textCount;
    }
    
    public void copyFrom(final Token t2) {
        this.text = t2.getTextArray();
        this.textOffset = t2.getTextOffset();
        this.textCount = t2.length();
        this.setOffset(t2.getOffset());
        this.setType(t2.getType());
        this.hyperlink = t2.isHyperlink();
        this.languageIndex = t2.getLanguageIndex();
        this.nextToken = t2.getNextToken();
    }
    
    public int documentToToken(final int pos) {
        return pos + (this.textOffset - this.getOffset());
    }
    
    public boolean endsWith(final char[] ch) {
        if (ch == null || ch.length > this.textCount) {
            return false;
        }
        final int start = this.textOffset + this.textCount - ch.length;
        for (int i = 0; i < ch.length; ++i) {
            if (this.text[start + i] != ch[i]) {
                return false;
            }
        }
        return true;
    }
    
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Token)) {
            return false;
        }
        final Token t2 = (Token)obj;
        return this.offset == t2.getOffset() && this.type == t2.getType() && this.languageIndex == t2.getLanguageIndex() && this.hyperlink == t2.isHyperlink() && ((this.getLexeme() == null && t2.getLexeme() == null) || (this.getLexeme() != null && this.getLexeme().equals(t2.getLexeme())));
    }
    
    public int getEndOffset() {
        return this.offset + this.textCount;
    }
    
    private static final String getHTMLFormatForColor(final Color color) {
        if (color == null) {
            return "black";
        }
        String hexRed = Integer.toHexString(color.getRed());
        if (hexRed.length() == 1) {
            hexRed = "0" + hexRed;
        }
        String hexGreen = Integer.toHexString(color.getGreen());
        if (hexGreen.length() == 1) {
            hexGreen = "0" + hexGreen;
        }
        String hexBlue = Integer.toHexString(color.getBlue());
        if (hexBlue.length() == 1) {
            hexBlue = "0" + hexBlue;
        }
        return "#" + hexRed + hexGreen + hexBlue;
    }
    
    public String getHTMLRepresentation(final RSyntaxTextArea textArea) {
        final StringBuilder buf = new StringBuilder();
        this.appendHTMLRepresentation(buf, textArea, true);
        return buf.toString();
    }
    
    public int getLanguageIndex() {
        return this.languageIndex;
    }
    
    public Token getLastNonCommentNonWhitespaceToken() {
        Token last = null;
        for (Token t = this; t != null && t.isPaintable(); t = t.getNextToken()) {
            switch (t.getType()) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 21: {
                    break;
                }
                default: {
                    last = t;
                    break;
                }
            }
        }
        return last;
    }
    
    public Token getLastPaintableToken() {
        Token next;
        for (Token t = this; t.isPaintable(); t = next) {
            next = t.getNextToken();
            if (next == null || !next.isPaintable()) {
                return t;
            }
        }
        return null;
    }
    
    public String getLexeme() {
        return (this.text == null) ? null : new String(this.text, this.textOffset, this.textCount);
    }
    
    public int getListOffset(final RSyntaxTextArea textArea, final TabExpander e, final float x0, final float x) {
        if (x0 >= x) {
            return this.getOffset();
        }
        float nextX = x0;
        float stableX = x0;
        TokenImpl token = this;
        int last = this.getOffset();
        FontMetrics fm = null;
        while (token != null && token.isPaintable()) {
            fm = textArea.getFontMetricsForTokenType(token.getType());
            final char[] text = token.text;
            int start = token.textOffset;
            final int end = start + token.textCount;
            int i = start;
            while (i < end) {
                final float currX = nextX;
                if (text[i] == '\t') {
                    nextX = (stableX = e.nextTabStop(nextX, 0));
                    start = i + 1;
                }
                else {
                    nextX = stableX + fm.charsWidth(text, start, i - start + 1);
                }
                if (x >= currX && x < nextX) {
                    if (x - currX < nextX - x) {
                        return last + i - token.textOffset;
                    }
                    return last + i + 1 - token.textOffset;
                }
                else {
                    ++i;
                }
            }
            stableX = nextX;
            last += token.textCount;
            token = (TokenImpl)token.getNextToken();
        }
        return last;
    }
    
    public Token getNextToken() {
        return this.nextToken;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public int getOffsetBeforeX(final RSyntaxTextArea textArea, final TabExpander e, final float startX, final float endBeforeX) {
        final FontMetrics fm = textArea.getFontMetricsForTokenType(this.getType());
        int i = this.textOffset;
        final int stop = i + this.textCount;
        float x = startX;
        while (i < stop) {
            if (this.text[i] == '\t') {
                x = e.nextTabStop(x, 0);
            }
            else {
                x += fm.charWidth(this.text[i]);
            }
            if (x > endBeforeX) {
                final int intoToken = Math.max(i - this.textOffset, 1);
                return this.getOffset() + intoToken;
            }
            ++i;
        }
        return this.getOffset() + this.textCount - 1;
    }
    
    public char[] getTextArray() {
        return this.text;
    }
    
    public int getTextOffset() {
        return this.textOffset;
    }
    
    public int getType() {
        return this.type;
    }
    
    public float getWidth(final RSyntaxTextArea textArea, final TabExpander e, final float x0) {
        return this.getWidthUpTo(this.textCount, textArea, e, x0);
    }
    
    public float getWidthUpTo(final int numChars, final RSyntaxTextArea textArea, final TabExpander e, final float x0) {
        float width = x0;
        final FontMetrics fm = textArea.getFontMetricsForTokenType(this.getType());
        if (fm != null) {
            int currentStart = this.textOffset;
            final int endBefore = this.textOffset + numChars;
            for (int i = currentStart; i < endBefore; ++i) {
                if (this.text[i] == '\t') {
                    final int w = i - currentStart;
                    if (w > 0) {
                        width += fm.charsWidth(this.text, currentStart, w);
                    }
                    currentStart = i + 1;
                    width = e.nextTabStop(width, 0);
                }
            }
            final int w = endBefore - currentStart;
            width += fm.charsWidth(this.text, currentStart, w);
        }
        return width - x0;
    }
    
    public int hashCode() {
        return this.offset + ((this.getLexeme() == null) ? 0 : this.getLexeme().hashCode());
    }
    
    public boolean is(final char[] lexeme) {
        if (this.textCount == lexeme.length) {
            for (int i = 0; i < this.textCount; ++i) {
                if (this.text[this.textOffset + i] != lexeme[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean is(final int type, final char[] lexeme) {
        if (this.getType() == type && this.textCount == lexeme.length) {
            for (int i = 0; i < this.textCount; ++i) {
                if (this.text[this.textOffset + i] != lexeme[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean is(final int type, final String lexeme) {
        return this.getType() == type && this.textCount == lexeme.length() && lexeme.equals(this.getLexeme());
    }
    
    public boolean isComment() {
        return this.getType() >= 1 && this.getType() <= 5;
    }
    
    public boolean isCommentOrWhitespace() {
        return this.isComment() || this.isWhitespace();
    }
    
    public boolean isHyperlink() {
        return this.hyperlink;
    }
    
    public boolean isIdentifier() {
        return this.getType() == 20;
    }
    
    public boolean isLeftCurly() {
        return this.getType() == 22 && this.isSingleChar('{');
    }
    
    public boolean isRightCurly() {
        return this.getType() == 22 && this.isSingleChar('}');
    }
    
    public boolean isPaintable() {
        return this.getType() > 0;
    }
    
    public boolean isSingleChar(final char ch) {
        return this.textCount == 1 && this.text[this.textOffset] == ch;
    }
    
    public boolean isSingleChar(final int type, final char ch) {
        return this.getType() == type && this.isSingleChar(ch);
    }
    
    public boolean isWhitespace() {
        return this.getType() == 21;
    }
    
    public int length() {
        return this.textCount;
    }
    
    public Rectangle listOffsetToView(final RSyntaxTextArea textArea, final TabExpander e, final int pos, final int x0, final Rectangle rect) {
        int stableX = x0;
        TokenImpl token = this;
        FontMetrics fm = null;
        final Segment s = new Segment();
        while (token != null && token.isPaintable()) {
            fm = textArea.getFontMetricsForTokenType(token.getType());
            if (fm == null) {
                return rect;
            }
            final char[] text = token.text;
            final int start = token.textOffset;
            int end = start + token.textCount;
            if (token.containsPosition(pos)) {
                s.array = token.text;
                s.offset = token.textOffset;
                s.count = pos - token.getOffset();
                final int w = Utilities.getTabbedTextWidth(s, fm, stableX, e, token.getOffset());
                rect.x = stableX + w;
                end = token.documentToToken(pos);
                if (text[end] == '\t') {
                    rect.width = fm.charWidth(' ');
                }
                else {
                    rect.width = fm.charWidth(text[end]);
                }
                return rect;
            }
            s.array = token.text;
            s.offset = token.textOffset;
            s.count = token.textCount;
            stableX += Utilities.getTabbedTextWidth(s, fm, stableX, e, token.getOffset());
            token = (TokenImpl)token.getNextToken();
        }
        rect.x = stableX;
        rect.width = 1;
        return rect;
    }
    
    public void makeStartAt(final int pos) {
        if (pos < this.getOffset() || pos >= this.getOffset() + this.textCount) {
            throw new IllegalArgumentException("pos " + pos + " is not in range " + this.getOffset() + "-" + (this.getOffset() + this.textCount - 1));
        }
        final int shift = pos - this.getOffset();
        this.setOffset(pos);
        this.textOffset += shift;
        this.textCount -= shift;
    }
    
    public void moveOffset(final int amt) {
        if (amt < 0 || amt > this.textCount) {
            throw new IllegalArgumentException("amt " + amt + " is not in range 0-" + this.textCount);
        }
        this.setOffset(this.getOffset() + amt);
        this.textOffset += amt;
        this.textCount -= amt;
    }
    
    public void set(final char[] line, final int beg, final int end, final int offset, final int type) {
        this.text = line;
        this.textOffset = beg;
        this.textCount = end - beg + 1;
        this.setType(type);
        this.setOffset(offset);
        this.nextToken = null;
    }
    
    public void setHyperlink(final boolean hyperlink) {
        this.hyperlink = hyperlink;
    }
    
    public void setLanguageIndex(final int languageIndex) {
        this.languageIndex = languageIndex;
    }
    
    public void setNextToken(final Token nextToken) {
        this.nextToken = nextToken;
    }
    
    public void setOffset(final int offset) {
        this.offset = offset;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public boolean startsWith(final char[] chars) {
        if (chars.length <= this.textCount) {
            for (int i = 0; i < chars.length; ++i) {
                if (this.text[this.textOffset + i] != chars[i]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    public int tokenToDocument(final int pos) {
        return pos + (this.getOffset() - this.textOffset);
    }
    
    public String toString() {
        return "[Token: " + ((this.getType() == 0) ? "<null token>" : ("text: '" + ((this.text == null) ? "<null>" : (this.getLexeme() + "'; " + "offset: " + this.getOffset() + "; type: " + this.getType() + "; " + "isPaintable: " + this.isPaintable() + "; nextToken==null: " + (this.nextToken == null))))) + "]";
    }
}
