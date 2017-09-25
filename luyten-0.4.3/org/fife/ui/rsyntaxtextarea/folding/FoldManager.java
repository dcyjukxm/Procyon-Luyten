package org.fife.ui.rsyntaxtextarea.folding;

import java.util.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import javax.swing.event.*;
import java.beans.*;
import javax.swing.text.*;

public class FoldManager
{
    private RSyntaxTextArea textArea;
    private FoldParser parser;
    private List<Fold> folds;
    private boolean codeFoldingEnabled;
    private PropertyChangeSupport support;
    public static final String PROPERTY_FOLDS_UPDATED = "FoldsUpdated";
    private Parser tempParser;
    
    public FoldManager(final RSyntaxTextArea textArea) {
        super();
        this.textArea = textArea;
        this.support = new PropertyChangeSupport(this);
        final Listener l = new Listener();
        textArea.getDocument().addDocumentListener(l);
        textArea.addPropertyChangeListener("RSTA.syntaxStyle", l);
        this.folds = new ArrayList<Fold>();
        this.updateFoldParser();
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.support.addPropertyChangeListener(l);
    }
    
    public void clear() {
        this.folds.clear();
    }
    
    public boolean ensureOffsetNotInClosedFold(final int offs) {
        boolean foldsOpened = false;
        for (Fold fold = this.getDeepestFoldContaining(offs); fold != null; fold = fold.getParent()) {
            if (fold.isCollapsed()) {
                fold.setCollapsed(false);
                foldsOpened = true;
            }
        }
        return foldsOpened;
    }
    
    public Fold getDeepestFoldContaining(final int offs) {
        Fold deepestFold = null;
        if (offs > -1) {
            for (int i = 0; i < this.folds.size(); ++i) {
                final Fold fold = this.getFold(i);
                if (fold.containsOffset(offs)) {
                    deepestFold = fold.getDeepestFoldContaining(offs);
                    break;
                }
            }
        }
        return deepestFold;
    }
    
    public Fold getDeepestOpenFoldContaining(final int offs) {
        Fold deepestFold = null;
        if (offs > -1) {
            int i = 0;
            while (i < this.folds.size()) {
                final Fold fold = this.getFold(i);
                if (fold.containsOffset(offs)) {
                    if (fold.isCollapsed()) {
                        return null;
                    }
                    deepestFold = fold.getDeepestOpenFoldContaining(offs);
                    break;
                }
                else {
                    ++i;
                }
            }
        }
        return deepestFold;
    }
    
    public Fold getFold(final int index) {
        return this.folds.get(index);
    }
    
    public int getFoldCount() {
        return this.folds.size();
    }
    
    public Fold getFoldForLine(final int line) {
        return this.getFoldForLineImpl(null, this.folds, line);
    }
    
    private Fold getFoldForLineImpl(final Fold parent, final List<Fold> folds, final int line) {
        int low = 0;
        int high = folds.size() - 1;
        while (low <= high) {
            final int mid = low + high >> 1;
            final Fold midFold = folds.get(mid);
            final int startLine = midFold.getStartLine();
            if (line == startLine) {
                return midFold;
            }
            if (line < startLine) {
                high = mid - 1;
            }
            else {
                final int endLine = midFold.getEndLine();
                if (line < endLine) {
                    final List<Fold> children = midFold.getChildren();
                    return (children != null) ? this.getFoldForLineImpl(midFold, children, line) : null;
                }
                low = mid + 1;
            }
        }
        return null;
    }
    
    public int getHiddenLineCount() {
        int count = 0;
        for (final Fold fold : this.folds) {
            count += fold.getCollapsedLineCount();
        }
        return count;
    }
    
    public int getHiddenLineCountAbove(final int line) {
        return this.getHiddenLineCountAbove(line, false);
    }
    
    public int getHiddenLineCountAbove(final int line, final boolean physical) {
        int count = 0;
        for (final Fold fold : this.folds) {
            final int comp = physical ? (line + count) : line;
            if (fold.getStartLine() >= comp) {
                break;
            }
            count += this.getHiddenLineCountAboveImpl(fold, comp, physical);
        }
        return count;
    }
    
    private int getHiddenLineCountAboveImpl(final Fold fold, final int line, final boolean physical) {
        int count = 0;
        if (fold.getEndLine() < line || (fold.isCollapsed() && fold.getStartLine() < line)) {
            count = fold.getCollapsedLineCount();
        }
        else {
            for (int childCount = fold.getChildCount(), i = 0; i < childCount; ++i) {
                final Fold child = fold.getChild(i);
                final int comp = physical ? (line + count) : line;
                if (child.getStartLine() >= comp) {
                    break;
                }
                count += this.getHiddenLineCountAboveImpl(child, comp, physical);
            }
        }
        return count;
    }
    
