package org.fife.ui.rtextarea;

import java.net.*;
import javax.swing.*;
import javax.imageio.*;
import java.awt.*;
import java.security.*;
import java.io.*;
import java.awt.image.*;

public class IconGroup
{
    private String path;
    private boolean separateLargeIcons;
    private String largeIconSubDir;
    private String extension;
    private String name;
    private String jarFile;
    private static final String DEFAULT_EXTENSION = "gif";
    
    public IconGroup(final String name, final String path) {
        this(name, path, null);
    }
    
    public IconGroup(final String name, final String path, final String largeIconSubDir) {
        this(name, path, largeIconSubDir, "gif");
    }
    
    public IconGroup(final String name, final String path, final String largeIconSubDir, final String extension) {
        this(name, path, largeIconSubDir, extension, null);
    }
    
    public IconGroup(final String name, final String path, final String largeIconSubDir, final String extension, final String jar) {
        super();
        this.name = name;
        this.path = path;
        if (path != null && path.length() > 0 && !path.endsWith("/")) {
            this.path += "/";
        }
        this.separateLargeIcons = (largeIconSubDir != null);
        this.largeIconSubDir = largeIconSubDir;
        this.extension = ((extension != null) ? extension : "gif");
        this.jarFile = jar;
    }
    
    public boolean equals(final Object o2) {
        if (o2 != null && o2 instanceof IconGroup) {
            final IconGroup ig2 = (IconGroup)o2;
            if (ig2.getName().equals(this.getName()) && this.separateLargeIcons == ig2.hasSeparateLargeIcons()) {
                return (!this.separateLargeIcons || this.largeIconSubDir.equals(ig2.largeIconSubDir)) && this.path.equals(ig2.path);
            }
        }
        return false;
    }
    
    public Icon getIcon(final String name) {
        Icon icon = this.getIconImpl(this.path + name + "." + this.extension);
        if (icon != null && (icon.getIconWidth() < 1 || icon.getIconHeight() < 1)) {
            icon = null;
        }
        return icon;
    }
    
    private Icon getIconImpl(final String iconFullPath) {
        try {
            if (this.jarFile != null) {
                final URL url = new URL("jar:file:///" + this.jarFile + "!/" + iconFullPath);
                return new ImageIcon(url);
            }
            final URL url = this.getClass().getClassLoader().getResource(iconFullPath);
            if (url != null) {
                return new ImageIcon(url);
            }
            final BufferedImage image = ImageIO.read(new File(iconFullPath));
            return (image != null) ? new ImageIcon(image) : null;
        }
        catch (AccessControlException ace) {
            return null;
        }
        catch (IOException ioe) {
            return null;
        }
    }
    
    public Icon getLargeIcon(final String name) {
        return this.getIconImpl(this.path + this.largeIconSubDir + "/" + name + "." + this.extension);
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean hasSeparateLargeIcons() {
        return this.separateLargeIcons;
    }
    
    public int hashCode() {
        return this.getName().hashCode();
    }
}
