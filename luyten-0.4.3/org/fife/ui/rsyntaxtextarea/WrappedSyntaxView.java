package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import javax.swing.text.*;
import java.awt.*;
import org.fife.ui.rtextarea.*;

public class WrappedSyntaxView extends BoxView implements TabExpander, RSTAView
{
    boolean widthChanging;
    int tabBase;
    int tabSize;
    private Segment s;
    private Segment drawSeg;
    private Rectangle tempRect;
    private RSyntaxTextArea host;
    private FontMetrics metrics;
    private TokenImpl tempToken;
    private TokenImpl lineCountTempToken;
    private static final int MIN_WIDTH = 20;
    
    public WrappedSyntaxView(final Element elem) {
        super(elem, 1);
        this.tempToken = new TokenImpl();
        this.s = new Segment();
        this.drawSeg = new Segment();
        this.tempRect = new Rectangle();
        this.lineCountTempToken = new TokenImpl();
    }
    
    protected int calculateBreakPosition(final int p0, final Token tokenList, float x0) {
        int p = p0;
        final RSyntaxTextArea textArea = (RSyntaxTextArea)this.getContainer();
        float currentWidth = this.getWidth();
        if (currentWidth == 2.14748365E9f) {
            currentWidth = this.getPreferredSpan(0);
        }
        currentWidth = Math.max(currentWidth, 20.0f);
        Token t = tokenList;
        while (t != null && t.isPaintable()) {
            final float tokenWidth = t.getWidth(textArea, this, x0);
            if (tokenWidth > currentWidth) {
                if (p == p0) {
                    return t.getOffsetBeforeX(textArea, this, 0.0f, currentWidth);
                }
                return t.isWhitespace() ? (p + t.length()) : p;
            }
            else {
                currentWidth -= tokenWidth;
                x0 += tokenWidth;
                p += t.length();
                t = t.getNextToken();
            }
        }
        return p + 1;
    }
    
    public void changedUpdate(final DocumentEvent e, final Shape a, final ViewFactory f) {
        this.updateChildren(e, a);
    }
    
    private void childAllocation2(final int line, final int y, final Rectangle alloc) {
        alloc.x += this.getOffset(0, line);
        alloc.y += y;
        alloc.width = this.getSpan(0, line);
        alloc.height = this.getSpan(1, line);
        final Insets margin = this.host.getMargin();
        if (margin != null) {
            alloc.y -= margin.top;
        }
    }
    
    protected void drawView(final TokenPainter painter, final Graphics2D g, final Rectangle r, final View view, final int fontHeight, int y) {
        float x = r.x;
        final LayeredHighlighter h = (LayeredHighlighter)this.host.getHighlighter();
        final RSyntaxDocument document = (RSyntaxDocument)this.getDocument();
        final Element map = this.getElement();
        int p0 = view.getStartOffset();
        final int lineNumber = map.getElementIndex(p0);
        final int p = view.getEndOffset();
        this.setSegment(p0, p - 1, document, this.drawSeg);
        final int start = p0 - this.drawSeg.offset;
        Token token = document.getTokenListForLine(lineNumber);
        if (token != null && token.getType() == 0) {
            h.paintLayeredHighlights(g, p0, p, r, this.host, this);
            return;
        }
        while (token != null && token.isPaintable()) {
            final int p2 = this.calculateBreakPosition(p0, token, x);
            x = r.x;
            h.paintLayeredHighlights(g, p0, p2, r, this.host, this);
            while (token != null && token.isPaintable() && token.getEndOffset() - 1 < p2) {
                x = painter.paint(token, g, x, y, this.host, this);
                token = token.getNextToken();
            }
            if (token != null && token.isPaintable() && token.getOffset() < p2) {
                final int tokenOffset = token.getOffset();
                this.tempToken.set(this.drawSeg.array, tokenOffset - start, p2 - 1 - start, tokenOffset, token.getType());
                painter.paint(this.tempToken, g, x, y, this.host, this);
                this.tempToken.copyFrom(token);
                this.tempToken.makeStartAt(p2);
                token = new TokenImpl(this.tempToken);
            }
            p0 = ((p2 == p0) ? p : p2);
            y += fontHeight;
        }
        if (this.host.getEOLMarkersVisible()) {
            g.setColor(this.host.getForegroundForTokenType(21));
            g.setFont(this.host.getFontForTokenType(21));
            g.drawString("¶", x, y - fontHeight);
        }
    }
    
