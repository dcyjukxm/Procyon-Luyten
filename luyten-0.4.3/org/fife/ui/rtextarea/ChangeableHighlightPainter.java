package org.fife.ui.rtextarea;

import javax.swing.plaf.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.*;

public class ChangeableHighlightPainter extends LayeredHighlighter.LayerPainter implements Serializable
{
    private Paint paint;
    private boolean roundedEdges;
    private transient AlphaComposite alphaComposite;
    private float alpha;
    private static final int ARCWIDTH = 8;
    private static final int ARCHEIGHT = 8;
    
    public ChangeableHighlightPainter() {
        this(null);
    }
    
    public ChangeableHighlightPainter(final Paint paint) {
        this(paint, false);
    }
    
    public ChangeableHighlightPainter(final Paint paint, final boolean rounded) {
        this(paint, rounded, 1.0f);
    }
    
    public ChangeableHighlightPainter(final Paint paint, final boolean rounded, final float alpha) {
        super();
        this.setPaint(paint);
        this.setRoundedEdges(rounded);
        this.setAlpha(alpha);
    }
    
    public float getAlpha() {
        return this.alpha;
    }
    
    private AlphaComposite getAlphaComposite() {
        if (this.alphaComposite == null) {
            this.alphaComposite = AlphaComposite.getInstance(3, this.alpha);
        }
        return this.alphaComposite;
    }
    
    public Paint getPaint() {
        return this.paint;
    }
    
    public boolean getRoundedEdges() {
        return this.roundedEdges;
    }
    
    public void paint(final Graphics g, final int offs0, final int offs1, final Shape bounds, final JTextComponent c) {
        final Rectangle alloc = bounds.getBounds();
        final Graphics2D g2d = (Graphics2D)g;
        Composite originalComposite = null;
        if (this.getAlpha() < 1.0f) {
            originalComposite = g2d.getComposite();
            g2d.setComposite(this.getAlphaComposite());
        }
        try {
            final TextUI mapper = c.getUI();
            final Rectangle p0 = mapper.modelToView(c, offs0);
            final Rectangle p = mapper.modelToView(c, offs1);
            final Paint paint = this.getPaint();
            if (paint == null) {
                g2d.setColor(c.getSelectionColor());
            }
            else {
                g2d.setPaint(paint);
            }
            if (p0.y == p.y) {
                final Rectangle r = p0.union(p);
                g2d.fillRect(r.x, r.y, r.width, r.height);
            }
            else {
                final int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                g2d.fillRect(p0.x, p0.y, p0ToMarginWidth, p0.height);
                if (p0.y + p0.height != p.y) {
                    g2d.fillRect(alloc.x, p0.y + p0.height, alloc.width, p.y - (p0.y + p0.height));
                }
                g2d.fillRect(alloc.x, p.y, p.x - alloc.x, p.height);
            }
        }
        catch (BadLocationException e) {
            e.printStackTrace();
        }
        finally {
            if (this.getAlpha() < 1.0f) {
                g2d.setComposite(originalComposite);
            }
        }
    }
    
    public Shape paintLayer(final Graphics g, final int offs0, final int offs1, final Shape bounds, final JTextComponent c, final View view) {
        final Graphics2D g2d = (Graphics2D)g;
        Composite originalComposite = null;
        if (this.getAlpha() < 1.0f) {
            originalComposite = g2d.getComposite();
            g2d.setComposite(this.getAlphaComposite());
        }
        final Paint paint = this.getPaint();
        if (paint == null) {
            g2d.setColor(c.getSelectionColor());
        }
        else {
            g2d.setPaint(paint);
        }
        if (offs0 == offs1) {
            try {
                final Shape s = view.modelToView(offs0, bounds, Position.Bias.Forward);
                final Rectangle r = s.getBounds();
                g.drawLine(r.x, r.y, r.x, r.y + r.height);
                return r;
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                return null;
            }
        }
        if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
            Rectangle alloc;
            if (bounds instanceof Rectangle) {
                alloc = (Rectangle)bounds;
            }
            else {
                alloc = bounds.getBounds();
            }
            g2d.fillRect(alloc.x, alloc.y, alloc.width, alloc.height);
            if (this.getAlpha() < 1.0f) {
                g2d.setComposite(originalComposite);
            }
            return alloc;
        }
        try {
            final Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
            final Rectangle r = (Rectangle)((shape instanceof Rectangle) ? shape : shape.getBounds());
            if (this.roundedEdges) {
                g2d.fillRoundRect(r.x, r.y, r.width, r.height, 8, 8);
            }
            else {
                g2d.fillRect(r.x, r.y, r.width, r.height);
            }
            if (this.getAlpha() < 1.0f) {
                g2d.setComposite(originalComposite);
            }
            return r;
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        finally {
            if (this.getAlpha() < 1.0f) {
                g2d.setComposite(originalComposite);
            }
        }
        return null;
    }
    
    private void readObject(final ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        final int rgb = s.readInt();
        this.paint = ((rgb == -1) ? null : new Color(rgb));
        this.alphaComposite = null;
    }
    
    public void setAlpha(final float alpha) {
        this.alpha = alpha;
        this.alpha = Math.max(alpha, 0.0f);
        this.alpha = Math.min(1.0f, alpha);
        this.alphaComposite = null;
    }
    
    public void setPaint(final Paint paint) {
        this.paint = paint;
    }
    
    public void setRoundedEdges(final boolean rounded) {
        this.roundedEdges = rounded;
    }
    
    private void writeObject(final ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int rgb = -1;
        if (this.paint != null) {
            final Color c = (this.paint instanceof Color) ? ((Color)this.paint) : SystemColor.textHighlight;
            rgb = c.getRGB();
        }
        s.writeInt(rgb);
    }
}