    public int getLastVisibleLine() {
        int lastLine = this.textArea.getLineCount() - 1;
        if (this.isCodeFoldingSupportedAndEnabled()) {
            final int foldCount = this.getFoldCount();
            if (foldCount > 0) {
                Fold lastFold = this.getFold(foldCount - 1);
                if (lastFold.containsLine(lastLine)) {
                    if (lastFold.isCollapsed()) {
                        lastLine = lastFold.getStartLine();
                    }
                    else {
                        while (lastFold.getHasChildFolds()) {
                            lastFold = lastFold.getLastChild();
                            if (!lastFold.containsLine(lastLine)) {
                                break;
                            }
                            if (lastFold.isCollapsed()) {
                                lastLine = lastFold.getStartLine();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return lastLine;
    }
    
    public int getVisibleLineAbove(int line) {
        if (line <= 0 || line >= this.textArea.getLineCount()) {
            return -1;
        }
        while (--line >= 0 && this.isLineHidden(line)) {}
        return line;
    }
    
    public int getVisibleLineBelow(int line) {
        final int lineCount = this.textArea.getLineCount();
        if (line < 0 || line >= lineCount - 1) {
            return -1;
        }
        while (++line < lineCount && this.isLineHidden(line)) {}
        return (line == lineCount) ? -1 : line;
    }
    
    public boolean isCodeFoldingEnabled() {
        return this.codeFoldingEnabled;
    }
    
    public boolean isCodeFoldingSupportedAndEnabled() {
        return this.codeFoldingEnabled && this.parser != null;
    }
    
    public boolean isFoldStartLine(final int line) {
        return this.getFoldForLine(line) != null;
    }
    
    public boolean isLineHidden(final int line) {
        for (final Fold fold : this.folds) {
            if (fold.containsLine(line)) {
                return fold.isCollapsed() || this.isLineHiddenImpl(fold, line);
            }
        }
        return false;
    }
    
    private boolean isLineHiddenImpl(final Fold parent, final int line) {
        for (int i = 0; i < parent.getChildCount(); ++i) {
            final Fold child = parent.getChild(i);
            if (child.containsLine(line)) {
                return child.isCollapsed() || this.isLineHiddenImpl(child, line);
            }
        }
        return false;
    }
    
    private void keepFoldState(final Fold newFold, final List<Fold> oldFolds) {
        final int previousLoc = Collections.binarySearch(oldFolds, newFold);
        if (previousLoc >= 0) {
            final Fold prevFold = oldFolds.get(previousLoc);
            newFold.setCollapsed(prevFold.isCollapsed());
        }
        else {
            final int insertionPoint = -(previousLoc + 1);
            if (insertionPoint > 0) {
                final Fold possibleParentFold = oldFolds.get(insertionPoint - 1);
                if (possibleParentFold.containsOffset(newFold.getStartOffset())) {
                    final List<Fold> children = possibleParentFold.getChildren();
                    if (children != null) {
                        this.keepFoldState(newFold, children);
                    }
                }
            }
        }
    }
    
    private void keepFoldStates(final List<Fold> newFolds, final List<Fold> oldFolds) {
        for (final Fold newFold : newFolds) {
            this.keepFoldState(newFold, this.folds);
            final List<Fold> newChildFolds = newFold.getChildren();
            if (newChildFolds != null) {
                this.keepFoldStates(newChildFolds, oldFolds);
            }
        }
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.support.removePropertyChangeListener(l);
    }
    
    public void reparse() {
        if (this.codeFoldingEnabled && this.parser != null) {
            List<Fold> newFolds = this.parser.getFolds(this.textArea);
            if (newFolds == null) {
                newFolds = Collections.emptyList();
            }
            else {
                this.keepFoldStates(newFolds, this.folds);
            }
            this.folds = newFolds;
            this.support.firePropertyChange("FoldsUpdated", null, this.folds);
            this.textArea.repaint();
        }
        else {
            this.folds.clear();
        }
    }
    
    public void setCodeFoldingEnabled(final boolean enabled) {
        if (enabled != this.codeFoldingEnabled) {
            this.codeFoldingEnabled = enabled;
            if (this.tempParser != null) {
                this.textArea.removeParser(this.tempParser);
            }
            if (enabled) {
                this.tempParser = new AbstractParser() {
                    public ParseResult parse(final RSyntaxDocument doc, final String style) {
                        FoldManager.this.reparse();
                        return new DefaultParseResult(this);
                    }
                };
                this.textArea.addParser(this.tempParser);
                this.support.firePropertyChange("FoldsUpdated", null, null);
            }
            else {
                this.folds = Collections.emptyList();
                this.textArea.repaint();
                this.support.firePropertyChange("FoldsUpdated", null, null);
            }
        }
    }
    
    public void setFolds(final List<Fold> folds) {
        this.folds = folds;
    }
    
    private void updateFoldParser() {
        this.parser = FoldParserManager.get().getFoldParser(this.textArea.getSyntaxEditingStyle());
    }
    
    static /* synthetic */ void access$100(final FoldManager x0) {
        x0.updateFoldParser();
    }
    
    static /* synthetic */ RSyntaxTextArea access$200(final FoldManager x0) {
        return x0.textArea;
    }
    
    private class Listener implements DocumentListener, PropertyChangeListener
    {
        public void changedUpdate(final DocumentEvent e) {
        }
        
        public void insertUpdate(final DocumentEvent e) {
            final int startOffs = e.getOffset();
            final int endOffs = startOffs + e.getLength();
            final Document doc = e.getDocument();
            final Element root = doc.getDefaultRootElement();
            final int startLine = root.getElementIndex(startOffs);
            final int endLine = root.getElementIndex(endOffs);
            if (startLine != endLine) {
                final Fold fold = FoldManager.this.getFoldForLine(startLine);
                if (fold != null && fold.isCollapsed()) {
                    fold.toggleCollapsedState();
                }
            }
        }
        
        public void propertyChange(final PropertyChangeEvent e) {
            FoldManager.access$100(FoldManager.this);
            FoldManager.this.reparse();
        }
        
        public void removeUpdate(final DocumentEvent e) {
            final int offs = e.getOffset();
            try {
                final int lastLineModified = FoldManager.access$200(FoldManager.this).getLineOfOffset(offs);
                final Fold fold = FoldManager.this.getFoldForLine(lastLineModified);
                if (fold != null && fold.isCollapsed()) {
                    fold.toggleCollapsedState();
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }
}
