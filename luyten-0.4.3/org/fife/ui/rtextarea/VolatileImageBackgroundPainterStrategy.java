package org.fife.ui.rtextarea;

import java.awt.*;
import java.awt.image.*;

public class VolatileImageBackgroundPainterStrategy extends ImageBackgroundPainterStrategy
{
    private VolatileImage bgImage;
    
    public VolatileImageBackgroundPainterStrategy(final RTextAreaBase ta) {
        super(ta);
    }
    
    protected void paintImage(final Graphics g, final int x, final int y) {
        if (this.bgImage != null) {
            do {
                final int rc = this.bgImage.validate(null);
                if (rc == 1) {
                    this.renderImage(this.bgImage.getWidth(), this.bgImage.getHeight(), this.getScalingHint());
                }
                g.drawImage(this.bgImage, x, y, null);
            } while (this.bgImage.contentsLost());
        }
    }
    
    private void renderImage(final int width, final int height, final int hint) {
        final Image master = this.getMasterImage();
        if (master != null) {
            do {
                final Image i = master.getScaledInstance(width, height, hint);
                this.tracker.addImage(i, 1);
                try {
                    this.tracker.waitForID(1);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    this.bgImage = null;
                    return;
                }
                finally {
                    this.tracker.removeImage(i, 1);
                }
                this.bgImage.getGraphics().drawImage(i, 0, 0, null);
                this.tracker.addImage(this.bgImage, 0);
                try {
                    this.tracker.waitForID(0);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    this.bgImage = null;
                }
                finally {
                    this.tracker.removeImage(this.bgImage, 0);
                }
            } while (this.bgImage.contentsLost());
        }
        else {
            this.bgImage = null;
        }
    }
    
    protected void rescaleImage(final int width, final int height, final int hint) {
        this.bgImage = this.getRTextAreaBase().createVolatileImage(width, height);
        if (this.bgImage != null) {
            this.renderImage(width, height, hint);
        }
    }
}
