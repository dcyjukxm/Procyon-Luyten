package com.strobel.decompiler;

import java.io.*;
import java.util.*;

class LineNumberPrintWriter extends PrintWriter
{
    public static final int NO_LINE_NUMBER = -1;
    private final String _emptyPrefix;
    private final String _format;
    private boolean _needsPrefix;
    private boolean _suppressLineNumbers;
    
    public LineNumberPrintWriter(final int maxLineNo, final Writer w) {
        super(w);
        final String maxNumberString = String.format("%d", maxLineNo);
        final int numberWidth = maxNumberString.length();
        this._format = "/*%" + numberWidth + "d*/";
        final String samplePrefix = String.format(this._format, maxLineNo);
        final char[] prefixChars = samplePrefix.toCharArray();
        Arrays.fill(prefixChars, ' ');
        this._emptyPrefix = new String(prefixChars);
        this._needsPrefix = true;
    }
    
    public void suppressLineNumbers() {
        this._suppressLineNumbers = true;
    }
    
    @Override
    public void print(final String s) {
        this.print(-1, s);
    }
    
    @Override
    public void println(final String s) {
        this.println(-1, s);
    }
    
    public void println(final int lineNumber, final String s) {
        this.doPrefix(lineNumber);
        super.println(s);
        this._needsPrefix = true;
    }
    
    public void print(final int lineNumber, final String s) {
        this.doPrefix(lineNumber);
        super.print(s);
    }
    
    private void doPrefix(final int lineNumber) {
        if (this._needsPrefix && !this._suppressLineNumbers) {
            if (lineNumber == -1) {
                super.print(this._emptyPrefix);
            }
            else {
                final String prefix = String.format(this._format, lineNumber);
                super.print(prefix);
            }
        }
        this._needsPrefix = false;
    }
}
