package org.fife.ui.rtextarea;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class RTextScrollPane extends JScrollPane
{
    private Gutter gutter;
    
    public RTextScrollPane() {
        this(null, true);
    }
    
    public RTextScrollPane(final Component comp) {
        this(comp, true);
    }
    
    public RTextScrollPane(final Component comp, final boolean lineNumbers) {
        this(comp, lineNumbers, Color.GRAY);
    }
    
    public RTextScrollPane(final Component comp, final boolean lineNumbers, final Color lineNumberColor) {
        super(comp);
        final RTextArea textArea = getFirstRTextAreaDescendant(comp);
        final Font defaultFont = new Font("Monospaced", 0, 12);
        (this.gutter = new Gutter(textArea)).setLineNumberFont(defaultFont);
        this.gutter.setLineNumberColor(lineNumberColor);
        this.setLineNumbersEnabled(lineNumbers);
        this.setVerticalScrollBarPolicy(22);
        this.setHorizontalScrollBarPolicy(30);
    }
    
    private void checkGutterVisibility() {
        final int count = this.gutter.getComponentCount();
        if (count == 0) {
            if (this.getRowHeader() != null && this.getRowHeader().getView() == this.gutter) {
                this.setRowHeaderView(null);
            }
        }
        else if (this.getRowHeader() == null || this.getRowHeader().getView() == null) {
            this.setRowHeaderView(this.gutter);
        }
    }
    
    public Gutter getGutter() {
        return this.gutter;
    }
    
    public boolean getLineNumbersEnabled() {
        return this.gutter.getLineNumbersEnabled();
    }
    
    public RTextArea getTextArea() {
        return (RTextArea)this.getViewport().getView();
    }
    
    public boolean isFoldIndicatorEnabled() {
        return this.gutter.isFoldIndicatorEnabled();
    }
    
    public boolean isIconRowHeaderEnabled() {
        return this.gutter.isIconRowHeaderEnabled();
    }
    
    public void setFoldIndicatorEnabled(final boolean enabled) {
        this.gutter.setFoldIndicatorEnabled(enabled);
        this.checkGutterVisibility();
    }
    
    public void setIconRowHeaderEnabled(final boolean enabled) {
        this.gutter.setIconRowHeaderEnabled(enabled);
        this.checkGutterVisibility();
    }
    
    public void setLineNumbersEnabled(final boolean enabled) {
        this.gutter.setLineNumbersEnabled(enabled);
        this.checkGutterVisibility();
    }
    
    public void setViewportView(final Component view) {
        RTextArea rtaCandidate = null;
        if (!(view instanceof RTextArea)) {
            rtaCandidate = getFirstRTextAreaDescendant(view);
            if (rtaCandidate == null) {
                throw new IllegalArgumentException("view must be either an RTextArea or a JLayer wrapping one");
            }
        }
        else {
            rtaCandidate = (RTextArea)view;
        }
        super.setViewportView(view);
        if (this.gutter != null) {
            this.gutter.setTextArea(rtaCandidate);
        }
    }
    
    private static final RTextArea getFirstRTextAreaDescendant(final Component comp) {
        final Stack<Component> stack = new Stack<Component>();
        stack.add(comp);
        while (!stack.isEmpty()) {
            final Component current = stack.pop();
            if (current instanceof RTextArea) {
                return (RTextArea)current;
            }
            if (!(current instanceof Container)) {
                continue;
            }
            final Container container = (Container)current;
            stack.addAll((Collection<?>)Arrays.asList(container.getComponents()));
        }
        return null;
    }
}
