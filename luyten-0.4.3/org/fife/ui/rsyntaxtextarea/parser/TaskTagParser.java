package org.fife.ui.rsyntaxtextarea.parser;

import java.awt.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.regex.*;

public class TaskTagParser extends AbstractParser
{
    private DefaultParseResult result;
    private String DEFAULT_TASK_PATTERN;
    private Pattern taskPattern;
    private static final Color COLOR;
    
    public TaskTagParser() {
        super();
        this.DEFAULT_TASK_PATTERN = "TODO|FIXME|HACK";
        this.result = new DefaultParseResult(this);
        this.setTaskPattern(this.DEFAULT_TASK_PATTERN);
    }
    
    public String getTaskPattern() {
        return (this.taskPattern == null) ? null : this.taskPattern.pattern();
    }
    
    public ParseResult parse(final RSyntaxDocument doc, final String style) {
        final Element root = doc.getDefaultRootElement();
        final int lineCount = root.getElementCount();
        if (this.taskPattern == null || style == null || "text/plain".equals(style)) {
            this.result.clearNotices();
            this.result.setParsedLines(0, lineCount - 1);
            return this.result;
        }
        this.result.clearNotices();
        this.result.setParsedLines(0, lineCount - 1);
        for (int line = 0; line < lineCount; ++line) {
            Token t = doc.getTokenListForLine(line);
            int offs = -1;
            int start = -1;
            String text = null;
            while (t != null && t.isPaintable()) {
                if (t.isComment()) {
                    offs = t.getOffset();
                    text = t.getLexeme();
                    final Matcher m = this.taskPattern.matcher(text);
                    if (m.find()) {
                        start = m.start();
                        offs += start;
                        break;
                    }
                }
                t = t.getNextToken();
            }
            if (start > -1) {
                text = text.substring(start);
                final int len = text.length();
                final TaskNotice pn = new TaskNotice(this, text, line, offs, len);
                pn.setLevel(ParserNotice.Level.INFO);
                pn.setShowInEditor(false);
                pn.setColor(TaskTagParser.COLOR);
                this.result.addNotice(pn);
            }
        }
        return this.result;
    }
    
    public void setTaskPattern(final String pattern) throws PatternSyntaxException {
        if (pattern == null || pattern.length() == 0) {
            this.taskPattern = null;
        }
        else {
            this.taskPattern = Pattern.compile(pattern);
        }
    }
    
    static {
        COLOR = new Color(48, 150, 252);
    }
    
    public static class TaskNotice extends DefaultParserNotice
    {
        public TaskNotice(final Parser parser, final String message, final int line, final int offs, final int length) {
            super(parser, message, line, offs, length);
        }
    }
}
