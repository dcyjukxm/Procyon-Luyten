package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import org.fife.util.*;
import javax.swing.event.*;
import org.fife.ui.rsyntaxtextarea.modes.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import javax.swing.text.*;

public class RSyntaxDocument extends RDocument implements Iterable<Token>, SyntaxConstants
{
    private transient TokenMakerFactory tokenMakerFactory;
    private transient TokenMaker tokenMaker;
    private String syntaxStyle;
    protected transient DynamicIntArray lastTokensOnLines;
    private transient int lastLine;
    private transient Token cachedTokenList;
    private transient int useCacheCount;
    private transient int tokenRetrievalCount;
    private transient Segment s;
    private static final boolean DEBUG_TOKEN_CACHING = false;
    
    public RSyntaxDocument(final String syntaxStyle) {
        this(null, syntaxStyle);
    }
    
    public RSyntaxDocument(final TokenMakerFactory tmf, final String syntaxStyle) {
        super();
        this.lastLine = -1;
        this.useCacheCount = 0;
        this.tokenRetrievalCount = 0;
        this.putProperty("tabSize", 5);
        (this.lastTokensOnLines = new DynamicIntArray(400)).add(0);
        this.s = new Segment();
        this.setTokenMakerFactory(tmf);
        this.setSyntaxStyle(syntaxStyle);
    }
    
    protected void fireInsertUpdate(final DocumentEvent e) {
        this.cachedTokenList = null;
        final Element lineMap = this.getDefaultRootElement();
        final DocumentEvent.ElementChange change = e.getChange(lineMap);
        final Element[] added = (Element[])((change == null) ? null : change.getChildrenAdded());
        final int numLines = lineMap.getElementCount();
        final int line = lineMap.getElementIndex(e.getOffset());
        final int previousLine = line - 1;
        int previousTokenType = (previousLine > -1) ? this.lastTokensOnLines.get(previousLine) : 0;
        if (added != null && added.length > 0) {
            final Element[] removed = change.getChildrenRemoved();
            final int numRemoved = (removed != null) ? removed.length : 0;
            final int endBefore = line + added.length - numRemoved;
            for (int i = line; i < endBefore; ++i) {
                this.setSharedSegment(i);
                final int tokenType = this.tokenMaker.getLastTokenTypeOnLine(this.s, previousTokenType);
                this.lastTokensOnLines.add(i, tokenType);
                previousTokenType = tokenType;
            }
            this.updateLastTokensBelow(endBefore, numLines, previousTokenType);
        }
        else {
            this.updateLastTokensBelow(line, numLines, previousTokenType);
        }
        super.fireInsertUpdate(e);
    }
    
    protected void fireRemoveUpdate(final DocumentEvent chng) {
        this.cachedTokenList = null;
        final Element lineMap = this.getDefaultRootElement();
        final int numLines = lineMap.getElementCount();
        final DocumentEvent.ElementChange change = chng.getChange(lineMap);
        final Element[] removed = (Element[])((change == null) ? null : change.getChildrenRemoved());
        if (removed != null && removed.length > 0) {
            final int line = change.getIndex();
            final int previousLine = line - 1;
            final int previousTokenType = (previousLine > -1) ? this.lastTokensOnLines.get(previousLine) : 0;
            final Element[] added = change.getChildrenAdded();
            final int numAdded = (added == null) ? 0 : added.length;
            final int endBefore = line + removed.length - numAdded;
            this.lastTokensOnLines.removeRange(line, endBefore);
            this.updateLastTokensBelow(line, numLines, previousTokenType);
        }
        else {
            final int line = lineMap.getElementIndex(chng.getOffset());
            if (line >= this.lastTokensOnLines.getSize()) {
                return;
            }
            final int previousLine = line - 1;
            final int previousTokenType = (previousLine > -1) ? this.lastTokensOnLines.get(previousLine) : 0;
            this.updateLastTokensBelow(line, numLines, previousTokenType);
        }
        super.fireRemoveUpdate(chng);
    }
    
    public int getClosestStandardTokenTypeForInternalType(final int type) {
        return this.tokenMaker.getClosestStandardTokenTypeForInternalType(type);
    }
    
    public boolean getCompleteMarkupCloseTags() {
        return this.getLanguageIsMarkup() && ((AbstractMarkupTokenMaker)this.tokenMaker).getCompleteCloseTags();
    }
    
    public boolean getCurlyBracesDenoteCodeBlocks(final int languageIndex) {
        return this.tokenMaker.getCurlyBracesDenoteCodeBlocks(languageIndex);
    }
    
    public boolean getLanguageIsMarkup() {
        return this.tokenMaker.isMarkupLanguage();
    }
    
    public int getLastTokenTypeOnLine(final int line) {
        return this.lastTokensOnLines.get(line);
    }
    