    protected void drawViewWithSelection(final TokenPainter painter, final Graphics2D g, final Rectangle r, final View view, final int fontHeight, int y, final int selStart, final int selEnd) {
        float x = r.x;
        final LayeredHighlighter h = (LayeredHighlighter)this.host.getHighlighter();
        final RSyntaxDocument document = (RSyntaxDocument)this.getDocument();
        final Element map = this.getElement();
        int p0 = view.getStartOffset();
        final int lineNumber = map.getElementIndex(p0);
        final int p = view.getEndOffset();
        this.setSegment(p0, p - 1, document, this.drawSeg);
        final int start = p0 - this.drawSeg.offset;
        Token token = document.getTokenListForLine(lineNumber);
        if (token != null && token.getType() == 0) {
            h.paintLayeredHighlights(g, p0, p, r, this.host, this);
            return;
        }
        while (token != null && token.isPaintable()) {
            final int p2 = this.calculateBreakPosition(p0, token, x);
            x = r.x;
            h.paintLayeredHighlights(g, p0, p2, r, this.host, this);
            while (token != null && token.isPaintable() && token.getEndOffset() - 1 < p2) {
                if (token.containsPosition(selStart)) {
                    if (selStart > token.getOffset()) {
                        this.tempToken.copyFrom(token);
                        this.tempToken.textCount = selStart - this.tempToken.getOffset();
                        x = painter.paint(this.tempToken, g, x, y, this.host, this);
                        this.tempToken.textCount = token.length();
                        this.tempToken.makeStartAt(selStart);
                        token = new TokenImpl(this.tempToken);
                    }
                    final int selCount = Math.min(token.length(), selEnd - token.getOffset());
                    if (selCount == token.length()) {
                        x = painter.paintSelected(token, g, x, y, this.host, this);
                    }
                    else {
                        this.tempToken.copyFrom(token);
                        this.tempToken.textCount = selCount;
                        x = painter.paintSelected(this.tempToken, g, x, y, this.host, this);
                        this.tempToken.textCount = token.length();
                        this.tempToken.makeStartAt(token.getOffset() + selCount);
                        token = this.tempToken;
                        x = painter.paint(token, g, x, y, this.host, this);
                    }
                }
                else if (token.containsPosition(selEnd)) {
                    this.tempToken.copyFrom(token);
                    this.tempToken.textCount = selEnd - this.tempToken.getOffset();
                    x = painter.paintSelected(this.tempToken, g, x, y, this.host, this);
                    this.tempToken.textCount = token.length();
                    this.tempToken.makeStartAt(selEnd);
                    token = this.tempToken;
                    x = painter.paint(token, g, x, y, this.host, this);
                }
                else if (token.getOffset() >= selStart && token.getEndOffset() <= selEnd) {
                    x = painter.paintSelected(token, g, x, y, this.host, this);
                }
                else {
                    x = painter.paint(token, g, x, y, this.host, this);
                }
                token = token.getNextToken();
            }
            if (token != null && token.isPaintable() && token.getOffset() < p2) {
                final int tokenOffset = token.getOffset();
                final Token orig = token;
                token = new TokenImpl(this.drawSeg, tokenOffset - start, p2 - 1 - start, tokenOffset, token.getType());
                if (token.containsPosition(selStart)) {
                    if (selStart > token.getOffset()) {
                        this.tempToken.copyFrom(token);
                        this.tempToken.textCount = selStart - this.tempToken.getOffset();
                        x = painter.paint(this.tempToken, g, x, y, this.host, this);
                        this.tempToken.textCount = token.length();
                        this.tempToken.makeStartAt(selStart);
                        token = new TokenImpl(this.tempToken);
                    }
                    final int selCount2 = Math.min(token.length(), selEnd - token.getOffset());
                    if (selCount2 == token.length()) {
                        x = painter.paintSelected(token, g, x, y, this.host, this);
                    }
                    else {
                        this.tempToken.copyFrom(token);
                        this.tempToken.textCount = selCount2;
                        x = painter.paintSelected(this.tempToken, g, x, y, this.host, this);
                        this.tempToken.textCount = token.length();
                        this.tempToken.makeStartAt(token.getOffset() + selCount2);
                        token = this.tempToken;
                        x = painter.paint(token, g, x, y, this.host, this);
                    }
                }
                else if (token.containsPosition(selEnd)) {
                    this.tempToken.copyFrom(token);
                    this.tempToken.textCount = selEnd - this.tempToken.getOffset();
                    x = painter.paintSelected(this.tempToken, g, x, y, this.host, this);
                    this.tempToken.textCount = token.length();
                    this.tempToken.makeStartAt(selEnd);
                    token = this.tempToken;
                    x = painter.paint(token, g, x, y, this.host, this);
                }
                else if (token.getOffset() >= selStart && token.getEndOffset() <= selEnd) {
                    x = painter.paintSelected(token, g, x, y, this.host, this);
                }
                else {
                    x = painter.paint(token, g, x, y, this.host, this);
                }
                token = new TokenImpl(orig);
                ((TokenImpl)token).makeStartAt(p2);
            }
            p0 = ((p2 == p0) ? p : p2);
            y += fontHeight;
        }
        if (this.host.getEOLMarkersVisible()) {
            g.setColor(this.host.getForegroundForTokenType(21));
            g.setFont(this.host.getFontForTokenType(21));
            g.drawString("¶", x, y - fontHeight);
        }
    }
    
