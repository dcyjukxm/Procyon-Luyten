package org.fife.ui.rtextarea;

import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.util.regex.*;
import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import java.awt.*;

public class SearchEngine
{
    public static SearchResult find(final JTextArea textArea, final SearchContext context) {
        if (textArea instanceof RTextArea || context.getMarkAll()) {
            ((RTextArea)textArea).clearMarkAllHighlights();
        }
        final boolean doMarkAll = textArea instanceof RTextArea && context.getMarkAll();
        final String text = context.getSearchFor();
        if (text == null || text.length() == 0) {
            if (doMarkAll) {
                final List<DocumentRange> emptyRangeList = Collections.emptyList();
                ((RTextArea)textArea).markAll(emptyRangeList);
            }
            return new SearchResult();
        }
        final Caret c = textArea.getCaret();
        final boolean forward = context.getSearchForward();
        final int start = forward ? Math.max(c.getDot(), c.getMark()) : Math.min(c.getDot(), c.getMark());
        final String findIn = getFindInText(textArea, start, forward);
        if (findIn == null || findIn.length() == 0) {
            return new SearchResult();
        }
        int markAllCount = 0;
        if (doMarkAll) {
            markAllCount = markAllImpl((RTextArea)textArea, context).getMarkedCount();
        }
        final SearchResult result = findImpl(findIn, context);
        if (result.wasFound() && !result.getMatchRange().isZeroLength()) {
            textArea.getCaret().setSelectionVisible(true);
            if (forward && start > -1) {
                result.getMatchRange().translate(start);
            }
            selectAndPossiblyCenter(textArea, result.getMatchRange(), true);
        }
        result.setMarkedCount(markAllCount);
        return result;
    }
    
    private static SearchResult findImpl(String findIn, final SearchContext context) {
        final String text = context.getSearchFor();
        final boolean forward = context.getSearchForward();
        DocumentRange range = null;
        if (!context.isRegularExpression()) {
            final int pos = getNextMatchPos(text, findIn, forward, context.getMatchCase(), context.getWholeWord());
            findIn = null;
            if (pos != -1) {
                range = new DocumentRange(pos, pos + text.length());
            }
        }
        else {
            Point regExPos = null;
            int start = 0;
            do {
                regExPos = getNextMatchPosRegEx(text, findIn.substring(start), forward, context.getMatchCase(), context.getWholeWord());
                if (regExPos != null) {
                    if (regExPos.x != regExPos.y) {
                        regExPos.translate(start, start);
                        range = new DocumentRange(regExPos.x, regExPos.y);
                    }
                    else {
                        start += regExPos.x + 1;
                    }
                }
            } while (start < findIn.length() && regExPos != null && range == null);
        }
        if (range != null) {
            return new SearchResult(range, 1, 0);
        }
        return new SearchResult();
    }
    
    private static CharSequence getFindInCharSequence(final RTextArea textArea, final int start, final boolean forward) {
        final RDocument doc = (RDocument)textArea.getDocument();
        int csStart = 0;
        int csEnd = 0;
        if (forward) {
            csStart = start;
            csEnd = doc.getLength();
        }
        else {
            csStart = 0;
            csEnd = start;
        }
        return new RDocumentCharSequence(doc, csStart, csEnd);
    }
    
