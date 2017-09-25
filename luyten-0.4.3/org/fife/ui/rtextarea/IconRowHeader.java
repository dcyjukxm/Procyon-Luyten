package org.fife.ui.rtextarea;

import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import java.util.*;
import javax.swing.*;

public class IconRowHeader extends AbstractGutterComponent implements MouseListener
{
    protected List<GutterIconImpl> trackingIcons;
    protected int width;
    private boolean bookmarkingEnabled;
    private Icon bookmarkIcon;
    protected Rectangle visibleRect;
    protected Insets textAreaInsets;
    protected int activeLineRangeStart;
    protected int activeLineRangeEnd;
    private Color activeLineRangeColor;
    private boolean inheritsGutterBackground;
    
    public IconRowHeader(final RTextArea textArea) {
        super(textArea);
    }
    
    public GutterIconInfo addOffsetTrackingIcon(final int offs, final Icon icon) throws BadLocationException {
        return this.addOffsetTrackingIcon(offs, icon, null);
    }
    
    public GutterIconInfo addOffsetTrackingIcon(final int offs, final Icon icon, final String tip) throws BadLocationException {
        final Position pos = this.textArea.getDocument().createPosition(offs);
        final GutterIconImpl ti = new GutterIconImpl(icon, pos, tip);
        if (this.trackingIcons == null) {
            this.trackingIcons = new ArrayList<GutterIconImpl>(1);
        }
        int index = Collections.binarySearch(this.trackingIcons, ti);
        if (index < 0) {
            index = -(index + 1);
        }
        this.trackingIcons.add(index, ti);
        this.repaint();
        return ti;
    }
    
    public void clearActiveLineRange() {
        if (this.activeLineRangeStart != -1 || this.activeLineRangeEnd != -1) {
            final int loc_0 = -1;
            this.activeLineRangeEnd = loc_0;
            this.activeLineRangeStart = loc_0;
            this.repaint();
        }
    }
    
    public Color getActiveLineRangeColor() {
        return this.activeLineRangeColor;
    }
    
    public Icon getBookmarkIcon() {
        return this.bookmarkIcon;
    }
    
    public GutterIconInfo[] getBookmarks() {
        final List<GutterIconInfo> retVal = new ArrayList<GutterIconInfo>(1);
        if (this.trackingIcons != null) {
            for (int i = 0; i < this.trackingIcons.size(); ++i) {
                final GutterIconImpl ti = this.getTrackingIcon(i);
                if (ti.getIcon() == this.bookmarkIcon) {
                    retVal.add(ti);
                }
            }
        }
        final GutterIconInfo[] array = new GutterIconInfo[retVal.size()];
        return retVal.toArray(array);
    }
    
    void handleDocumentEvent(final DocumentEvent e) {
        final int newLineCount = this.textArea.getLineCount();
        if (newLineCount != this.currentLineCount) {
            this.currentLineCount = newLineCount;
            this.repaint();
        }
    }
    
    public Dimension getPreferredSize() {
        final int h = (this.textArea != null) ? this.textArea.getHeight() : 100;
        return new Dimension(this.width, h);
    }
    
