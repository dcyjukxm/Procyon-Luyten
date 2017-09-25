package org.fife.ui.rtextarea;

import java.awt.image.*;
import java.util.*;
import java.awt.*;

public class BufferedImageBackgroundPainterStrategy extends ImageBackgroundPainterStrategy
{
    private BufferedImage bgImage;
    
    public BufferedImageBackgroundPainterStrategy(final RTextAreaBase ta) {
        super(ta);
    }
    
    protected void paintImage(final Graphics g, final int x, final int y) {
        if (this.bgImage != null) {
            g.drawImage(this.bgImage, x, y, null);
        }
    }
    
    protected void rescaleImage(final int width, final int height, final int hint) {
        final Image master = this.getMasterImage();
        if (master != null) {
            final Map<RenderingHints.Key, Object> hints = new HashMap<RenderingHints.Key, Object>();
            switch (hint) {
                default: {
                    hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    this.bgImage = this.createAcceleratedImage(width, height);
                    final Graphics2D g = this.bgImage.createGraphics();
                    g.addRenderingHints(hints);
                    g.drawImage(master, 0, 0, width, height, null);
                    g.dispose();
                    break;
                }
            }
        }
        else {
            this.bgImage = null;
        }
    }
    
    private BufferedImage createAcceleratedImage(final int width, final int height) {
        final GraphicsConfiguration gc = this.getRTextAreaBase().getGraphicsConfiguration();
        final BufferedImage image = gc.createCompatibleImage(width, height);
        return image;
    }
}
