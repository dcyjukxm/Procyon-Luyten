package org.fife.ui.rsyntaxtextarea;

import java.util.*;
import org.fife.ui.rtextarea.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import java.util.regex.*;

public class RSyntaxUtilities implements SwingConstants
{
    public static final int OS_WINDOWS = 1;
    public static final int OS_MAC_OSX = 2;
    public static final int OS_LINUX = 4;
    public static final int OS_OTHER = 8;
    private static final Color LIGHT_HYPERLINK_FG;
    private static final int OS;
    private static final int LETTER_MASK = 2;
    private static final int HEX_CHARACTER_MASK = 16;
    private static final int LETTER_OR_DIGIT_MASK = 32;
    private static final int BRACKET_MASK = 64;
    private static final int JAVA_OPERATOR_MASK = 128;
    private static final int[] dataTable;
    private static Segment charSegment;
    private static final TokenImpl tempToken;
    private static final char[] JS_KEYWORD_RETURN;
    private static final char[] JS_AND;
    private static final char[] JS_OR;
    private static final String BRACKETS = "{([})]";
    
    public static final String escapeForHtml(final String s, String newlineReplacement, final boolean inPreBlock) {
        if (s == null) {
            return null;
        }
        if (newlineReplacement == null) {
            newlineReplacement = "";
        }
        final String tabString = "   ";
        boolean lastWasSpace = false;
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            final char ch = s.charAt(i);
            switch (ch) {
                case ' ': {
                    if (inPreBlock || !lastWasSpace) {
                        sb.append(' ');
                    }
                    else {
                        sb.append("&nbsp;");
                    }
                    lastWasSpace = true;
                    break;
                }
                case '\n': {
                    sb.append(newlineReplacement);
                    lastWasSpace = false;
                    break;
                }
                case '&': {
                    sb.append("&amp;");
                    lastWasSpace = false;
                    break;
                }
                case '\t': {
                    sb.append("   ");
                    lastWasSpace = false;
                    break;
                }
                case '<': {
                    sb.append("&lt;");
                    lastWasSpace = false;
                    break;
                }
                case '>': {
                    sb.append("&gt;");
                    lastWasSpace = false;
                    break;
                }
                default: {
                    sb.append(ch);
                    lastWasSpace = false;
                    break;
                }
            }
        }
        return sb.toString();
    }
    
    public static Map<?, ?> getDesktopAntiAliasHints() {
        return (Map)Toolkit.getDefaultToolkit().getDesktopProperty("awt.font.desktophints");
    }
    
    public static Color getFoldedLineBottomColor(final RSyntaxTextArea textArea) {
        Color color = Color.gray;
        final Gutter gutter = getGutter(textArea);
        if (gutter != null) {
            color = gutter.getFoldIndicatorForeground();
        }
        return color;
    }
    
    public static Gutter getGutter(final RTextArea textArea) {
        Gutter gutter = null;
        Container parent = textArea.getParent();
        if (parent instanceof JViewport) {
            parent = parent.getParent();
            if (parent instanceof RTextScrollPane) {
                final RTextScrollPane sp = (RTextScrollPane)parent;
                gutter = sp.getGutter();
            }
        }
        return gutter;
    }
    
    public static final Color getHyperlinkForeground() {
        Color fg = UIManager.getColor("Label.foreground");
        if (fg == null) {
            fg = new JLabel().getForeground();
        }
        return isLightForeground(fg) ? RSyntaxUtilities.LIGHT_HYPERLINK_FG : Color.blue;
    }
    
    public static String getLeadingWhitespace(final String text) {
        int count = 0;
        for (int len = text.length(); count < len && isWhitespace(text.charAt(count)); ++count) {}
        return text.substring(0, count);
    }
    
    public static String getLeadingWhitespace(final Document doc, final int offs) throws BadLocationException {
        final Element root = doc.getDefaultRootElement();
        final int line = root.getElementIndex(offs);
        final Element elem = root.getElement(line);
        final int startOffs = elem.getStartOffset();
        final int endOffs = elem.getEndOffset() - 1;
        final String text = doc.getText(startOffs, endOffs - startOffs);
        return getLeadingWhitespace(text);
    }
    
    private static final Element getLineElem(final Document d, final int offs) {
        final Element map = d.getDefaultRootElement();
        final int index = map.getElementIndex(offs);
        final Element elem = map.getElement(index);
        if (offs >= elem.getStartOffset() && offs < elem.getEndOffset()) {
            return elem;
        }
        return null;
    }
    
    public static Rectangle getLineWidthUpTo(final RSyntaxTextArea textArea, final Segment s, final int p0, final int p1, final TabExpander e, Rectangle rect, final int x0) throws BadLocationException {
        final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
        if (p0 < 0) {
            throw new BadLocationException("Invalid document position", p0);
        }
        if (p1 > doc.getLength()) {
            throw new BadLocationException("Invalid document position", p1);
        }
        final Element map = doc.getDefaultRootElement();
        final int lineNum = map.getElementIndex(p0);
        if (Math.abs(lineNum - map.getElementIndex(p1)) > 1) {
            throw new IllegalArgumentException("p0 and p1 are not on the same line (" + p0 + ", " + p1 + ").");
        }
        Token t = doc.getTokenListForLine(lineNum);
        final TokenUtils.TokenSubList subList = TokenUtils.getSubTokenList(t, p0, e, textArea, 0.0f, RSyntaxUtilities.tempToken);
        t = subList.tokenList;
        rect = t.listOffsetToView(textArea, e, p1, x0, rect);
        return rect;
    }
    
    public static Point getMatchingBracketPosition(final RSyntaxTextArea textArea, Point input) {
        if (input == null) {
            input = new Point();
        }
        input.setLocation(-1, -1);
        try {
            int caretPosition = textArea.getCaretPosition() - 1;
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            char bracket = '\0';
            if (caretPosition >= 0) {
                bracket = doc.charAt(caretPosition);
            }
            int index = "{([})]".indexOf(bracket);
            if (index == -1 && caretPosition < doc.getLength() - 1) {
                bracket = doc.charAt(++caretPosition);
            }
            if (index == -1) {
                index = "{([})]".indexOf(bracket);
                if (index == -1) {
                    return input;
                }
            }
            final Element map = doc.getDefaultRootElement();
            int curLine = map.getElementIndex(caretPosition);
            Element line = map.getElement(curLine);
            int start = line.getStartOffset();
            int end = line.getEndOffset();
            Token token = doc.getTokenListForLine(curLine);
            token = getTokenAtOffset(token, caretPosition);
            if (token.getType() != 22) {
                return input;
            }
            final int languageIndex = token.getLanguageIndex();
            boolean goForward;
            char bracketMatch;
            if (index < 3) {
                goForward = true;
                bracketMatch = "{([})]".charAt(index + 3);
            }
            else {
                goForward = false;
                bracketMatch = "{([})]".charAt(index - 3);
            }
            if (goForward) {
                final int lastLine = map.getElementCount();
                start = caretPosition + 1;
                int numEmbedded = 0;
                boolean haveTokenList = false;
                while (true) {
                    doc.getText(start, end - start, RSyntaxUtilities.charSegment);
                    int i;
                    for (int segOffset = i = RSyntaxUtilities.charSegment.offset; i < segOffset + RSyntaxUtilities.charSegment.count; ++i) {
                        final char ch = RSyntaxUtilities.charSegment.array[i];
                        if (ch == bracket) {
                            if (!haveTokenList) {
                                token = doc.getTokenListForLine(curLine);
                                haveTokenList = true;
                            }
                            final int offset = start + (i - segOffset);
                            token = getTokenAtOffset(token, offset);
                            if (token.getType() == 22 && token.getLanguageIndex() == languageIndex) {
                                ++numEmbedded;
                            }
                        }
                        else if (ch == bracketMatch) {
                            if (!haveTokenList) {
                                token = doc.getTokenListForLine(curLine);
                                haveTokenList = true;
                            }
                            final int offset = start + (i - segOffset);
                            token = getTokenAtOffset(token, offset);
                            if (token.getType() == 22 && token.getLanguageIndex() == languageIndex) {
                                if (numEmbedded == 0) {
                                    if (textArea.isCodeFoldingEnabled() && textArea.getFoldManager().isLineHidden(curLine)) {
                                        return input;
                                    }
                                    input.setLocation(caretPosition, offset);
                                    return input;
                                }
                                else {
                                    --numEmbedded;
                                }
                            }
                        }
                    }
                    if (++curLine == lastLine) {
                        return input;
                    }
                    haveTokenList = false;
                    line = map.getElement(curLine);
                    start = line.getStartOffset();
                    end = line.getEndOffset();
                }
            }
            else {
                end = caretPosition;
                int numEmbedded2 = 0;
                boolean haveTokenList2 = false;
                while (true) {
                    doc.getText(start, end - start, RSyntaxUtilities.charSegment);
                    int j;
                    for (int segOffset = RSyntaxUtilities.charSegment.offset, iStart = j = segOffset + RSyntaxUtilities.charSegment.count - 1; j >= segOffset; --j) {
                        final char ch2 = RSyntaxUtilities.charSegment.array[j];
                        if (ch2 == bracket) {
                            if (!haveTokenList2) {
                                token = doc.getTokenListForLine(curLine);
                                haveTokenList2 = true;
                            }
                            final int offset2 = start + (j - segOffset);
                            final Token t2 = getTokenAtOffset(token, offset2);
                            if (t2.getType() == 22 && token.getLanguageIndex() == languageIndex) {
                                ++numEmbedded2;
                            }
                        }
                        else if (ch2 == bracketMatch) {
                            if (!haveTokenList2) {
                                token = doc.getTokenListForLine(curLine);
                                haveTokenList2 = true;
                            }
                            final int offset2 = start + (j - segOffset);
                            final Token t2 = getTokenAtOffset(token, offset2);
                            if (t2.getType() == 22 && token.getLanguageIndex() == languageIndex) {
                                if (numEmbedded2 == 0) {
                                    input.setLocation(caretPosition, offset2);
                                    return input;
                                }
                                --numEmbedded2;
                            }
                        }
                    }
                    if (--curLine == -1) {
                        return input;
                    }
                    haveTokenList2 = false;
                    line = map.getElement(curLine);
                    start = line.getStartOffset();
                    end = line.getEndOffset();
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
            return input;
        }
    }
    
    public static final Token getNextImportantToken(Token t, final RSyntaxTextArea textArea, int line) {
        while (t != null && t.isPaintable() && t.isCommentOrWhitespace()) {
            t = t.getNextToken();
        }
        if ((t == null || !t.isPaintable()) && line < textArea.getLineCount() - 1) {
            t = textArea.getTokenListForLine(++line);
            return getNextImportantToken(t, textArea, line);
        }
        return t;
    }
    
    public static int getNextVisualPositionFrom(int pos, final Position.Bias b, final Shape a, final int direction, final Position.Bias[] biasRet, final View view) throws BadLocationException {
        final RSyntaxTextArea target = (RSyntaxTextArea)view.getContainer();
        biasRet[0] = Position.Bias.Forward;
        switch (direction) {
            case 1:
            case 5: {
                if (pos == -1) {
                    pos = ((direction == 1) ? Math.max(0, view.getEndOffset() - 1) : view.getStartOffset());
                    break;
                }
                final Caret c = (target != null) ? target.getCaret() : null;
                Point mcp;
                if (c != null) {
                    mcp = c.getMagicCaretPosition();
                }
                else {
                    mcp = null;
                }
                int x;
                if (mcp == null) {
                    final Rectangle loc = target.modelToView(pos);
                    x = ((loc == null) ? 0 : loc.x);
                }
                else {
                    x = mcp.x;
                }
                if (direction == 1) {
                    pos = getPositionAbove(target, pos, x, (TabExpander)view);
                    break;
                }
                pos = getPositionBelow(target, pos, x, (TabExpander)view);
                break;
            }
            case 7: {
                if (pos == -1) {
                    pos = Math.max(0, view.getEndOffset() - 1);
                    break;
                }
                pos = Math.max(0, pos - 1);
                if (target.isCodeFoldingEnabled()) {
                    final int last = target.getLineOfOffset(pos + 1);
                    int current = target.getLineOfOffset(pos);
                    if (last != current) {
                        final FoldManager fm = target.getFoldManager();
                        if (fm.isLineHidden(current)) {
                            while (--current > 0 && fm.isLineHidden(current)) {}
                            pos = target.getLineEndOffset(current) - 1;
                        }
                    }
                    break;
                }
                break;
            }
            case 3: {
                if (pos == -1) {
                    pos = view.getStartOffset();
                    break;
                }
                pos = Math.min(pos + 1, view.getDocument().getLength());
                if (target.isCodeFoldingEnabled()) {
                    final int last = target.getLineOfOffset(pos - 1);
                    int current = target.getLineOfOffset(pos);
                    if (last != current) {
                        final FoldManager fm = target.getFoldManager();
                        if (fm.isLineHidden(current)) {
                            final int lineCount = target.getLineCount();
                            while (++current < lineCount && fm.isLineHidden(current)) {}
                            pos = ((current == lineCount) ? (target.getLineEndOffset(last) - 1) : target.getLineStartOffset(current));
                        }
                    }
                    break;
                }
                break;
            }
            default: {
                throw new IllegalArgumentException("Bad direction: " + direction);
            }
        }
        return pos;
    }
    
    public static final int getOS() {
        return RSyntaxUtilities.OS;
    }
    
    private static final int getOSImpl() {
        int os = 8;
        String osName = System.getProperty("os.name");
        if (osName != null) {
            osName = osName.toLowerCase();
            if (osName.indexOf("windows") > -1) {
                os = 1;
            }
            else if (osName.indexOf("mac os x") > -1) {
                os = 2;
            }
            else if (osName.indexOf("linux") > -1) {
                os = 4;
            }
            else {
                os = 8;
            }
        }
        return os;
    }
    
    public static final int getPatternFlags(final boolean matchCase, int others) {
        if (!matchCase) {
            others |= 0x42;
        }
        return others;
    }
    
    public static final int getPositionAbove(final RSyntaxTextArea c, final int offs, final float x, final TabExpander e) throws BadLocationException {
        final TokenOrientedView tov = (TokenOrientedView)e;
        final Token token = tov.getTokenListForPhysicalLineAbove(offs);
        if (token == null) {
            return -1;
        }
        if (token.getType() == 0) {
            final int line = c.getLineOfOffset(offs);
            return c.getLineStartOffset(line - 1);
        }
        return token.getListOffset(c, e, 0.0f, x);
    }
    
    public static final int getPositionBelow(final RSyntaxTextArea c, final int offs, final float x, final TabExpander e) throws BadLocationException {
        final TokenOrientedView tov = (TokenOrientedView)e;
        final Token token = tov.getTokenListForPhysicalLineBelow(offs);
        if (token == null) {
            return -1;
        }
        if (token.getType() == 0) {
            int line = c.getLineOfOffset(offs);
            final FoldManager fm = c.getFoldManager();
            line = fm.getVisibleLineBelow(line);
            return c.getLineStartOffset(line);
        }
        return token.getListOffset(c, e, 0.0f, x);
    }
    
    public static final Token getPreviousImportantToken(final RSyntaxDocument doc, final int line) {
        if (line < 0) {
            return null;
        }
        Token t = doc.getTokenListForLine(line);
        if (t != null) {
            t = t.getLastNonCommentNonWhitespaceToken();
            if (t != null) {
                return t;
            }
        }
        return getPreviousImportantToken(doc, line - 1);
    }
    
    public static final Token getPreviousImportantTokenFromOffs(final RSyntaxDocument doc, final int offs) {
        final Element root = doc.getDefaultRootElement();
        final int line = root.getElementIndex(offs);
        Token t = doc.getTokenListForLine(line);
        Token target = null;
        while (t != null && t.isPaintable() && !t.containsPosition(offs)) {
            if (!t.isCommentOrWhitespace()) {
                target = t;
            }
            t = t.getNextToken();
        }
        if (target == null) {
            target = getPreviousImportantToken(doc, line - 1);
        }
        return target;
    }
    
    public static final Token getTokenAtOffset(final RSyntaxTextArea textArea, final int offset) {
        final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
        return getTokenAtOffset(doc, offset);
    }
    
    public static final Token getTokenAtOffset(final RSyntaxDocument doc, final int offset) {
        final Element root = doc.getDefaultRootElement();
        final int lineIndex = root.getElementIndex(offset);
        final Token t = doc.getTokenListForLine(lineIndex);
        return getTokenAtOffset(t, offset);
    }
    
    public static final Token getTokenAtOffset(final Token tokenList, final int offset) {
        for (Token t = tokenList; t != null && t.isPaintable(); t = t.getNextToken()) {
            if (t.containsPosition(offset)) {
                return t;
            }
        }
        return null;
    }
    
    public static int getWordEnd(final RSyntaxTextArea textArea, int offs) throws BadLocationException {
        final Document doc = textArea.getDocument();
        final int endOffs = textArea.getLineEndOffsetOfCurrentLine();
        final int lineEnd = Math.min(endOffs, doc.getLength());
        if (offs == lineEnd) {
            return offs;
        }
        final String s = doc.getText(offs, lineEnd - offs - 1);
        if (s != null && s.length() > 0) {
            int i = 0;
            final int count = s.length();
            final char ch = s.charAt(i);
            if (Character.isWhitespace(ch)) {
                while (i < count && Character.isWhitespace(s.charAt(i++))) {}
            }
            else if (Character.isLetterOrDigit(ch)) {
                while (i < count && Character.isLetterOrDigit(s.charAt(i++))) {}
            }
            else {
                i = 2;
            }
            offs += i - 1;
        }
        return offs;
    }
    
    public static int getWordStart(final RSyntaxTextArea textArea, int offs) throws BadLocationException {
        final Document doc = textArea.getDocument();
        final Element line = getLineElem(doc, offs);
        if (line == null) {
            throw new BadLocationException("No word at " + offs, offs);
        }
        final int lineStart = line.getStartOffset();
        if (offs == lineStart) {
            return offs;
        }
        final int endOffs = Math.min(offs + 1, doc.getLength());
        final String s = doc.getText(lineStart, endOffs - lineStart);
        if (s != null && s.length() > 0) {
            int i = s.length() - 1;
            final char ch = s.charAt(i);
            if (Character.isWhitespace(ch)) {
                while (i > 0 && Character.isWhitespace(s.charAt(i - 1))) {
                    --i;
                }
                offs = lineStart + i;
            }
            else if (Character.isLetterOrDigit(ch)) {
                while (i > 0 && Character.isLetterOrDigit(s.charAt(i - 1))) {
                    --i;
                }
                offs = lineStart + i;
            }
        }
        return offs;
    }
    
    public static final float getTokenListWidth(final Token tokenList, final RSyntaxTextArea textArea, final TabExpander e) {
        return getTokenListWidth(tokenList, textArea, e, 0.0f);
    }
    
    public static final float getTokenListWidth(final Token tokenList, final RSyntaxTextArea textArea, final TabExpander e, final float x0) {
        float width = x0;
        for (Token t = tokenList; t != null && t.isPaintable(); t = t.getNextToken()) {
            width += t.getWidth(textArea, e, width);
        }
        return width - x0;
    }
    
    public static final float getTokenListWidthUpTo(final Token tokenList, final RSyntaxTextArea textArea, final TabExpander e, final float x0, final int upTo) {
        float width = 0.0f;
        for (Token t = tokenList; t != null && t.isPaintable(); t = t.getNextToken()) {
            if (t.containsPosition(upTo)) {
                return width + t.getWidthUpTo(upTo - t.getOffset(), textArea, e, x0 + width);
            }
            width += t.getWidth(textArea, e, x0 + width);
        }
        return width;
    }
    
    public static final boolean isBracket(final char ch) {
        return ch <= '}' && (RSyntaxUtilities.dataTable[ch] & 0x40) > 0;
    }
    
    public static final boolean isDigit(final char ch) {
        return ch >= '0' && ch <= '9';
    }
    
    public static final boolean isHexCharacter(final char ch) {
        return ch <= 'f' && (RSyntaxUtilities.dataTable[ch] & 0x10) > 0;
    }
    
    public static final boolean isJavaOperator(final char ch) {
        return ch <= '~' && (RSyntaxUtilities.dataTable[ch] & 0x80) > 0;
    }
    
    public static final boolean isLetter(final char ch) {
        return ch <= 'z' && (RSyntaxUtilities.dataTable[ch] & 0x2) > 0;
    }
    
    public static final boolean isLetterOrDigit(final char ch) {
        return ch <= 'z' && (RSyntaxUtilities.dataTable[ch] & 0x20) > 0;
    }
    
    public static final boolean isLightForeground(final Color fg) {
        return fg.getRed() > 160 && fg.getGreen() > 160 && fg.getBlue() > 160;
    }
    
    public static final boolean isNonWordChar(final Token t) {
        return t.length() == 1 && !isLetter(t.charAt(0));
    }
    
    public static final boolean isWhitespace(final char ch) {
        return ch == ' ' || ch == '\t';
    }
    
    public static boolean regexCanFollowInJavaScript(final Token t) {
        final char ch;
        return t == null || (t.length() == 1 && ((ch = t.charAt(0)) == '=' || ch == '(' || ch == ',' || ch == '?' || ch == ':' || ch == '[' || ch == '!' || ch == '&')) || (t.getType() == 23 && (t.charAt(t.length() - 1) == '=' || t.is(RSyntaxUtilities.JS_AND) || t.is(RSyntaxUtilities.JS_OR))) || t.is(7, RSyntaxUtilities.JS_KEYWORD_RETURN);
    }
    
    public static final char toLowerCase(final char ch) {
        if (ch >= 'A' && ch <= 'Z') {
            return (char)(ch | ' ');
        }
        return ch;
    }
    
    public static Pattern wildcardToPattern(final String wildcard, final boolean matchCase, final boolean escapeStartChar) {
        final int flags = getPatternFlags(matchCase, 0);
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wildcard.length(); ++i) {
            final char ch = wildcard.charAt(i);
            switch (ch) {
                case '*': {
                    sb.append(".*");
                    break;
                }
                case '?': {
                    sb.append('.');
                    break;
                }
                case '^': {
                    if (i > 0 || escapeStartChar) {
                        sb.append('\\');
                    }
                    sb.append('^');
                    break;
                }
                case '$':
                case '(':
                case ')':
                case '+':
                case '-':
                case '.':
                case '[':
                case '\\':
                case ']':
                case '{':
                case '|':
                case '}': {
                    sb.append('\\').append(ch);
                    break;
                }
                default: {
                    sb.append(ch);
                    break;
                }
            }
        }
        Pattern p = null;
        try {
            p = Pattern.compile(sb.toString(), flags);
        }
        catch (PatternSyntaxException pse) {
            pse.printStackTrace();
            p = Pattern.compile(".+");
        }
        return p;
    }
    
    static {
        LIGHT_HYPERLINK_FG = new Color(14221311);
        OS = getOSImpl();
        dataTable = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 128, 0, 0, 0, 128, 128, 0, 64, 64, 128, 128, 0, 128, 0, 128, 49, 49, 49, 49, 49, 49, 49, 49, 49, 49, 128, 0, 128, 128, 128, 128, 0, 58, 58, 58, 58, 58, 58, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 42, 64, 0, 64, 128, 0, 0, 50, 50, 50, 50, 50, 50, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 34, 64, 128, 64, 128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        RSyntaxUtilities.charSegment = new Segment();
        tempToken = new TokenImpl();
        JS_KEYWORD_RETURN = new char[] { 'r', 'e', 't', 'u', 'r', 'n' };
        JS_AND = new char[] { '&', '&' };
        JS_OR = new char[] { '|', '|' };
    }
}
