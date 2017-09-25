package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import java.awt.*;

public class SyntaxView extends View implements TabExpander, TokenOrientedView, RSTAView
{
    private Font font;
    private FontMetrics metrics;
    private Element longLine;
    private float longLineWidth;
    private int tabSize;
    private int tabBase;
    private RSyntaxTextArea host;
    private int lineHeight;
    private int ascent;
    private int clipStart;
    private int clipEnd;
    private TokenImpl tempToken;
    
    public SyntaxView(final Element elem) {
        super(elem);
        this.lineHeight = 0;
        this.tempToken = new TokenImpl();
    }
    
    void calculateLongestLine() {
        final Component c = this.getContainer();
        this.font = c.getFont();
        this.metrics = c.getFontMetrics(this.font);
        this.tabSize = this.getTabSize() * this.metrics.charWidth(' ');
        final Element lines = this.getElement();
        for (int n = lines.getElementCount(), i = 0; i < n; ++i) {
            final Element line = lines.getElement(i);
            final float w = this.getLineWidth(i);
            if (w > this.longLineWidth) {
                this.longLineWidth = w;
                this.longLine = line;
            }
        }
    }
    
    public void changedUpdate(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        this.updateDamage(changes, a, f);
    }
    
    protected void damageLineRange(final int line0, final int line1, final Shape a, final Component host) {
        if (a != null) {
            final Rectangle area0 = this.lineToRect(a, line0);
            final Rectangle area = this.lineToRect(a, line1);
            if (area0 != null && area != null) {
                final Rectangle dmg = area0.union(area);
                host.repaint(dmg.x, dmg.y, dmg.width, dmg.height);
            }
            else {
                host.repaint();
            }
        }
    }
    
    private float drawLine(final TokenPainter painter, Token token, final Graphics2D g, final float x, final float y) {
        float nextX;
        for (nextX = x; token != null && token.isPaintable() && nextX < this.clipEnd; nextX = painter.paint(token, g, nextX, y, this.host, this, this.clipStart), token = token.getNextToken()) {}
        if (this.host.getEOLMarkersVisible()) {
            g.setColor(this.host.getForegroundForTokenType(21));
            g.setFont(this.host.getFontForTokenType(21));
            g.drawString("¶", nextX, y);
        }
        return nextX;
    }
    
    private float drawLineWithSelection(final TokenPainter painter, Token token, final Graphics2D g, final float x, final float y, final int selStart, final int selEnd) {
        float nextX;
        for (nextX = x; token != null && token.isPaintable() && nextX < this.clipEnd; token = token.getNextToken()) {
            if (token.containsPosition(selStart)) {
                if (selStart > token.getOffset()) {
                    this.tempToken.copyFrom(token);
                    this.tempToken.textCount = selStart - this.tempToken.getOffset();
                    nextX = painter.paint(this.tempToken, g, nextX, y, this.host, this, this.clipStart);
                    this.tempToken.textCount = token.length();
                    this.tempToken.makeStartAt(selStart);
                    token = new TokenImpl(this.tempToken);
                }
                final int tokenLen = token.length();
                final int selCount = Math.min(tokenLen, selEnd - token.getOffset());
                if (selCount == tokenLen) {
                    nextX = painter.paintSelected(token, g, nextX, y, this.host, this, this.clipStart);
                }
                else {
                    this.tempToken.copyFrom(token);
                    this.tempToken.textCount = selCount;
                    nextX = painter.paintSelected(this.tempToken, g, nextX, y, this.host, this, this.clipStart);
                    this.tempToken.textCount = token.length();
                    this.tempToken.makeStartAt(token.getOffset() + selCount);
                    token = this.tempToken;
                    nextX = painter.paint(token, g, nextX, y, this.host, this, this.clipStart);
                }
            }
            else if (token.containsPosition(selEnd)) {
                this.tempToken.copyFrom(token);
                this.tempToken.textCount = selEnd - this.tempToken.getOffset();
                nextX = painter.paintSelected(this.tempToken, g, nextX, y, this.host, this, this.clipStart);
                this.tempToken.textCount = token.length();
                this.tempToken.makeStartAt(selEnd);
                token = this.tempToken;
                nextX = painter.paint(token, g, nextX, y, this.host, this, this.clipStart);
            }
            else if (token.getOffset() >= selStart && token.getEndOffset() <= selEnd) {
                nextX = painter.paintSelected(token, g, nextX, y, this.host, this, this.clipStart);
            }
            else {
                nextX = painter.paint(token, g, nextX, y, this.host, this, this.clipStart);
            }
        }
        if (this.host.getEOLMarkersVisible()) {
            g.setColor(this.host.getForegroundForTokenType(21));
            g.setFont(this.host.getFontForTokenType(21));
            g.drawString("¶", nextX, y);
        }
        return nextX;
    }
    
