package org.fife.ui.rsyntaxtextarea;

import javax.swing.*;
import java.util.*;
import org.fife.ui.rtextarea.*;
import java.awt.event.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import java.net.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.security.*;

class ParserManager implements DocumentListener, ActionListener, HyperlinkListener
{
    private RSyntaxTextArea textArea;
    private List<Parser> parsers;
    private Timer timer;
    private boolean running;
    private Parser parserForTip;
    private Position firstOffsetModded;
    private Position lastOffsetModded;
    private List<NoticeHighlightPair> noticeHighlightPairs;
    private SquiggleUnderlineHighlightPainter parserErrorHighlightPainter;
    private static final String PROPERTY_DEBUG_PARSING = "rsta.debugParsing";
    private static final boolean DEBUG_PARSING;
    private static final int DEFAULT_DELAY_MS = 1250;
    
    public ParserManager(final RSyntaxTextArea textArea) {
        this(1250, textArea);
    }
    
    public ParserManager(final int delay, final RSyntaxTextArea textArea) {
        super();
        this.parserErrorHighlightPainter = new SquiggleUnderlineHighlightPainter(Color.RED);
        this.textArea = textArea;
        textArea.getDocument().addDocumentListener(this);
        this.parsers = new ArrayList<Parser>(1);
        (this.timer = new Timer(delay, this)).setRepeats(false);
        this.running = true;
    }
    
    public void actionPerformed(final ActionEvent e) {
        final int parserCount = this.getParserCount();
        if (parserCount == 0) {
            return;
        }
        long begin = 0L;
        if (ParserManager.DEBUG_PARSING) {
            begin = System.currentTimeMillis();
        }
        final RSyntaxDocument doc = (RSyntaxDocument)this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int firstLine = (this.firstOffsetModded == null) ? 0 : root.getElementIndex(this.firstOffsetModded.getOffset());
        final int lastLine = (this.lastOffsetModded == null) ? (root.getElementCount() - 1) : root.getElementIndex(this.lastOffsetModded.getOffset());
        final Position loc_0 = null;
        this.lastOffsetModded = loc_0;
        this.firstOffsetModded = loc_0;
        if (ParserManager.DEBUG_PARSING) {
            System.out.println("[DEBUG]: Minimum lines to parse: " + firstLine + "-" + lastLine);
        }
        final String style = this.textArea.getSyntaxEditingStyle();
        doc.readLock();
        try {
            for (int i = 0; i < parserCount; ++i) {
                final Parser parser = this.getParser(i);
                if (parser.isEnabled()) {
                    final ParseResult res = parser.parse(doc, style);
                    this.addParserNoticeHighlights(res);
                }
                else {
                    this.clearParserNoticeHighlights(parser);
                }
            }
            this.textArea.fireParserNoticesChange();
        }
        finally {
            doc.readUnlock();
        }
        if (ParserManager.DEBUG_PARSING) {
            final float time = (System.currentTimeMillis() - begin) / 1000.0f;
            System.out.println("Total parsing time: " + time + " seconds");
        }
    }
    
    public void addParser(final Parser parser) {
        if (parser != null && !this.parsers.contains(parser)) {
            if (this.running) {
                this.timer.stop();
            }
            this.parsers.add(parser);
            if (this.parsers.size() == 1) {
                ToolTipManager.sharedInstance().registerComponent(this.textArea);
            }
            if (this.running) {
                this.timer.restart();
            }
        }
    }
    
