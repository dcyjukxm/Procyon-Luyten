package org.fife.ui.rtextarea;

import org.fife.ui.rsyntaxtextarea.focusabletip.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.beans.*;

public class FoldIndicator extends AbstractGutterComponent
{
    private Insets textAreaInsets;
    private Rectangle visibleRect;
    private Fold foldWithOutlineShowing;
    private Color foldIconBackground;
    private Icon collapsedFoldIcon;
    private Icon expandedFoldIcon;
    private boolean showFoldRegionTips;
    static final Color DEFAULT_FOREGROUND;
    static final Color DEFAULT_FOLD_BACKGROUND;
    private Listener listener;
    private static final int WIDTH = 12;
    
    public FoldIndicator(final RTextArea textArea) {
        super(textArea);
    }
    
    public JToolTip createToolTip() {
        final JToolTip tip = super.createToolTip();
        final Color textAreaBG = this.textArea.getBackground();
        if (textAreaBG != null && !Color.white.equals(textAreaBG)) {
            final Color bg = TipUtil.getToolTipBackground();
            if (bg.getRed() >= 240 && bg.getGreen() >= 240 && bg.getBlue() >= 200) {
                tip.setBackground(textAreaBG);
            }
        }
        return tip;
    }
    
    private Fold findOpenFoldClosestTo(final Point p) {
        Fold fold = null;
        final RSyntaxTextArea rsta = (RSyntaxTextArea)this.textArea;
        if (rsta.isCodeFoldingEnabled()) {
            final int offs = rsta.viewToModel(p);
            if (offs > -1) {
                try {
                    final int line = rsta.getLineOfOffset(offs);
                    final FoldManager fm = rsta.getFoldManager();
                    fold = fm.getFoldForLine(line);
                    if (fold == null) {
                        fold = fm.getDeepestOpenFoldContaining(offs);
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
        return fold;
    }
    
    public Color getFoldIconBackground() {
        return this.foldIconBackground;
    }
    
    public Dimension getPreferredSize() {
        final int h = (this.textArea != null) ? this.textArea.getHeight() : 100;
        return new Dimension(12, h);
    }
    
    public boolean getShowCollapsedRegionToolTips() {
        return this.showFoldRegionTips;
    }
    
    public Point getToolTipLocation(final MouseEvent e) {
        final String text = this.getToolTipText(e);
        if (text == null) {
            return null;
        }
        final Point p = e.getPoint();
        p.y = p.y / this.textArea.getLineHeight() * this.textArea.getLineHeight();
        p.x = this.getWidth() + this.textArea.getMargin().left;
        final Gutter gutter = this.getGutter();
        final int gutterMargin = gutter.getInsets().right;
        final Point loc_0 = p;
        loc_0.x += gutterMargin;
        final JToolTip tempTip = this.createToolTip();
        final Point loc_1 = p;
        loc_1.x -= tempTip.getInsets().left;
        final Point loc_2 = p;
        loc_2.y += 16;
        return p;
    }
    
    public String getToolTipText(final MouseEvent e) {
        String text = null;
        final RSyntaxTextArea rsta = (RSyntaxTextArea)this.textArea;
        if (rsta.isCodeFoldingEnabled()) {
            final FoldManager fm = rsta.getFoldManager();
            final int pos = rsta.viewToModel(new Point(0, e.getY()));
            if (pos >= 0) {
                int line = 0;
                try {
                    line = rsta.getLineOfOffset(pos);
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                    return null;
                }
                final Fold fold = fm.getFoldForLine(line);
                if (fold != null && fold.isCollapsed()) {
                    int endLine = fold.getEndLine();
                    if (fold.getLineCount() > 25) {
                        endLine = fold.getStartLine() + 25;
                    }
                    final StringBuilder sb = new StringBuilder("<html><nobr>");
                    while (line <= endLine && line < rsta.getLineCount()) {
                        for (Token t = rsta.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                            t.appendHTMLRepresentation(sb, rsta, true, true);
                        }
                        sb.append("<br>");
                        ++line;
                    }
                    text = sb.toString();
                }
            }
        }
        return text;
    }
    
    void handleDocumentEvent(final DocumentEvent e) {
        final int newLineCount = this.textArea.getLineCount();
        if (newLineCount != this.currentLineCount) {
            this.currentLineCount = newLineCount;
            this.repaint();
        }
    }
    
    protected void init() {
        super.init();
        this.setForeground(FoldIndicator.DEFAULT_FOREGROUND);
        this.setFoldIconBackground(FoldIndicator.DEFAULT_FOLD_BACKGROUND);
        this.collapsedFoldIcon = new FoldIcon(true);
        this.expandedFoldIcon = new FoldIcon(false);
        this.listener = new Listener(this);
        this.visibleRect = new Rectangle();
        this.setShowCollapsedRegionToolTips(true);
    }
    
    void lineHeightsChanged() {
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
        g.fillRect(0, this.visibleRect.y, this.getWidth(), this.visibleRect.height);
        final RSyntaxTextArea rsta = (RSyntaxTextArea)this.textArea;
        if (!rsta.isCodeFoldingEnabled()) {
            return;
        }
        if (this.textArea.getLineWrap()) {
            this.paintComponentWrapped(g);
            return;
        }
        this.textAreaInsets = this.textArea.getInsets(this.textAreaInsets);
        if (this.visibleRect.y < this.textAreaInsets.top) {
            final Rectangle loc_0 = this.visibleRect;
            loc_0.height -= this.textAreaInsets.top - this.visibleRect.y;
            this.visibleRect.y = this.textAreaInsets.top;
        }
        final int cellHeight = this.textArea.getLineHeight();
        int topLine = (this.visibleRect.y - this.textAreaInsets.top) / cellHeight;
        int y = topLine * cellHeight + (cellHeight - this.collapsedFoldIcon.getIconHeight()) / 2;
        y += this.textAreaInsets.top;
        final FoldManager fm = rsta.getFoldManager();
        topLine += fm.getHiddenLineCountAbove(topLine, true);
        final int width = this.getWidth();
        final int x = width - 10;
        int line = topLine;
        boolean paintingOutlineLine = this.foldWithOutlineShowing != null && this.foldWithOutlineShowing.containsLine(line);
        while (y < this.visibleRect.y + this.visibleRect.height) {
            if (paintingOutlineLine) {
                g.setColor(this.getForeground());
                final int w2 = width / 2;
                if (line == this.foldWithOutlineShowing.getEndLine()) {
                    final int y2 = y + cellHeight / 2;
                    g.drawLine(w2, y, w2, y2);
                    g.drawLine(w2, y2, width - 2, y2);
                    paintingOutlineLine = false;
                }
                else {
                    g.drawLine(w2, y, w2, y + cellHeight);
                }
            }
            Fold fold = fm.getFoldForLine(line);
            if (fold != null) {
                if (fold == this.foldWithOutlineShowing && !fold.isCollapsed()) {
                    g.setColor(this.getForeground());
                    final int w2 = width / 2;
                    g.drawLine(w2, y + cellHeight / 2, w2, y + cellHeight);
                    paintingOutlineLine = true;
                }
                if (fold.isCollapsed()) {
                    this.collapsedFoldIcon.paintIcon(this, g, x, y);
                    do {
                        final int hiddenLineCount = fold.getLineCount();
                        if (hiddenLineCount == 0) {
                            break;
                        }
                        line += hiddenLineCount;
                        fold = fm.getFoldForLine(line);
                        if (fold != null) {
                            continue;
                        }
                        break;
                    } while (fold.isCollapsed());
                }
                else {
                    this.expandedFoldIcon.paintIcon(this, g, x, y);
                }
            }
            ++line;
            y += cellHeight;
        }
    }
    
    private void paintComponentWrapped(final Graphics g) {
        final int width = this.getWidth();
        final RTextAreaUI ui = (RTextAreaUI)this.textArea.getUI();
        final View v = ui.getRootView(this.textArea).getView(0);
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int topPosition = this.textArea.viewToModel(new Point(this.visibleRect.x, this.visibleRect.y));
        final int topLine = root.getElementIndex(topPosition);
        final int cellHeight = this.textArea.getLineHeight();
        final FoldManager fm = ((RSyntaxTextArea)this.textArea).getFoldManager();
        final Rectangle visibleEditorRect = ui.getVisibleEditorRect();
        final Rectangle r = AbstractGutterComponent.getChildViewBounds(v, topLine, visibleEditorRect);
        int y = r.y;
        y += (cellHeight - this.collapsedFoldIcon.getIconHeight()) / 2;
        final int visibleBottom = this.visibleRect.y + this.visibleRect.height;
        final int x = width - 10;
        int line = topLine;
        boolean paintingOutlineLine = this.foldWithOutlineShowing != null && this.foldWithOutlineShowing.containsLine(line);
        final int lineCount = root.getElementCount();
        while (y < visibleBottom && line < lineCount) {
            final int curLineH = AbstractGutterComponent.getChildViewBounds(v, line, visibleEditorRect).height;
            if (paintingOutlineLine) {
                g.setColor(this.getForeground());
                final int w2 = width / 2;
                if (line == this.foldWithOutlineShowing.getEndLine()) {
                    final int y2 = y + curLineH - cellHeight / 2;
                    g.drawLine(w2, y, w2, y2);
                    g.drawLine(w2, y2, width - 2, y2);
                    paintingOutlineLine = false;
                }
                else {
                    g.drawLine(w2, y, w2, y + curLineH);
                }
            }
            final Fold fold = fm.getFoldForLine(line);
            if (fold != null) {
                if (fold == this.foldWithOutlineShowing && !fold.isCollapsed()) {
                    g.setColor(this.getForeground());
                    final int w2 = width / 2;
                    g.drawLine(w2, y + cellHeight / 2, w2, y + curLineH);
                    paintingOutlineLine = true;
                }
                if (fold.isCollapsed()) {
                    this.collapsedFoldIcon.paintIcon(this, g, x, y);
                    y += AbstractGutterComponent.getChildViewBounds(v, line, visibleEditorRect).height;
                    line += fold.getLineCount() + 1;
                }
                else {
                    this.expandedFoldIcon.paintIcon(this, g, x, y);
                    y += curLineH;
                    ++line;
                }
            }
            else {
                y += curLineH;
                ++line;
            }
        }
    }
    
    private int rowAtPoint(final Point p) {
        int line = 0;
        try {
            final int offs = this.textArea.viewToModel(p);
            if (offs > -1) {
                line = this.textArea.getLineOfOffset(offs);
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return line;
    }
    
    public void setFoldIconBackground(final Color bg) {
        this.foldIconBackground = bg;
    }
    
    public void setFoldIcons(final Icon collapsedIcon, final Icon expandedIcon) {
        this.collapsedFoldIcon = collapsedIcon;
        this.expandedFoldIcon = expandedIcon;
        this.revalidate();
        this.repaint();
    }
    
    public void setShowCollapsedRegionToolTips(final boolean show) {
        if (show != this.showFoldRegionTips) {
            if (show) {
                ToolTipManager.sharedInstance().registerComponent(this);
            }
            else {
                ToolTipManager.sharedInstance().unregisterComponent(this);
            }
            this.showFoldRegionTips = show;
        }
    }
    
    public void setTextArea(final RTextArea textArea) {
        if (this.textArea != null) {
            this.textArea.removePropertyChangeListener("RSTA.codeFolding", this.listener);
        }
        super.setTextArea(textArea);
        if (this.textArea != null) {
            this.textArea.addPropertyChangeListener("RSTA.codeFolding", this.listener);
        }
    }
    
    static /* synthetic */ Color access$000(final FoldIndicator x0) {
        return x0.foldIconBackground;
    }
    
    static /* synthetic */ int access$100(final FoldIndicator x0, final Point x1) {
        return x0.rowAtPoint(x1);
    }
    
    static /* synthetic */ Fold access$200(final FoldIndicator x0) {
        return x0.foldWithOutlineShowing;
    }
    
    static /* synthetic */ Fold access$202(final FoldIndicator x0, final Fold x1) {
        return x0.foldWithOutlineShowing = x1;
    }
    
    static /* synthetic */ Fold access$300(final FoldIndicator x0, final Point x1) {
        return x0.findOpenFoldClosestTo(x1);
    }
    
    static {
        DEFAULT_FOREGROUND = Color.gray;
        DEFAULT_FOLD_BACKGROUND = Color.white;
    }
    
    private class FoldIcon implements Icon
    {
        private boolean collapsed;
        
        public FoldIcon(final boolean collapsed) {
            super();
            this.collapsed = collapsed;
        }
        
        public int getIconHeight() {
            return 8;
        }
        
        public int getIconWidth() {
            return 8;
        }
        
        public void paintIcon(final Component c, final Graphics g, final int x, final int y) {
            g.setColor(FoldIndicator.access$000(FoldIndicator.this));
            g.fillRect(x, y, 8, 8);
            g.setColor(FoldIndicator.this.getForeground());
            g.drawRect(x, y, 8, 8);
            g.drawLine(x + 2, y + 4, x + 2 + 4, y + 4);
            if (this.collapsed) {
                g.drawLine(x + 4, y + 2, x + 4, y + 6);
            }
        }
    }
    
    private class Listener extends MouseInputAdapter implements PropertyChangeListener
    {
        public Listener(final FoldIndicator fgc) {
            super();
            fgc.addMouseListener(this);
            fgc.addMouseMotionListener(this);
        }
        
        public void mouseClicked(final MouseEvent e) {
            final Point p = e.getPoint();
            final int line = FoldIndicator.access$100(FoldIndicator.this, p);
            final RSyntaxTextArea rsta = (RSyntaxTextArea)FoldIndicator.this.textArea;
            final FoldManager fm = rsta.getFoldManager();
            final Fold fold = fm.getFoldForLine(line);
            if (fold != null) {
                fold.toggleCollapsedState();
                FoldIndicator.this.getGutter().repaint();
                FoldIndicator.this.textArea.repaint();
            }
        }
        
        public void mouseExited(final MouseEvent e) {
            if (FoldIndicator.access$200(FoldIndicator.this) != null) {
                FoldIndicator.access$202(FoldIndicator.this, null);
                FoldIndicator.this.repaint();
            }
        }
        
        public void mouseMoved(final MouseEvent e) {
            final Fold newSelectedFold = FoldIndicator.access$300(FoldIndicator.this, e.getPoint());
            if (newSelectedFold != FoldIndicator.access$200(FoldIndicator.this) && newSelectedFold != null && !newSelectedFold.isOnSingleLine()) {
                FoldIndicator.access$202(FoldIndicator.this, newSelectedFold);
                FoldIndicator.this.repaint();
            }
        }
        
        public void propertyChange(final PropertyChangeEvent e) {
            FoldIndicator.this.repaint();
        }
    }
}