    public Shape getChildAllocation(final int index, final Shape a) {
        if (a != null) {
            final Shape ca = this.getChildAllocationImpl(index, a);
            if (ca != null && !this.isAllocationValid()) {
                final Rectangle r = (Rectangle)((ca instanceof Rectangle) ? ca : ca.getBounds());
                if (r.width == 0 && r.height == 0) {
                    return null;
                }
            }
            return ca;
        }
        return null;
    }
    
    public Shape getChildAllocationImpl(final int line, final Shape a) {
        final Rectangle alloc = this.getInsideAllocation(a);
        this.host = (RSyntaxTextArea)this.getContainer();
        final FoldManager fm = this.host.getFoldManager();
        int y = alloc.y;
        for (int i = 0; i < line; ++i) {
            y += this.getSpan(1, i);
            final Fold fold = fm.getFoldForLine(i);
            if (fold != null && fold.isCollapsed()) {
                i += fold.getCollapsedLineCount();
            }
        }
        this.childAllocation2(line, y, alloc);
        return alloc;
    }
    
    public float getMaximumSpan(final int axis) {
        this.updateMetrics();
        float span = super.getPreferredSpan(axis);
        if (axis == 0) {
            span += this.metrics.charWidth('¶');
        }
        return span;
    }
    
    public float getMinimumSpan(final int axis) {
        this.updateMetrics();
        float span = super.getPreferredSpan(axis);
        if (axis == 0) {
            span += this.metrics.charWidth('¶');
        }
        return span;
    }
    
    public float getPreferredSpan(final int axis) {
        this.updateMetrics();
        float span = 0.0f;
        if (axis == 0) {
            span = super.getPreferredSpan(axis);
            span += this.metrics.charWidth('¶');
        }
        else {
            span = super.getPreferredSpan(axis);
            this.host = (RSyntaxTextArea)this.getContainer();
            if (this.host.isCodeFoldingEnabled()) {
                final int lineCount = this.host.getLineCount();
                final FoldManager fm = this.host.getFoldManager();
                for (int i = 0; i < lineCount; ++i) {
                    if (fm.isLineHidden(i)) {
                        span -= this.getSpan(1, i);
                    }
                }
            }
        }
        return span;
    }
    
