package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rsyntaxtextarea.folding.*;
import javax.swing.text.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;
import java.awt.*;

public class FoldingAwareIconRowHeader extends IconRowHeader
{
    public FoldingAwareIconRowHeader(final RSyntaxTextArea textArea) {
        super(textArea);
    }
    
    protected void paintComponent(final Graphics g) {
        if (this.textArea == null) {
            return;
        }
        final RSyntaxTextArea rsta = (RSyntaxTextArea)this.textArea;
        final FoldManager fm = rsta.getFoldManager();
        if (!fm.isCodeFoldingSupportedAndEnabled()) {
            super.paintComponent(g);
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
        int topLine = (this.visibleRect.y - this.textAreaInsets.top) / cellHeight;
        final int y = topLine * cellHeight + this.textAreaInsets.top;
        topLine += fm.getHiddenLineCountAbove(topLine, true);
        if (this.activeLineRangeStart > -1 && this.activeLineRangeEnd > -1) {
            final Color activeLineRangeColor = this.getActiveLineRangeColor();
            g.setColor(activeLineRangeColor);
            try {
                final int realY1 = rsta.yForLine(this.activeLineRangeStart);
                if (realY1 > -1) {
                    int y2 = realY1;
                    int y3 = rsta.yForLine(this.activeLineRangeEnd);
                    if (y3 == -1) {
                        y3 = y2;
                    }
                    y3 += cellHeight - 1;
                    if (y3 < this.visibleRect.y || y2 > this.visibleRect.y + this.visibleRect.height) {
                        return;
                    }
                    y2 = Math.max(y, realY1);
                    y3 = Math.min(y3, this.visibleRect.y + this.visibleRect.height);
                    for (int j = y2; j <= y3; j += 2) {
                        final int yEnd = Math.min(y3, j + this.getWidth());
                        final int xEnd = yEnd - j;
                        g.drawLine(0, j, xEnd, yEnd);
                    }
                    for (int i = 2; i < this.getWidth(); i += 2) {
                        final int yEnd2 = y2 + this.getWidth() - i;
                        g.drawLine(i, y2, this.getWidth(), yEnd2);
                    }
                    if (realY1 >= y && realY1 < this.visibleRect.y + this.visibleRect.height) {
                        g.drawLine(0, realY1, this.getWidth(), realY1);
                    }
                    if (y3 >= y && y3 < this.visibleRect.y + this.visibleRect.height) {
                        g.drawLine(0, y3, this.getWidth(), y3);
                    }
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        if (this.trackingIcons != null) {
            int lastLine = this.textArea.getLineCount() - 1;
            for (int k = this.trackingIcons.size() - 1; k >= 0; --k) {
                final GutterIconInfo ti = this.getTrackingIcon(k);
                final int offs = ti.getMarkedOffset();
                if (offs >= 0 && offs <= doc.getLength()) {
                    final int line = root.getElementIndex(offs);
                    if (line <= lastLine && line >= topLine) {
                        try {
                            final Icon icon = ti.getIcon();
                            if (icon != null) {
                                final int lineY = rsta.yForLine(line);
                                if (lineY >= y && lineY <= this.visibleRect.y + this.visibleRect.height) {
                                    final int y4 = lineY + (cellHeight - icon.getIconHeight()) / 2;
                                    icon.paintIcon(this, g, 0, y4);
                                    lastLine = line - 1;
                                }
                            }
                        }
                        catch (BadLocationException ble2) {
                            ble2.printStackTrace();
                        }
                    }
                    else if (line < topLine) {
                        break;
                    }
                }
            }
        }
    }
    
    private void paintComponentWrapped(final Graphics g) {
        final RSyntaxTextArea rsta = (RSyntaxTextArea)this.textArea;
        final Document doc = this.textArea.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int topPosition = this.textArea.viewToModel(new Point(this.visibleRect.x, this.visibleRect.y));
        final int topLine = root.getElementIndex(topPosition);
        final int topY = this.visibleRect.y;
        final int bottomY = this.visibleRect.y + this.visibleRect.height;
        final int cellHeight = this.textArea.getLineHeight();
        if (this.trackingIcons != null) {
            int lastLine = this.textArea.getLineCount() - 1;
            for (int i = this.trackingIcons.size() - 1; i >= 0; --i) {
                final GutterIconInfo ti = this.getTrackingIcon(i);
                final Icon icon = ti.getIcon();
                if (icon != null) {
                    final int iconH = icon.getIconHeight();
                    final int offs = ti.getMarkedOffset();
                    if (offs >= 0 && offs <= doc.getLength()) {
                        final int line = root.getElementIndex(offs);
                        if (line <= lastLine && line >= topLine) {
                            try {
                                final int lineY = rsta.yForLine(line);
                                if (lineY <= bottomY && lineY + iconH >= topY) {
                                    final int y2 = lineY + (cellHeight - iconH) / 2;
                                    ti.getIcon().paintIcon(this, g, 0, y2);
                                    lastLine = line - 1;
                                }
                            }
                            catch (BadLocationException ble) {
                                ble.printStackTrace();
                            }
                        }
                        else if (line < topLine) {
                            break;
                        }
                    }
                }
            }
        }
    }
}
