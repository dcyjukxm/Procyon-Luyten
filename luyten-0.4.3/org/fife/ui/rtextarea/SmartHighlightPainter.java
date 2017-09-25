package org.fife.ui.rtextarea;

import javax.swing.text.*;
import java.awt.*;

public class SmartHighlightPainter extends ChangeableHighlightPainter
{
    private Color borderColor;
    private boolean paintBorder;
    
    public SmartHighlightPainter() {
        super(Color.BLUE);
    }
    
    public SmartHighlightPainter(final Paint paint) {
        super(paint);
    }
    
    public boolean getPaintBorder() {
        return this.paintBorder;
    }
    
    public Shape paintLayer(final Graphics g, final int p0, final int p1, final Shape viewBounds, final JTextComponent c, final View view) {
        g.setColor((Color)this.getPaint());
        if (p0 == p1) {
            try {
                final Shape s = view.modelToView(p0, viewBounds, Position.Bias.Forward);
                final Rectangle r = s.getBounds();
                g.drawLine(r.x, r.y, r.x, r.y + r.height);
                return r;
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                return null;
            }
        }
        if (p0 == view.getStartOffset() && p1 == view.getEndOffset()) {
            Rectangle alloc;
            if (viewBounds instanceof Rectangle) {
                alloc = (Rectangle)viewBounds;
            }
            else {
                alloc = viewBounds.getBounds();
            }
            g.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
            return alloc;
        }
        try {
            final Shape shape = view.modelToView(p0, Position.Bias.Forward, p1, Position.Bias.Backward, viewBounds);
            final Rectangle r = (Rectangle)((shape instanceof Rectangle) ? shape : shape.getBounds());
            g.fillRect(r.x, r.y, r.width, r.height);
            if (this.paintBorder) {
                g.setColor(this.borderColor);
                g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
            }
            return r;
        }
        catch (BadLocationException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void setPaint(final Paint paint) {
        super.setPaint(paint);
        if (paint instanceof Color) {
            this.borderColor = ((Color)paint).darker();
        }
    }
    
    public void setPaintBorder(final boolean paint) {
        this.paintBorder = paint;
    }
}
