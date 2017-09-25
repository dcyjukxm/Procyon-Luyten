package org.fife.ui.rtextarea;

import java.util.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.event.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.beans.*;

public class LineNumberList extends AbstractGutterComponent implements MouseInputListener
{
    private int currentLine;
    private int lastY;
    private int lastVisibleLine;
    private int cellHeight;
    private int cellWidth;
    private int ascent;
    private Map<?, ?> aaHints;
    private int mouseDragStartOffset;
    private Listener l;
    private Insets textAreaInsets;
    private Rectangle visibleRect;
    private int lineNumberingStartIndex;
    
    public LineNumberList(final RTextArea textArea) {
        this(textArea, null);
    }
    
    public LineNumberList(final RTextArea textArea, final Color numberColor) {
        super(textArea);
        this.lastY = -1;
        if (numberColor != null) {
            this.setForeground(numberColor);
        }
        else {
            this.setForeground(Color.GRAY);
        }
    }
    
    public void addNotify() {
        super.addNotify();
        if (this.textArea != null) {
            this.l.install(this.textArea);
        }
        this.updateCellWidths();
        this.updateCellHeights();
    }
    
    private int calculateLastVisibleLineNumber() {
        int lastLine = 0;
        if (this.textArea != null) {
            lastLine = this.textArea.getLineCount() + this.getLineNumberingStartIndex() - 1;
        }
        return lastLine;
    }
    
    public int getLineNumberingStartIndex() {
        return this.lineNumberingStartIndex;
    }
    
    public Dimension getPreferredSize() {
        final int h = (this.textArea != null) ? this.textArea.getHeight() : 100;
        return new Dimension(this.cellWidth, h);
    }
    
    private int getRhsBorderWidth() {
        int w = 4;
        if (this.textArea instanceof RSyntaxTextArea && ((RSyntaxTextArea)this.textArea).isCodeFoldingEnabled()) {
            w = 0;
        }
        return w;
    }
    
    void handleDocumentEvent(final DocumentEvent e) {
        final int newLastLine = this.calculateLastVisibleLineNumber();
        if (newLastLine != this.lastVisibleLine) {
            if (newLastLine / 10 != this.lastVisibleLine / 10) {
                this.updateCellWidths();
            }
            this.lastVisibleLine = newLastLine;
            this.repaint();
        }
    }
    
    protected void init() {
        super.init();
        this.currentLine = 0;
        this.setLineNumberingStartIndex(1);
        this.visibleRect = new Rectangle();
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.aaHints = RSyntaxUtilities.getDesktopAntiAliasHints();
    }
    
    void lineHeightsChanged() {
        this.updateCellHeights();
    }
    
    public void mouseClicked(final MouseEvent e) {
    }
    
    public void mouseDragged(final MouseEvent e) {
        if (this.mouseDragStartOffset > -1) {
            final int pos = this.textArea.viewToModel(new Point(0, e.getY()));
            if (pos >= 0) {
                this.textArea.setCaretPosition(this.mouseDragStartOffset);
                this.textArea.moveCaretPosition(pos);
            }
        }
    }
    
    public void mouseEntered(final MouseEvent e) {
    }
    
    public void mouseExited(final MouseEvent e) {
    }
    
    public void mouseMoved(final MouseEvent e) {
    }
    
    public void mousePressed(final MouseEvent e) {
        if (this.textArea == null) {
            return;
        }
        if (e.getButton() == 1) {
            final int pos = this.textArea.viewToModel(new Point(0, e.getY()));
            if (pos >= 0) {
                this.textArea.setCaretPosition(pos);
            }
            this.mouseDragStartOffset = pos;
        }
        else {
            this.mouseDragStartOffset = -1;
        }
    }
    
    public void mouseReleased(final MouseEvent e) {
    }
    
