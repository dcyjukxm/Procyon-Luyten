package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import javax.swing.plaf.*;
import java.util.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import java.awt.*;
import javax.swing.text.*;

public class RSyntaxTextAreaHighlighter extends RTextAreaHighlighter
{
    private List<SyntaxLayeredHighlightInfoImpl> markedOccurrences;
    private List<SyntaxLayeredHighlightInfoImpl> parserHighlights;
    private static final Color DEFAULT_PARSER_NOTICE_COLOR;
    
    public RSyntaxTextAreaHighlighter() {
        super();
        this.markedOccurrences = new ArrayList<SyntaxLayeredHighlightInfoImpl>();
        this.parserHighlights = new ArrayList<SyntaxLayeredHighlightInfoImpl>(0);
    }
    
    Object addMarkedOccurrenceHighlight(final int start, final int end, final SmartHighlightPainter p) throws BadLocationException {
        final Document doc = this.textArea.getDocument();
        final TextUI mapper = this.textArea.getUI();
        final SyntaxLayeredHighlightInfoImpl i = new SyntaxLayeredHighlightInfoImpl();
        i.setPainter(p);
        i.setStartOffset(doc.createPosition(start));
        i.setEndOffset(doc.createPosition(end - 1));
        this.markedOccurrences.add(i);
        mapper.damageRange(this.textArea, start, end);
        return i;
    }
    
    HighlightInfo addParserHighlight(final ParserNotice notice, final Highlighter.HighlightPainter p) throws BadLocationException {
        final Document doc = this.textArea.getDocument();
        final TextUI mapper = this.textArea.getUI();
        int start = notice.getOffset();
        int end = 0;
        if (start == -1) {
            final int line = notice.getLine();
            final Element root = doc.getDefaultRootElement();
            if (line >= 0 && line < root.getElementCount()) {
                final Element elem = root.getElement(line);
                start = elem.getStartOffset();
                end = elem.getEndOffset();
            }
        }
        else {
            end = start + notice.getLength();
        }
        final SyntaxLayeredHighlightInfoImpl i = new SyntaxLayeredHighlightInfoImpl();
        i.setPainter(p);
        i.setStartOffset(doc.createPosition(start));
        i.setEndOffset(doc.createPosition(end - 1));
        i.notice = notice;
        this.parserHighlights.add(i);
        mapper.damageRange(this.textArea, start, end);
        return i;
    }
    
    void clearMarkOccurrencesHighlights() {
        for (final HighlightInfo info : this.markedOccurrences) {
            this.repaintListHighlight(info);
        }
        this.markedOccurrences.clear();
    }
    
    void clearParserHighlights() {
        for (int i = 0; i < this.parserHighlights.size(); ++i) {
            this.repaintListHighlight(this.parserHighlights.get(i));
        }
        this.parserHighlights.clear();
    }
    
    public void clearParserHighlights(final Parser parser) {
        final Iterator<SyntaxLayeredHighlightInfoImpl> i = this.parserHighlights.iterator();
        while (i.hasNext()) {
            final SyntaxLayeredHighlightInfoImpl info = i.next();
            if (info.notice.getParser() == parser) {
                if (info.width > 0 && info.height > 0) {
                    this.textArea.repaint(info.x, info.y, info.width, info.height);
                }
                i.remove();
            }
        }
    }
    
    public void deinstall(final JTextComponent c) {
        super.deinstall(c);
        this.markedOccurrences.clear();
        this.parserHighlights.clear();
    }
    
    public List<DocumentRange> getMarkedOccurrences() {
        final List<DocumentRange> list = new ArrayList<DocumentRange>(this.markedOccurrences.size());
        for (final HighlightInfo info : this.markedOccurrences) {
            final int start = info.getStartOffset();
            final int end = info.getEndOffset() + 1;
            final DocumentRange range = new DocumentRange(start, end);
            list.add(range);
        }
        return list;
    }
    
    public void paintLayeredHighlights(final Graphics g, final int lineStart, final int lineEnd, final Shape viewBounds, final JTextComponent editor, final View view) {
        this.paintListLayered(g, lineStart, lineEnd, viewBounds, editor, view, this.markedOccurrences);
        super.paintLayeredHighlights(g, lineStart, lineEnd, viewBounds, editor, view);
        this.paintListLayered(g, lineStart, lineEnd, viewBounds, editor, view, this.parserHighlights);
    }
    
    void removeParserHighlight(final HighlightInfo tag) {
        this.repaintListHighlight(tag);
        this.parserHighlights.remove(tag);
    }
    
    static /* synthetic */ Color access$100() {
        return RSyntaxTextAreaHighlighter.DEFAULT_PARSER_NOTICE_COLOR;
    }
    
    static {
        DEFAULT_PARSER_NOTICE_COLOR = Color.RED;
    }
    
    private static class SyntaxLayeredHighlightInfoImpl extends LayeredHighlightInfoImpl
    {
        ParserNotice notice;
        
        public Color getColor() {
            Color color = null;
            if (this.notice != null) {
                color = this.notice.getColor();
                if (color == null) {
                    color = RSyntaxTextAreaHighlighter.access$100();
                }
            }
            return color;
        }
    }
}
