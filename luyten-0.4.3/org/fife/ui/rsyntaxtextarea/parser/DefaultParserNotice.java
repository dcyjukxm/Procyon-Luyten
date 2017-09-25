package org.fife.ui.rsyntaxtextarea.parser;

import java.awt.*;

public class DefaultParserNotice implements ParserNotice
{
    private Parser parser;
    private Level level;
    private int line;
    private int offset;
    private int length;
    private boolean showInEditor;
    private Color color;
    private String message;
    private String toolTipText;
    private static final Color[] DEFAULT_COLORS;
    
    public DefaultParserNotice(final Parser parser, final String msg, final int line) {
        this(parser, msg, line, -1, -1);
    }
    
    public DefaultParserNotice(final Parser parser, final String message, final int line, final int offset, final int length) {
        super();
        this.parser = parser;
        this.message = message;
        this.line = line;
        this.offset = offset;
        this.length = length;
        this.setLevel(Level.ERROR);
        this.setShowInEditor(true);
    }
    
    public int compareTo(final ParserNotice other) {
        int diff = -1;
        if (other != null) {
            diff = this.level.getNumericValue() - other.getLevel().getNumericValue();
            if (diff == 0) {
                diff = this.line - other.getLine();
                if (diff == 0) {
                    diff = this.message.compareTo(other.getMessage());
                }
            }
        }
        return diff;
    }
    
    public boolean containsPosition(final int pos) {
        return this.offset <= pos && pos < this.offset + this.length;
    }
    
    public boolean equals(final Object obj) {
        return obj instanceof ParserNotice && this.compareTo((ParserNotice)obj) == 0;
    }
    
    public Color getColor() {
        Color c = this.color;
        if (c == null) {
            c = DefaultParserNotice.DEFAULT_COLORS[this.getLevel().getNumericValue()];
        }
        return c;
    }
    
    public boolean getKnowsOffsetAndLength() {
        return this.offset >= 0 && this.length >= 0;
    }
    
    public int getLength() {
        return this.length;
    }
    
    public Level getLevel() {
        return this.level;
    }
    
    public int getLine() {
        return this.line;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public int getOffset() {
        return this.offset;
    }
    
    public Parser getParser() {
        return this.parser;
    }
    
    public boolean getShowInEditor() {
        return this.showInEditor;
    }
    
    public String getToolTipText() {
        return (this.toolTipText != null) ? this.toolTipText : this.getMessage();
    }
    
    public int hashCode() {
        return this.line << 16 | this.offset;
    }
    
    public void setColor(final Color color) {
        this.color = color;
    }
    
    public void setLevel(Level level) {
        if (level == null) {
            level = Level.ERROR;
        }
        this.level = level;
    }
    
    public void setShowInEditor(final boolean show) {
        this.showInEditor = show;
    }
    
    public void setToolTipText(final String text) {
        this.toolTipText = text;
    }
    
    public String toString() {
        return "Line " + this.getLine() + ": " + this.getMessage();
    }
    
    static {
        DEFAULT_COLORS = new Color[] { new Color(255, 0, 128), new Color(244, 200, 45), Color.gray };
    }
}