    private static String getFindInText(final JTextArea textArea, final int start, final boolean forward) {
        String findIn = null;
        try {
            if (forward) {
                findIn = textArea.getText(start, textArea.getDocument().getLength() - start);
            }
            else {
                findIn = textArea.getText(0, start);
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return findIn;
    }
    
    private static List getMatches(final Matcher m, final String replaceStr) {
        final ArrayList matches = new ArrayList();
        while (m.find()) {
            final Point loc = new Point(m.start(), m.end());
            if (replaceStr == null) {
                matches.add(loc);
            }
            else {
                matches.add(new RegExReplaceInfo(m.group(0), loc.x, loc.y, getReplacementText(m, replaceStr)));
            }
        }
        return matches;
    }
    
    public static final int getNextMatchPos(final String searchFor, final String searchIn, final boolean forward, final boolean matchCase, final boolean wholeWord) {
        if (!matchCase) {
            return getNextMatchPosImpl(searchFor.toLowerCase(), searchIn.toLowerCase(), forward, matchCase, wholeWord);
        }
        return getNextMatchPosImpl(searchFor, searchIn, forward, matchCase, wholeWord);
    }
    
    private static final int getNextMatchPosImpl(final String searchFor, final String searchIn, final boolean goForward, final boolean matchCase, final boolean wholeWord) {
        if (!wholeWord) {
            return goForward ? searchIn.indexOf(searchFor) : searchIn.lastIndexOf(searchFor);
        }
        final int len = searchFor.length();
        int temp = goForward ? 0 : searchIn.length();
        final int tempChange = goForward ? 1 : -1;
        while (true) {
            if (goForward) {
                temp = searchIn.indexOf(searchFor, temp);
            }
            else {
                temp = searchIn.lastIndexOf(searchFor, temp);
            }
            if (temp == -1) {
                return temp;
            }
            if (isWholeWord(searchIn, temp, len)) {
                return temp;
            }
            temp += tempChange;
        }
    }
    
    private static Point getNextMatchPosRegEx(final String regEx, final CharSequence searchIn, final boolean goForward, final boolean matchCase, final boolean wholeWord) {
        return (Point)getNextMatchPosRegExImpl(regEx, searchIn, goForward, matchCase, wholeWord, null);
    }
    
    private static Object getNextMatchPosRegExImpl(String regEx, final CharSequence searchIn, final boolean goForward, final boolean matchCase, final boolean wholeWord, final String replaceStr) {
        if (wholeWord) {
            regEx = "\\b" + regEx + "\\b";
        }
        int flags = 8;
        flags = RSyntaxUtilities.getPatternFlags(matchCase, flags);
        Pattern pattern = null;
        try {
            pattern = Pattern.compile(regEx, flags);
        }
        catch (PatternSyntaxException pse) {
            return null;
        }
        final Matcher m = pattern.matcher(searchIn);
        if (goForward) {
            if (m.find()) {
                if (replaceStr == null) {
                    return new Point(m.start(), m.end());
                }
                return new RegExReplaceInfo(m.group(0), m.start(), m.end(), getReplacementText(m, replaceStr));
            }
        }
        else {
            final List<?> matches = getMatches(m, replaceStr);
            if (!matches.isEmpty()) {
                return matches.get(matches.size() - 1);
            }
        }
        return null;
    }
    
    private static RegExReplaceInfo getRegExReplaceInfo(final CharSequence searchIn, final SearchContext context) {
        String replacement = context.getReplaceWith();
        if (replacement == null) {
            replacement = "";
        }
        final String regex = context.getSearchFor();
        final boolean goForward = context.getSearchForward();
        final boolean matchCase = context.getMatchCase();
        final boolean wholeWord = context.getWholeWord();
        return (RegExReplaceInfo)getNextMatchPosRegExImpl(regex, searchIn, goForward, matchCase, wholeWord, replacement);
    }
    
    public static String getReplacementText(final Matcher m, final CharSequence template) {
        int cursor = 0;
        final StringBuilder result = new StringBuilder();
        while (cursor < template.length()) {
            char nextChar = template.charAt(cursor);
            if (nextChar == '\\') {
                nextChar = template.charAt(++cursor);
                switch (nextChar) {
                    case 'n': {
                        nextChar = '\n';
                        break;
                    }
                    case 't': {
                        nextChar = '\t';
                        break;
                    }
                }
                result.append(nextChar);
                ++cursor;
            }
            else if (nextChar == '$') {
                ++cursor;
                int refNum = template.charAt(cursor) - '0';
                if (refNum < 0 || refNum > 9) {
                    throw new IndexOutOfBoundsException("No group " + template.charAt(cursor));
                }
                ++cursor;
                boolean done = false;
                while (!done) {
                    if (cursor >= template.length()) {
                        break;
                    }
                    final int nextDigit = template.charAt(cursor) - '0';
                    if (nextDigit < 0) {
                        break;
                    }
                    if (nextDigit > 9) {
                        break;
                    }
                    final int newRefNum = refNum * 10 + nextDigit;
                    if (m.groupCount() < newRefNum) {
                        done = true;
                    }
                    else {
                        refNum = newRefNum;
                        ++cursor;
                    }
                }
                if (m.group(refNum) == null) {
                    continue;
                }
                result.append(m.group(refNum));
            }
            else {
                result.append(nextChar);
                ++cursor;
            }
        }
        return result.toString();
    }
    
    private static final boolean isWholeWord(final CharSequence searchIn, final int offset, final int len) {
        boolean wsBefore;
        try {
            wsBefore = !Character.isLetterOrDigit(searchIn.charAt(offset - 1));
        }
        catch (IndexOutOfBoundsException e) {
            wsBefore = true;
        }
        boolean wsAfter;
        try {
            wsAfter = !Character.isLetterOrDigit(searchIn.charAt(offset + len));
        }
        catch (IndexOutOfBoundsException e) {
            wsAfter = true;
        }
        return wsBefore && wsAfter;
    }
    
    private static final int makeMarkAndDotEqual(final JTextArea textArea, final boolean forward) {
        final Caret c = textArea.getCaret();
        final int val = forward ? Math.min(c.getDot(), c.getMark()) : Math.max(c.getDot(), c.getMark());
        c.setDot(val);
        return val;
    }
    
    public static final SearchResult markAll(final RTextArea textArea, final SearchContext context) {
        textArea.clearMarkAllHighlights();
        return markAllImpl(textArea, context);
    }
    
    private static final SearchResult markAllImpl(final RTextArea textArea, SearchContext context) {
        final String toMark = context.getSearchFor();
        int markAllCount = 0;
        if (context.getMarkAll() && toMark != null && toMark.length() > 0) {
            final List<DocumentRange> highlights = new ArrayList<DocumentRange>();
            context = context.clone();
            context.setSearchForward(true);
            context.setMarkAll(false);
            String findIn = textArea.getText();
            int start = 0;
            if (!context.getMatchCase()) {
                context.setMatchCase(true);
                context.setSearchFor(toMark.toLowerCase());
                findIn = findIn.toLowerCase();
            }
            for (SearchResult res = findImpl(findIn, context); res.wasFound(); res = findImpl(findIn.substring(start), context)) {
                final DocumentRange match = res.getMatchRange().translate(start);
                if (match.isZeroLength()) {
                    start = match.getEndOffset() + 1;
                    if (start > findIn.length()) {
                        break;
                    }
                }
                else {
                    highlights.add(match);
                    start = match.getEndOffset();
                }
            }
            textArea.markAll(highlights);
            markAllCount = highlights.size();
        }
        else {
            final List<DocumentRange> empty = Collections.emptyList();
            textArea.markAll(empty);
        }
        return new SearchResult(null, 0, markAllCount);
    }
    
    private static SearchResult regexReplace(final RTextArea textArea, final SearchContext context) throws PatternSyntaxException {
        final Caret c = textArea.getCaret();
        final boolean forward = context.getSearchForward();
        final int start = makeMarkAndDotEqual(textArea, forward);
        CharSequence findIn = getFindInCharSequence(textArea, start, forward);
        if (findIn == null) {
            return new SearchResult();
        }
        int markAllCount = 0;
        if (context.getMarkAll()) {
            markAllCount = markAllImpl(textArea, context).getMarkedCount();
        }
        RegExReplaceInfo info = getRegExReplaceInfo(findIn, context);
        DocumentRange range = null;
        if (info != null) {
            c.setSelectionVisible(true);
            int matchStart = info.getStartIndex();
            int matchEnd = info.getEndIndex();
            if (forward) {
                matchStart += start;
                matchEnd += start;
            }
            textArea.setSelectionStart(matchStart);
            textArea.setSelectionEnd(matchEnd);
            final String replacement = info.getReplacement();
            textArea.replaceSelection(replacement);
            final int dot = matchStart + replacement.length();
            findIn = getFindInCharSequence(textArea, dot, forward);
            info = getRegExReplaceInfo(findIn, context);
            if (info != null) {
                matchStart = info.getStartIndex();
                matchEnd = info.getEndIndex();
                if (forward) {
                    matchStart += dot;
                    matchEnd += dot;
                }
                range = new DocumentRange(matchStart, matchEnd);
            }
            else {
                range = new DocumentRange(dot, dot);
            }
            selectAndPossiblyCenter(textArea, range, true);
        }
        final int count = (range != null) ? 1 : 0;
        return new SearchResult(range, count, markAllCount);
    }
    
    public static SearchResult replace(final RTextArea textArea, final SearchContext context) throws PatternSyntaxException {
        if (context.getMarkAll()) {
            textArea.clearMarkAllHighlights();
        }
        final String toFind = context.getSearchFor();
        if (toFind == null || toFind.length() == 0) {
            return new SearchResult();
        }
        textArea.beginAtomicEdit();
        try {
            if (context.isRegularExpression()) {
                return regexReplace(textArea, context);
            }
            makeMarkAndDotEqual(textArea, context.getSearchForward());
            final SearchResult res = find(textArea, context);
            if (res.wasFound() && !res.getMatchRange().isZeroLength()) {
                final String replacement = context.getReplaceWith();
                textArea.replaceSelection(replacement);
                int dot = res.getMatchRange().getStartOffset();
                if (context.getSearchForward()) {
                    final int length = (replacement == null) ? 0 : replacement.length();
                    dot += length;
                }
                textArea.setCaretPosition(dot);
                final SearchResult next = find(textArea, context);
                DocumentRange range;
                if (next.wasFound()) {
                    range = next.getMatchRange();
                }
                else {
                    range = new DocumentRange(dot, dot);
                }
                res.setMatchRange(range);
                selectAndPossiblyCenter(textArea, range, true);
            }
            return res;
        }
        finally {
            textArea.endAtomicEdit();
        }
    }
    
    public static SearchResult replaceAll(final RTextArea textArea, SearchContext context) throws PatternSyntaxException {
        if (context.getMarkAll()) {
            textArea.clearMarkAllHighlights();
        }
        context.setSearchForward(true);
        final String toFind = context.getSearchFor();
        if (toFind == null || toFind.length() == 0) {
            return new SearchResult();
        }
        if (context.getMarkAll()) {
            context = context.clone();
            context.setMarkAll(false);
        }
        SearchResult lastFound = null;
        int count = 0;
        textArea.beginAtomicEdit();
        try {
            final int oldOffs = textArea.getCaretPosition();
            textArea.setCaretPosition(0);
            for (SearchResult res = replace(textArea, context); res.wasFound(); res = replace(textArea, context)) {
                lastFound = res;
                ++count;
            }
            if (lastFound == null) {
                textArea.setCaretPosition(oldOffs);
                lastFound = new SearchResult();
            }
        }
        finally {
            textArea.endAtomicEdit();
        }
        lastFound.setCount(count);
        return lastFound;
    }
    
    private static void selectAndPossiblyCenter(final JTextArea textArea, final DocumentRange range, final boolean select) {
        final int start = range.getStartOffset();
        final int end = range.getEndOffset();
        boolean foldsExpanded = false;
        if (textArea instanceof RSyntaxTextArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            final FoldManager fm = rsta.getFoldManager();
            if (fm.isCodeFoldingSupportedAndEnabled()) {
                foldsExpanded = fm.ensureOffsetNotInClosedFold(start);
                foldsExpanded |= fm.ensureOffsetNotInClosedFold(end);
            }
        }
        if (select) {
            textArea.setSelectionStart(start);
            textArea.setSelectionEnd(end);
        }
        Rectangle r = null;
        try {
            r = textArea.modelToView(start);
            if (r == null) {
                return;
            }
            if (end != start) {
                r = r.union(textArea.modelToView(end));
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
            if (select) {
                textArea.setSelectionStart(start);
                textArea.setSelectionEnd(end);
            }
            return;
        }
        final Rectangle visible = textArea.getVisibleRect();
        if (!foldsExpanded && visible.contains(r)) {
            if (select) {
                textArea.setSelectionStart(start);
                textArea.setSelectionEnd(end);
            }
            return;
        }
        visible.x = r.x - (visible.width - r.width) / 2;
        visible.y = r.y - (visible.height - r.height) / 2;
        final Rectangle bounds = textArea.getBounds();
        final Insets i = textArea.getInsets();
        bounds.x = i.left;
        bounds.y = i.top;
        final Rectangle loc_0 = bounds;
        loc_0.width -= i.left + i.right;
        final Rectangle loc_1 = bounds;
        loc_1.height -= i.top + i.bottom;
        if (visible.x < bounds.x) {
            visible.x = bounds.x;
        }
        if (visible.x + visible.width > bounds.x + bounds.width) {
            visible.x = bounds.x + bounds.width - visible.width;
        }
        if (visible.y < bounds.y) {
            visible.y = bounds.y;
        }
        if (visible.y + visible.height > bounds.y + bounds.height) {
            visible.y = bounds.y + bounds.height - visible.height;
        }
        textArea.scrollRectToVisible(visible);
    }
}
