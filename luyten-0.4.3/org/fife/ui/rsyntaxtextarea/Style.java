package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import javax.swing.*;

public class Style implements Cloneable
{
    public static final Color DEFAULT_FOREGROUND;
    public static final Color DEFAULT_BACKGROUND;
    public static final Font DEFAULT_FONT;
    public Color foreground;
    public Color background;
    public boolean underline;
    public Font font;
    FontMetrics fontMetrics;
    
    public Style() {
        this(Style.DEFAULT_FOREGROUND);
    }
    
    public Style(final Color fg) {
        this(fg, Style.DEFAULT_BACKGROUND);
    }
    
    public Style(final Color fg, final Color bg) {
        this(fg, bg, Style.DEFAULT_FONT);
    }
    
    public Style(final Color fg, final Color bg, final Font font) {
        this(fg, bg, font, false);
    }
    
    public Style(final Color fg, final Color bg, final Font font, final boolean underline) {
        super();
        this.foreground = fg;
        this.background = bg;
        this.font = font;
        this.underline = underline;
        this.fontMetrics = ((font == null) ? null : new JPanel().getFontMetrics(font));
    }
    
    private boolean areEqual(final Object o1, final Object o2) {
        return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2));
    }
    
    public Object clone() {
        Style clone = null;
        try {
            clone = (Style)super.clone();
        }
        catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace();
            return null;
        }
        clone.foreground = this.foreground;
        clone.background = this.background;
        clone.font = this.font;
        clone.underline = this.underline;
        clone.fontMetrics = this.fontMetrics;
        return clone;
    }
    
    public boolean equals(final Object o2) {
        if (o2 instanceof Style) {
            final Style ss2 = (Style)o2;
            if (this.underline == ss2.underline && this.areEqual(this.foreground, ss2.foreground) && this.areEqual(this.background, ss2.background) && this.areEqual(this.font, ss2.font) && this.areEqual(this.fontMetrics, ss2.fontMetrics)) {
                return true;
            }
        }
        return false;
    }
    
    public int hashCode() {
        int hashCode = this.underline ? 1 : 0;
        if (this.foreground != null) {
            hashCode ^= this.foreground.hashCode();
        }
        if (this.background != null) {
            hashCode ^= this.background.hashCode();
        }
        return hashCode;
    }
    
    public String toString() {
        return "[Style: foreground: " + this.foreground + ", background: " + this.background + ", underline: " + this.underline + ", font: " + this.font + "]";
    }
    
    static {
        DEFAULT_FOREGROUND = Color.BLACK;
        DEFAULT_BACKGROUND = null;
        DEFAULT_FONT = null;
    }
}
