package org.fife.ui.rtextarea;

import java.util.*;
import java.awt.*;
import javax.swing.text.*;

class LineHighlightManager
{
    private RTextArea textArea;
    private List<LineHighlightInfo> lineHighlights;
    
    public LineHighlightManager(final RTextArea textArea) {
        super();
        this.textArea = textArea;
    }
    
    public Object addLineHighlight(final int line, final Color color) throws BadLocationException {
        final int offs = this.textArea.getLineStartOffset(line);
        final LineHighlightInfo lhi = new LineHighlightInfo(this.textArea.getDocument().createPosition(offs), color);
        if (this.lineHighlights == null) {
            this.lineHighlights = new ArrayList<LineHighlightInfo>(1);
        }
        int index = Collections.binarySearch(this.lineHighlights, lhi);
        if (index < 0) {
            index = -(index + 1);
        }
        this.lineHighlights.add(index, lhi);
        this.repaintLine(lhi);
        return lhi;
    }
    
    public void paintLineHighlights(final Graphics g) {
        final int count = (this.lineHighlights == null) ? 0 : this.lineHighlights.size();
        if (count > 0) {
            final int docLen = this.textArea.getDocument().getLength();
            final Rectangle vr = this.textArea.getVisibleRect();
            final int lineHeight = this.textArea.getLineHeight();
            try {
                for (int i = 0; i < count; ++i) {
                    final LineHighlightInfo lhi = this.lineHighlights.get(i);
                    final int offs = lhi.getOffset();
                    if (offs >= 0 && offs <= docLen) {
                        final int y = this.textArea.yForLineContaining(offs);
                        if (y > vr.y - lineHeight) {
                            if (y >= vr.y + vr.height) {
                                break;
                            }
                            g.setColor(lhi.getColor());
                            g.fillRect(0, y, this.textArea.getWidth(), lineHeight);
                        }
                    }
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }
    
    public void removeAllLineHighlights() {
        if (this.lineHighlights != null) {
            this.lineHighlights.clear();
            this.textArea.repaint();
        }
    }
    
    public void removeLineHighlight(final Object tag) {
        if (tag instanceof LineHighlightInfo) {
            this.lineHighlights.remove(tag);
            this.repaintLine((LineHighlightInfo)tag);
        }
    }
    
    private void repaintLine(final LineHighlightInfo lhi) {
        final int offs = lhi.getOffset();
        if (offs >= 0 && offs <= this.textArea.getDocument().getLength()) {
            try {
                final int y = this.textArea.yForLineContaining(offs);
                if (y > -1) {
                    this.textArea.repaint(0, y, this.textArea.getWidth(), this.textArea.getLineHeight());
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }
    
    private static class LineHighlightInfo implements Comparable<LineHighlightInfo>
    {
        private Position offs;
        private Color color;
        
        public LineHighlightInfo(final Position offs, final Color c) {
            super();
            this.offs = offs;
            this.color = c;
        }
        
        public int compareTo(final LineHighlightInfo o) {
            if (o != null) {
                return this.offs.getOffset() - o.getOffset();
            }
            return -1;
        }
        
        public boolean equals(final Object o) {
            return o == this || (o instanceof LineHighlightInfo && this.offs.getOffset() == ((LineHighlightInfo)o).getOffset());
        }
        
        public Color getColor() {
            return this.color;
        }
        
        public int getOffset() {
            return this.offs.getOffset();
        }
        
        public int hashCode() {
            return this.getOffset();
        }
    }
}
