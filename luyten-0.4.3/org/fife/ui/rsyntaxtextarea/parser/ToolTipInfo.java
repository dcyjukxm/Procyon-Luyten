package org.fife.ui.rsyntaxtextarea.parser;

import javax.swing.event.*;
import java.net.*;

public class ToolTipInfo
{
    private String text;
    private HyperlinkListener listener;
    private URL imageBase;
    
    public ToolTipInfo(final String text, final HyperlinkListener listener) {
        this(text, listener, null);
    }
    
    public ToolTipInfo(final String text, final HyperlinkListener l, final URL imageBase) {
        super();
        this.text = text;
        this.listener = l;
        this.imageBase = imageBase;
    }
    
    public HyperlinkListener getHyperlinkListener() {
        return this.listener;
    }
    
    public URL getImageBase() {
        return this.imageBase;
    }
    
    public String getToolTipText() {
        return this.text;
    }
}