    private float getLineWidth(final int lineNumber) {
        final Token tokenList = ((RSyntaxDocument)this.getDocument()).getTokenListForLine(lineNumber);
        return RSyntaxUtilities.getTokenListWidth(tokenList, (RSyntaxTextArea)this.getContainer(), this);
    }
    
    public int getNextVisualPositionFrom(final int pos, final Position.Bias b, final Shape a, final int direction, final Position.Bias[] biasRet) throws BadLocationException {
        return RSyntaxUtilities.getNextVisualPositionFrom(pos, b, a, direction, biasRet, this);
    }
    
    public float getPreferredSpan(final int axis) {
        this.updateMetrics();
        switch (axis) {
            case 0: {
                float span = this.longLineWidth + this.getRhsCorrection();
                if (this.host.getEOLMarkersVisible()) {
                    span += this.metrics.charWidth('¶');
                }
                return span;
            }
            case 1: {
                this.lineHeight = ((this.host != null) ? this.host.getLineHeight() : this.lineHeight);
                int visibleLineCount = this.getElement().getElementCount();
                if (this.host.isCodeFoldingEnabled()) {
                    visibleLineCount -= this.host.getFoldManager().getHiddenLineCount();
                }
                return visibleLineCount * this.lineHeight;
            }
            default: {
                throw new IllegalArgumentException("Invalid axis: " + axis);
            }
        }
    }
    
    private final int getRhsCorrection() {
        int rhsCorrection = 10;
        if (this.host != null) {
            rhsCorrection = this.host.getRightHandSideCorrection();
        }
        return rhsCorrection;
    }
    
    private int getTabSize() {
        final Integer i = (Integer)this.getDocument().getProperty("tabSize");
        final int size = (i != null) ? i : 5;
        return size;
    }
    
    public Token getTokenListForPhysicalLineAbove(final int offset) {
        final RSyntaxDocument document = (RSyntaxDocument)this.getDocument();
        final Element map = document.getDefaultRootElement();
        int line = map.getElementIndex(offset);
        final FoldManager fm = this.host.getFoldManager();
        if (fm == null) {
            if (--line >= 0) {
                return document.getTokenListForLine(line);
            }
        }
        else {
            line = fm.getVisibleLineAbove(line);
            if (line >= 0) {
                return document.getTokenListForLine(line);
            }
        }
        return null;
    }
    
    public Token getTokenListForPhysicalLineBelow(final int offset) {
        final RSyntaxDocument document = (RSyntaxDocument)this.getDocument();
        final Element map = document.getDefaultRootElement();
        final int lineCount = map.getElementCount();
        int line = map.getElementIndex(offset);
        if (!this.host.isCodeFoldingEnabled()) {
            if (line < lineCount - 1) {
                return document.getTokenListForLine(line + 1);
            }
        }
        else {
            final FoldManager fm = this.host.getFoldManager();
            line = fm.getVisibleLineBelow(line);
            if (line >= 0 && line < lineCount) {
                return document.getTokenListForLine(line);
            }
        }
        return null;
    }
    
    public void insertUpdate(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        this.updateDamage(changes, a, f);
    }
    
    protected Rectangle lineToRect(final Shape a, int line) {
        Rectangle r = null;
        this.updateMetrics();
        if (this.metrics != null) {
            final Rectangle alloc = a.getBounds();
            this.lineHeight = ((this.host != null) ? this.host.getLineHeight() : this.lineHeight);
            if (this.host.isCodeFoldingEnabled()) {
                final FoldManager fm = this.host.getFoldManager();
                final int hiddenCount = fm.getHiddenLineCountAbove(line);
                line -= hiddenCount;
            }
            r = new Rectangle(alloc.x, alloc.y + line * this.lineHeight, alloc.width, this.lineHeight);
        }
        return r;
    }
    
    public Shape modelToView(final int pos, final Shape a, final Position.Bias b) throws BadLocationException {
        final Element map = this.getElement();
        final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
        final int lineIndex = map.getElementIndex(pos);
        final Token tokenList = doc.getTokenListForLine(lineIndex);
        Rectangle lineArea = this.lineToRect(a, lineIndex);
        this.tabBase = lineArea.x;
        lineArea = tokenList.listOffsetToView((RSyntaxTextArea)this.getContainer(), this, pos, this.tabBase, lineArea);
        return lineArea;
    }
    