    protected int getTabSize() {
        final Integer i = (Integer)this.getDocument().getProperty("tabSize");
        final int size = (i != null) ? i : 5;
        return size;
    }
    
    protected View getViewAtPoint(final int x, final int y, final Rectangle alloc) {
        final int lineCount = this.getViewCount();
        int curY = alloc.y + this.getOffset(1, 0);
        this.host = (RSyntaxTextArea)this.getContainer();
        final FoldManager fm = this.host.getFoldManager();
        for (int line = 1; line < lineCount; ++line) {
            final int span = this.getSpan(1, line - 1);
            if (y < curY + span) {
                this.childAllocation2(line - 1, curY, alloc);
                return this.getView(line - 1);
            }
            curY += span;
            final Fold fold = fm.getFoldForLine(line - 1);
            if (fold != null && fold.isCollapsed()) {
                line += fold.getCollapsedLineCount();
            }
        }
        this.childAllocation2(lineCount - 1, curY, alloc);
        return this.getView(lineCount - 1);
    }
    
    public void insertUpdate(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        this.updateChildren(changes, a);
        final Rectangle alloc = (a != null && this.isAllocationValid()) ? this.getInsideAllocation(a) : null;
        final int pos = changes.getOffset();
        final View v = this.getViewAtPosition(pos, alloc);
        if (v != null) {
            v.insertUpdate(changes, alloc, f);
        }
    }
    
    protected void loadChildren(final ViewFactory f) {
        final Element e = this.getElement();
        final int n = e.getElementCount();
        if (n > 0) {
            final View[] added = new View[n];
            for (int i = 0; i < n; ++i) {
                added[i] = new WrappedLine(e.getElement(i));
            }
            this.replace(0, 0, added);
        }
    }
    
    public Shape modelToView(final int pos, final Shape a, final Position.Bias b) throws BadLocationException {
        if (!this.isAllocationValid()) {
            final Rectangle alloc = a.getBounds();
            this.setSize(alloc.width, alloc.height);
        }
        final boolean isBackward = b == Position.Bias.Backward;
        final int testPos = isBackward ? Math.max(0, pos - 1) : pos;
        if (isBackward && testPos < this.getStartOffset()) {
            return null;
        }
        int vIndex = this.getViewIndexAtPosition(testPos);
        if (vIndex != -1 && vIndex < this.getViewCount()) {
            View v = this.getView(vIndex);
            if (v != null && testPos >= v.getStartOffset() && testPos < v.getEndOffset()) {
                final Shape childShape = this.getChildAllocation(vIndex, a);
                if (childShape == null) {
                    return null;
                }
                Shape retShape = v.modelToView(pos, childShape, b);
                if (retShape == null && v.getEndOffset() == pos && ++vIndex < this.getViewCount()) {
                    v = this.getView(vIndex);
                    retShape = v.modelToView(pos, this.getChildAllocation(vIndex, a), b);
                }
                return retShape;
            }
        }
        throw new BadLocationException("Position not represented by view", pos);
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
        final Rectangle r0 = s0.getBounds();
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
        final Rectangle alloc = (Rectangle)((a instanceof Rectangle) ? a : a.getBounds());
        this.tabBase = alloc.x;
        final Graphics2D g2d = (Graphics2D)g;
        this.host = (RSyntaxTextArea)this.getContainer();
        final int ascent = this.host.getMaxAscent();
        final int fontHeight = this.host.getLineHeight();
        final FoldManager fm = this.host.getFoldManager();
        final TokenPainter painter = this.host.getTokenPainter();
        final Element root = this.getElement();
        final int selStart = this.host.getSelectionStart();
        final int selEnd = this.host.getSelectionEnd();
        final boolean useSelectedTextColor = this.host.getUseSelectedTextColor();
        final int n = this.getViewCount();
        final int x = alloc.x + this.getLeftInset();
        this.tempRect.y = alloc.y + this.getTopInset();
        final Rectangle clip = g.getClipBounds();
        for (int i = 0; i < n; ++i) {
            this.tempRect.x = x + this.getOffset(0, i);
            this.tempRect.width = this.getSpan(0, i);
            this.tempRect.height = this.getSpan(1, i);
            if (this.tempRect.intersects(clip)) {
                final Element lineElement = root.getElement(i);
                final int startOffset = lineElement.getStartOffset();
                final int endOffset = lineElement.getEndOffset() - 1;
                final View view = this.getView(i);
                if (!useSelectedTextColor || selStart == selEnd || startOffset >= selEnd || endOffset < selStart) {
                    this.drawView(painter, g2d, alloc, view, fontHeight, this.tempRect.y + ascent);
                }
                else {
                    this.drawViewWithSelection(painter, g2d, alloc, view, fontHeight, this.tempRect.y + ascent, selStart, selEnd);
                }
            }
            final Rectangle loc_0 = this.tempRect;
            loc_0.y += this.tempRect.height;
            final Fold possibleFold = fm.getFoldForLine(i);
            if (possibleFold != null && possibleFold.isCollapsed()) {
                i += possibleFold.getCollapsedLineCount();
                final Color c = RSyntaxUtilities.getFoldedLineBottomColor(this.host);
                if (c != null) {
                    g.setColor(c);
                    g.drawLine(x, this.tempRect.y - 1, this.host.getWidth(), this.tempRect.y - 1);
                }
            }
        }
    }
    
