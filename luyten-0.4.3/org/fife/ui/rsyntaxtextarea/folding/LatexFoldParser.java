package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;

public class LatexFoldParser implements FoldParser
{
    private static final char[] BEGIN;
    private static final char[] END;
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final List<Fold> folds = new ArrayList<Fold>();
        final Stack<String> expectedStack = new Stack<String>();
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        try {
            for (int line = 0; line < lineCount; ++line) {
                for (Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    if (t.is(6, LatexFoldParser.BEGIN)) {
                        Token temp = t.getNextToken();
                        if (temp != null && temp.isLeftCurly()) {
                            temp = temp.getNextToken();
                            if (temp != null && temp.getType() == 6) {
                                if (currentFold == null) {
                                    currentFold = new Fold(0, textArea, t.getOffset());
                                    folds.add(currentFold);
                                }
                                else {
                                    currentFold = currentFold.createChild(0, t.getOffset());
                                }
                                expectedStack.push(temp.getLexeme());
                                t = temp;
                            }
                        }
                    }
                    else if (t.is(6, LatexFoldParser.END) && currentFold != null && !expectedStack.isEmpty()) {
                        Token temp = t.getNextToken();
                        if (temp != null && temp.isLeftCurly()) {
                            temp = temp.getNextToken();
                            if (temp != null && temp.getType() == 6) {
                                final String value = temp.getLexeme();
                                if (expectedStack.peek().equals(value)) {
                                    expectedStack.pop();
                                    currentFold.setEndOffset(t.getOffset());
                                    final Fold parentFold = currentFold.getParent();
                                    if (currentFold.isOnSingleLine() && !currentFold.removeFromParent()) {
                                        folds.remove(folds.size() - 1);
                                    }
                                    t = temp;
                                    currentFold = parentFold;
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return folds;
    }
    
    static {
        BEGIN = "\\begin".toCharArray();
        END = "\\end".toCharArray();
    }
}
