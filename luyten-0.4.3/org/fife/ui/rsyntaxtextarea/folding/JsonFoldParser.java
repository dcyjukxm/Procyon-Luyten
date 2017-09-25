package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;

public class JsonFoldParser implements FoldParser
{
    private static final Object OBJECT_BLOCK;
    private static final Object ARRAY_BLOCK;
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final Stack<Object> blocks = new Stack<Object>();
        final List<Fold> folds = new ArrayList<Fold>();
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        try {
            for (int line = 0; line < lineCount; ++line) {
                for (Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    if (t.isLeftCurly()) {
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                        blocks.push(JsonFoldParser.OBJECT_BLOCK);
                    }
                    else if (t.isRightCurly() && popOffTop(blocks, JsonFoldParser.OBJECT_BLOCK)) {
                        if (currentFold != null) {
                            currentFold.setEndOffset(t.getOffset());
                            final Fold parentFold = currentFold.getParent();
                            if (currentFold.isOnSingleLine() && !currentFold.removeFromParent()) {
                                folds.remove(folds.size() - 1);
                            }
                            currentFold = parentFold;
                        }
                    }
                    else if (isLeftBracket(t)) {
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                        blocks.push(JsonFoldParser.ARRAY_BLOCK);
                    }
                    else if (isRightBracket(t) && popOffTop(blocks, JsonFoldParser.ARRAY_BLOCK) && currentFold != null) {
                        currentFold.setEndOffset(t.getOffset());
                        final Fold parentFold = currentFold.getParent();
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
    
    private static final boolean isLeftBracket(final Token t) {
        return t.getType() == 22 && t.isSingleChar('[');
    }
    
    private static final boolean isRightBracket(final Token t) {
        return t.getType() == 22 && t.isSingleChar(']');
    }
    
    private static final boolean popOffTop(final Stack<Object> stack, final Object value) {
        if (stack.size() > 0 && stack.peek() == value) {
            stack.pop();
            return true;
        }
        return false;
    }
    
    static {
        OBJECT_BLOCK = new Object();
        ARRAY_BLOCK = new Object();
    }
}