    public Shape modelToView(final int p0, final Position.Bias b0, final int p1, final Position.Bias b1, final Shape a) throws BadLocationException {
        final Shape s0 = this.modelToView(p0, a, b0);
        Shape s;
        if (p1 == this.getEndOffset()) {
            try {
                s = this.modelToView(p1, a, b1);
            }
            catch (BadLocationException ble) {
                s = null;
            }
            if (s == null) {
                final Rectangle alloc = (Rectangle)((a instanceof Rectangle) ? a : a.getBounds());
                s = new Rectangle(alloc.x + alloc.width - 1, alloc.y, 1, alloc.height);
            }
        }
        else {
            s = this.modelToView(p1, a, b1);
        }
        final Rectangle r0 = (Rectangle)((s0 instanceof Rectangle) ? s0 : s0.getBounds());
        final Rectangle r = (Rectangle)((s instanceof Rectangle) ? s : s.getBounds());
        if (r0.y != r.y) {
            final Rectangle alloc2 = (Rectangle)((a instanceof Rectangle) ? a : a.getBounds());
            r0.x = alloc2.x;
            r0.width = alloc2.width;
        }
        r0.add(r);
        if (p1 > p0) {
            final Rectangle loc_0 = r0;
            loc_0.width -= r.width;
        }
        return r0;
    }
    
    public float nextTabStop(final float x, final int tabOffset) {
        if (this.tabSize == 0) {
            return x;
        }
        final int ntabs = ((int)x - this.tabBase) / this.tabSize;
        return this.tabBase + (ntabs + 1) * this.tabSize;
    }
    
    public void paint(final Graphics g, final Shape a) {
        final RSyntaxDocument document = (RSyntaxDocument)this.getDocument();
        final Rectangle alloc = a.getBounds();
        this.tabBase = alloc.x;
        this.host = (RSyntaxTextArea)this.getContainer();
        final Rectangle clip = g.getClipBounds();
        this.clipStart = clip.x;
        this.clipEnd = this.clipStart + clip.width;
        this.lineHeight = this.host.getLineHeight();
        this.ascent = this.host.getMaxAscent();
        final int heightAbove = clip.y - alloc.y;
        int linesAbove = Math.max(0, heightAbove / this.lineHeight);
        final FoldManager fm = this.host.getFoldManager();
        linesAbove += fm.getHiddenLineCountAbove(linesAbove, true);
        final Rectangle lineArea = this.lineToRect(a, linesAbove);
        int y = lineArea.y + this.ascent;
        final int x = lineArea.x;
        final Element map = this.getElement();
        final int lineCount = map.getElementCount();
        final int selStart = this.host.getSelectionStart();
        final int selEnd = this.host.getSelectionEnd();
        final boolean useSelectedTextColor = this.host.getUseSelectedTextColor();
        final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.host.getHighlighter();
        final Graphics2D g2d = (Graphics2D)g;
        final TokenPainter painter = this.host.getTokenPainter();
        for (int line = linesAbove; y < clip.y + clip.height + this.ascent && line < lineCount; y += this.lineHeight, ++line) {
            Fold fold = fm.getFoldForLine(line);
            final Element lineElement = map.getElement(line);
            final int startOffset = lineElement.getStartOffset();
            final int endOffset = lineElement.getEndOffset() - 1;
            h.paintLayeredHighlights(g2d, startOffset, endOffset, a, this.host, this);
            final Token token = document.getTokenListForLine(line);
            if (!useSelectedTextColor || selStart == selEnd || startOffset >= selEnd || endOffset < selStart) {
                this.drawLine(painter, token, g2d, x, y);
            }
            else {
                this.drawLineWithSelection(painter, token, g2d, x, y, selStart, selEnd);
            }
            if (fold != null && fold.isCollapsed()) {
                final Color c = RSyntaxUtilities.getFoldedLineBottomColor(this.host);
                if (c != null) {
                    g.setColor(c);
                    g.drawLine(x, y + this.lineHeight - this.ascent - 1, this.host.getWidth(), y + this.lineHeight - this.ascent - 1);
                }
                do {
                    final int hiddenLineCount = fold.getLineCount();
                    if (hiddenLineCount == 0) {
                        break;
                    }
                    line += hiddenLineCount;
                    fold = fm.getFoldForLine(line);
                } while (fold != null && fold.isCollapsed());
            }
        }
    }
    
    private boolean possiblyUpdateLongLine(final Element line, final int lineNumber) {
        final float w = this.getLineWidth(lineNumber);
        if (w > this.longLineWidth) {
            this.longLineWidth = w;
            this.longLine = line;
            return true;
        }
        return false;
    }
    
