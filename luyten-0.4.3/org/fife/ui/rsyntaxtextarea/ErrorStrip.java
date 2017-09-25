package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;
import java.text.*;
import javax.swing.text.*;
import java.awt.event.*;
import javax.swing.*;
import java.beans.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import java.util.*;
import java.awt.*;

public class ErrorStrip extends JComponent
{
    private RSyntaxTextArea textArea;
    private Listener listener;
    private boolean showMarkedOccurrences;
    private boolean showMarkAll;
    private Map<Color, Color> brighterColors;
    private ParserNotice.Level levelThreshold;
    private boolean followCaret;
    private Color caretMarkerColor;
    private int caretLineY;
    private int lastLineY;
    private static final int PREFERRED_WIDTH = 14;
    private static final String MSG = "org.fife.ui.rsyntaxtextarea.ErrorStrip";
    private static final ResourceBundle msg;
    private static final Color MARKED_OCCURRENCE_COLOR;
    
    public ErrorStrip(final RSyntaxTextArea textArea) {
        super();
        this.textArea = textArea;
        this.listener = new Listener();
        ToolTipManager.sharedInstance().registerComponent(this);
        this.setLayout(null);
        this.addMouseListener(this.listener);
        this.setShowMarkedOccurrences(true);
        this.setShowMarkAll(true);
        this.setLevelThreshold(ParserNotice.Level.WARNING);
        this.setFollowCaret(true);
        this.setCaretMarkerColor(Color.BLACK);
    }
    
    public void addNotify() {
        super.addNotify();
        this.textArea.addCaretListener(this.listener);
        this.textArea.addPropertyChangeListener("RSTA.parserNotices", this.listener);
        this.textArea.addPropertyChangeListener("RSTA.markOccurrences", this.listener);
        this.textArea.addPropertyChangeListener("RSTA.markedOccurrencesChanged", this.listener);
        this.textArea.addPropertyChangeListener("RTA.markAllOccurrencesChanged", this.listener);
        this.refreshMarkers();
    }
    
    public void doLayout() {
        for (int i = 0; i < this.getComponentCount(); ++i) {
            final Marker m = (Marker)this.getComponent(i);
            m.updateLocation();
        }
        this.listener.caretUpdate(null);
    }
    
    private Color getBrighterColor(final Color c) {
        if (this.brighterColors == null) {
            this.brighterColors = new HashMap<Color, Color>(5);
        }
        Color brighter = this.brighterColors.get(c);
        if (brighter == null) {
            final int r = possiblyBrighter(c.getRed());
            final int g = possiblyBrighter(c.getGreen());
            final int b = possiblyBrighter(c.getBlue());
            brighter = new Color(r, g, b);
            this.brighterColors.put(c, brighter);
        }
        return brighter;
    }
    
    public Color getCaretMarkerColor() {
        return this.caretMarkerColor;
    }
    
    public boolean getFollowCaret() {
        return this.followCaret;
    }
    
    public Dimension getPreferredSize() {
        final int height = this.textArea.getPreferredScrollableViewportSize().height;
        return new Dimension(14, height);
    }
    
    public ParserNotice.Level getLevelThreshold() {
        return this.levelThreshold;
    }
    
    public boolean getShowMarkAll() {
        return this.showMarkAll;
    }
    
    public boolean getShowMarkedOccurrences() {
        return this.showMarkedOccurrences;
    }
    
    public String getToolTipText(final MouseEvent e) {
        String text = null;
        final int line = this.yToLine(e.getY());
        if (line > -1) {
            text = ErrorStrip.msg.getString("Line");
            text = MessageFormat.format(text, line + 1);
        }
        return text;
    }
    
