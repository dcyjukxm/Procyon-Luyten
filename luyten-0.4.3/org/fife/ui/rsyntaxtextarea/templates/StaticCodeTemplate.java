package org.fife.ui.rsyntaxtextarea.templates;

import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.text.*;
import java.io.*;

public class StaticCodeTemplate extends AbstractCodeTemplate
{
    private static final long serialVersionUID = 1L;
    private String beforeCaret;
    private String afterCaret;
    private transient int firstBeforeNewline;
    private transient int firstAfterNewline;
    private static final String EMPTY_STRING = "";
    
    public StaticCodeTemplate() {
        super();
    }
    
    public StaticCodeTemplate(final String id, final String beforeCaret, final String afterCaret) {
        super(id);
        this.setBeforeCaretText(beforeCaret);
        this.setAfterCaretText(afterCaret);
    }
    
    public String getAfterCaretText() {
        return this.afterCaret;
    }
    
    public String getBeforeCaretText() {
        return this.beforeCaret;
    }
    
    private String getAfterTextIndented(final String indent) {
        return this.getTextIndented(this.getAfterCaretText(), this.firstAfterNewline, indent);
    }
    
    private String getBeforeTextIndented(final String indent) {
        return this.getTextIndented(this.getBeforeCaretText(), this.firstBeforeNewline, indent);
    }
    
    private String getTextIndented(final String text, final int firstNewline, final String indent) {
        if (firstNewline == -1) {
            return text;
        }
        int pos = 0;
        int old = firstNewline + 1;
        final StringBuilder sb = new StringBuilder(text.substring(0, old));
        sb.append(indent);
        while ((pos = text.indexOf(10, old)) > -1) {
            sb.append(text.substring(old, pos + 1));
            sb.append(indent);
            old = pos + 1;
        }
        if (old < text.length()) {
            sb.append(text.substring(old));
        }
        return sb.toString();
    }
    
    public void invoke(final RSyntaxTextArea textArea) throws BadLocationException {
        final Caret c = textArea.getCaret();
        final int dot = c.getDot();
        final int mark = c.getMark();
        int p0 = Math.min(dot, mark);
        final int p = Math.max(dot, mark);
        final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
        final Element map = doc.getDefaultRootElement();
        final int lineNum = map.getElementIndex(dot);
        final Element line = map.getElement(lineNum);
        final int start = line.getStartOffset();
        final int end = line.getEndOffset() - 1;
        String s;
        int len;
        int endWS;
        for (s = textArea.getText(start, end - start), len = s.length(), endWS = 0; endWS < len && RSyntaxUtilities.isWhitespace(s.charAt(endWS)); ++endWS) {}
        s = s.substring(0, endWS);
        p0 -= this.getID().length();
        final String beforeText = this.getBeforeTextIndented(s);
        final String afterText = this.getAfterTextIndented(s);
        doc.replace(p0, p - p0, beforeText + afterText, null);
        textArea.setCaretPosition(p0 + beforeText.length());
    }
    
    private void readObject(final ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.setBeforeCaretText(this.beforeCaret);
        this.setAfterCaretText(this.afterCaret);
    }
    
    public void setAfterCaretText(final String afterCaret) {
        this.afterCaret = ((afterCaret == null) ? "" : afterCaret);
        this.firstAfterNewline = this.afterCaret.indexOf(10);
    }
    
    public void setBeforeCaretText(final String beforeCaret) {
        this.beforeCaret = ((beforeCaret == null) ? "" : beforeCaret);
        this.firstBeforeNewline = this.beforeCaret.indexOf(10);
    }
    
    public String toString() {
        return "[StaticCodeTemplate: id=" + this.getID() + ", text=" + this.getBeforeCaretText() + "|" + this.getAfterCaretText() + "]";
    }
}
