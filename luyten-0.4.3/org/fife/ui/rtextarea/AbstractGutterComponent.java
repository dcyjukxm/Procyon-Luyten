package org.fife.ui.rtextarea;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import javax.swing.event.*;

abstract class AbstractGutterComponent extends JPanel
{
    protected RTextArea textArea;
    protected int currentLineCount;
    
    public AbstractGutterComponent(final RTextArea textArea) {
        super();
        this.init();
        this.setTextArea(textArea);
    }
    
    protected static final Rectangle getChildViewBounds(final View parent, final int line, final Rectangle editorRect) {
        final Shape alloc = parent.getChildAllocation(line, editorRect);
        if (alloc == null) {
            return new Rectangle();
        }
        return (Rectangle)((alloc instanceof Rectangle) ? alloc : alloc.getBounds());
    }
    
    protected Gutter getGutter() {
        final Container parent = this.getParent();
        return (parent instanceof Gutter) ? ((Gutter)parent) : null;
    }
    
    abstract void handleDocumentEvent(final DocumentEvent param_0);
    
    protected void init() {
    }
    
    abstract void lineHeightsChanged();
    
    public void setTextArea(final RTextArea textArea) {
        this.textArea = textArea;
        final int lineCount = (textArea == null) ? 0 : textArea.getLineCount();
        if (this.currentLineCount != lineCount) {
            this.currentLineCount = lineCount;
            this.repaint();
        }
    }
}
