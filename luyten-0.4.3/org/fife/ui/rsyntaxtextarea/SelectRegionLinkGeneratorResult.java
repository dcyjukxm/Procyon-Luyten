package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;

public class SelectRegionLinkGeneratorResult implements LinkGeneratorResult
{
    private RSyntaxTextArea textArea;
    private int sourceOffset;
    private int selStart;
    private int selEnd;
    
    public SelectRegionLinkGeneratorResult(final RSyntaxTextArea textArea, final int sourceOffset, final int selStart, final int selEnd) {
        super();
        this.textArea = textArea;
        this.sourceOffset = sourceOffset;
        this.selStart = selStart;
        this.selEnd = selEnd;
    }
    
    public HyperlinkEvent execute() {
        this.textArea.select(this.selStart, this.selEnd);
        return null;
    }
    
    public int getSourceOffset() {
        return this.sourceOffset;
    }
}
