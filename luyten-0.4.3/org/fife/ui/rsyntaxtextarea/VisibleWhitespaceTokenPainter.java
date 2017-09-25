package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;
import java.awt.*;

class VisibleWhitespaceTokenPainter extends DefaultTokenPainter
{
    protected float paintImpl(final Token token, final Graphics2D g, float x, final float y, final RSyntaxTextArea host, final TabExpander e, final float clipStart, final boolean selected) {
        final int origX = (int)x;
        final int textOffs = token.getTextOffset();
        final char[] text = token.getTextArray();
        final int end = textOffs + token.length();
        float nextX = x;
        int flushLen = 0;
        int flushIndex = textOffs;
        Color fg;
        Color bg;
        if (selected) {
            fg = host.getSelectedTextColor();
            bg = null;
        }
        else {
            fg = host.getForegroundForToken(token);
            bg = host.getBackgroundForToken(token);
        }
        g.setFont(host.getFontForTokenType(token.getType()));
        final FontMetrics fm = host.getFontMetricsForTokenType(token.getType());
        final int ascent = fm.getAscent();
        final int height = fm.getHeight();
        for (int i = textOffs; i < end; ++i) {
            switch (text[i]) {
                case '\t': {
                    nextX = x + fm.charsWidth(text, flushIndex, flushLen);
                    final float nextNextX = e.nextTabStop(nextX, 0);
                    if (bg != null) {
                        this.paintBackground(x, y, nextNextX - x, height, g, ascent, host, bg, !selected);
                    }
                    g.setColor(fg);
                    if (flushLen > 0) {
                        g.drawChars(text, flushIndex, flushLen, (int)x, (int)y);
                        flushLen = 0;
                    }
                    flushIndex = i + 1;
                    final int halfHeight = height / 2;
                    final int quarterHeight = halfHeight / 2;
                    final int ymid = (int)y - ascent + halfHeight;
                    g.drawLine((int)nextX, ymid, (int)nextNextX, ymid);
                    g.drawLine((int)nextNextX, ymid, (int)nextNextX - 4, ymid - quarterHeight);
                    g.drawLine((int)nextNextX, ymid, (int)nextNextX - 4, ymid + quarterHeight);
                    x = nextNextX;
                    break;
                }
                case ' ': {
                    nextX = x + fm.charsWidth(text, flushIndex, flushLen + 1);
                    final int width = fm.charWidth(' ');
                    if (bg != null) {
                        this.paintBackground(x, y, nextX - x, height, g, ascent, host, bg, !selected);
                    }
                    g.setColor(fg);
                    if (flushLen > 0) {
                        g.drawChars(text, flushIndex, flushLen, (int)x, (int)y);
                        flushLen = 0;
                    }
                    final int dotX = (int)(nextX - width / 2.0f);
                    final int dotY = (int)(y - ascent + height / 2.0f);
                    g.drawLine(dotX, dotY, dotX, dotY);
                    flushIndex = i + 1;
                    x = nextX;
                    break;
                }
                default: {
                    ++flushLen;
                    break;
                }
            }
        }
        nextX = x + fm.charsWidth(text, flushIndex, flushLen);
        if (flushLen > 0 && nextX >= clipStart) {
            if (bg != null) {
                this.paintBackground(x, y, nextX - x, height, g, ascent, host, bg, !selected);
            }
            g.setColor(fg);
            g.drawChars(text, flushIndex, flushLen, (int)x, (int)y);
        }
        if (host.getUnderlineForToken(token)) {
            g.setColor(fg);
            final int y2 = (int)(y + 1.0f);
            g.drawLine(origX, y2, (int)nextX, y2);
        }
        if (host.getPaintTabLines() && origX == host.getMargin().left) {
            this.paintTabLines(token, origX, (int)y, (int)nextX, g, e, host);
        }
        return nextX;
    }
}