    private void addParserNoticeHighlights(final ParseResult res) {
        if (res == null) {
            return;
        }
        if (ParserManager.DEBUG_PARSING) {
            System.out.println("[DEBUG]: Adding parser notices from " + res.getParser());
        }
        if (this.noticeHighlightPairs == null) {
            this.noticeHighlightPairs = new ArrayList<NoticeHighlightPair>();
        }
        this.removeParserNotices(res);
        final List<ParserNotice> notices = res.getNotices();
        if (notices.size() > 0) {
            final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
            for (final ParserNotice notice : notices) {
                if (ParserManager.DEBUG_PARSING) {
                    System.out.println("[DEBUG]: ... adding: " + notice);
                }
                try {
                    RTextAreaHighlighter.HighlightInfo highlight = null;
                    if (notice.getShowInEditor()) {
                        highlight = h.addParserHighlight(notice, this.parserErrorHighlightPainter);
                    }
                    this.noticeHighlightPairs.add(new NoticeHighlightPair(notice, highlight));
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
        if (ParserManager.DEBUG_PARSING) {
            System.out.println("[DEBUG]: Done adding parser notices from " + res.getParser());
        }
    }
    
    public void changedUpdate(final DocumentEvent e) {
    }
    
    private void clearParserNoticeHighlights() {
        final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
        if (h != null) {
            h.clearParserHighlights();
        }
        if (this.noticeHighlightPairs != null) {
            this.noticeHighlightPairs.clear();
        }
    }
    
    private void clearParserNoticeHighlights(final Parser parser) {
        final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
        if (h != null) {
            h.clearParserHighlights(parser);
        }
        if (this.noticeHighlightPairs != null) {
            final Iterator<NoticeHighlightPair> i = this.noticeHighlightPairs.iterator();
            while (i.hasNext()) {
                final NoticeHighlightPair pair = i.next();
                if (pair.notice.getParser() == parser) {
                    i.remove();
                }
            }
        }
    }
    
    public void clearParsers() {
        this.timer.stop();
        this.clearParserNoticeHighlights();
        this.parsers.clear();
        this.textArea.fireParserNoticesChange();
    }
    
    public void forceReparsing(final int parser) {
        final Parser p = this.getParser(parser);
        final RSyntaxDocument doc = (RSyntaxDocument)this.textArea.getDocument();
        final String style = this.textArea.getSyntaxEditingStyle();
        doc.readLock();
        try {
            if (p.isEnabled()) {
                final ParseResult res = p.parse(doc, style);
                this.addParserNoticeHighlights(res);
            }
            else {
                this.clearParserNoticeHighlights(p);
            }
            this.textArea.fireParserNoticesChange();
        }
        finally {
            doc.readUnlock();
        }
    }
    
    public int getDelay() {
        return this.timer.getDelay();
    }
    
    public Parser getParser(final int index) {
        return this.parsers.get(index);
    }
    
    public int getParserCount() {
        return this.parsers.size();
    }
    
    public List<ParserNotice> getParserNotices() {
        final List<ParserNotice> notices = new ArrayList<ParserNotice>();
        if (this.noticeHighlightPairs != null) {
            for (final NoticeHighlightPair pair : this.noticeHighlightPairs) {
                notices.add(pair.notice);
            }
        }
        return notices;
    }
    
    public ToolTipInfo getToolTipText(final MouseEvent e) {
        String tip = null;
        HyperlinkListener listener = null;
        this.parserForTip = null;
        final Point p = e.getPoint();
        final int pos = this.textArea.viewToModel(p);
        if (this.noticeHighlightPairs != null) {
            for (final NoticeHighlightPair pair : this.noticeHighlightPairs) {
                final ParserNotice notice = pair.notice;
                if (this.noticeContainsPosition(notice, pos) && this.noticeContainsPointInView(notice, p)) {
                    tip = notice.getToolTipText();
                    this.parserForTip = notice.getParser();
                    if (this.parserForTip instanceof HyperlinkListener) {
                        listener = (HyperlinkListener)this.parserForTip;
                        break;
                    }
                    break;
                }
            }
        }
        final URL imageBase = (this.parserForTip == null) ? null : this.parserForTip.getImageBase();
        return new ToolTipInfo(tip, listener, imageBase);
    }
    
    public void handleDocumentEvent(final DocumentEvent e) {
        if (this.running && this.parsers.size() > 0) {
            this.timer.restart();
        }
    }
    
    public void hyperlinkUpdate(final HyperlinkEvent e) {
        if (this.parserForTip != null && this.parserForTip.getHyperlinkListener() != null) {
            this.parserForTip.getHyperlinkListener().linkClicked(this.textArea, e);
        }
    }
    
    public void insertUpdate(final DocumentEvent e) {
        try {
            int offs = e.getOffset();
            if (this.firstOffsetModded == null || offs < this.firstOffsetModded.getOffset()) {
                this.firstOffsetModded = e.getDocument().createPosition(offs);
            }
            offs = e.getOffset() + e.getLength();
            if (this.lastOffsetModded == null || offs > this.lastOffsetModded.getOffset()) {
                this.lastOffsetModded = e.getDocument().createPosition(offs);
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        this.handleDocumentEvent(e);
    }
    
    private final boolean noticeContainsPosition(final ParserNotice notice, final int offs) {
        if (notice.getKnowsOffsetAndLength()) {
            return notice.containsPosition(offs);
        }
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int line = notice.getLine();
        if (line < 0) {
            return false;
        }
        final Element elem = root.getElement(line);
        return offs >= elem.getStartOffset() && offs < elem.getEndOffset();
    }
    
    private final boolean noticeContainsPointInView(final ParserNotice notice, final Point p) {
        try {
            int start;
            int end;
            if (notice.getKnowsOffsetAndLength()) {
                start = notice.getOffset();
                end = start + notice.getLength() - 1;
            }
            else {
                final Document doc = this.textArea.getDocument();
                final Element root = doc.getDefaultRootElement();
                final int line = notice.getLine();
                if (line < 0) {
                    return false;
                }
                final Element elem = root.getElement(line);
                start = elem.getStartOffset();
                end = elem.getEndOffset() - 1;
            }
            final Rectangle r1 = this.textArea.modelToView(start);
            final Rectangle r2 = this.textArea.modelToView(end);
            if (r1.y != r2.y) {
                return true;
            }
            final Rectangle loc_0 = r1;
            --loc_0.y;
            final Rectangle loc_1 = r1;
            loc_1.height += 2;
            return p.x >= r1.x && p.x < r2.x + r2.width && p.y >= r1.y && p.y < r1.y + r1.height;
        }
        catch (BadLocationException ble) {
            return true;
        }
    }
    
    public boolean removeParser(final Parser parser) {
        this.removeParserNotices(parser);
        final boolean removed = this.parsers.remove(parser);
        if (removed) {
            this.textArea.fireParserNoticesChange();
        }
        return removed;
    }
    
    private void removeParserNotices(final Parser parser) {
        if (this.noticeHighlightPairs != null) {
            final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
            final Iterator<NoticeHighlightPair> i = this.noticeHighlightPairs.iterator();
            while (i.hasNext()) {
                final NoticeHighlightPair pair = i.next();
                if (pair.notice.getParser() == parser && pair.highlight != null) {
                    h.removeParserHighlight(pair.highlight);
                    i.remove();
                }
            }
        }
    }
    
    private void removeParserNotices(final ParseResult res) {
        if (this.noticeHighlightPairs != null) {
            final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
            final Iterator<NoticeHighlightPair> i = this.noticeHighlightPairs.iterator();
            while (i.hasNext()) {
                final NoticeHighlightPair pair = i.next();
                boolean removed = false;
                if (this.shouldRemoveNotice(pair.notice, res)) {
                    if (pair.highlight != null) {
                        h.removeParserHighlight(pair.highlight);
                    }
                    i.remove();
                    removed = true;
                }
                if (ParserManager.DEBUG_PARSING) {
                    final String text = removed ? "[DEBUG]: ... notice removed: " : "[DEBUG]: ... notice not removed: ";
                    System.out.println(text + pair.notice);
                }
            }
        }
    }
    
    public void removeUpdate(final DocumentEvent e) {
        try {
            final int offs = e.getOffset();
            if (this.firstOffsetModded == null || offs < this.firstOffsetModded.getOffset()) {
                this.firstOffsetModded = e.getDocument().createPosition(offs);
            }
            if (this.lastOffsetModded == null || offs > this.lastOffsetModded.getOffset()) {
                this.lastOffsetModded = e.getDocument().createPosition(offs);
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        this.handleDocumentEvent(e);
    }
    
    public void restartParsing() {
        this.timer.restart();
        this.running = true;
    }
    
    public void setDelay(final int millis) {
        if (this.running) {
            this.timer.stop();
        }
        this.timer.setDelay(millis);
        if (this.running) {
            this.timer.start();
        }
    }
    
    private final boolean shouldRemoveNotice(final ParserNotice notice, final ParseResult res) {
        if (ParserManager.DEBUG_PARSING) {
            System.out.println("[DEBUG]: ... ... shouldRemoveNotice " + notice + ": " + (notice.getParser() == res.getParser()));
        }
        return notice.getParser() == res.getParser();
    }
    
    public void stopParsing() {
        this.timer.stop();
        this.running = false;
    }
    
    static {
        boolean debugParsing = false;
        try {
            debugParsing = Boolean.getBoolean("rsta.debugParsing");
        }
        catch (AccessControlException ace) {
            debugParsing = false;
        }
        DEBUG_PARSING = debugParsing;
    }
    
    private static class NoticeHighlightPair
    {
        public ParserNotice notice;
        public RTextAreaHighlighter.HighlightInfo highlight;
        
        public NoticeHighlightPair(final ParserNotice notice, final RTextAreaHighlighter.HighlightInfo highlight) {
            super();
            this.notice = notice;
            this.highlight = highlight;
        }
    }
}
