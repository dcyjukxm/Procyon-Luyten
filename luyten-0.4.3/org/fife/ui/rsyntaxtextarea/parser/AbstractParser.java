package org.fife.ui.rsyntaxtextarea.parser;

import java.net.*;

public abstract class AbstractParser implements Parser
{
    private boolean enabled;
    private ExtendedHyperlinkListener linkListener;
    
    protected AbstractParser() {
        super();
        this.setEnabled(true);
    }
    
    public ExtendedHyperlinkListener getHyperlinkListener() {
        return this.linkListener;
    }
    
    public URL getImageBase() {
        return null;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public void setHyperlinkListener(final ExtendedHyperlinkListener listener) {
        this.linkListener = listener;
    }
}
