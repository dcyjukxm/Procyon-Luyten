package org.fife.ui.rsyntaxtextarea.focusabletip;

import javax.swing.border.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.*;
import java.awt.*;
import javax.swing.text.html.*;
import org.fife.ui.rsyntaxtextarea.*;

public class TipUtil
{
    private static final String getHexString(final Color c) {
        if (c == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder("#");
        final int r = c.getRed();
        if (r < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(r));
        final int g = c.getGreen();
        if (g < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(g));
        final int b = c.getBlue();
        if (b < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(b));
        return sb.toString();
    }
    
    public static Rectangle getScreenBoundsForPoint(final int x, final int y) {
        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice[] devices = env.getScreenDevices();
        for (int i = 0; i < devices.length; ++i) {
            final GraphicsConfiguration[] configs = devices[i].getConfigurations();
            for (int j = 0; j < configs.length; ++j) {
                final Rectangle gcBounds = configs[j].getBounds();
                if (gcBounds.contains(x, y)) {
                    return gcBounds;
                }
            }
        }
        return env.getMaximumWindowBounds();
    }
    
    public static Color getToolTipBackground() {
        Color c = UIManager.getColor("ToolTip.background");
        final boolean isNimbus = isNimbusLookAndFeel();
        if (c == null || isNimbus) {
            c = UIManager.getColor("info");
            if (c == null || (isNimbus && isDerivedColor(c))) {
                c = SystemColor.info;
            }
        }
        if (c instanceof ColorUIResource) {
            c = new Color(c.getRGB());
        }
        return c;
    }
    
    public static Border getToolTipBorder() {
        Border border = UIManager.getBorder("ToolTip.border");
        if (border == null || isNimbusLookAndFeel()) {
            border = UIManager.getBorder("nimbusBorder");
            if (border == null) {
                border = BorderFactory.createLineBorder(SystemColor.controlDkShadow);
            }
        }
        return border;
    }
    
    private static final boolean isDerivedColor(final Color c) {
        return c != null && c.getClass().getName().endsWith(".DerivedColor");
    }
    
    private static final boolean isNimbusLookAndFeel() {
        return UIManager.getLookAndFeel().getName().equals("Nimbus");
    }
    
    public static void tweakTipEditorPane(final JEditorPane textArea) {
        final boolean isNimbus = isNimbusLookAndFeel();
        if (isNimbus) {
            final Color selBG = textArea.getSelectionColor();
            final Color selFG = textArea.getSelectedTextColor();
            textArea.setUI(new BasicEditorPaneUI());
            textArea.setSelectedTextColor(selFG);
            textArea.setSelectionColor(selBG);
        }
        textArea.setEditable(false);
        textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textArea.getCaret().setSelectionVisible(true);
        Color fg = UIManager.getColor("Label.foreground");
        if (fg == null || (isNimbus && isDerivedColor(fg))) {
            fg = SystemColor.textText;
        }
        textArea.setForeground(fg);
        textArea.setBackground(getToolTipBackground());
        Font font = UIManager.getFont("Label.font");
        if (font == null) {
            font = new Font("SansSerif", 0, 12);
        }
        final HTMLDocument doc = (HTMLDocument)textArea.getDocument();
        doc.getStyleSheet().addRule("body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt" + "; color: " + getHexString(fg) + "; }");
        final Color linkFG = RSyntaxUtilities.getHyperlinkForeground();
        doc.getStyleSheet().addRule("a { color: " + getHexString(linkFG) + "; }");
    }
}
