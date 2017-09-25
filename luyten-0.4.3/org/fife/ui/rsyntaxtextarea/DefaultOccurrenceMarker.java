package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;
import org.fife.ui.rtextarea.*;

class DefaultOccurrenceMarker implements OccurrenceMarker
{
    public Token getTokenToMark(final RSyntaxTextArea textArea) {
        final int line = textArea.getCaretLineNumber();
        final Token tokenList = textArea.getTokenListForLine(line);
        final Caret c = textArea.getCaret();
        int dot = c.getDot();
        Token t = RSyntaxUtilities.getTokenAtOffset(tokenList, dot);
        if (t == null || !this.isValidType(textArea, t) || RSyntaxUtilities.isNonWordChar(t)) {
            --dot;
            try {
                if (dot >= textArea.getLineStartOffset(line)) {
                    t = RSyntaxUtilities.getTokenAtOffset(tokenList, dot);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        return t;
    }
    
    public boolean isValidType(final RSyntaxTextArea textArea, final Token t) {
        return textArea.getMarkOccurrencesOfTokenType(t.getType());
    }
    
    public void markOccurrences(final RSyntaxDocument doc, final Token t, final RSyntaxTextAreaHighlighter h, final SmartHighlightPainter p) {
        markOccurrencesOfToken(doc, t, h, p);
    }
    
    public static final void markOccurrencesOfToken(final RSyntaxDocument doc, final Token t, final RSyntaxTextAreaHighlighter h, final SmartHighlightPainter p) {
        final char[] lexeme = t.getLexeme().toCharArray();
        final int type = t.getType();
        for (int lineCount = doc.getDefaultRootElement().getElementCount(), i = 0; i < lineCount; ++i) {
            for (Token temp = doc.getTokenListForLine(i); temp != null && temp.isPaintable(); temp = temp.getNextToken()) {
                if (temp.is(type, lexeme)) {
                    try {
                        final int end = temp.getEndOffset();
                        h.addMarkedOccurrenceHighlight(temp.getOffset(), end, p);
                    }
                    catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                }
            }
        }
    }
}
