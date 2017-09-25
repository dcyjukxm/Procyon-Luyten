package org.fife.ui.rsyntaxtextarea.folding;

import org.fife.ui.rsyntaxtextarea.*;
import java.util.*;
import javax.swing.text.*;

public class Fold implements Comparable<Fold>
{
    private int type;
    private RSyntaxTextArea textArea;
    private Position startOffs;
    private Position endOffs;
    private Fold parent;
    private List<Fold> children;
    private boolean collapsed;
    private int childCollapsedLineCount;
    private int lastStartOffs;
    private int cachedStartLine;
    private int lastEndOffs;
    private int cachedEndLine;
    
    public Fold(final int type, final RSyntaxTextArea textArea, final int startOffs) throws BadLocationException {
        super();
        this.lastStartOffs = -1;
        this.lastEndOffs = -1;
        this.type = type;
        this.textArea = textArea;
        this.startOffs = textArea.getDocument().createPosition(startOffs);
    }
    
    public Fold createChild(final int type, final int startOffs) throws BadLocationException {
        final Fold child = new Fold(type, this.textArea, startOffs);
        child.parent = this;
        if (this.children == null) {
            this.children = new ArrayList<Fold>();
        }
        this.children.add(child);
        return child;
    }
    
    public int compareTo(final Fold otherFold) {
        int result = -1;
        if (otherFold != null) {
            result = this.startOffs.getOffset() - otherFold.startOffs.getOffset();
        }
        return result;
    }
    
    public boolean containsLine(final int line) {
        return line > this.getStartLine() && line <= this.getEndLine();
    }
    
    public boolean containsOrStartsOnLine(final int line) {
        return line >= this.getStartLine() && line <= this.getEndLine();
    }
    
    public boolean containsOffset(final int offs) {
        boolean contained = false;
        if (offs > this.getStartOffset()) {
            final Element root = this.textArea.getDocument().getDefaultRootElement();
            final int line = root.getElementIndex(offs);
            contained = (line <= this.getEndLine());
        }
        return contained;
    }
    
    public boolean equals(final Object otherFold) {
        return otherFold instanceof Fold && this.compareTo((Fold)otherFold) == 0;
    }
    
    public Fold getChild(final int index) {
        return this.children.get(index);
    }
    
    public int getChildCount() {
        return (this.children == null) ? 0 : this.children.size();
    }
    
    List<Fold> getChildren() {
        return this.children;
    }
    
    public int getCollapsedLineCount() {
        return this.collapsed ? this.getLineCount() : this.childCollapsedLineCount;
    }
    
    Fold getDeepestFoldContaining(final int offs) {
        Fold deepestFold = this;
        for (int i = 0; i < this.getChildCount(); ++i) {
            final Fold fold = this.getChild(i);
            if (fold.containsOffset(offs)) {
                deepestFold = fold.getDeepestFoldContaining(offs);
                break;
            }
        }
        return deepestFold;
    }
    
    Fold getDeepestOpenFoldContaining(final int offs) {
        Fold deepestFold = this;
        int i = 0;
        while (i < this.getChildCount()) {
            final Fold fold = this.getChild(i);
            if (fold.containsOffset(offs)) {
                if (fold.isCollapsed()) {
                    break;
                }
                deepestFold = fold.getDeepestOpenFoldContaining(offs);
                break;
            }
            else {
                ++i;
            }
        }
        return deepestFold;
    }
    
    public int getEndLine() {
        final int endOffs = this.getEndOffset();
        if (this.lastEndOffs == endOffs) {
            return this.cachedEndLine;
        }
        this.lastEndOffs = endOffs;
        final Element root = this.textArea.getDocument().getDefaultRootElement();
        return this.cachedEndLine = root.getElementIndex(endOffs);
    }
    
    public int getEndOffset() {
        return (this.endOffs != null) ? this.endOffs.getOffset() : Integer.MAX_VALUE;
    }
    
    public int getFoldType() {
        return this.type;
    }
    
    public boolean getHasChildFolds() {
        return this.getChildCount() > 0;
    }
    
    public Fold getLastChild() {
        final int childCount = this.getChildCount();
        return (childCount == 0) ? null : this.getChild(childCount - 1);
    }
    
    public int getLineCount() {
        return this.getEndLine() - this.getStartLine();
    }
    
    public Fold getParent() {
        return this.parent;
    }
    
    public int getStartLine() {
        final int startOffs = this.getStartOffset();
        if (this.lastStartOffs == startOffs) {
            return this.cachedStartLine;
        }
        this.lastStartOffs = startOffs;
        final Element root = this.textArea.getDocument().getDefaultRootElement();
        return this.cachedStartLine = root.getElementIndex(startOffs);
    }
    
    public int getStartOffset() {
        return this.startOffs.getOffset();
    }
    
    public int hashCode() {
        return this.getStartLine();
    }
    
    public boolean isCollapsed() {
        return this.collapsed;
    }
    
    public boolean isOnSingleLine() {
        return this.getStartLine() == this.getEndLine();
    }
    
    public boolean removeFromParent() {
        if (this.parent != null) {
            this.parent.removeMostRecentChild();
            this.parent = null;
            return true;
        }
        return false;
    }
    
    private void removeMostRecentChild() {
        this.children.remove(this.children.size() - 1);
    }
    
    public void setCollapsed(final boolean collapsed) {
        if (collapsed != this.collapsed) {
            final int lineCount = this.getLineCount();
            int linesToCollapse = lineCount - this.childCollapsedLineCount;
            if (!collapsed) {
                linesToCollapse = -linesToCollapse;
            }
            this.collapsed = collapsed;
            if (this.parent != null) {
                this.parent.updateChildCollapsedLineCount(linesToCollapse);
            }
            if (collapsed) {
                int dot = this.textArea.getSelectionStart();
                final Element root = this.textArea.getDocument().getDefaultRootElement();
                final int dotLine = root.getElementIndex(dot);
                boolean updateCaret = this.containsLine(dotLine);
                if (!updateCaret) {
                    final int mark = this.textArea.getSelectionEnd();
                    if (mark != dot) {
                        final int markLine = root.getElementIndex(mark);
                        updateCaret = this.containsLine(markLine);
                    }
                }
                if (updateCaret) {
                    dot = root.getElement(this.getStartLine()).getEndOffset() - 1;
                    this.textArea.setCaretPosition(dot);
                }
            }
            this.textArea.foldToggled(this);
        }
    }
    
    public void setEndOffset(final int endOffs) throws BadLocationException {
        this.endOffs = this.textArea.getDocument().createPosition(endOffs);
    }
    
    public void toggleCollapsedState() {
        this.setCollapsed(!this.collapsed);
    }
    
    private void updateChildCollapsedLineCount(final int count) {
        this.childCollapsedLineCount += count;
        if (!this.collapsed && this.parent != null) {
            this.parent.updateChildCollapsedLineCount(count);
        }
    }
    
    public String toString() {
        return "[Fold: startOffs=" + this.getStartOffset() + ", endOffs=" + this.getEndOffset() + ", collapsed=" + this.collapsed + "]";
    }
}
