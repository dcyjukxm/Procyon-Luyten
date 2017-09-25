package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import javax.swing.text.*;
import java.util.*;

public class HtmlOccurrenceMarker implements OccurrenceMarker
{
    private static final char[] CLOSE_TAG_START;
    private static final char[] TAG_SELF_CLOSE;
    private static final Set<String> TAGS_REQUIRING_CLOSING;
    
    public static final Set<String> getRequiredClosingTags() {
        final String[] tags = { "html", "head", "title", "style", "script", "noscript", "body", "section", "nav", "article", "aside", "h1", "h2", "h3", "h4", "h5", "h6", "header", "footer", "address", "pre", "dialog", "blockquote", "ol", "ul", "dl", "a", "q", "cite", "em", "strong", "small", "mark", "dfn", "abbr", "time", "progress", "meter", "code", "var", "samp", "kbd", "sub", "sup", "span", "i", "b", "bdo", "ruby", "rt", "rp", "ins", "del", "figure", "iframe", "object", "video", "audio", "canvas", "map", "table", "caption", "form", "fieldset", "label", "button", "select", "datalist", "textarea", "output", "details", "bb", "menu", "legend", "div", "acronym", "applet", "big", "blink", "center", "dir", "font", "frame", "frameset", "isindex", "listing", "marquee", "nobr", "noembed", "noframes", "plaintext", "s", "spacer", "strike", "tt", "u", "xmp" };
        return new HashSet<String>(Arrays.asList(tags));
    }
    
    public static final Token getTagNameTokenForCaretOffset(final RSyntaxTextArea textArea, final OccurrenceMarker occurrenceMarker) {
        final int dot = textArea.getCaretPosition();
        Token t = textArea.getTokenListForLine(textArea.getCaretLineNumber());
        Token toMark = null;
        while (t != null && t.isPaintable()) {
            if (t.getType() == 26) {
                toMark = t;
            }
            if (t.getEndOffset() == dot || t.containsPosition(dot)) {
                if (occurrenceMarker.isValidType(textArea, t) && t.getType() != 26) {
                    return t;
                }
                if (t.containsPosition(dot)) {
                    break;
                }
            }
            if (t.getType() == 25 && (t.isSingleChar('>') || t.is(HtmlOccurrenceMarker.TAG_SELF_CLOSE))) {
                toMark = null;
            }
            t = t.getNextToken();
        }
        return toMark;
    }
    
    public Token getTokenToMark(final RSyntaxTextArea textArea) {
        return getTagNameTokenForCaretOffset(textArea, this);
    }
    
    public boolean isValidType(final RSyntaxTextArea textArea, final Token t) {
        return textArea.getMarkOccurrencesOfTokenType(t.getType());
    }
    
