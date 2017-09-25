package org.fife.ui.rtextarea;

import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import java.util.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.*;
import javax.swing.text.*;

public class RTextAreaHighlighter extends BasicTextUI.BasicHighlighter
{
    protected RTextArea textArea;
    private List<HighlightInfo> markAllHighlights;
    
    public RTextAreaHighlighter() {
        super();
        this.markAllHighlights = new ArrayList<HighlightInfo>();
    }
    
    Object addMarkAllHighlight(final int start, final int end, final Highlighter.HighlightPainter p) throws BadLocationException {
        final Document doc = this.textArea.getDocument();
        final TextUI mapper = this.textArea.getUI();
        final HighlightInfoImpl i = new LayeredHighlightInfoImpl();
        i.setPainter(p);
        HighlightInfoImpl.access$002(i, doc.createPosition(start));
        HighlightInfoImpl.access$102(i, doc.createPosition(end - 1));
        this.markAllHighlights.add(i);
        mapper.damageRange(this.textArea, start, end);
        return i;
    }
    
    void clearMarkAllHighlights() {
        for (final HighlightInfo info : this.markAllHighlights) {
            this.repaintListHighlight(info);
        }
        this.markAllHighlights.clear();
    }
    
    public void deinstall(final JTextComponent c) {
        this.textArea = null;
        this.markAllHighlights.clear();
    }
    
    public int getMarkAllHighlightCount() {
        return this.markAllHighlights.size();
    }
    
    public List<DocumentRange> getMarkAllHighlightRanges() {
        final List<DocumentRange> list = new ArrayList<DocumentRange>(this.markAllHighlights.size());
        for (final HighlightInfo info : this.markAllHighlights) {
            final int start = info.getStartOffset();
            final int end = info.getEndOffset() + 1;
            final DocumentRange range = new DocumentRange(start, end);
            list.add(range);
        }
        return list;
    }
    
    public void install(final JTextComponent c) {
        super.install(c);
        this.textArea = (RTextArea)c;
    }
    
    protected void paintList(final Graphics g, final List<? extends HighlightInfo> highlights) {
        for (int len = highlights.size(), i = 0; i < len; ++i) {
            HighlightInfo info = (HighlightInfo)highlights.get(i);
            if (!(info instanceof LayeredHighlightInfo)) {
                final Rectangle a = this.textArea.getBounds();
                final Insets insets = this.textArea.getInsets();
                a.x = insets.left;
                a.y = insets.top;
                final Rectangle loc_0 = a;
                loc_0.width -= insets.left + insets.right;
                final Rectangle loc_1 = a;
                loc_1.height -= insets.top + insets.bottom;
                while (i < len) {
                    info = (HighlightInfo)highlights.get(i);
                    if (!(info instanceof LayeredHighlightInfo)) {
                        final Color c = ((HighlightInfoImpl)info).getColor();
                        final Highlighter.HighlightPainter p = info.getPainter();
                        if (c != null && p instanceof ChangeableHighlightPainter) {
                            ((ChangeableHighlightPainter)p).setPaint(c);
                        }
                        p.paint(g, info.getStartOffset(), info.getEndOffset(), a, this.textArea);
                    }
                    ++i;
                }
            }
        }
    }
    
    public void paintLayeredHighlights(final Graphics g, final int lineStart, final int lineEnd, final Shape viewBounds, final JTextComponent editor, final View view) {
        this.paintListLayered(g, lineStart, lineEnd, viewBounds, editor, view, this.markAllHighlights);
        super.paintLayeredHighlights(g, lineStart, lineEnd, viewBounds, editor, view);
    }
    
