package com.strobel.decompiler.languages;

import java.util.*;

public class LineNumberPosition
{
    private final int _originalLine;
    private final int _emittedLine;
    private final int _emittedColumn;
    
    public LineNumberPosition(final int originalLine, final int emittedLine, final int emittedColumn) {
        super();
        this._originalLine = originalLine;
        this._emittedLine = emittedLine;
        this._emittedColumn = emittedColumn;
    }
    
    public int getOriginalLine() {
        return this._originalLine;
    }
    
    public int getEmittedLine() {
        return this._emittedLine;
    }
    
    public int getEmittedColumn() {
        return this._emittedColumn;
    }
    
    public static int computeMaxLineNumber(final List<LineNumberPosition> lineNumPositions) {
        int maxLineNo = 1;
        for (final LineNumberPosition pos : lineNumPositions) {
            final int originalLine = pos.getOriginalLine();
            maxLineNo = Math.max(maxLineNo, originalLine);
        }
        return maxLineNo;
    }
    
    @Override
    public String toString() {
        return "Line # Position : {orig=" + this._originalLine + ", " + "emitted=" + this._emittedLine + "/" + this._emittedColumn + "}";
    }
}