    protected void paintComponent(final Graphics g) {
        if (this.textArea == null) {
            return;
        }
        this.visibleRect = g.getClipBounds(this.visibleRect);
        if (this.visibleRect == null) {
            this.visibleRect = this.getVisibleRect();
        }
        if (this.visibleRect == null) {
            return;
        }
        Color bg = this.getBackground();
        if (this.getGutter() != null) {
            bg = this.getGutter().getBackground();
        }
        g.setColor(bg);
        g.fillRect(0, this.visibleRect.y, this.cellWidth, this.visibleRect.height);
        g.setFont(this.getFont());
        if (this.aaHints != null) {
            ((Graphics2D)g).addRenderingHints(this.aaHints);
        }
        if (this.textArea.getLineWrap()) {
            this.paintWrappedLineNumbers(g, this.visibleRect);
            return;
        }
        this.textAreaInsets = this.textArea.getInsets(this.textAreaInsets);
        if (this.visibleRect.y < this.textAreaInsets.top) {
            final Rectangle loc_0 = this.visibleRect;
            loc_0.height -= this.textAreaInsets.top - this.visibleRect.y;
            this.visibleRect.y = this.textAreaInsets.top;
        }
        int topLine = (this.visibleRect.y - this.textAreaInsets.top) / this.cellHeight;
        final int actualTopY = topLine * this.cellHeight + this.textAreaInsets.top;
        int y = actualTopY + this.ascent;
        FoldManager fm = null;
        if (this.textArea instanceof RSyntaxTextArea) {
            fm = ((RSyntaxTextArea)this.textArea).getFoldManager();
            topLine += fm.getHiddenLineCountAbove(topLine, true);
        }
        final int RHS_BORDER_WIDTH = this.getRhsBorderWidth();
        g.setColor(this.getForeground());
        final boolean ltr = this.getComponentOrientation().isLeftToRight();
        if (ltr) {
            final FontMetrics metrics = g.getFontMetrics();
            final int rhs = this.getWidth() - RHS_BORDER_WIDTH;
            for (int line = topLine + 1; y < this.visibleRect.y + this.visibleRect.height + this.ascent && line <= this.textArea.getLineCount(); ++line) {
                final String number = Integer.toString(line + this.getLineNumberingStartIndex() - 1);
                final int width = metrics.stringWidth(number);
                g.drawString(number, rhs - width, y);
                y += this.cellHeight;
                if (fm != null) {
                    for (Fold fold = fm.getFoldForLine(line - 1); fold != null && fold.isCollapsed(); fold = fm.getFoldForLine(line - 1)) {
                        final int hiddenLineCount = fold.getLineCount();
                        if (hiddenLineCount == 0) {
                            break;
                        }
                        line += hiddenLineCount;
                    }
                }
            }
        }
        else {
            for (int line2 = topLine + 1; y < this.visibleRect.y + this.visibleRect.height && line2 < this.textArea.getLineCount(); ++line2) {
                final String number2 = Integer.toString(line2 + this.getLineNumberingStartIndex() - 1);
                g.drawString(number2, RHS_BORDER_WIDTH, y);
                y += this.cellHeight;
                if (fm != null) {
                    for (Fold fold2 = fm.getFoldForLine(line2 - 1); fold2 != null && fold2.isCollapsed(); fold2 = fm.getFoldForLine(line2)) {
                        line2 += fold2.getLineCount();
                    }
                }
            }
        }
    }
    
    private void paintWrappedLineNumbers(final Graphics g, final Rectangle visibleRect) {
        final int width = this.getWidth();
        final RTextAreaUI ui = (RTextAreaUI)this.textArea.getUI();
        final View v = ui.getRootView(this.textArea).getView(0);
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int lineCount = root.getElementCount();
        final int topPosition = this.textArea.viewToModel(new Point(visibleRect.x, visibleRect.y));
        int topLine = root.getElementIndex(topPosition);
        FoldManager fm = null;
        if (this.textArea instanceof RSyntaxTextArea) {
            fm = ((RSyntaxTextArea)this.textArea).getFoldManager();
        }
        final Rectangle visibleEditorRect = ui.getVisibleEditorRect();
        Rectangle r = AbstractGutterComponent.getChildViewBounds(v, topLine, visibleEditorRect);
        int y = r.y;
        final int RHS_BORDER_WIDTH = this.getRhsBorderWidth();
        final boolean ltr = this.getComponentOrientation().isLeftToRight();
        int rhs;
        if (ltr) {
            rhs = width - RHS_BORDER_WIDTH;
        }
        else {
            rhs = RHS_BORDER_WIDTH;
        }
        final int visibleBottom = visibleRect.y + visibleRect.height;
        final FontMetrics metrics = g.getFontMetrics();
        g.setColor(this.getForeground());
        while (y < visibleBottom) {
            r = AbstractGutterComponent.getChildViewBounds(v, topLine, visibleEditorRect);
            final int index = topLine + 1 + this.getLineNumberingStartIndex() - 1;
            final String number = Integer.toString(index);
            if (ltr) {
                final int strWidth = metrics.stringWidth(number);
                g.drawString(number, rhs - strWidth, y + this.ascent);
            }
            else {
                final int x = RHS_BORDER_WIDTH;
                g.drawString(number, x, y + this.ascent);
            }
            y += r.height;
            if (fm != null) {
                final Fold fold = fm.getFoldForLine(topLine);
                if (fold != null && fold.isCollapsed()) {
                    topLine += fold.getCollapsedLineCount();
                }
            }
            if (++topLine >= lineCount) {
                break;
            }
        }
    }
    
