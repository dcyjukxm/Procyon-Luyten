package org.fife.ui.rsyntaxtextarea.folding;

import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.*;

public class HtmlFoldParser implements FoldParser
{
    public static final int LANGUAGE_HTML = -1;
    public static final int LANGUAGE_PHP = 0;
    public static final int LANGUAGE_JSP = 1;
    private final int language;
    private static final Set<String> FOLDABLE_TAGS;
    private static final char[] MARKUP_CLOSING_TAG_START;
    private static final char[] MLC_START;
    private static final char[] MLC_END;
    private static final char[] PHP_START;
    private static final char[] PHP_END;
    private static final char[] JSP_START;
    private static final char[] JSP_END;
    private static final char[][] LANG_START;
    private static final char[][] LANG_END;
    private static final char[] JSP_COMMENT_START;
    private static final char[] JSP_COMMENT_END;
    
    public HtmlFoldParser(final int language) {
        super();
        if (language < -1 && language > 1) {
            throw new IllegalArgumentException("Invalid language: " + language);
        }
        this.language = language;
    }
    
    public List<Fold> getFolds(final RSyntaxTextArea textArea) {
        final List<Fold> folds = new ArrayList<Fold>();
        final Stack<String> tagNameStack = new Stack<String>();
        boolean inSublanguage = false;
        Fold currentFold = null;
        final int lineCount = textArea.getLineCount();
        boolean inMLC = false;
        boolean inJSMLC = false;
        final TagCloseInfo tci = new TagCloseInfo();
        try {
            for (int line = 0; line < lineCount; ++line) {
                Token t = textArea.getTokenListForLine(line);
                while (t != null && t.isPaintable()) {
                    if (this.language >= 0 && t.getType() == 22) {
                        if (t.startsWith(HtmlFoldParser.LANG_START[this.language])) {
                            if (currentFold == null) {
                                currentFold = new Fold(0, textArea, t.getOffset());
                                folds.add(currentFold);
                            }
                            else {
                                currentFold = currentFold.createChild(0, t.getOffset());
                            }
                            inSublanguage = true;
                        }
                        else if (t.startsWith(HtmlFoldParser.LANG_END[this.language])) {
                            final int phpEnd = t.getEndOffset() - 1;
                            currentFold.setEndOffset(phpEnd);
                            final Fold parentFold = currentFold.getParent();
                            if (currentFold.isOnSingleLine()) {
                                removeFold(currentFold, folds);
                            }
                            currentFold = parentFold;
                            inSublanguage = false;
                            t = t.getNextToken();
                            continue;
                        }
                    }
                    if (!inSublanguage) {
                        if (t.getType() == 2) {
                            if (inMLC) {
                                if (t.endsWith(HtmlFoldParser.MLC_END)) {
                                    final int mlcEnd = t.getEndOffset() - 1;
                                    currentFold.setEndOffset(mlcEnd);
                                    final Fold parentFold = currentFold.getParent();
                                    if (currentFold.isOnSingleLine()) {
                                        removeFold(currentFold, folds);
                                    }
                                    currentFold = parentFold;
                                    inMLC = false;
                                }
                            }
                            else if (inJSMLC) {
                                if (t.endsWith(HtmlFoldParser.JSP_COMMENT_END)) {
                                    final int mlcEnd = t.getEndOffset() - 1;
                                    currentFold.setEndOffset(mlcEnd);
                                    final Fold parentFold = currentFold.getParent();
                                    if (currentFold.isOnSingleLine()) {
                                        removeFold(currentFold, folds);
                                    }
                                    currentFold = parentFold;
                                    inJSMLC = false;
                                }
                            }
                            else if (t.startsWith(HtmlFoldParser.MLC_START) && !t.endsWith(HtmlFoldParser.MLC_END)) {
                                if (currentFold == null) {
                                    currentFold = new Fold(1, textArea, t.getOffset());
                                    folds.add(currentFold);
                                }
                                else {
                                    currentFold = currentFold.createChild(1, t.getOffset());
                                }
                                inMLC = true;
                            }
                            else if (this.language == 1 && t.startsWith(HtmlFoldParser.JSP_COMMENT_START) && !t.endsWith(HtmlFoldParser.JSP_COMMENT_END)) {
                                if (currentFold == null) {
                                    currentFold = new Fold(1, textArea, t.getOffset());
                                    folds.add(currentFold);
                                }
                                else {
                                    currentFold = currentFold.createChild(1, t.getOffset());
                                }
                                inJSMLC = true;
                            }
                        }
                        else if (t.isSingleChar(25, '<')) {
                            final Token tagStartToken = t;
                            final Token tagNameToken = t.getNextToken();
                            if (isFoldableTag(tagNameToken)) {
                                this.getTagCloseInfo(tagNameToken, textArea, line, tci);
                                if (TagCloseInfo.access$100(tci) == -1) {
                                    return folds;
                                }
                                final Token tagCloseToken = TagCloseInfo.access$200(tci);
                                if (tagCloseToken.isSingleChar(25, '>')) {
                                    if (currentFold == null) {
                                        currentFold = new Fold(0, textArea, tagStartToken.getOffset());
                                        folds.add(currentFold);
                                    }
                                    else {
                                        currentFold = currentFold.createChild(0, tagStartToken.getOffset());
                                    }
                                    tagNameStack.push(tagNameToken.getLexeme());
                                }
                                t = tagCloseToken;
                            }
                        }
                        else if (t.is(25, HtmlFoldParser.MARKUP_CLOSING_TAG_START) && currentFold != null) {
                            final Token tagNameToken2 = t.getNextToken();
                            if (isFoldableTag(tagNameToken2) && isEndOfLastFold(tagNameStack, tagNameToken2)) {
                                tagNameStack.pop();
                                currentFold.setEndOffset(t.getOffset());
                                final Fold parentFold = currentFold.getParent();
                                if (currentFold.isOnSingleLine()) {
                                    removeFold(currentFold, folds);
                                }
                                currentFold = parentFold;
                                t = tagNameToken2;
                            }
                        }
                    }
                    t = t.getNextToken();
                }
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        return folds;
    }
    
    private void getTagCloseInfo(final Token tagNameToken, final RSyntaxTextArea textArea, int line, final TagCloseInfo info) {
        info.reset();
        Token t = tagNameToken.getNextToken();
        while (true) {
            if (t != null && t.getType() != 25) {
                t = t.getNextToken();
            }
            else {
                if (t != null) {
                    TagCloseInfo.access$202(info, t);
                    TagCloseInfo.access$102(info, line);
                    break;
                }
                if (++line >= textArea.getLineCount() || (t = textArea.getTokenListForLine(line)) == null) {
                    break;
                }
                continue;
            }
        }
    }
    
    private static final boolean isEndOfLastFold(final Stack<String> tagNameStack, final Token tagNameToken) {
        return tagNameToken != null && !tagNameStack.isEmpty() && tagNameToken.getLexeme().equalsIgnoreCase(tagNameStack.peek());
    }
    
    private static final boolean isFoldableTag(final Token tagNameToken) {
        return tagNameToken != null && HtmlFoldParser.FOLDABLE_TAGS.contains(tagNameToken.getLexeme().toLowerCase());
    }
    
    private static final void removeFold(final Fold fold, final List<Fold> folds) {
        if (!fold.removeFromParent()) {
            folds.remove(folds.size() - 1);
        }
    }
    
    static {
        MARKUP_CLOSING_TAG_START = "</".toCharArray();
        MLC_START = "<!--".toCharArray();
        MLC_END = "-->".toCharArray();
        PHP_START = "<?".toCharArray();
        PHP_END = "?>".toCharArray();
        JSP_START = "<%".toCharArray();
        JSP_END = "%>".toCharArray();
        LANG_START = new char[][] { HtmlFoldParser.PHP_START, HtmlFoldParser.JSP_START };
        LANG_END = new char[][] { HtmlFoldParser.PHP_END, HtmlFoldParser.JSP_END };
        JSP_COMMENT_START = "<%--".toCharArray();
        JSP_COMMENT_END = "--%>".toCharArray();
        (FOLDABLE_TAGS = new HashSet<String>()).add("body");
        HtmlFoldParser.FOLDABLE_TAGS.add("canvas");
        HtmlFoldParser.FOLDABLE_TAGS.add("div");
        HtmlFoldParser.FOLDABLE_TAGS.add("form");
        HtmlFoldParser.FOLDABLE_TAGS.add("head");
        HtmlFoldParser.FOLDABLE_TAGS.add("html");
        HtmlFoldParser.FOLDABLE_TAGS.add("ol");
        HtmlFoldParser.FOLDABLE_TAGS.add("pre");
        HtmlFoldParser.FOLDABLE_TAGS.add("script");
        HtmlFoldParser.FOLDABLE_TAGS.add("span");
        HtmlFoldParser.FOLDABLE_TAGS.add("style");
        HtmlFoldParser.FOLDABLE_TAGS.add("table");
        HtmlFoldParser.FOLDABLE_TAGS.add("tfoot");
        HtmlFoldParser.FOLDABLE_TAGS.add("thead");
        HtmlFoldParser.FOLDABLE_TAGS.add("tr");
        HtmlFoldParser.FOLDABLE_TAGS.add("td");
        HtmlFoldParser.FOLDABLE_TAGS.add("ul");
    }
    
    private static class TagCloseInfo
    {
        private Token closeToken;
        private int line;
        
        public void reset() {
            this.closeToken = null;
            this.line = -1;
        }
        
        public String toString() {
            return "[TagCloseInfo: closeToken=" + this.closeToken + ", line=" + this.line + "]";
        }
        
        static /* synthetic */ int access$100(final TagCloseInfo x0) {
            return x0.line;
        }
        
        static /* synthetic */ Token access$200(final TagCloseInfo x0) {
            return x0.closeToken;
        }
        
        static /* synthetic */ Token access$202(final TagCloseInfo x0, final Token x1) {
            return x0.closeToken = x1;
        }
        
        static /* synthetic */ int access$102(final TagCloseInfo x0, final int x1) {
            return x0.line = x1;
        }
    }
}
