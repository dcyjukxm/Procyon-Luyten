package org.fife.ui.rsyntaxtextarea;

import java.awt.event.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import java.util.regex.*;

public abstract class AbstractJFlexCTokenMaker extends AbstractJFlexTokenMaker
{
    protected static final Action INSERT_BREAK_ACTION;
    
    public boolean getCurlyBracesDenoteCodeBlocks(final int languageIndex) {
        return true;
    }
    
    public Action getInsertBreakAction() {
        return AbstractJFlexCTokenMaker.INSERT_BREAK_ACTION;
    }
    
    public boolean getMarkOccurrencesOfTokenType(final int type) {
        return type == 20 || type == 8;
    }
    
    public boolean getShouldIndentNextLineAfter(final Token t) {
        if (t != null && t.length() == 1) {
            final char ch = t.charAt(0);
            return ch == '{' || ch == '(';
        }
        return false;
    }
    
    static {
        INSERT_BREAK_ACTION = new InsertBreakAction();
    }
    
    private static class InsertBreakAction extends RSyntaxTextAreaEditorKit.InsertBreakAction
    {
        private static final Pattern p;
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final RSyntaxTextArea rsta = (RSyntaxTextArea)this.getTextComponent(e);
            final RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
            final int line = textArea.getCaretLineNumber();
            int type = doc.getLastTokenTypeOnLine(line);
            if (type < 0) {
                type = doc.getClosestStandardTokenTypeForInternalType(type);
            }
            if (type == 3 || type == 2) {
                this.insertBreakInMLC(e, rsta, line);
            }
            else {
                this.handleInsertBreak(rsta, true);
            }
        }
        
        private boolean appearsNested(final RSyntaxTextArea textArea, int line, final int offs) {
            final int firstLine = line;
            while (line < textArea.getLineCount()) {
                Token t = textArea.getTokenListForLine(line);
                int i = 0;
                if (line++ == firstLine) {
                    t = RSyntaxUtilities.getTokenAtOffset(t, offs);
                    if (t == null) {
                        continue;
                    }
                    i = t.documentToToken(offs);
                }
                else {
                    i = t.getTextOffset();
                }
                for (int textOffset = t.getTextOffset(); i < textOffset + t.length() - 1; ++i) {
                    if (t.charAt(i - textOffset) == '/' && t.charAt(i - textOffset + 1) == '*') {
                        return true;
                    }
                }
                if (t.getNextToken() != null) {
                    return false;
                }
            }
            return true;
        }
        
        private void insertBreakInMLC(final ActionEvent e, final RSyntaxTextArea textArea, final int line) {
            Matcher m = null;
            int start = -1;
            int end = -1;
            String text = null;
            try {
                start = textArea.getLineStartOffset(line);
                end = textArea.getLineEndOffset(line);
                text = textArea.getText(start, end - start);
                m = InsertBreakAction.p.matcher(text);
            }
            catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                ble.printStackTrace();
                return;
            }
            if (m.lookingAt()) {
                final String leadingWS = m.group(1);
                final String mlcMarker = m.group(2);
                int dot = textArea.getCaretPosition();
                if (dot >= start && dot < start + leadingWS.length() + mlcMarker.length()) {
                    if (mlcMarker.charAt(0) == '/') {
                        this.handleInsertBreak(textArea, true);
                        return;
                    }
                    textArea.setCaretPosition(end - 1);
                }
                else {
                    boolean moved = false;
                    while (dot < end - 1 && Character.isWhitespace(text.charAt(dot - start))) {
                        moved = true;
                        ++dot;
                    }
                    if (moved) {
                        textArea.setCaretPosition(dot);
                    }
                }
                final boolean firstMlcLine = mlcMarker.charAt(0) == '/';
                final boolean nested = this.appearsNested(textArea, line, start + leadingWS.length() + 2);
                final String header = leadingWS + (firstMlcLine ? " * " : "*") + m.group(3);
                textArea.replaceSelection("\n" + header);
                if (nested) {
                    dot = textArea.getCaretPosition();
                    textArea.insert("\n" + leadingWS + " */", dot);
                    textArea.setCaretPosition(dot);
                }
            }
            else {
                this.handleInsertBreak(textArea, true);
            }
        }
        
        static {
            p = Pattern.compile("([ \\t]*)(/?[\\*]+)([ \\t]*)");
        }
    }
}
