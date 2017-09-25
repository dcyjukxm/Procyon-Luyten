package org.fife.ui.rsyntaxtextarea.folding;

import org.fife.ui.rsyntaxtextarea.*;
import java.util.*;
import javax.swing.text.*;

public class NsisFoldParser implements FoldParser
{
    private static final char[] KEYWORD_FUNCTION;
    private static final char[] KEYWORD_FUNCTION_END;
    private static final char[] KEYWORD_SECTION;
    private static final char[] KEYWORD_SECTION_END;
    protected static final char[] C_MLC_END;
    
    private static final boolean foundEndKeyword(final char[] keyword, final Token t, final Stack<char[]> endWordStack) {
        return t.is(6, keyword) && !endWordStack.isEmpty() && keyword == endWordStack.peek();
    }
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final List<Fold> folds = new ArrayList<Fold>();
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        boolean inMLC = false;
        int mlcStart = 0;
        final Stack<char[]> endWordStack = new Stack<char[]>();
        try {
            for (int line = 0; line < lineCount; ++line) {
                for (Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    if (t.isComment()) {
                        if (inMLC) {
                            if (t.endsWith(NsisFoldParser.C_MLC_END)) {
                                final int mlcEnd = t.getEndOffset() - 1;
                                if (currentFold == null) {
                                    currentFold = new Fold(1, textArea, mlcStart);
                                    currentFold.setEndOffset(mlcEnd);
                                    folds.add(currentFold);
                                    currentFold = null;
                                }
                                else {
                                    currentFold = currentFold.createChild(1, mlcStart);
                                    currentFold.setEndOffset(mlcEnd);
                                    currentFold = currentFold.getParent();
                                }
                                inMLC = false;
                                mlcStart = 0;
                            }
                        }
                        else if (t.getType() != 1 && !t.endsWith(NsisFoldParser.C_MLC_END)) {
                            inMLC = true;
                            mlcStart = t.getOffset();
                        }
                    }
                    else if (t.is(6, NsisFoldParser.KEYWORD_SECTION)) {
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                        endWordStack.push(NsisFoldParser.KEYWORD_SECTION_END);
                    }
                    else if (t.is(6, NsisFoldParser.KEYWORD_FUNCTION)) {
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                        endWordStack.push(NsisFoldParser.KEYWORD_FUNCTION_END);
                    }
                    else if ((foundEndKeyword(NsisFoldParser.KEYWORD_SECTION_END, t, endWordStack) || foundEndKeyword(NsisFoldParser.KEYWORD_FUNCTION_END, t, endWordStack)) && currentFold != null) {
                        currentFold.setEndOffset(t.getOffset());
                        final Fold parentFold = currentFold.getParent();
                        endWordStack.pop();
                        if (currentFold.isOnSingleLine() && !currentFold.removeFromParent()) {
                            folds.remove(folds.size() - 1);
                        }
                        currentFold = parentFold;
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
        KEYWORD_FUNCTION = "Function".toCharArray();
        KEYWORD_FUNCTION_END = "FunctionEnd".toCharArray();
        KEYWORD_SECTION = "Section".toCharArray();
        KEYWORD_SECTION_END = "SectionEnd".toCharArray();
        C_MLC_END = "*/".toCharArray();
    }
}
