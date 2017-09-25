package org.fife.ui.rtextarea;

import java.beans.*;

public class SearchContext implements Cloneable
{
    public static final String PROPERTY_SEARCH_FOR = "Search.searchFor";
    public static final String PROPERTY_REPLACE_WITH = "Search.replaceWith";
    public static final String PROPERTY_MATCH_CASE = "Search.MatchCase";
    public static final String PROPERTY_MATCH_WHOLE_WORD = "Search.MatchWholeWord";
    public static final String PROPERTY_SEARCH_FORWARD = "Search.Forward";
    public static final String PROPERTY_SELECTION_ONLY = "Search.SelectionOnly";
    public static final String PROPERTY_USE_REGEX = "Search.UseRegex";
    public static final String PROPERTY_MARK_ALL = "Search.MarkAll";
    private String searchFor;
    private String replaceWith;
    private boolean forward;
    private boolean matchCase;
    private boolean wholeWord;
    private boolean regex;
    private boolean selectionOnly;
    private boolean markAll;
    private PropertyChangeSupport support;
    
    public SearchContext() {
        this(null);
    }
    
    public SearchContext(final String searchFor) {
        this(searchFor, false);
    }
    
    public SearchContext(final String searchFor, final boolean matchCase) {
        super();
        this.support = new PropertyChangeSupport(this);
        this.searchFor = searchFor;
        this.matchCase = matchCase;
        this.markAll = true;
        this.forward = true;
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        this.support.addPropertyChangeListener(l);
    }
    
    public SearchContext clone() {
        try {
            SearchContext context = null;
            context = (SearchContext)super.clone();
            context.support = new PropertyChangeSupport(context);
            return context;
        }
        catch (CloneNotSupportedException cnse) {
            throw new RuntimeException("Should never happen", cnse);
        }
    }
    
    protected void firePropertyChange(final String property, final boolean oldValue, final boolean newValue) {
        this.support.firePropertyChange(property, oldValue, newValue);
    }
    
    protected void firePropertyChange(final String property, final String oldValue, final String newValue) {
        this.support.firePropertyChange(property, oldValue, newValue);
    }
    
    public boolean getMarkAll() {
        return this.markAll;
    }
    
    public boolean getMatchCase() {
        return this.matchCase;
    }
    
    public String getReplaceWith() {
        return this.replaceWith;
    }
    
    public String getSearchFor() {
        return this.searchFor;
    }
    
    public boolean getSearchForward() {
        return this.forward;
    }
    
    public boolean getSearchSelectionOnly() {
        return this.selectionOnly;
    }
    
    public boolean getWholeWord() {
        return this.wholeWord;
    }
    
    public boolean isRegularExpression() {
        return this.regex;
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        this.support.removePropertyChangeListener(l);
    }
    
    public void setMarkAll(final boolean markAll) {
        if (markAll != this.markAll) {
            this.markAll = markAll;
            this.firePropertyChange("Search.MarkAll", !markAll, markAll);
        }
    }
    
    public void setMatchCase(final boolean matchCase) {
        if (matchCase != this.matchCase) {
            this.matchCase = matchCase;
            this.firePropertyChange("Search.MatchCase", !matchCase, matchCase);
        }
    }
    
    public void setRegularExpression(final boolean regex) {
        if (regex != this.regex) {
            this.regex = regex;
            this.firePropertyChange("Search.UseRegex", !regex, regex);
        }
    }
    
    public void setReplaceWith(final String replaceWith) {
        if ((replaceWith == null && this.replaceWith != null) || (replaceWith != null && !replaceWith.equals(this.replaceWith))) {
            final String old = this.replaceWith;
            this.firePropertyChange("Search.replaceWith", old, this.replaceWith = replaceWith);
        }
    }
    
    public void setSearchFor(final String searchFor) {
        if ((searchFor == null && this.searchFor != null) || (searchFor != null && !searchFor.equals(this.searchFor))) {
            final String old = this.searchFor;
            this.firePropertyChange("Search.searchFor", old, this.searchFor = searchFor);
        }
    }
    
    public void setSearchForward(final boolean forward) {
        if (forward != this.forward) {
            this.forward = forward;
            this.firePropertyChange("Search.Forward", !forward, forward);
        }
    }
    
    public void setSearchSelectionOnly(final boolean selectionOnly) {
        if (selectionOnly != this.selectionOnly) {
            this.selectionOnly = selectionOnly;
            this.firePropertyChange("Search.SelectionOnly", !selectionOnly, selectionOnly);
            if (selectionOnly) {
                throw new UnsupportedOperationException("Searching in selection is not currently supported");
            }
        }
    }
    
    public void setWholeWord(final boolean wholeWord) {
        if (wholeWord != this.wholeWord) {
            this.wholeWord = wholeWord;
            this.firePropertyChange("Search.MatchWholeWord", !wholeWord, wholeWord);
        }
    }
    
    public String toString() {
        return "[SearchContext: searchFor='" + this.getSearchFor() + "'" + ", replaceWith='" + this.getReplaceWith() + "'" + ", matchCase=" + this.getMatchCase() + ", wholeWord=" + this.getWholeWord() + ", regex=" + this.isRegularExpression() + ", markAll=" + this.getMarkAll() + "]";
    }
}