    public void removeUpdate(final DocumentEvent changes, final Shape a, final ViewFactory f) {
        this.updateChildren(changes, a);
        final Rectangle alloc = (a != null && this.isAllocationValid()) ? this.getInsideAllocation(a) : null;
        final int pos = changes.getOffset();
        final View v = this.getViewAtPosition(pos, alloc);
        if (v != null) {
            v.removeUpdate(changes, alloc, f);
        }
    }
    
    private void setSegment(final int p0, final int p1, final Document document, final Segment seg) {
        try {
            document.getText(p0, p1 - p0, seg);
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
    }
    
    public void setSize(final float width, final float height) {
        this.updateMetrics();
        if ((int)width != this.getWidth()) {
            this.preferenceChanged(null, true, true);
            this.widthChanging = true;
        }
        super.setSize(width, height);
        this.widthChanging = false;
    }
    
    void updateChildren(final DocumentEvent e, final Shape a) {
        final Element elem = this.getElement();
        final DocumentEvent.ElementChange ec = e.getChange(elem);
        if (e.getType() == DocumentEvent.EventType.CHANGE) {
            this.getContainer().repaint();
        }
        else if (ec != null) {
            final Element[] removedElems = ec.getChildrenRemoved();
            final Element[] addedElems = ec.getChildrenAdded();
            final View[] added = new View[addedElems.length];
            for (int i = 0; i < addedElems.length; ++i) {
                added[i] = new WrappedLine(addedElems[i]);
            }
            this.replace(ec.getIndex(), removedElems.length, added);
            if (a != null) {
                this.preferenceChanged(null, true, true);
                this.getContainer().repaint();
            }
        }
        this.updateMetrics();
    }
    
    final void updateMetrics() {
        final Component host = this.getContainer();
        final Font f = host.getFont();
        this.metrics = host.getFontMetrics(f);
        this.tabSize = this.getTabSize() * this.metrics.charWidth('m');
    }
    
    public int viewToModel(final float x, final float y, final Shape a, final Position.Bias[] bias) {
        int offs = -1;
        if (!this.isAllocationValid()) {
            final Rectangle alloc = a.getBounds();
            this.setSize(alloc.width, alloc.height);
        }
        final Rectangle alloc = this.getInsideAllocation(a);
        final View v = this.getViewAtPoint((int)x, (int)y, alloc);
        if (v != null) {
            offs = v.viewToModel(x, y, alloc, bias);
        }
        if (this.host.isCodeFoldingEnabled() && v == this.getView(this.getViewCount() - 1) && offs == v.getEndOffset() - 1) {
            offs = this.host.getLastVisibleOffset();
        }
        return offs;
    }
    
    public int yForLine(final Rectangle alloc, final int line) throws BadLocationException {
        return this.yForLineContaining(alloc, this.getElement().getElement(line).getStartOffset());
    }
    
    public int yForLineContaining(final Rectangle alloc, final int offs) throws BadLocationException {
        if (this.isAllocationValid()) {
            final Rectangle r = (Rectangle)this.modelToView(offs, alloc, Position.Bias.Forward);
            if (r != null) {
                if (this.host.isCodeFoldingEnabled()) {
                    final int line = this.host.getLineOfOffset(offs);
                    final FoldManager fm = this.host.getFoldManager();
                    if (fm.isLineHidden(line)) {
                        return -1;
                    }
                }
                return r.y;
            }
        }
        return -1;
    }
    
    static /* synthetic */ TokenImpl access$000(final WrappedSyntaxView x0) {
        return x0.lineCountTempToken;
    }
    
    static /* synthetic */ Segment access$100(final WrappedSyntaxView x0) {
        return x0.s;
    }
    
    class WrappedLine extends View
    {
        int nlines;
        
        WrappedLine(final Element elem) {
            super(elem);
        }
        
        final int calculateLineCount() {
            int nlines = 0;
            final int startOffset = this.getStartOffset();
            final int p1 = this.getEndOffset();
            final RSyntaxTextArea textArea = (RSyntaxTextArea)this.getContainer();
            final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
            final Element map = doc.getDefaultRootElement();
            final int line = map.getElementIndex(startOffset);
            Token tokenList = doc.getTokenListForLine(line);
            float x0 = 0.0f;
            int p3;
            for (int p2 = startOffset; p2 < p1; p2 = ((p3 == p2) ? (++p3) : p3)) {
                ++nlines;
                final TokenUtils.TokenSubList subList = TokenUtils.getSubTokenList(tokenList, p2, WrappedSyntaxView.this, textArea, x0, WrappedSyntaxView.access$000(WrappedSyntaxView.this));
                x0 = ((subList != null) ? subList.x : x0);
                tokenList = ((subList != null) ? subList.tokenList : null);
                p3 = WrappedSyntaxView.this.calculateBreakPosition(p2, tokenList, x0);
            }
            return nlines;
        }
        
        public float getPreferredSpan(final int axis) {
            switch (axis) {
                case 0: {
                    final float width = WrappedSyntaxView.this.getWidth();
                    if (width == 2.14748365E9f) {
                        return 100.0f;
                    }
                    return width;
                }
                case 1: {
                    if (this.nlines == 0 || WrappedSyntaxView.this.widthChanging) {
                        this.nlines = this.calculateLineCount();
                    }
                    final int h = this.nlines * ((RSyntaxTextArea)this.getContainer()).getLineHeight();
                    return h;
                }
                default: {
                    throw new IllegalArgumentException("Invalid axis: " + axis);
                }
            }
        }
        
        public void paint(final Graphics g, final Shape a) {
        }
        
        public Shape modelToView(final int pos, final Shape a, final Position.Bias b) throws BadLocationException {
            Rectangle alloc = a.getBounds();
            final RSyntaxTextArea textArea = (RSyntaxTextArea)this.getContainer();
            alloc.height = textArea.getLineHeight();
            alloc.width = 1;
            int p0 = this.getStartOffset();
            final int p = this.getEndOffset();
            final int testP = (b == Position.Bias.Forward) ? pos : Math.max(p0, pos - 1);
            final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
            final Element map = doc.getDefaultRootElement();
            final int line = map.getElementIndex(p0);
            Token tokenList = doc.getTokenListForLine(line);
            float x0 = alloc.x;
            while (p0 < p) {
                final TokenUtils.TokenSubList subList = TokenUtils.getSubTokenList(tokenList, p0, WrappedSyntaxView.this, textArea, x0, WrappedSyntaxView.access$000(WrappedSyntaxView.this));
                x0 = ((subList != null) ? subList.x : x0);
                tokenList = ((subList != null) ? subList.tokenList : null);
                final int p2 = WrappedSyntaxView.this.calculateBreakPosition(p0, tokenList, x0);
                if (pos >= p0 && testP < p2) {
                    alloc = RSyntaxUtilities.getLineWidthUpTo(textArea, WrappedSyntaxView.access$100(WrappedSyntaxView.this), p0, pos, WrappedSyntaxView.this, alloc, alloc.x);
                    return alloc;
                }
                if (p2 == p - 1 && pos == p - 1) {
                    if (pos > p0) {
                        alloc = RSyntaxUtilities.getLineWidthUpTo(textArea, WrappedSyntaxView.access$100(WrappedSyntaxView.this), p0, pos, WrappedSyntaxView.this, alloc, alloc.x);
                    }
                    return alloc;
                }
                p0 = ((p2 == p0) ? p : p2);
                final Rectangle loc_0 = alloc;
                loc_0.y += alloc.height;
            }
            throw new BadLocationException(null, pos);
        }
        
        public int viewToModel(final float fx, final float fy, final Shape a, final Position.Bias[] bias) {
            bias[0] = Position.Bias.Forward;
            final Rectangle alloc = (Rectangle)a;
            final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
            final int x = (int)fx;
            final int y = (int)fy;
            if (y < alloc.y) {
                return this.getStartOffset();
            }
            if (y > alloc.y + alloc.height) {
                return this.getEndOffset() - 1;
            }
            final RSyntaxTextArea textArea = (RSyntaxTextArea)this.getContainer();
            alloc.height = textArea.getLineHeight();
            final int p1 = this.getEndOffset();
            final Element map = doc.getDefaultRootElement();
            int p2 = this.getStartOffset();
            final int line = map.getElementIndex(p2);
            Token tlist = doc.getTokenListForLine(line);
            while (p2 < p1) {
                final TokenUtils.TokenSubList subList = TokenUtils.getSubTokenList(tlist, p2, WrappedSyntaxView.this, textArea, alloc.x, WrappedSyntaxView.access$000(WrappedSyntaxView.this));
                tlist = ((subList != null) ? subList.tokenList : null);
                final int p3 = WrappedSyntaxView.this.calculateBreakPosition(p2, tlist, alloc.x);
                if (y >= alloc.y && y < alloc.y + alloc.height) {
                    if (x < alloc.x) {
                        return p2;
                    }
                    if (x > alloc.x + alloc.width) {
                        return p3 - 1;
                    }
                    final int n = tlist.getListOffset(textArea, WrappedSyntaxView.this, alloc.x, x);
                    return Math.max(Math.min(n, p1 - 1), p2);
                }
                else {
                    p2 = ((p3 == p2) ? p1 : p3);
                    final Rectangle loc_0 = alloc;
                    loc_0.y += alloc.height;
                }
            }
            return this.getEndOffset() - 1;
        }
        
        private void handleDocumentEvent(final DocumentEvent e, final Shape a, final ViewFactory f) {
            final int n = this.calculateLineCount();
            if (this.nlines != n) {
                this.nlines = n;
                WrappedSyntaxView.this.preferenceChanged(this, false, true);
                final RSyntaxTextArea textArea = (RSyntaxTextArea)this.getContainer();
                textArea.repaint();
                final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
                if (gutter != null) {
                    gutter.revalidate();
                    gutter.repaint();
                }
            }
            else if (a != null) {
                final Component c = this.getContainer();
                final Rectangle alloc = (Rectangle)a;
                c.repaint(alloc.x, alloc.y, alloc.width, alloc.height);
            }
        }
        
        public void insertUpdate(final DocumentEvent e, final Shape a, final ViewFactory f) {
            this.handleDocumentEvent(e, a, f);
        }
        
        public void removeUpdate(final DocumentEvent e, final Shape a, final ViewFactory f) {
            this.handleDocumentEvent(e, a, f);
        }
    }
}
