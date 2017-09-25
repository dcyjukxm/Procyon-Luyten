package us.deathmarine.luyten;

import javax.swing.*;
import java.awt.*;

public class WindowPosition
{
    private boolean isFullScreen;
    private int windowWidth;
    private int windowHeight;
    private int windowX;
    private int windowY;
    
    public void readPositionFromWindow(final JFrame window) {
        if (!(this.isFullScreen = (window.getExtendedState() == 6))) {
            this.readPositionFromComponent(window);
        }
    }
    
    public void readPositionFromDialog(final JDialog dialog) {
        this.readPositionFromComponent(dialog);
    }
    
    private void readPositionFromComponent(final Component component) {
        this.isFullScreen = false;
        this.windowWidth = component.getWidth();
        this.windowHeight = component.getHeight();
        this.windowX = component.getX();
        this.windowY = component.getY();
    }
    
    public boolean isSavedWindowPositionValid() {
        if (this.isFullScreen) {
            return true;
        }
        if (this.windowWidth < 100 || this.windowHeight < 100) {
            return false;
        }
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return this.windowWidth <= screenSize.width + 50 && this.windowHeight <= screenSize.height + 50 && this.windowY >= -20 && this.windowY <= screenSize.height - 50 && this.windowX >= 50 - this.windowWidth && this.windowX <= screenSize.width - 50;
    }
    
    public boolean isFullScreen() {
        return this.isFullScreen;
    }
    
    public void setFullScreen(final boolean isFullScreen) {
        this.isFullScreen = isFullScreen;
    }
    
    public int getWindowWidth() {
        return this.windowWidth;
    }
    
    public void setWindowWidth(final int windowWidth) {
        this.windowWidth = windowWidth;
    }
    
    public int getWindowHeight() {
        return this.windowHeight;
    }
    
    public void setWindowHeight(final int windowHeight) {
        this.windowHeight = windowHeight;
    }
    
    public int getWindowX() {
        return this.windowX;
    }
    
    public void setWindowX(final int windowX) {
        this.windowX = windowX;
    }
    
    public int getWindowY() {
        return this.windowY;
    }
    
    public void setWindowY(final int windowY) {
        this.windowY = windowY;
    }
}
