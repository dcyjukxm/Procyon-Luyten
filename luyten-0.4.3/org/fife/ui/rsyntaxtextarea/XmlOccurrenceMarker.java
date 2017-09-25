package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;
import javax.swing.text.*;
import java.util.*;

public class XmlOccurrenceMarker implements OccurrenceMarker
{
    private static final char[] CLOSE_TAG_START;
    private static final char[] TAG_SELF_CLOSE;
    
    public Token getTokenToMark(final RSyntaxTextArea textArea) {
        return HtmlOccurrenceMarker.getTagNameTokenForCaretOffset(textArea, this);
    }
    
    public boolean isValidType(final RSyntaxTextArea textArea, final Token t) {
        return textArea.getMarkOccurrencesOfTokenType(t.getType());
    }
    
    public void markOccurrences(final RSyntaxDocument doc, Token t, final RSyntaxTextAreaHighlighter h, final SmartHighlightPainter p) {
        final char[] lexeme = t.getLexeme().toCharArray();
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
                    found = true;
                    break;
                }
                if (t.is(XmlOccurrenceMarker.CLOSE_TAG_START) && t.getOffset() + 2 == tokenOffs) {
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
                        if (t.is(XmlOccurrenceMarker.CLOSE_TAG_START)) {
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
                        else if (inPossibleMatch && t.is(XmlOccurrenceMarker.TAG_SELF_CLOSE)) {
                            openCloses.remove(openCloses.size() - 1);
                            inPossibleMatch = false;
                        }
                        else if (t.is(XmlOccurrenceMarker.CLOSE_TAG_START)) {
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
