package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;

public class CurlyFoldParser implements FoldParser
{
    private boolean foldableMultiLineComments;
    private final boolean java;
    private static final char[] KEYWORD_IMPORT;
    protected static final char[] C_MLC_END;
    
    public CurlyFoldParser() {
        this(true, false);
    }
    
    public CurlyFoldParser(final boolean cStyleMultiLineComments, final boolean java) {
        super();
        this.foldableMultiLineComments = cStyleMultiLineComments;
        this.java = java;
    }
    
    public boolean getFoldableMultiLineComments() {
        return this.foldableMultiLineComments;
    }
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final List<Fold> folds = new ArrayList<Fold>();
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        boolean inMLC = false;
        int mlcStart = 0;
        int importStartLine = -1;
        int lastSeenImportLine = -1;
        int importGroupStartOffs = -1;
        int importGroupEndOffs = -1;
        try {
            for (int line = 0; line < lineCount; ++line) {
                for (Token t = textArea.getTokenListForLine(line); t != null && t.isPaintable(); t = t.getNextToken()) {
                    if (this.getFoldableMultiLineComments() && t.isComment()) {
                        if (this.java && importStartLine > -1) {
                            if (lastSeenImportLine > importStartLine) {
                                Fold fold = null;
                                if (currentFold == null) {
                                    fold = new Fold(2, textArea, importGroupStartOffs);
                                    folds.add(fold);
                                }
                                else {
                                    fold = currentFold.createChild(2, importGroupStartOffs);
                                }
                                fold.setEndOffset(importGroupEndOffs);
                            }
                            lastSeenImportLine = (importStartLine = (importGroupStartOffs = (importGroupEndOffs = -1)));
                        }
                        if (inMLC) {
                            if (t.endsWith(CurlyFoldParser.C_MLC_END)) {
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
                        else if (t.getType() != 1 && !t.endsWith(CurlyFoldParser.C_MLC_END)) {
                            inMLC = true;
                            mlcStart = t.getOffset();
                        }
                    }
                    else if (this.isLeftCurly(t)) {
                        if (this.java && importStartLine > -1) {
                            if (lastSeenImportLine > importStartLine) {
                                Fold fold = null;
                                if (currentFold == null) {
                                    fold = new Fold(2, textArea, importGroupStartOffs);
                                    folds.add(fold);
                                }
                                else {
                                    fold = currentFold.createChild(2, importGroupStartOffs);
                                }
                                fold.setEndOffset(importGroupEndOffs);
                            }
                            lastSeenImportLine = (importStartLine = (importGroupStartOffs = (importGroupEndOffs = -1)));
                        }
                        if (currentFold == null) {
                            currentFold = new Fold(0, textArea, t.getOffset());
                            folds.add(currentFold);
                        }
                        else {
                            currentFold = currentFold.createChild(0, t.getOffset());
                        }
                    }
                    else if (this.isRightCurly(t)) {
                        if (currentFold != null) {
                            currentFold.setEndOffset(t.getOffset());
                            final Fold parentFold = currentFold.getParent();
                            if (currentFold.isOnSingleLine() && !currentFold.removeFromParent()) {
                                folds.remove(folds.size() - 1);
                            }
                            currentFold = parentFold;
                        }
                    }
                    else if (this.java) {
                        if (t.is(6, CurlyFoldParser.KEYWORD_IMPORT)) {
                            if (importStartLine == -1) {
                                importStartLine = line;
                                importGroupStartOffs = t.getOffset();
                                importGroupEndOffs = t.getOffset();
                            }
                            lastSeenImportLine = line;
                        }
                        else if (importStartLine > -1 && t.isIdentifier() && t.isSingleChar(';')) {
                            importGroupEndOffs = t.getOffset();
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
    
    public boolean isLeftCurly(final Token t) {
        return t.isLeftCurly();
    }
    
    public boolean isRightCurly(final Token t) {
        return t.isRightCurly();
    }
    
    public void setFoldableMultiLineComments(final boolean foldable) {
        this.foldableMultiLineComments = foldable;
    }
    
    static {
        KEYWORD_IMPORT = "import".toCharArray();
        C_MLC_END = "*/".toCharArray();
    }
}