    public void removeNotify() {
        super.removeNotify();
        if (this.textArea != null) {
            this.l.uninstall(this.textArea);
        }
    }
    
    private void repaintLine(final int line) {
        int y = this.textArea.getInsets().top;
        y += line * this.cellHeight;
        this.repaint(0, y, this.cellWidth, this.cellHeight);
    }
    
    public void setFont(final Font font) {
        super.setFont(font);
        this.updateCellWidths();
        this.updateCellHeights();
    }
    
    public void setLineNumberingStartIndex(final int index) {
        if (index != this.lineNumberingStartIndex) {
            this.lineNumberingStartIndex = index;
            this.updateCellWidths();
            this.repaint();
        }
    }
    
    public void setTextArea(final RTextArea textArea) {
        if (this.l == null) {
            this.l = new Listener();
        }
        if (this.textArea != null) {
            this.l.uninstall(textArea);
        }
        super.setTextArea(textArea);
        this.lastVisibleLine = this.calculateLastVisibleLineNumber();
        if (textArea != null) {
            this.l.install(textArea);
            this.updateCellHeights();
            this.updateCellWidths();
        }
    }
    
    private void updateCellHeights() {
        if (this.textArea != null) {
            this.cellHeight = this.textArea.getLineHeight();
            this.ascent = this.textArea.getMaxAscent();
        }
        else {
            this.cellHeight = 20;
            this.ascent = 5;
        }
        this.repaint();
    }
    
    void updateCellWidths() {
        final int oldCellWidth = this.cellWidth;
        this.cellWidth = this.getRhsBorderWidth();
        if (this.textArea != null) {
            final Font font = this.getFont();
            if (font != null) {
                final FontMetrics fontMetrics = this.getFontMetrics(font);
                int count = 0;
                int lineCount = this.textArea.getLineCount() + this.getLineNumberingStartIndex() - 1;
                do {
                    lineCount /= 10;
                    ++count;
                } while (lineCount >= 10);
                this.cellWidth += fontMetrics.charWidth('9') * (count + 1) + 3;
            }
        }
        if (this.cellWidth != oldCellWidth) {
            this.revalidate();
        }
    }
    
    static /* synthetic */ int access$100(final LineNumberList x0) {
        return x0.currentLine;
    }
    
    static /* synthetic */ void access$200(final LineNumberList x0, final int x1) {
        x0.repaintLine(x1);
    }
    
    static /* synthetic */ int access$102(final LineNumberList x0, final int x1) {
        return x0.currentLine = x1;
    }
    
    static /* synthetic */ int access$300(final LineNumberList x0) {
        return x0.lastY;
    }
    
    static /* synthetic */ int access$302(final LineNumberList x0, final int x1) {
        return x0.lastY = x1;
    }
    
    private class Listener implements CaretListener, PropertyChangeListener
    {
        private boolean installed;
        
        public void caretUpdate(final CaretEvent e) {
            final int dot = LineNumberList.this.textArea.getCaretPosition();
            if (!LineNumberList.this.textArea.getLineWrap()) {
                final int line = LineNumberList.this.textArea.getDocument().getDefaultRootElement().getElementIndex(dot);
                if (LineNumberList.access$100(LineNumberList.this) != line) {
                    LineNumberList.access$200(LineNumberList.this, line);
                    LineNumberList.access$200(LineNumberList.this, LineNumberList.access$100(LineNumberList.this));
                    LineNumberList.access$102(LineNumberList.this, line);
                }
            }
            else {
                try {
                    final int y = LineNumberList.this.textArea.yForLineContaining(dot);
                    if (y != LineNumberList.access$300(LineNumberList.this)) {
                        LineNumberList.access$302(LineNumberList.this, y);
                        LineNumberList.access$102(LineNumberList.this, LineNumberList.this.textArea.getDocument().getDefaultRootElement().getElementIndex(dot));
                        LineNumberList.this.repaint();
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
        
        public void install(final RTextArea textArea) {
            if (!this.installed) {
                textArea.addCaretListener(this);
                textArea.addPropertyChangeListener(this);
                this.caretUpdate(null);
                this.installed = true;
            }
        }
        
        public void propertyChange(final PropertyChangeEvent e) {
            final String name = e.getPropertyName();
            if ("RTA.currentLineHighlight".equals(name) || "RTA.currentLineHighlightColor".equals(name)) {
                LineNumberList.access$200(LineNumberList.this, LineNumberList.access$100(LineNumberList.this));
            }
        }
        
        public void uninstall(final RTextArea textArea) {
            if (this.installed) {
                textArea.removeCaretListener(this);
                textArea.removePropertyChangeListener(this);
                this.installed = false;
            }
        }
    }
}
