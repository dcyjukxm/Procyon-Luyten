package org.fife.ui.rtextarea;

import org.fife.ui.rsyntaxtextarea.*;

public class SearchResult implements Comparable<SearchResult>
{
    private DocumentRange matchRange;
    private int count;
    private int markedCount;
    
    public SearchResult() {
        this(null, 0, 0);
    }
    
    public SearchResult(final DocumentRange range, final int count, final int markedCount) {
        super();
        this.matchRange = range;
        this.count = count;
        this.markedCount = markedCount;
    }
    
    public int compareTo(final SearchResult other) {
        if (other == null) {
            return 1;
        }
        if (other == this) {
            return 0;
        }
        int diff = this.count - other.count;
        if (diff != 0) {
            return diff;
        }
        diff = this.markedCount - other.markedCount;
        if (diff != 0) {
            return diff;
        }
        if (this.matchRange == null) {
            return (other.matchRange == null) ? 0 : -1;
        }
        return this.matchRange.compareTo(other.matchRange);
    }
    
    public boolean equals(final Object other) {
        return other == this || (other instanceof SearchResult && this.compareTo((SearchResult)other) == 0);
    }
    
    public int getCount() {
        return this.count;
    }
    
    public int getMarkedCount() {
        return this.markedCount;
    }
    
    public DocumentRange getMatchRange() {
        return this.matchRange;
    }
    
    public int hashCode() {
        int hash = this.count + this.markedCount;
        if (this.matchRange != null) {
            hash += this.matchRange.hashCode();
        }
        return hash;
    }
    
    public void setCount(final int count) {
        this.count = count;
    }
    
    public void setMarkedCount(final int markedCount) {
        this.markedCount = markedCount;
    }
    
    public void setMatchRange(final DocumentRange range) {
        this.matchRange = range;
    }
    
    public String toString() {
        return "[SearchResult: count=" + this.getCount() + ", markedCount=" + this.getMarkedCount() + ", matchRange=" + this.getMatchRange() + "]";
    }
    
    public boolean wasFound() {
        return this.getCount() > 0;
    }
}
