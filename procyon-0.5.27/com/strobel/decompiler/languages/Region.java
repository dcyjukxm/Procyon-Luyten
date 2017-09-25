package com.strobel.decompiler.languages;

import java.io.*;
import com.strobel.core.*;

public final class Region implements Serializable
{
    private static final long serialVersionUID = -7580225960304530502L;
    public static final Region EMPTY;
    private final String _fileName;
    private final int _beginLine;
    private final int _endLine;
    private final int _beginColumn;
    private final int _endColumn;
    
    static {
        EMPTY = new Region(0, 0, 0, 0);
    }
    
    public Region(final TextLocation begin, final TextLocation end) {
        this(null, begin, end);
    }
    
    public Region(final String fileName, final TextLocation begin, final TextLocation end) {
        this(fileName, (begin != null) ? begin.line() : TextLocation.EMPTY.line(), (end != null) ? end.line() : TextLocation.EMPTY.line(), (begin != null) ? begin.column() : TextLocation.EMPTY.column(), (end != null) ? end.column() : TextLocation.EMPTY.column());
    }
    
    public Region(final int beginLine, final int endLine, final int beginColumn, final int endColumn) {
        this(null, beginLine, endLine, beginColumn, endColumn);
    }
    
    public Region(final String fileName, final int beginLine, final int endLine, final int beginColumn, final int endColumn) {
        super();
        this._fileName = fileName;
        this._beginLine = beginLine;
        this._endLine = endLine;
        this._beginColumn = beginColumn;
        this._endColumn = endColumn;
    }
    
    public final String getFileName() {
        return this._fileName;
    }
    
    public final int getBeginLine() {
        return this._beginLine;
    }
    
    public final int getEndLine() {
        return this._endLine;
    }
    
    public final int getBeginColumn() {
        return this._beginColumn;
    }
    
    public final int getEndColumn() {
        return this._endColumn;
    }
    
    public final boolean isEmpty() {
        return this._beginColumn <= 0;
    }
    
    public final boolean isInside(final int line, final int column) {
        return !this.isEmpty() && (line >= this._beginLine && (line <= this._endLine || this._endLine == -1) && (line != this._beginLine || column >= this._beginColumn) && (line != this._endLine || column <= this._endColumn));
    }
    
    public final boolean IsInside(final TextLocation location) {
        return this.isInside((location != null) ? location.line() : TextLocation.EMPTY.line(), (location != null) ? location.column() : TextLocation.EMPTY.column());
    }
    
    @Override
    public final int hashCode() {
        return ((this._fileName != null) ? this._fileName.hashCode() : 0) ^ this._beginColumn + 1100009 * this._beginLine + 1200007 * this._endLine + 1300021 * this._endColumn;
    }
    
    @Override
    public final boolean equals(final Object obj) {
        if (obj instanceof Region) {
            final Region other = (Region)obj;
            return other._beginLine == this._beginLine && other._beginColumn == this._beginColumn && other._endLine == this._endLine && other._endColumn == this._endColumn && StringUtilities.equals(other._fileName, this._fileName);
        }
        return false;
    }
    
    @Override
    public final String toString() {
        return String.format("[Region FileName=%s, Begin=(%d, %d), End=(%d, %d)]", this._fileName, this._beginLine, this._beginColumn, this._endLine, this._endColumn);
    }
}
