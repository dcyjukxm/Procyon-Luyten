package org.fife.ui.rsyntaxtextarea;

import javax.swing.*;
import org.fife.ui.rtextarea.*;
import java.awt.event.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;

class MarkOccurrencesSupport implements CaretListener, ActionListener
{
    private RSyntaxTextArea textArea;
    private Timer timer;
    private SmartHighlightPainter p;
    public static final Color DEFAULT_COLOR;
    private static final int DEFAULT_DELAY_MS = 1000;
    
    public MarkOccurrencesSupport() {
        this(1000);
    }
    
    public MarkOccurrencesSupport(final int delay) {
        this(delay, MarkOccurrencesSupport.DEFAULT_COLOR);
    }
    
    public MarkOccurrencesSupport(final int delay, final Color color) {
        super();
        (this.timer = new Timer(delay, this)).setRepeats(false);
        this.p = new SmartHighlightPainter();
        this.setColor(color);
    }
    
    public void actionPerformed(final ActionEvent e) {
        final Caret c = this.textArea.getCaret();
        if (c.getDot() != c.getMark()) {
            return;
        }
        final RSyntaxDocument doc = (RSyntaxDocument)this.textArea.getDocument();
        final OccurrenceMarker occurrenceMarker = doc.getOccurrenceMarker();
        boolean occurrencesChanged = false;
        if (occurrenceMarker != null) {
            doc.readLock();
            try {
                final Token t = occurrenceMarker.getTokenToMark(this.textArea);
                if (t != null && occurrenceMarker.isValidType(this.textArea, t) && !RSyntaxUtilities.isNonWordChar(t)) {
                    this.removeHighlights();
                    final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
                    occurrenceMarker.markOccurrences(doc, t, h, this.p);
                    occurrencesChanged = true;
                }
            }
            finally {
                doc.readUnlock();
            }
        }
        if (occurrencesChanged) {
            this.textArea.fireMarkedOccurrencesChanged();
        }
    }
    
    public void caretUpdate(final CaretEvent e) {
        this.timer.restart();
    }
    
    public Color getColor() {
        return (Color)this.p.getPaint();
    }
    
    public int getDelay() {
        return this.timer.getDelay();
    }
    
    public boolean getPaintBorder() {
        return this.p.getPaintBorder();
    }
    
    public void install(final RSyntaxTextArea textArea) {
        if (this.textArea != null) {
            this.uninstall();
        }
        (this.textArea = textArea).addCaretListener(this);
        if (textArea.getMarkOccurrencesColor() != null) {
            this.setColor(textArea.getMarkOccurrencesColor());
        }
    }
    
    private void removeHighlights() {
        if (this.textArea != null) {
            final RSyntaxTextAreaHighlighter h = (RSyntaxTextAreaHighlighter)this.textArea.getHighlighter();
            h.clearMarkOccurrencesHighlights();
        }
    }
    
    public void setColor(final Color color) {
        this.p.setPaint(color);
        if (this.textArea != null) {
            this.removeHighlights();
            this.caretUpdate(null);
        }
    }
    
    public void setDelay(final int delay) {
        this.timer.setDelay(delay);
    }
    
    public void setPaintBorder(final boolean paint) {
        if (paint != this.p.getPaintBorder()) {
            this.p.setPaintBorder(paint);
            if (this.textArea != null) {
                this.textArea.repaint();
            }
        }
    }
    
    public void uninstall() {
        if (this.textArea != null) {
            this.removeHighlights();
            this.textArea.removeCaretListener(this);
        }
    }
    
    static {
        DEFAULT_COLOR = new Color(224, 224, 224);
    }
}
