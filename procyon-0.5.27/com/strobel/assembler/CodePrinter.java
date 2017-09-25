package com.strobel.assembler;

import java.io.*;
import java.util.*;

public class CodePrinter extends PrintWriter
{
    public CodePrinter(final Writer out) {
        super(out, true);
    }
    
    public CodePrinter(final Writer out, final boolean autoFlush) {
        super(out, autoFlush);
    }
    
    public CodePrinter(final OutputStream out) {
        super(out);
    }
    
    public CodePrinter(final OutputStream out, final boolean autoFlush) {
        super(out, autoFlush);
    }
    
    public CodePrinter(final String fileName) throws FileNotFoundException {
        super(fileName);
    }
    
    public CodePrinter(final String fileName, final String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }
    
    public CodePrinter(final File file) throws FileNotFoundException {
        super(file);
    }
    
    public CodePrinter(final File file, final String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }
    
    @Override
    public CodePrinter printf(final String format, final Object... args) {
        return (CodePrinter)super.printf(format, args);
    }
    
    @Override
    public CodePrinter printf(final Locale l, final String format, final Object... args) {
        return (CodePrinter)super.printf(l, format, args);
    }
    
    @Override
    public CodePrinter format(final String format, final Object... args) {
        return (CodePrinter)super.format(format, args);
    }
    
    @Override
    public CodePrinter format(final Locale l, final String format, final Object... args) {
        return (CodePrinter)super.format(l, format, args);
    }
    
    public void increaseIndent() {
    }
    
    public void decreaseIndent() {
    }
}
