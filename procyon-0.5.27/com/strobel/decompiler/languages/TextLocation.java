package com.strobel.decompiler.languages;

import java.io.*;

public final class TextLocation implements Comparable<TextLocation>, Serializable
{
    private static final long serialVersionUID = -165593440170614692L;
    public static final int MIN_LINE = 1;
    public static final int MIN_COLUMN = 1;
    public static final TextLocation EMPTY;
    private final int _line;
    private final int _column;
    
    static {
        EMPTY = new TextLocation();
    }
    
    private TextLocation() {
        super();
        this._line = 0;
        this._column = 0;
    }
    
    public TextLocation(final int line, final int column) {
        super();
        this._line = line;
        this._column = column;
    }
    
    public final int line() {
        return this._line;
    }
    
    public final int column() {
        return this._column;
    }
    
    public final boolean isEmpty() {
        return this._line < 1 && this._column < 1;
    }
    
    public final boolean isBefore(final TextLocation other) {
        return other != null && !other.isEmpty() && (this._line < other._line || (this._line == other._line && this._column < other._column));
    }
    
    public final boolean isAfter(final TextLocation other) {
        return other == null || other.isEmpty() || this._line > other._line || (this._line == other._line && this._column > other._column);
    }
    
    @Override
    public final String toString() {
        return String.format("(Line %d, Column %d)", this._line, this._column);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public final int compareTo(final TextLocation o) {
        if (this.isBefore(o)) {
            return -1;
        }
        if (this.isAfter(o)) {
            return 1;
        }
        return 0;
    }
}
