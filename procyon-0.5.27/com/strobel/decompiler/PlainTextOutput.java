package com.strobel.decompiler;

import java.lang.reflect.*;
import java.io.*;
import com.strobel.core.*;

public class PlainTextOutput implements ITextOutput
{
    private static final String NULL_TEXT;
    private final Writer _writer;
    private String _indentToken;
    private int _indent;
    private boolean _needsIndent;
    private boolean _isUnicodeOutputEnabled;
    protected int line;
    protected int column;
    
    static {
        NULL_TEXT = String.valueOf((Object)null);
    }
    
    public PlainTextOutput() {
        super();
        this._indentToken = "    ";
        this.line = 1;
        this.column = 1;
        this._writer = new StringWriter();
    }
    
    public PlainTextOutput(final Writer writer) {
        super();
        this._indentToken = "    ";
        this.line = 1;
        this.column = 1;
        this._writer = VerifyArgument.notNull(writer, "writer");
    }
    
    @Override
    public final String getIndentToken() {
        final String indentToken = this._indentToken;
        return (indentToken != null) ? indentToken : "";
    }
    
    @Override
    public final void setIndentToken(final String indentToken) {
        this._indentToken = indentToken;
    }
    
    public final boolean isUnicodeOutputEnabled() {
        return this._isUnicodeOutputEnabled;
    }
    
    public final void setUnicodeOutputEnabled(final boolean unicodeOutputEnabled) {
        this._isUnicodeOutputEnabled = unicodeOutputEnabled;
    }
    
    protected void writeIndent() {
        if (this._needsIndent) {
            this._needsIndent = false;
            final String indentToken = this.getIndentToken();
            for (int i = 0; i < this._indent; ++i) {
                try {
                    this._writer.write(indentToken);
                }
                catch (IOException e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
            this.column += indentToken.length() * this._indent;
        }
    }
    
    @Override
    public int getRow() {
        return this.line;
    }
    
    @Override
    public int getColumn() {
        return this._needsIndent ? (this.column + this._indent * this.getIndentToken().length()) : this.column;
    }
    
    @Override
    public void indent() {
        ++this._indent;
    }
    
    @Override
    public void unindent() {
        --this._indent;
    }
    
    @Override
    public void write(final char ch) {
        this.writeIndent();
        try {
            if (this.isUnicodeOutputEnabled()) {
                this._writer.write(ch);
            }
            else {
                this._writer.write(StringUtilities.escape(ch));
            }
            ++this.column;
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    @Override
    public void write(final String text) {
        this.writeRaw(this.isUnicodeOutputEnabled() ? text : StringUtilities.escape(text));
    }
    
    protected void writeRaw(final String text) {
        this.writeIndent();
        try {
            final int length = (text != null) ? text.length() : PlainTextOutput.NULL_TEXT.length();
            this._writer.write(text);
            this.column += length;
            if (text == null) {
                return;
            }
            boolean newLineSeen = false;
            for (int i = 0; i < length; ++i) {
                if (text.charAt(i) == '\n') {
                    ++this.line;
                    this.column = 0;
                    newLineSeen = true;
                }
                else if (newLineSeen) {
                    ++this.column;
                }
            }
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
    
    @Override
    public void writeError(final String value) {
        this.write(value);
    }
    
    @Override
    public void writeLabel(final String value) {
        this.write(value);
    }
    
    @Override
    public void writeLiteral(final Object value) {
        this.write(String.valueOf(value));
    }
    
    @Override
    public void writeTextLiteral(final Object value) {
        this.write(String.valueOf(value));
    }
    
    @Override
    public void writeComment(final String value) {
        this.write(value);
    }
    
    @Override
    public void writeComment(final String format, final Object... args) {
        this.write(format, args);
    }
    
    @Override
    public void write(final String format, final Object... args) {
        this.write(String.format(format, args));
    }
    
    @Override
    public void writeLine(final String text) {
        this.write(text);
        this.writeLine();
    }
    
    @Override
    public void writeLine(final String format, final Object... args) {
        this.write(String.format(format, args));
        this.writeLine();
    }
    
    @Override
    public void writeLine() {
        this.writeIndent();
        try {
            this._writer.write("\n");
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
        this._needsIndent = true;
        ++this.line;
        this.column = 1;
    }
    
    @Override
    public void writeDelimiter(final String text) {
        this.write(text);
    }
    
    @Override
    public void writeOperator(final String text) {
        this.write(text);
    }
    
    @Override
    public void writeKeyword(final String text) {
        this.write(text);
    }
    
    @Override
    public void writeAttribute(final String text) {
        this.write(text);
    }
    
    @Override
    public void writeDefinition(final String text, final Object definition) {
        this.writeDefinition(text, definition, true);
    }
    
    @Override
    public void writeDefinition(final String text, final Object definition, final boolean isLocal) {
        this.write(text);
    }
    
    @Override
    public void writeReference(final String text, final Object reference) {
        this.writeReference(text, reference, false);
    }
    
    @Override
    public void writeReference(final String text, final Object reference, final boolean isLocal) {
        this.write(text);
    }
    
    @Override
    public boolean isFoldingSupported() {
        return false;
    }
    
    @Override
    public void markFoldStart(final String collapsedText, final boolean defaultCollapsed) {
    }
    
    @Override
    public void markFoldEnd() {
    }
    
    @Override
    public String toString() {
        return this._writer.toString();
    }
}