    private int lineToY(final int line) {
        final int h = this.textArea.getVisibleRect().height;
        final float lineCount = this.textArea.getLineCount();
        return (int)(line / lineCount * h) - 2;
    }
    
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (this.caretLineY > -1) {
            g.setColor(this.getCaretMarkerColor());
            g.fillRect(0, this.caretLineY, this.getWidth(), 2);
        }
    }
    
    private static final int possiblyBrighter(int i) {
        if (i < 255) {
            i += (int)((255 - i) * 0.8f);
        }
        return i;
    }
    
    private void refreshMarkers() {
        this.removeAll();
        final Map<Integer, Marker> markerMap = new HashMap<Integer, Marker>();
        final List<ParserNotice> notices = this.textArea.getParserNotices();
        for (final ParserNotice notice : notices) {
            if (notice.getLevel().isEqualToOrWorseThan(this.levelThreshold) || notice instanceof TaskTagParser.TaskNotice) {
                final Integer key = notice.getLine();
                Marker m = markerMap.get(key);
                if (m == null) {
                    m = new Marker(notice);
                    m.addMouseListener(this.listener);
                    markerMap.put(key, m);
                    this.add(m);
                }
                else {
                    m.addNotice(notice);
                }
            }
        }
        if (this.getShowMarkedOccurrences() && this.textArea.getMarkOccurrences()) {
            final List<DocumentRange> occurrences = this.textArea.getMarkedOccurrences();
            this.addMarkersForRanges(occurrences, markerMap, ErrorStrip.MARKED_OCCURRENCE_COLOR);
        }
        if (this.getShowMarkAll()) {
            final Color markAllColor = this.textArea.getMarkAllHighlightColor();
            final List<DocumentRange> ranges = this.textArea.getMarkAllHighlightRanges();
            this.addMarkersForRanges(ranges, markerMap, markAllColor);
        }
        this.revalidate();
        this.repaint();
    }
    
    private void addMarkersForRanges(final List<DocumentRange> ranges, final Map<Integer, Marker> markerMap, final Color color) {
        for (final DocumentRange range : ranges) {
            int line = 0;
            try {
                line = this.textArea.getLineOfOffset(range.getStartOffset());
            }
            catch (BadLocationException ble) {
                continue;
            }
            final ParserNotice notice = new MarkedOccurrenceNotice(range, color);
            final Integer key = line;
            Marker m = markerMap.get(key);
            if (m == null) {
                m = new Marker(notice);
                m.addMouseListener(this.listener);
                markerMap.put(key, m);
                this.add(m);
            }
            else {
                if (m.containsMarkedOccurence()) {
                    continue;
                }
                m.addNotice(notice);
            }
        }
    }
    
    public void removeNotify() {
        super.removeNotify();
        this.textArea.removeCaretListener(this.listener);
        this.textArea.removePropertyChangeListener("RSTA.parserNotices", this.listener);
        this.textArea.removePropertyChangeListener("RSTA.markOccurrences", this.listener);
        this.textArea.removePropertyChangeListener("RSTA.markedOccurrencesChanged", this.listener);
        this.textArea.removePropertyChangeListener("RTA.markAllOccurrencesChanged", this.listener);
    }
    
    public void setCaretMarkerColor(final Color color) {
        if (color != null) {
            this.caretMarkerColor = color;
            this.listener.caretUpdate(null);
        }
    }
    
    public void setFollowCaret(final boolean follow) {
        if (this.followCaret != follow) {
            if (this.followCaret) {
                this.repaint(0, this.caretLineY, this.getWidth(), 2);
            }
            this.caretLineY = -1;
            this.lastLineY = -1;
            this.followCaret = follow;
            this.listener.caretUpdate(null);
        }
    }
    
    public void setLevelThreshold(final ParserNotice.Level level) {
        this.levelThreshold = level;
        if (this.isDisplayable()) {
            this.refreshMarkers();
        }
    }
    
    public void setShowMarkAll(final boolean show) {
        if (show != this.showMarkAll) {
            this.showMarkAll = show;
            if (this.isDisplayable()) {
                this.refreshMarkers();
            }
        }
    }
    
    public void setShowMarkedOccurrences(final boolean show) {
        if (show != this.showMarkedOccurrences) {
            this.showMarkedOccurrences = show;
            if (this.isDisplayable()) {
                this.refreshMarkers();
            }
        }
    }
    
    private final int yToLine(final int y) {
        int line = -1;
        final int h = this.textArea.getVisibleRect().height;
        if (y < h) {
            final float at = y / h;
            line = (int)(this.textArea.getLineCount() * at);
        }
        return line;
    }
    
    static /* synthetic */ RSyntaxTextArea access$100(final ErrorStrip x0) {
        return x0.textArea;
    }
    
    static /* synthetic */ int access$202(final ErrorStrip x0, final int x1) {
        return x0.caretLineY = x1;
    }
    
    static /* synthetic */ int access$200(final ErrorStrip x0) {
        return x0.caretLineY;
    }
    
    static /* synthetic */ int access$300(final ErrorStrip x0) {
        return x0.lastLineY;
    }
    
    static /* synthetic */ int access$302(final ErrorStrip x0, final int x1) {
        return x0.lastLineY = x1;
    }
    
    static /* synthetic */ int access$400(final ErrorStrip x0, final int x1) {
        return x0.yToLine(x1);
    }
    
    static /* synthetic */ void access$500(final ErrorStrip x0) {
        x0.refreshMarkers();
    }
    
    static /* synthetic */ ResourceBundle access$600() {
        return ErrorStrip.msg;
    }
    
    static /* synthetic */ Color access$700(final ErrorStrip x0, final Color x1) {
        return x0.getBrighterColor(x1);
    }
    
    static /* synthetic */ Listener access$800(final ErrorStrip x0) {
        return x0.listener;
    }
    
    static /* synthetic */ int access$900(final ErrorStrip x0, final int x1) {
        return x0.lineToY(x1);
    }
    
    static {
        msg = ResourceBundle.getBundle("org.fife.ui.rsyntaxtextarea.ErrorStrip");
        MARKED_OCCURRENCE_COLOR = new Color(220, 220, 220);
    }
    
    private class Listener extends MouseAdapter implements PropertyChangeListener, CaretListener
    {
        private Rectangle visibleRect;
        
        private Listener() {
            super();
            this.visibleRect = new Rectangle();
        }
        
        public void caretUpdate(final CaretEvent e) {
            if (ErrorStrip.this.getFollowCaret()) {
                final int line = ErrorStrip.access$100(ErrorStrip.this).getCaretLineNumber();
                final float percent = line / ErrorStrip.access$100(ErrorStrip.this).getLineCount();
                ErrorStrip.access$100(ErrorStrip.this).computeVisibleRect(this.visibleRect);
                ErrorStrip.access$202(ErrorStrip.this, (int)(this.visibleRect.height * percent));
                if (ErrorStrip.access$200(ErrorStrip.this) != ErrorStrip.access$300(ErrorStrip.this)) {
                    ErrorStrip.this.repaint(0, ErrorStrip.access$300(ErrorStrip.this), ErrorStrip.this.getWidth(), 2);
                    ErrorStrip.this.repaint(0, ErrorStrip.access$200(ErrorStrip.this), ErrorStrip.this.getWidth(), 2);
                    ErrorStrip.access$302(ErrorStrip.this, ErrorStrip.access$200(ErrorStrip.this));
                }
            }
        }
        
        public void mouseClicked(final MouseEvent e) {
            final Component source = (Component)e.getSource();
            if (source instanceof Marker) {
                ((Marker)source).mouseClicked(e);
                return;
            }
            final int line = ErrorStrip.access$400(ErrorStrip.this, e.getY());
            if (line > -1) {
                try {
                    final int offs = ErrorStrip.access$100(ErrorStrip.this).getLineStartOffset(line);
                    ErrorStrip.access$100(ErrorStrip.this).setCaretPosition(offs);
                }
                catch (BadLocationException ble) {
                    UIManager.getLookAndFeel().provideErrorFeedback(ErrorStrip.access$100(ErrorStrip.this));
                }
            }
        }
        
        public void propertyChange(final PropertyChangeEvent e) {
            final String propName = e.getPropertyName();
            if ("RSTA.markOccurrences".equals(propName)) {
                if (ErrorStrip.this.getShowMarkedOccurrences()) {
                    ErrorStrip.access$500(ErrorStrip.this);
                }
            }
            else if ("RSTA.parserNotices".equals(propName)) {
                ErrorStrip.access$500(ErrorStrip.this);
            }
            else if ("RSTA.markedOccurrencesChanged".equals(propName)) {
                if (ErrorStrip.this.getShowMarkedOccurrences()) {
                    ErrorStrip.access$500(ErrorStrip.this);
                }
            }
            else if ("RTA.markAllOccurrencesChanged".equals(propName) && ErrorStrip.this.getShowMarkAll()) {
                ErrorStrip.access$500(ErrorStrip.this);
            }
        }
    }
    
    private class MarkedOccurrenceNotice implements ParserNotice
    {
        private DocumentRange range;
        private Color color;
        
        public MarkedOccurrenceNotice(final DocumentRange range, final Color color) {
            super();
            this.range = range;
            this.color = color;
        }
        
        public int compareTo(final ParserNotice other) {
            return 0;
        }
        
        public boolean containsPosition(final int pos) {
            return pos >= this.range.getStartOffset() && pos < this.range.getEndOffset();
        }
        
        public boolean equals(final Object o) {
            return o instanceof ParserNotice && this.compareTo((ParserNotice)o) == 0;
        }
        
        public Color getColor() {
            return this.color;
        }
        
        public boolean getKnowsOffsetAndLength() {
            return true;
        }
        
        public int getLength() {
            return this.range.getEndOffset() - this.range.getStartOffset();
        }
        
        public Level getLevel() {
            return Level.INFO;
        }
        
        public int getLine() {
            try {
                return ErrorStrip.access$100(ErrorStrip.this).getLineOfOffset(this.range.getStartOffset());
            }
            catch (BadLocationException ble) {
                return 0;
            }
        }
        
        public String getMessage() {
            String text = null;
            try {
                final String word = ErrorStrip.access$100(ErrorStrip.this).getText(this.range.getStartOffset(), this.getLength());
                text = ErrorStrip.access$600().getString("OccurrenceOf");
                text = MessageFormat.format(text, word);
            }
            catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(ErrorStrip.access$100(ErrorStrip.this));
            }
            return text;
        }
        
        public int getOffset() {
            return this.range.getStartOffset();
        }
        
        public Parser getParser() {
            return null;
        }
        
        public boolean getShowInEditor() {
            return false;
        }
        
        public String getToolTipText() {
            return null;
        }
        
        public int hashCode() {
            return 0;
        }
    }
    
    private class Marker extends JComponent
    {
        private List<ParserNotice> notices;
        
        public Marker(final ParserNotice notice) {
            super();
            this.notices = new ArrayList<ParserNotice>(1);
            this.addNotice(notice);
            this.setCursor(Cursor.getPredefinedCursor(12));
            this.setSize(this.getPreferredSize());
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        
        public void addNotice(final ParserNotice notice) {
            this.notices.add(notice);
        }
        
        public boolean containsMarkedOccurence() {
            boolean result = false;
            for (int i = 0; i < this.notices.size(); ++i) {
                if (this.notices.get(i) instanceof MarkedOccurrenceNotice) {
                    result = true;
                    break;
                }
            }
            return result;
        }
        
        public Color getColor() {
            Color c = null;
            int lowestLevel = Integer.MAX_VALUE;
            for (final ParserNotice notice : this.notices) {
                if (notice.getLevel().getNumericValue() < lowestLevel) {
                    lowestLevel = notice.getLevel().getNumericValue();
                    c = notice.getColor();
                }
            }
            return c;
        }
        
        public Dimension getPreferredSize() {
            final int w = 10;
            return new Dimension(w, 5);
        }
        
        public String getToolTipText() {
            String text = null;
            if (this.notices.size() == 1) {
                text = this.notices.get(0).getMessage();
            }
            else {
                final StringBuilder sb = new StringBuilder("<html>");
                sb.append(ErrorStrip.access$600().getString("MultipleMarkers"));
                sb.append("<br>");
                for (int i = 0; i < this.notices.size(); ++i) {
                    final ParserNotice pn = this.notices.get(i);
                    sb.append("&nbsp;&nbsp;&nbsp;- ");
                    sb.append(pn.getMessage());
                    sb.append("<br>");
                }
                text = sb.toString();
            }
            return text;
        }
        
        protected void mouseClicked(final MouseEvent e) {
            final ParserNotice pn = this.notices.get(0);
            int offs = pn.getOffset();
            final int len = pn.getLength();
            if (offs > -1 && len > -1) {
                ErrorStrip.access$100(ErrorStrip.this).setSelectionStart(offs);
                ErrorStrip.access$100(ErrorStrip.this).setSelectionEnd(offs + len);
            }
            else {
                final int line = pn.getLine();
                try {
                    offs = ErrorStrip.access$100(ErrorStrip.this).getLineStartOffset(line);
                    ErrorStrip.access$100(ErrorStrip.this).setCaretPosition(offs);
                }
                catch (BadLocationException ble) {
                    UIManager.getLookAndFeel().provideErrorFeedback(ErrorStrip.access$100(ErrorStrip.this));
                }
            }
        }
        
        protected void paintComponent(final Graphics g) {
            Color borderColor = this.getColor();
            if (borderColor == null) {
                borderColor = Color.DARK_GRAY;
            }
            final Color fillColor = ErrorStrip.access$700(ErrorStrip.this, borderColor);
            final int w = this.getWidth();
            final int h = this.getHeight();
            g.setColor(fillColor);
            g.fillRect(0, 0, w, h);
            g.setColor(borderColor);
            g.drawRect(0, 0, w - 1, h - 1);
        }
        
        public void removeNotify() {
            super.removeNotify();
            ToolTipManager.sharedInstance().unregisterComponent(this);
            this.removeMouseListener(ErrorStrip.access$800(ErrorStrip.this));
        }
        
        public void updateLocation() {
            final int line = this.notices.get(0).getLine();
            final int y = ErrorStrip.access$900(ErrorStrip.this, line);
            this.setLocation(2, y);
        }
    }
}