    public String[] getLineCommentStartAndEnd(final int languageIndex) {
        return this.tokenMaker.getLineCommentStartAndEnd(languageIndex);
    }
    
    boolean getMarkOccurrencesOfTokenType(final int type) {
        return this.tokenMaker.getMarkOccurrencesOfTokenType(type);
    }
    
    OccurrenceMarker getOccurrenceMarker() {
        return this.tokenMaker.getOccurrenceMarker();
    }
    
    public boolean getShouldIndentNextLine(final int line) {
        Token t = this.getTokenListForLine(line);
        t = t.getLastNonCommentNonWhitespaceToken();
        return this.tokenMaker.getShouldIndentNextLineAfter(t);
    }
    
    public final Token getTokenListForLine(final int line) {
        ++this.tokenRetrievalCount;
        if (line == this.lastLine && this.cachedTokenList != null) {
            return this.cachedTokenList;
        }
        this.lastLine = line;
        final Element map = this.getDefaultRootElement();
        final Element elem = map.getElement(line);
        final int startOffset = elem.getStartOffset();
        final int endOffset = elem.getEndOffset() - 1;
        try {
            this.getText(startOffset, endOffset - startOffset, this.s);
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
            return null;
        }
        final int initialTokenType = (line == 0) ? 0 : this.getLastTokenTypeOnLine(line - 1);
        return this.cachedTokenList = this.tokenMaker.getTokenList(this.s, initialTokenType, startOffset);
    }
    
    boolean insertBreakSpecialHandling(final ActionEvent e) {
        final Action a = this.tokenMaker.getInsertBreakAction();
        if (a != null) {
            a.actionPerformed(e);
            return true;
        }
        return false;
    }
    
    public Iterator<Token> iterator() {
        return new TokenIterator(this);
    }
    
    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.setTokenMakerFactory(null);
        this.s = new Segment();
        final int lineCount = this.getDefaultRootElement().getElementCount();
        this.lastTokensOnLines = new DynamicIntArray(lineCount);
        this.setSyntaxStyle(this.syntaxStyle);
    }
    
    private final void setSharedSegment(final int line) {
        final Element map = this.getDefaultRootElement();
        final Element element = map.getElement(line);
        if (element == null) {
            throw new InternalError("Invalid line number: " + line);
        }
        final int startOffset = element.getStartOffset();
        final int endOffset = element.getEndOffset() - 1;
        try {
            this.getText(startOffset, endOffset - startOffset, this.s);
        }
        catch (BadLocationException ble) {
            throw new InternalError("Text range not in document: " + startOffset + "-" + endOffset);
        }
    }
    
    public void setSyntaxStyle(final String styleKey) {
        this.tokenMaker = this.tokenMakerFactory.getTokenMaker(styleKey);
        this.updateSyntaxHighlightingInformation();
        this.syntaxStyle = styleKey;
    }
    
    public void setSyntaxStyle(final TokenMaker tokenMaker) {
        this.tokenMaker = tokenMaker;
        this.updateSyntaxHighlightingInformation();
    }
    
    public void setTokenMakerFactory(final TokenMakerFactory tmf) {
        this.tokenMakerFactory = ((tmf != null) ? tmf : TokenMakerFactory.getDefaultInstance());
    }
    
    private int updateLastTokensBelow(int line, final int numLines, int previousTokenType) {
        final int firstLine = line;
        while (line < numLines) {
            this.setSharedSegment(line);
            final int oldTokenType = this.lastTokensOnLines.get(line);
            final int newTokenType = this.tokenMaker.getLastTokenTypeOnLine(this.s, previousTokenType);
            if (oldTokenType == newTokenType) {
                this.fireChangedUpdate(new DefaultDocumentEvent(this, firstLine, line, DocumentEvent.EventType.CHANGE));
                return line;
            }
            this.lastTokensOnLines.setUnsafe(line, newTokenType);
            previousTokenType = newTokenType;
            ++line;
        }
        if (line > firstLine) {
            this.fireChangedUpdate(new DefaultDocumentEvent(this, firstLine, line, DocumentEvent.EventType.CHANGE));
        }
        return line;
    }
    
    private void updateSyntaxHighlightingInformation() {
        final Element map = this.getDefaultRootElement();
        final int numLines = map.getElementCount();
        int lastTokenType = 0;
        for (int i = 0; i < numLines; ++i) {
            this.setSharedSegment(i);
            lastTokenType = this.tokenMaker.getLastTokenTypeOnLine(this.s, lastTokenType);
            this.lastTokensOnLines.set(i, lastTokenType);
        }
        this.lastLine = -1;
        this.cachedTokenList = null;
        this.fireChangedUpdate(new DefaultDocumentEvent(this, 0, numLines - 1, DocumentEvent.EventType.CHANGE));
    }
}