    public String getToolTipText(final MouseEvent e) {
        try {
            final int line = this.viewToModelLine(e.getPoint());
            if (line > -1) {
                final GutterIconInfo[] infos = this.getTrackingIcons(line);
                if (infos.length > 0) {
                    return infos[infos.length - 1].getToolTip();
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return null;
    }
    
    protected GutterIconImpl getTrackingIcon(final int index) {
        return this.trackingIcons.get(index);
    }
    
    public GutterIconInfo[] getTrackingIcons(final int line) throws BadLocationException {
        final List<GutterIconInfo> retVal = new ArrayList<GutterIconInfo>(1);
        if (this.trackingIcons != null) {
            final int start = this.textArea.getLineStartOffset(line);
            int end = this.textArea.getLineEndOffset(line);
            if (line == this.textArea.getLineCount() - 1) {
                ++end;
            }
            for (int i = 0; i < this.trackingIcons.size(); ++i) {
                final GutterIconImpl ti = this.getTrackingIcon(i);
                final int offs = ti.getMarkedOffset();
                if (offs >= start && offs < end) {
                    retVal.add(ti);
                }
                else if (offs >= end) {
                    break;
                }
            }
        }
        final GutterIconInfo[] array = new GutterIconInfo[retVal.size()];
        return retVal.toArray(array);
    }
    
    protected void init() {
        super.init();
        this.visibleRect = new Rectangle();
        this.width = 16;
        this.addMouseListener(this);
        final int loc_0 = -1;
        this.activeLineRangeEnd = loc_0;
        this.activeLineRangeStart = loc_0;
        this.setActiveLineRangeColor(null);
        this.updateBackground();
        ToolTipManager.sharedInstance().registerComponent(this);
    }
    
    public boolean isBookmarkingEnabled() {
        return this.bookmarkingEnabled;
    }
    
    void lineHeightsChanged() {
        this.repaint();
    }
    
    public void mouseClicked(final MouseEvent e) {
    }
    
    public void mouseEntered(final MouseEvent e) {
    }
    
    public void mouseExited(final MouseEvent e) {
    }
    
    public void mousePressed(final MouseEvent e) {
        if (this.bookmarkingEnabled && this.bookmarkIcon != null) {
            try {
                final int line = this.viewToModelLine(e.getPoint());
                if (line > -1) {
                    this.toggleBookmark(line);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
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
        this.paintBackgroundImpl(g, this.visibleRect);
        if (this.textArea.getLineWrap()) {
            this.paintComponentWrapped(g);
            return;
        }
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        this.textAreaInsets = this.textArea.getInsets(this.textAreaInsets);
        if (this.visibleRect.y < this.textAreaInsets.top) {
            final Rectangle loc_0 = this.visibleRect;
            loc_0.height -= this.textAreaInsets.top - this.visibleRect.y;
            this.visibleRect.y = this.textAreaInsets.top;
        }
        final int cellHeight = this.textArea.getLineHeight();
        final int topLine = (this.visibleRect.y - this.textAreaInsets.top) / cellHeight;
        final int bottomLine = Math.min(topLine + this.visibleRect.height / cellHeight + 1, root.getElementCount());
        final int y = topLine * cellHeight + this.textAreaInsets.top;
        if ((this.activeLineRangeStart >= topLine && this.activeLineRangeStart <= bottomLine) || (this.activeLineRangeEnd >= topLine && this.activeLineRangeEnd <= bottomLine) || (this.activeLineRangeStart <= topLine && this.activeLineRangeEnd >= bottomLine)) {
            g.setColor(this.activeLineRangeColor);
            final int firstLine = Math.max(this.activeLineRangeStart, topLine);
            final int y2 = firstLine * cellHeight + this.textAreaInsets.top;
            final int lastLine = Math.min(this.activeLineRangeEnd, bottomLine);
            final int y3 = (lastLine + 1) * cellHeight + this.textAreaInsets.top - 1;
            for (int j = y2; j <= y3; j += 2) {
                final int yEnd = Math.min(y3, j + this.getWidth());
                final int xEnd = yEnd - j;
                g.drawLine(0, j, xEnd, yEnd);
            }
            for (int i = 2; i < this.getWidth(); i += 2) {
                final int yEnd2 = y2 + this.getWidth() - i;
                g.drawLine(i, y2, this.getWidth(), yEnd2);
            }
            if (firstLine == this.activeLineRangeStart) {
                g.drawLine(0, y2, this.getWidth(), y2);
            }
            if (lastLine == this.activeLineRangeEnd) {
                g.drawLine(0, y3, this.getWidth(), y3);
            }
        }
        if (this.trackingIcons != null) {
            int lastLine2 = bottomLine;
            for (int k = this.trackingIcons.size() - 1; k >= 0; --k) {
                final GutterIconInfo ti = this.getTrackingIcon(k);
                final int offs = ti.getMarkedOffset();
                if (offs >= 0 && offs <= doc.getLength()) {
                    final int line = root.getElementIndex(offs);
                    if (line <= lastLine2 && line >= topLine) {
                        final Icon icon = ti.getIcon();
                        if (icon != null) {
                            int y4 = y + (line - topLine) * cellHeight;
                            y4 += (cellHeight - icon.getIconHeight()) / 2;
                            ti.getIcon().paintIcon(this, g, 0, y4);
                            lastLine2 = line - 1;
                        }
                    }
                    else if (line < topLine) {
                        break;
                    }
                }
            }
        }
    }
    
    protected void paintBackgroundImpl(final Graphics g, final Rectangle visibleRect) {
        Color bg = this.getBackground();
        if (this.inheritsGutterBackground && this.getGutter() != null) {
            bg = this.getGutter().getBackground();
        }
        g.setColor(bg);
        g.fillRect(0, visibleRect.y, this.width, visibleRect.height);
    }
    
    private void paintComponentWrapped(final Graphics g) {
        final RTextAreaUI ui = (RTextAreaUI)this.textArea.getUI();
        final View v = ui.getRootView(this.textArea).getView(0);
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int lineCount = root.getElementCount();
        final int topPosition = this.textArea.viewToModel(new Point(this.visibleRect.x, this.visibleRect.y));
        int topLine = root.getElementIndex(topPosition);
        final Rectangle visibleEditorRect = ui.getVisibleEditorRect();
        Rectangle r = AbstractGutterComponent.getChildViewBounds(v, topLine, visibleEditorRect);
        int y = r.y;
        final int visibleBottom = this.visibleRect.y + this.visibleRect.height;
        int currentIcon = -1;
        if (this.trackingIcons != null) {
            for (int i = 0; i < this.trackingIcons.size(); ++i) {
                final GutterIconImpl icon = this.getTrackingIcon(i);
                final int offs = icon.getMarkedOffset();
                if (offs >= 0 && offs <= doc.getLength()) {
                    final int line = root.getElementIndex(offs);
                    if (line >= topLine) {
                        currentIcon = i;
                        break;
                    }
                }
            }
        }
        g.setColor(this.getForeground());
        final int cellHeight = this.textArea.getLineHeight();
        while (y < visibleBottom) {
            r = AbstractGutterComponent.getChildViewBounds(v, topLine, visibleEditorRect);
            if (currentIcon > -1) {
                GutterIconImpl toPaint = null;
                while (currentIcon < this.trackingIcons.size()) {
                    final GutterIconImpl ti = this.getTrackingIcon(currentIcon);
                    final int offs2 = ti.getMarkedOffset();
                    if (offs2 >= 0 && offs2 <= doc.getLength()) {
                        final int line2 = root.getElementIndex(offs2);
                        if (line2 == topLine) {
                            toPaint = ti;
                        }
                        else if (line2 > topLine) {
                            break;
                        }
                    }
                    ++currentIcon;
                }
                if (toPaint != null) {
                    final Icon icon2 = toPaint.getIcon();
                    if (icon2 != null) {
                        final int y2 = y + (cellHeight - icon2.getIconHeight()) / 2;
                        icon2.paintIcon(this, g, 0, y2);
                    }
                }
            }
            y += r.height;
            if (++topLine >= lineCount) {
                break;
            }
        }
    }
    
    public void removeTrackingIcon(final Object tag) {
        if (this.trackingIcons != null && this.trackingIcons.remove(tag)) {
            this.repaint();
        }
    }
    
    public void removeAllTrackingIcons() {
        if (this.trackingIcons != null && this.trackingIcons.size() > 0) {
            this.trackingIcons.clear();
            this.repaint();
        }
    }
    
    private void removeBookmarkTrackingIcons() {
        if (this.trackingIcons != null) {
            final Iterator<GutterIconImpl> i = this.trackingIcons.iterator();
            while (i.hasNext()) {
                final GutterIconImpl ti = i.next();
                if (ti.getIcon() == this.bookmarkIcon) {
                    i.remove();
                }
            }
        }
    }
    
    public void setActiveLineRange(final int startLine, final int endLine) {
        if (startLine != this.activeLineRangeStart || endLine != this.activeLineRangeEnd) {
            this.activeLineRangeStart = startLine;
            this.activeLineRangeEnd = endLine;
            this.repaint();
        }
    }
    
    public void setActiveLineRangeColor(Color color) {
        if (color == null) {
            color = Gutter.DEFAULT_ACTIVE_LINE_RANGE_COLOR;
        }
        if (!color.equals(this.activeLineRangeColor)) {
            this.activeLineRangeColor = color;
            this.repaint();
        }
    }
    
    public void setBookmarkIcon(final Icon icon) {
        this.removeBookmarkTrackingIcons();
        this.bookmarkIcon = icon;
        this.repaint();
    }
    
    public void setBookmarkingEnabled(final boolean enabled) {
        if (enabled != this.bookmarkingEnabled) {
            if (!(this.bookmarkingEnabled = enabled)) {
                this.removeBookmarkTrackingIcons();
            }
            this.repaint();
        }
    }
    
    public void setInheritsGutterBackground(final boolean inherits) {
        if (inherits != this.inheritsGutterBackground) {
            this.inheritsGutterBackground = inherits;
            this.repaint();
        }
    }
    
    public void setTextArea(final RTextArea textArea) {
        this.removeAllTrackingIcons();
        super.setTextArea(textArea);
    }
    
    public boolean toggleBookmark(final int line) throws BadLocationException {
        if (!this.isBookmarkingEnabled() || this.getBookmarkIcon() == null) {
            return false;
        }
        final GutterIconInfo[] icons = this.getTrackingIcons(line);
        if (icons.length == 0) {
            final int offs = this.textArea.getLineStartOffset(line);
            this.addOffsetTrackingIcon(offs, this.bookmarkIcon);
            return true;
        }
        boolean found = false;
        for (int i = 0; i < icons.length; ++i) {
            if (icons[i].getIcon() == this.bookmarkIcon) {
                this.removeTrackingIcon(icons[i]);
                found = true;
            }
        }
        if (!found) {
            final int offs2 = this.textArea.getLineStartOffset(line);
            this.addOffsetTrackingIcon(offs2, this.bookmarkIcon);
        }
        return !found;
    }
    
    private void updateBackground() {
        Color bg = UIManager.getColor("Panel.background");
        if (bg == null) {
            bg = new JPanel().getBackground();
        }
        this.setBackground(bg);
    }
    
    public void updateUI() {
        super.updateUI();
        this.updateBackground();
    }
    
    private int viewToModelLine(final Point p) throws BadLocationException {
        final int offs = this.textArea.viewToModel(p);
        return (offs > -1) ? this.textArea.getLineOfOffset(offs) : -1;
    }
    
    private static class GutterIconImpl implements GutterIconInfo, Comparable<GutterIconInfo>
    {
        private Icon icon;
        private Position pos;
        private String toolTip;
        
        public GutterIconImpl(final Icon icon, final Position pos, final String toolTip) {
            super();
            this.icon = icon;
            this.pos = pos;
            this.toolTip = toolTip;
        }
        
        public int compareTo(final GutterIconInfo other) {
            if (other != null) {
                return this.pos.getOffset() - other.getMarkedOffset();
            }
            return -1;
        }
        
        public boolean equals(final Object o) {
            return o == this;
        }
        
        public Icon getIcon() {
            return this.icon;
        }
        
        public int getMarkedOffset() {
            return this.pos.getOffset();
        }
        
        public String getToolTip() {
            return this.toolTip;
        }
        
        public int hashCode() {
            return this.icon.hashCode();
        }
    }
}