    protected void paintListLayered(final Graphics g, final int lineStart, final int lineEnd, final Shape viewBounds, final JTextComponent editor, final View view, final List<? extends HighlightInfo> highlights) {
        for (int i = highlights.size() - 1; i >= 0; --i) {
            final HighlightInfo tag = (HighlightInfo)highlights.get(i);
            if (tag instanceof LayeredHighlightInfo) {
                final LayeredHighlightInfo lhi = (LayeredHighlightInfo)tag;
                final int highlightStart = lhi.getStartOffset();
                final int highlightEnd = lhi.getEndOffset() + 1;
                if ((lineStart < highlightStart && lineEnd > highlightStart) || (lineStart >= highlightStart && lineStart < highlightEnd)) {
                    lhi.paintLayeredHighlights(g, lineStart, lineEnd, viewBounds, editor, view);
                }
            }
        }
    }
    
    protected void repaintListHighlight(final HighlightInfo info) {
        if (info instanceof LayeredHighlightInfoImpl) {
            final LayeredHighlightInfoImpl lhi = (LayeredHighlightInfoImpl)info;
            if (lhi.width > 0 && lhi.height > 0) {
                this.textArea.repaint(lhi.x, lhi.y, lhi.width, lhi.height);
            }
        }
        else {
            final TextUI ui = this.textArea.getUI();
            ui.damageRange(this.textArea, info.getStartOffset(), info.getEndOffset());
        }
    }
    
    protected static class HighlightInfoImpl implements HighlightInfo
    {
        private Position p0;
        private Position p1;
        private Highlighter.HighlightPainter painter;
        
        public Color getColor() {
            return null;
        }
        
        public int getStartOffset() {
            return this.p0.getOffset();
        }
        
        public int getEndOffset() {
            return this.p1.getOffset();
        }
        
        public Highlighter.HighlightPainter getPainter() {
            return this.painter;
        }
        
        public void setStartOffset(final Position startOffset) {
            this.p0 = startOffset;
        }
        
        public void setEndOffset(final Position endOffset) {
            this.p1 = endOffset;
        }
        
        public void setPainter(final Highlighter.HighlightPainter painter) {
            this.painter = painter;
        }
        
        static /* synthetic */ Position access$002(final HighlightInfoImpl x0, final Position x1) {
            return x0.p0 = x1;
        }
        
        static /* synthetic */ Position access$102(final HighlightInfoImpl x0, final Position x1) {
            return x0.p1 = x1;
        }
    }
    
    protected static class LayeredHighlightInfoImpl extends HighlightInfoImpl implements LayeredHighlightInfo
    {
        public int x;
        public int y;
        public int width;
        public int height;
        
        void union(final Shape bounds) {
            if (bounds == null) {
                return;
            }
            final Rectangle alloc = (Rectangle)((bounds instanceof Rectangle) ? bounds : bounds.getBounds());
            if (this.width == 0 || this.height == 0) {
                this.x = alloc.x;
                this.y = alloc.y;
                this.width = alloc.width;
                this.height = alloc.height;
            }
            else {
                this.width = Math.max(this.x + this.width, alloc.x + alloc.width);
                this.height = Math.max(this.y + this.height, alloc.y + alloc.height);
                this.x = Math.min(this.x, alloc.x);
                this.width -= this.x;
                this.y = Math.min(this.y, alloc.y);
                this.height -= this.y;
            }
        }
        
        public void paintLayeredHighlights(final Graphics g, int p0, int p1, final Shape viewBounds, final JTextComponent editor, final View view) {
            final int start = this.getStartOffset();
            int end = this.getEndOffset();
            ++end;
            p0 = Math.max(start, p0);
            p1 = Math.min(end, p1);
            if (this.getColor() != null && this.getPainter() instanceof ChangeableHighlightPainter) {
                ((ChangeableHighlightPainter)this.getPainter()).setPaint(this.getColor());
            }
            this.union(((LayerPainter)this.getPainter()).paintLayer(g, p0, p1, viewBounds, editor, view));
        }
    }
    
    public interface HighlightInfo extends Highlighter.Highlight
    {
    }
    
    public interface LayeredHighlightInfo extends HighlightInfo
    {
        void paintLayeredHighlights(Graphics param_0, int param_1, int param_2, Shape param_3, JTextComponent param_4, View param_5);
    }
}
