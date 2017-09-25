package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import java.awt.*;
import javax.swing.text.*;

public class SquiggleUnderlineHighlightPainter extends ChangeableHighlightPainter
{
    private static final int AMT = 2;
    
    public SquiggleUnderlineHighlightPainter(final Color color) {
        super(color);
        this.setPaint(color);
    }
    
    public Shape paintLayer(final Graphics g, final int offs0, final int offs1, final Shape bounds, final JTextComponent c, final View view) {
        g.setColor((Color)this.getPaint());
        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            Rectangle alloc;
            if (bounds instanceof Rectangle) {
                alloc = (Rectangle)bounds;
            }
            else {
                alloc = bounds.getBounds();
            }
            this.paintSquiggle(g, alloc);
            return alloc;
        }
        try {
            final Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
            final Rectangle r = (Rectangle)((shape instanceof Rectangle) ? shape : shape.getBounds());
            this.paintSquiggle(g, r);
            return r;
        }
        catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    protected void paintSquiggle(final Graphics g, final Rectangle r) {
        int x = r.x;
        int y = r.y + r.height - 2;
        int delta = -2;
        while (x < r.x + r.width) {
            g.drawLine(x, y, x + 2, y + delta);
            y += delta;
            delta = -delta;
            x += 2;
        }
    }
}