    public void markOccurrences(final RSyntaxDocument doc, Token t, final RSyntaxTextAreaHighlighter h, final SmartHighlightPainter p) {
        if (t.getType() != 26) {
            DefaultOccurrenceMarker.markOccurrencesOfToken(doc, t, h, p);
            return;
        }
        String lexemeStr = t.getLexeme();
        final char[] lexeme = lexemeStr.toCharArray();
        lexemeStr = lexemeStr.toLowerCase();
        final int tokenOffs = t.getOffset();
        final Element root = doc.getDefaultRootElement();
        final int lineCount = root.getElementCount();
        int curLine = root.getElementIndex(t.getOffset());
        int depth = 0;
        boolean found = false;
        boolean forward = true;
        for (t = doc.getTokenListForLine(curLine); t != null && t.isPaintable(); t = t.getNextToken()) {
            if (t.getType() == 25) {
                if (t.isSingleChar('<') && t.getOffset() + 1 == tokenOffs) {
                    if (HtmlOccurrenceMarker.TAGS_REQUIRING_CLOSING.contains(lexemeStr)) {
                        found = true;
                        break;
                    }
                    break;
                }
                else if (t.is(HtmlOccurrenceMarker.CLOSE_TAG_START) && t.getOffset() + 2 == tokenOffs) {
                    found = true;
                    forward = false;
                    break;
                }
            }
        }
        if (!found) {
            return;
        }
        if (forward) {
            t = t.getNextToken().getNextToken();
            while (true) {
                if (t != null && t.isPaintable()) {
                    if (t.getType() == 25) {
                        if (t.is(HtmlOccurrenceMarker.CLOSE_TAG_START)) {
                            final Token match = t.getNextToken();
                            if (match != null && match.is(lexeme)) {
                                if (depth <= 0) {
                                    try {
                                        int end = match.getOffset() + match.length();
                                        h.addMarkedOccurrenceHighlight(match.getOffset(), end, p);
                                        end = tokenOffs + match.length();
                                        h.addMarkedOccurrenceHighlight(tokenOffs, end, p);
                                    }
                                    catch (BadLocationException ble) {
                                        ble.printStackTrace();
                                    }
                                    return;
                                }
                                --depth;
                            }
                        }
                        else if (t.isSingleChar('<')) {
                            t = t.getNextToken();
                            if (t != null && t.is(lexeme)) {
                                ++depth;
                            }
                        }
                    }
                    t = ((t == null) ? null : t.getNextToken());
                }
                else {
                    if (++curLine < lineCount) {
                        t = doc.getTokenListForLine(curLine);
                    }
                    if (curLine >= lineCount) {
                        break;
                    }
                    continue;
                }
            }
        }
        else {
            final List<Entry> openCloses = new ArrayList<Entry>();
            boolean inPossibleMatch = false;
            t = doc.getTokenListForLine(curLine);
            final int endBefore = tokenOffs - 2;
            while (true) {
                if (t != null && t.getOffset() < endBefore && t.isPaintable()) {
                    if (t.getType() == 25) {
                        if (t.isSingleChar('<')) {
                            final Token next = t.getNextToken();
                            if (next != null) {
                                if (next.is(lexeme)) {
                                    openCloses.add(new Entry(true, next));
                                    inPossibleMatch = true;
                                }
                                else {
                                    inPossibleMatch = false;
                                }
                                t = next;
                            }
                        }
                        else if (t.isSingleChar('>')) {
                            inPossibleMatch = false;
                        }
                        else if (inPossibleMatch && t.is(HtmlOccurrenceMarker.TAG_SELF_CLOSE)) {
                            openCloses.remove(openCloses.size() - 1);
                            inPossibleMatch = false;
                        }
                        else if (t.is(HtmlOccurrenceMarker.CLOSE_TAG_START)) {
                            final Token next = t.getNextToken();
                            if (next != null) {
                                if (next.is(lexeme)) {
                                    openCloses.add(new Entry(false, next));
                                }
                                t = next;
                            }
                        }
                    }
                    t = t.getNextToken();
                }
                else {
                    for (int i = openCloses.size() - 1; i >= 0; --i) {
                        final Entry entry = openCloses.get(i);
                        depth += (entry.open ? -1 : 1);
                        if (depth == -1) {
                            try {
                                final Token match2 = entry.t;
                                int end2 = match2.getOffset() + match2.length();
                                h.addMarkedOccurrenceHighlight(match2.getOffset(), end2, p);
                                end2 = tokenOffs + match2.length();
                                h.addMarkedOccurrenceHighlight(tokenOffs, end2, p);
                            }
                            catch (BadLocationException ble2) {
                                ble2.printStackTrace();
                            }
                            openCloses.clear();
                            return;
                        }
                    }
                    openCloses.clear();
                    if (--curLine >= 0) {
                        t = doc.getTokenListForLine(curLine);
                    }
                    if (curLine < 0) {
                        break;
                    }
                    continue;
                }
            }
        }
    }
    
    static {
        CLOSE_TAG_START = new char[] { '<', '/' };
        TAG_SELF_CLOSE = new char[] { '/', '>' };
        TAGS_REQUIRING_CLOSING = getRequiredClosingTags();
    }
    
    private static class Entry
    {
        public boolean open;
        public Token t;
        
        public Entry(final boolean open, final Token t) {
            super();
            this.open = open;
            this.t = t;
        }
    }
}