    public void removeUpdate(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        this.updateDamage(changes, a, f);
    }
    
    public void setSize(final float width, final float height) {
        super.setSize(width, height);
        this.updateMetrics();
    }
    
    protected void updateDamage(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        final Component host = this.getContainer();
        this.updateMetrics();
        final Element elem = this.getElement();
        final DocumentEvent.ElementChange ec = changes.getChange(elem);
        final Element[] added = (Element[])((ec != null) ? ec.getChildrenAdded() : null);
        final Element[] removed = (Element[])((ec != null) ? ec.getChildrenRemoved() : null);
        if ((added != null && added.length > 0) || (removed != null && removed.length > 0)) {
            if (added != null) {
                final int addedAt = ec.getIndex();
                for (int i = 0; i < added.length; ++i) {
                    this.possiblyUpdateLongLine(added[i], addedAt + i);
                }
            }
            if (removed != null) {
                for (int j = 0; j < removed.length; ++j) {
                    if (removed[j] == this.longLine) {
                        this.longLineWidth = -1.0f;
                        this.calculateLongestLine();
                        break;
                    }
                }
            }
            this.preferenceChanged(null, true, true);
            host.repaint();
        }
        else if (changes.getType() == DocumentEvent.EventType.CHANGE) {
            final int startLine = changes.getOffset();
            final int endLine = changes.getLength();
            this.damageLineRange(startLine, endLine, a, host);
        }
        else {
            final Element map = this.getElement();
            final int line = map.getElementIndex(changes.getOffset());
            this.damageLineRange(line, line, a, host);
            if (changes.getType() == DocumentEvent.EventType.INSERT) {
                final Element e = map.getElement(line);
                if (e == this.longLine) {
                    this.longLineWidth = this.getLineWidth(line);
                    this.preferenceChanged(null, true, false);
                }
                else if (this.possiblyUpdateLongLine(e, line)) {
                    this.preferenceChanged(null, true, false);
                }
            }
            else if (changes.getType() == DocumentEvent.EventType.REMOVE && map.getElement(line) == this.longLine) {
                this.longLineWidth = -1.0f;
                this.calculateLongestLine();
                this.preferenceChanged(null, true, false);
            }
        }
    }
    
    private void updateMetrics() {
        this.host = (RSyntaxTextArea)this.getContainer();
        final Font f = this.host.getFont();
        if (this.font != f) {
            this.calculateLongestLine();
        }
    }
    
    public int viewToModel(final float fx, final float fy, final Shape a, final Position.Bias[] bias) {
        bias[0] = Position.Bias.Forward;
        final Rectangle alloc = a.getBounds();
        final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
        final int x = (int)fx;
        final int y = (int)fy;
        if (y < alloc.y) {
            return this.getStartOffset();
        }
        if (y > alloc.y + alloc.height) {
            return this.host.getLastVisibleOffset();
        }
        final Element map = doc.getDefaultRootElement();
        int lineIndex = Math.abs((y - alloc.y) / this.lineHeight);
        final FoldManager fm = this.host.getFoldManager();
        lineIndex += fm.getHiddenLineCountAbove(lineIndex, true);
        if (lineIndex >= map.getElementCount()) {
            return this.host.getLastVisibleOffset();
        }
        final Element line = map.getElement(lineIndex);
        if (x < alloc.x) {
            return line.getStartOffset();
        }
        if (x > alloc.x + alloc.width) {
            return line.getEndOffset() - 1;
        }
        final int p0 = line.getStartOffset();
        final Token tokenList = doc.getTokenListForLine(lineIndex);
        this.tabBase = alloc.x;
        final int offs = tokenList.getListOffset((RSyntaxTextArea)this.getContainer(), this, this.tabBase, x);
        return (offs != -1) ? offs : p0;
    }
    
    public int yForLine(final Rectangle alloc, int line) throws BadLocationException {
        this.updateMetrics();
        if (this.metrics != null) {
            this.lineHeight = ((this.host != null) ? this.host.getLineHeight() : this.lineHeight);
            final FoldManager fm = this.host.getFoldManager();
            if (!fm.isLineHidden(line)) {
                line -= fm.getHiddenLineCountAbove(line);
                return alloc.y + line * this.lineHeight;
            }
        }
        return -1;
    }
    
    public int yForLineContaining(final Rectangle alloc, final int offs) throws BadLocationException {
        final Element map = this.getElement();
        final int line = map.getElementIndex(offs);
        return this.yForLine(alloc, line);
    }
}
