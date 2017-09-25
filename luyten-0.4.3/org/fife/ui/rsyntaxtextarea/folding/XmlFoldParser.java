package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;

public class XmlFoldParser implements FoldParser
{
    private static final char[] MARKUP_CLOSING_TAG_START;
    private static final char[] MARKUP_SHORT_TAG_END;
    private static final char[] MLC_END;
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final List<Fold> folds = new ArrayList<Fold>();
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        boolean inMLC = false;
        int mlcStart = 0;
        try {
            for (int line = 0; line < lineCount; ++line) {
                for (Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    if (t.isComment()) {
                        if (inMLC) {
                            if (t.endsWith(XmlFoldParser.MLC_END)) {
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
                        else if (t.getType() == 2 && !t.endsWith(XmlFoldParser.MLC_END)) {
                            inMLC = true;
                            mlcStart = t.getOffset();
                        }
                    }
                    else if (t.isSingleChar(25, '<')) {
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                    }
                    else if (t.is(25, XmlFoldParser.MARKUP_SHORT_TAG_END)) {
                        if (currentFold != null) {
                            final Fold parentFold = currentFold.getParent();
                            removeFold(currentFold, folds);
                            currentFold = parentFold;
                        }
                    }
                    else if (t.is(25, XmlFoldParser.MARKUP_CLOSING_TAG_START) && currentFold != null) {
                        currentFold.setEndOffset(t.getOffset());
                        final Fold parentFold = currentFold.getParent();
                        if (currentFold.isOnSingleLine()) {
                            removeFold(currentFold, folds);
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
    
    private static final void removeFold(final Fold fold, final List<Fold> folds) {
        if (!fold.removeFromParent()) {
            folds.remove(folds.size() - 1);
        }
    }
    
    static {
        MARKUP_CLOSING_TAG_START = new char[] { '<', '/' };
        MARKUP_SHORT_TAG_END = new char[] { '/', '>' };
        MLC_END = new char[] { '-', '-', '>' };
    }
}
