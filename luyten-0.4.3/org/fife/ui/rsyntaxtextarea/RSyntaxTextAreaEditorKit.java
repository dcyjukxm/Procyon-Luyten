package org.fife.ui.rsyntaxtextarea;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import org.fife.ui.rtextarea.*;
import java.util.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.templates.*;

public class RSyntaxTextAreaEditorKit extends RTextAreaEditorKit
{
    private static final long serialVersionUID = 1L;
    public static final String rstaCloseCurlyBraceAction = "RSTA.CloseCurlyBraceAction";
    public static final String rstaCloseMarkupTagAction = "RSTA.CloseMarkupTagAction";
    public static final String rstaCollapseAllFoldsAction = "RSTA.CollapseAllFoldsAction";
    public static final String rstaCollapseAllCommentFoldsAction = "RSTA.CollapseAllCommentFoldsAction";
    public static final String rstaCollapseFoldAction = "RSTA.CollapseFoldAction";
    public static final String rstaCopyAsRtfAction = "RSTA.CopyAsRtfAction";
    public static final String rstaDecreaseIndentAction = "RSTA.DecreaseIndentAction";
    public static final String rstaExpandAllFoldsAction = "RSTA.ExpandAllFoldsAction";
    public static final String rstaExpandFoldAction = "RSTA.ExpandFoldAction";
    public static final String rstaGoToMatchingBracketAction = "RSTA.GoToMatchingBracketAction";
    public static final String rstaPossiblyInsertTemplateAction = "RSTA.TemplateAction";
    public static final String rstaToggleCommentAction = "RSTA.ToggleCommentAction";
    public static final String rstaToggleCurrentFoldAction = "RSTA.ToggleCurrentFoldAction";
    private static final String MSG = "org.fife.ui.rsyntaxtextarea.RSyntaxTextArea";
    private static final ResourceBundle msg;
    private static final Action[] defaultActions;
    
    public Document createDefaultDocument() {
        return new RSyntaxDocument("text/plain");
    }
    
    public IconRowHeader createIconRowHeader(final RTextArea textArea) {
        return new FoldingAwareIconRowHeader((RSyntaxTextArea)textArea);
    }
    
    public Action[] getActions() {
        return TextAction.augmentList(super.getActions(), RSyntaxTextAreaEditorKit.defaultActions);
    }
    
    public static String getString(final String key) {
        return RSyntaxTextAreaEditorKit.msg.getString(key);
    }
    
    static /* synthetic */ ResourceBundle access$000() {
        return RSyntaxTextAreaEditorKit.msg;
    }
    
    static {
        msg = ResourceBundle.getBundle("org.fife.ui.rsyntaxtextarea.RSyntaxTextArea");
        defaultActions = new Action[] { new CloseCurlyBraceAction(), new CloseMarkupTagAction(), new BeginWordAction("caret-begin-word", false), new BeginWordAction("selection-begin-word", true), new ChangeFoldStateAction("RSTA.CollapseFoldAction", true), new ChangeFoldStateAction("RSTA.ExpandFoldAction", false), new CollapseAllFoldsAction(), new CopyAsRtfAction(), new DecreaseIndentAction(), new DeletePrevWordAction(), new EndAction("caret-end", false), new EndAction("selection-end", true), new EndWordAction("caret-end-word", false), new EndWordAction("caret-end-word", true), new ExpandAllFoldsAction(), new GoToMatchingBracketAction(), new InsertBreakAction(), new InsertTabAction(), new NextWordAction("caret-next-word", false), new NextWordAction("selection-next-word", true), new PossiblyInsertTemplateAction(), new PreviousWordAction("caret-previous-word", false), new PreviousWordAction("selection-previous-word", true), new SelectWordAction(), new ToggleCommentAction() };
    }
    
    protected static class BeginWordAction extends RTextAreaEditorKit.BeginWordAction
    {
        private Segment seg;
        
        protected BeginWordAction(final String name, final boolean select) {
            super(name, select);
            this.seg = new Segment();
        }
        
        protected int getWordStart(final RTextArea textArea, int offs) throws BadLocationException {
            if (offs == 0) {
                return offs;
            }
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            final int line = textArea.getLineOfOffset(offs);
            final int start = textArea.getLineStartOffset(line);
            if (offs == start) {
                return start;
            }
            int end = textArea.getLineEndOffset(line);
            if (line != textArea.getLineCount() - 1) {
                --end;
            }
            doc.getText(start, end - start, this.seg);
            final int firstIndex = this.seg.getBeginIndex() + (offs - start) - 1;
            this.seg.setIndex(firstIndex);
            char ch = this.seg.current();
            final char nextCh = (offs == end) ? '\0' : this.seg.array[this.seg.getIndex() + 1];
            if (Character.isLetterOrDigit(ch)) {
                if (offs != end && !Character.isLetterOrDigit(nextCh)) {
                    return offs;
                }
                do {
                    ch = this.seg.previous();
                } while (Character.isLetterOrDigit(ch));
            }
            else if (Character.isWhitespace(ch)) {
                if (offs != end && !Character.isWhitespace(nextCh)) {
                    return offs;
                }
                do {
                    ch = this.seg.previous();
                } while (Character.isWhitespace(ch));
            }
            offs -= firstIndex - this.seg.getIndex() + 1;
            if (ch != '\uffff' && nextCh != '\n') {
                ++offs;
            }
            return offs;
        }
    }
    
    public static class ChangeFoldStateAction extends FoldRelatedAction
    {
        private boolean collapse;
        
        public ChangeFoldStateAction(final String name, final boolean collapse) {
            super(name);
            this.collapse = collapse;
        }
        
        public ChangeFoldStateAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final Fold fold = this.getClosestFold(rsta);
                if (fold != null) {
                    fold.setCollapsed(this.collapse);
                }
                this.possiblyRepaintGutter(textArea);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class CloseCurlyBraceAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        private Point bracketInfo;
        private Segment seg;
        
        public CloseCurlyBraceAction() {
            super("RSTA.CloseCurlyBraceAction");
            this.seg = new Segment();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            final RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
            int languageIndex = 0;
            int dot = textArea.getCaretPosition();
            if (dot > 0) {
                final Token t = RSyntaxUtilities.getTokenAtOffset(rsta, dot - 1);
                languageIndex = ((t == null) ? 0 : t.getLanguageIndex());
            }
            final boolean alignCurlyBraces = rsta.isAutoIndentEnabled() && doc.getCurlyBracesDenoteCodeBlocks(languageIndex);
            if (alignCurlyBraces) {
                textArea.beginAtomicEdit();
            }
            try {
                textArea.replaceSelection("}");
                if (alignCurlyBraces) {
                    final Element root = doc.getDefaultRootElement();
                    dot = rsta.getCaretPosition() - 1;
                    final int line = root.getElementIndex(dot);
                    final Element elem = root.getElement(line);
                    final int start = elem.getStartOffset();
                    try {
                        doc.getText(start, dot - start, this.seg);
                    }
                    catch (BadLocationException ble) {
                        ble.printStackTrace();
                        return;
                    }
                    for (int i = 0; i < this.seg.count; ++i) {
                        final char ch = this.seg.array[this.seg.offset + i];
                        if (!Character.isWhitespace(ch)) {
                            return;
                        }
                    }
                    this.bracketInfo = RSyntaxUtilities.getMatchingBracketPosition(rsta, this.bracketInfo);
                    if (this.bracketInfo.y > -1) {
                        try {
                            final String ws = RSyntaxUtilities.getLeadingWhitespace(doc, this.bracketInfo.y);
                            rsta.replaceRange(ws, start, dot);
                        }
                        catch (BadLocationException ble) {
                            ble.printStackTrace();
                        }
                    }
                }
            }
            finally {
                if (alignCurlyBraces) {
                    textArea.endAtomicEdit();
                }
            }
        }
        
        public final String getMacroID() {
            return "RSTA.CloseCurlyBraceAction";
        }
    }
    
    public static class CloseMarkupTagAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        
        public CloseMarkupTagAction() {
            super("RSTA.CloseMarkupTagAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            final RSyntaxDocument doc = (RSyntaxDocument)rsta.getDocument();
            final Caret c = rsta.getCaret();
            final boolean selection = c.getDot() != c.getMark();
            rsta.replaceSelection("/");
            final int dot = c.getDot();
            if (doc.getLanguageIsMarkup() && doc.getCompleteMarkupCloseTags() && !selection && rsta.getCloseMarkupTags() && dot > 1) {
                try {
                    final char ch = doc.charAt(dot - 2);
                    if (ch == '<' || ch == '[') {
                        Token t = doc.getTokenListForLine(rsta.getCaretLineNumber());
                        t = RSyntaxUtilities.getTokenAtOffset(t, dot - 1);
                        if (t != null && t.getType() == 25) {
                            final String tagName = this.discoverTagName(doc, dot);
                            if (tagName != null) {
                                rsta.replaceSelection(tagName + (char)(ch + '\u0002'));
                            }
                        }
                    }
                }
                catch (BadLocationException ble) {
                    UIManager.getLookAndFeel().provideErrorFeedback(rsta);
                    ble.printStackTrace();
                }
            }
        }
        
        private String discoverTagName(final RSyntaxDocument doc, final int dot) {
            final Stack<String> stack = new Stack<String>();
            final Element root = doc.getDefaultRootElement();
            for (int curLine = root.getElementIndex(dot), i = 0; i <= curLine; ++i) {
                for (Token t = doc.getTokenListForLine(i); t != null && t.isPaintable(); t = ((t == null) ? null : t.getNextToken())) {
                    if (t.getType() == 25) {
                        if (t.isSingleChar('<') || t.isSingleChar('[')) {
                            for (t = t.getNextToken(); t != null && t.isPaintable(); t = t.getNextToken()) {
                                if (t.getType() == 26 || t.getType() == 27) {
                                    stack.push(t.getLexeme());
                                    break;
                                }
                            }
                        }
                        else if (t.length() == 2 && t.charAt(0) == '/' && (t.charAt(1) == '>' || t.charAt(1) == ']')) {
                            if (!stack.isEmpty()) {
                                stack.pop();
                            }
                        }
                        else if (t.length() == 2 && (t.charAt(0) == '<' || t.charAt(0) == '[') && t.charAt(1) == '/') {
                            String tagName = null;
                            if (!stack.isEmpty()) {
                                tagName = stack.pop();
                            }
                            if (t.getEndOffset() >= dot) {
                                return tagName;
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        public String getMacroID() {
            return this.getName();
        }
    }
    
    public static class CollapseAllCommentFoldsAction extends FoldRelatedAction
    {
        private static final long serialVersionUID = 1L;
        
        public CollapseAllCommentFoldsAction() {
            super("RSTA.CollapseAllCommentFoldsAction");
            this.setProperties(RSyntaxTextAreaEditorKit.access$000(), "Action.CollapseCommentFolds");
        }
        
        public CollapseAllCommentFoldsAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final FoldCollapser collapser = new FoldCollapser();
                collapser.collapseFolds(rsta.getFoldManager());
                this.possiblyRepaintGutter(textArea);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        public final String getMacroID() {
            return "RSTA.CollapseAllCommentFoldsAction";
        }
    }
    
    public static class CollapseAllFoldsAction extends FoldRelatedAction
    {
        private static final long serialVersionUID = 1L;
        
        public CollapseAllFoldsAction() {
            this(false);
        }
        
        public CollapseAllFoldsAction(final boolean localizedName) {
            super("RSTA.CollapseAllFoldsAction");
            if (localizedName) {
                this.setProperties(RSyntaxTextAreaEditorKit.access$000(), "Action.CollapseAllFolds");
            }
        }
        
        public CollapseAllFoldsAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final FoldCollapser collapser = new FoldCollapser() {
                    public boolean getShouldCollapse(final Fold fold) {
                        return true;
                    }
                };
                collapser.collapseFolds(rsta.getFoldManager());
                this.possiblyRepaintGutter(textArea);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        public final String getMacroID() {
            return "RSTA.CollapseAllFoldsAction";
        }
    }
    
    public static class CopyAsRtfAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        
        public CopyAsRtfAction() {
            super("RSTA.CopyAsRtfAction");
        }
        
        public CopyAsRtfAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            ((RSyntaxTextArea)textArea).copyAsRtf();
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class DecreaseFontSizeAction extends RTextAreaEditorKit.DecreaseFontSizeAction
    {
        private static final long serialVersionUID = 1L;
        
        public DecreaseFontSizeAction() {
            super();
        }
        
        public DecreaseFontSizeAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            final SyntaxScheme scheme = rsta.getSyntaxScheme();
            boolean changed = false;
            for (int count = scheme.getStyleCount(), i = 0; i < count; ++i) {
                final Style ss = scheme.getStyle(i);
                if (ss != null) {
                    final Font font = ss.font;
                    if (font != null) {
                        final float oldSize = font.getSize2D();
                        final float newSize = oldSize - this.decreaseAmount;
                        if (newSize >= 2.0f) {
                            ss.font = font.deriveFont(newSize);
                            changed = true;
                        }
                        else if (oldSize > 2.0f) {
                            ss.font = font.deriveFont(2.0f);
                            changed = true;
                        }
                    }
                }
            }
            final Font font2 = rsta.getFont();
            final float oldSize2 = font2.getSize2D();
            final float newSize2 = oldSize2 - this.decreaseAmount;
            if (newSize2 >= 2.0f) {
                rsta.setFont(font2.deriveFont(newSize2));
                changed = true;
            }
            else if (oldSize2 > 2.0f) {
                rsta.setFont(font2.deriveFont(2.0f));
                changed = true;
            }
            if (changed) {
                rsta.setSyntaxScheme(scheme);
                Component parent = rsta.getParent();
                if (parent instanceof JViewport) {
                    parent = parent.getParent();
                    if (parent instanceof JScrollPane) {
                        parent.repaint();
                    }
                }
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
    }
    
    public static class DecreaseIndentAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        private Segment s;
        
        public DecreaseIndentAction() {
            this("RSTA.DecreaseIndentAction");
        }
        
        public DecreaseIndentAction(final String name) {
            super(name);
            this.s = new Segment();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final Document document = textArea.getDocument();
            final Element map = document.getDefaultRootElement();
            final Caret c = textArea.getCaret();
            int dot = c.getDot();
            int mark = c.getMark();
            int line1 = map.getElementIndex(dot);
            final int tabSize = textArea.getTabSize();
            if (dot != mark) {
                final int line2 = map.getElementIndex(mark);
                dot = Math.min(line1, line2);
                mark = Math.max(line1, line2);
                textArea.beginAtomicEdit();
                try {
                    for (line1 = dot; line1 < mark; ++line1) {
                        final Element elem = map.getElement(line1);
                        this.handleDecreaseIndent(elem, document, tabSize);
                    }
                    final Element elem = map.getElement(mark);
                    final int start = elem.getStartOffset();
                    if (Math.max(c.getDot(), c.getMark()) != start) {
                        this.handleDecreaseIndent(elem, document, tabSize);
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
                finally {
                    textArea.endAtomicEdit();
                }
            }
            else {
                final Element elem2 = map.getElement(line1);
                try {
                    this.handleDecreaseIndent(elem2, document, tabSize);
                }
                catch (BadLocationException ble2) {
                    ble2.printStackTrace();
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
            }
        }
        
        public final String getMacroID() {
            return "RSTA.DecreaseIndentAction";
        }
        
        private final void handleDecreaseIndent(final Element elem, final Document doc, final int tabSize) throws BadLocationException {
            final int start = elem.getStartOffset();
            int end = elem.getEndOffset() - 1;
            doc.getText(start, end - start, this.s);
            int i = this.s.offset;
            end = i + this.s.count;
            if (end > i) {
                if (this.s.array[i] == '\t') {
                    doc.remove(start, 1);
                }
                else if (this.s.array[i] == ' ') {
                    ++i;
                    int toRemove;
                    for (toRemove = 1; i < end && this.s.array[i] == ' ' && toRemove < tabSize; ++i, ++toRemove) {}
                    doc.remove(start, toRemove);
                }
            }
        }
    }
    
    public static class DeletePrevWordAction extends RTextAreaEditorKit.DeletePrevWordAction
    {
        private Segment seg;
        
        public DeletePrevWordAction() {
            super();
            this.seg = new Segment();
        }
        
        protected int getPreviousWordStart(final RTextArea textArea, int offs) throws BadLocationException {
            if (offs == 0) {
                return offs;
            }
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            final int line = textArea.getLineOfOffset(offs);
            final int start = textArea.getLineStartOffset(line);
            if (offs == start) {
                return start - 1;
            }
            int end = textArea.getLineEndOffset(line);
            if (line != textArea.getLineCount() - 1) {
                --end;
            }
            doc.getText(start, end - start, this.seg);
            final int firstIndex = this.seg.getBeginIndex() + (offs - start) - 1;
            this.seg.setIndex(firstIndex);
            char ch = this.seg.current();
            if (Character.isWhitespace(ch)) {
                do {
                    ch = this.seg.previous();
                } while (Character.isWhitespace(ch));
            }
            if (Character.isLetterOrDigit(ch)) {
                do {
                    ch = this.seg.previous();
                } while (Character.isLetterOrDigit(ch));
            }
            else {
                while (!Character.isWhitespace(ch) && !Character.isLetterOrDigit(ch) && ch != '\uffff') {
                    ch = this.seg.previous();
                }
            }
            if (ch == '\uffff') {
                return start;
            }
            offs -= firstIndex - this.seg.getIndex();
            return offs;
        }
    }
    
    protected static class EndWordAction extends RTextAreaEditorKit.EndWordAction
    {
        private Segment seg;
        
        protected EndWordAction(final String name, final boolean select) {
            super(name, select);
            this.seg = new Segment();
        }
        
        protected int getWordEnd(final RTextArea textArea, int offs) throws BadLocationException {
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            if (offs == doc.getLength()) {
                return offs;
            }
            final int line = textArea.getLineOfOffset(offs);
            int end = textArea.getLineEndOffset(line);
            if (line != textArea.getLineCount() - 1) {
                --end;
            }
            if (offs == end) {
                return end;
            }
            doc.getText(offs, end - offs, this.seg);
            char ch = this.seg.first();
            if (Character.isLetterOrDigit(ch)) {
                do {
                    ch = this.seg.next();
                } while (Character.isLetterOrDigit(ch));
            }
            else if (Character.isWhitespace(ch)) {
                do {
                    ch = this.seg.next();
                } while (Character.isWhitespace(ch));
            }
            offs += this.seg.getIndex() - this.seg.getBeginIndex();
            return offs;
        }
    }
    
    public static class ExpandAllFoldsAction extends FoldRelatedAction
    {
        private static final long serialVersionUID = 1L;
        
        public ExpandAllFoldsAction() {
            this(false);
        }
        
        public ExpandAllFoldsAction(final boolean localizedName) {
            super("RSTA.ExpandAllFoldsAction");
            if (localizedName) {
                this.setProperties(RSyntaxTextAreaEditorKit.access$000(), "Action.ExpandAllFolds");
            }
        }
        
        public ExpandAllFoldsAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final FoldManager fm = rsta.getFoldManager();
                for (int i = 0; i < fm.getFoldCount(); ++i) {
                    this.expand(fm.getFold(i));
                }
                this.possiblyRepaintGutter(rsta);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        private void expand(final Fold fold) {
            fold.setCollapsed(false);
            for (int i = 0; i < fold.getChildCount(); ++i) {
                this.expand(fold.getChild(i));
            }
        }
        
        public final String getMacroID() {
            return "RSTA.ExpandAllFoldsAction";
        }
    }
    
    abstract static class FoldRelatedAction extends RecordableTextAction
    {
        public FoldRelatedAction(final String name) {
            super(name);
        }
        
        public FoldRelatedAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        protected Fold getClosestFold(final RSyntaxTextArea textArea) {
            final int offs = textArea.getCaretPosition();
            final int line = textArea.getCaretLineNumber();
            final FoldManager fm = textArea.getFoldManager();
            Fold fold = fm.getFoldForLine(line);
            if (fold == null) {
                fold = fm.getDeepestOpenFoldContaining(offs);
            }
            return fold;
        }
        
        protected void possiblyRepaintGutter(final RTextArea textArea) {
            final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
            if (gutter != null) {
                gutter.repaint();
            }
        }
    }
    
    public static class GoToMatchingBracketAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        private Point bracketInfo;
        
        public GoToMatchingBracketAction() {
            super("RSTA.GoToMatchingBracketAction");
        }
        
        public GoToMatchingBracketAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            this.bracketInfo = RSyntaxUtilities.getMatchingBracketPosition(rsta, this.bracketInfo);
            if (this.bracketInfo.y > -1) {
                rsta.setCaretPosition(this.bracketInfo.y + 1);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        public final String getMacroID() {
            return "RSTA.GoToMatchingBracketAction";
        }
        
        public static class EndAction extends RTextAreaEditorKit.EndAction
        {
            public EndAction(final String name, final boolean select) {
                super(name, select);
            }
            
            protected int getVisibleEnd(final RTextArea textArea) {
                final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
                return rsta.getLastVisibleOffset();
            }
        }
    }
    
    public static class IncreaseFontSizeAction extends RTextAreaEditorKit.IncreaseFontSizeAction
    {
        private static final long serialVersionUID = 1L;
        
        public IncreaseFontSizeAction() {
            super();
        }
        
        public IncreaseFontSizeAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            final SyntaxScheme scheme = rsta.getSyntaxScheme();
            boolean changed = false;
            for (int count = scheme.getStyleCount(), i = 0; i < count; ++i) {
                final Style ss = scheme.getStyle(i);
                if (ss != null) {
                    final Font font = ss.font;
                    if (font != null) {
                        final float oldSize = font.getSize2D();
                        final float newSize = oldSize + this.increaseAmount;
                        if (newSize <= 40.0f) {
                            ss.font = font.deriveFont(newSize);
                            changed = true;
                        }
                        else if (oldSize < 40.0f) {
                            ss.font = font.deriveFont(40.0f);
                            changed = true;
                        }
                    }
                }
            }
            final Font font2 = rsta.getFont();
            final float oldSize2 = font2.getSize2D();
            final float newSize2 = oldSize2 + this.increaseAmount;
            if (newSize2 <= 40.0f) {
                rsta.setFont(font2.deriveFont(newSize2));
                changed = true;
            }
            else if (oldSize2 < 40.0f) {
                rsta.setFont(font2.deriveFont(40.0f));
                changed = true;
            }
            if (changed) {
                rsta.setSyntaxScheme(scheme);
                Component parent = rsta.getParent();
                if (parent instanceof JViewport) {
                    parent = parent.getParent();
                    if (parent instanceof JScrollPane) {
                        parent.repaint();
                    }
                }
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
    }
    
    public static class InsertBreakAction extends RTextAreaEditorKit.InsertBreakAction
    {
        private static final long serialVersionUID = 1L;
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final RSyntaxTextArea sta = (RSyntaxTextArea)textArea;
            final boolean noSelection = sta.getSelectionStart() == sta.getSelectionEnd();
            boolean handled = false;
            if (noSelection) {
                final RSyntaxDocument doc = (RSyntaxDocument)sta.getDocument();
                handled = doc.insertBreakSpecialHandling(e);
            }
            if (!handled) {
                this.handleInsertBreak(sta, noSelection);
            }
        }
        
        private static final int atEndOfLine(final int pos, final String s, final int sLen) {
            for (int i = pos; i < sLen; ++i) {
                if (!RSyntaxUtilities.isWhitespace(s.charAt(i))) {
                    return i;
                }
            }
            return -1;
        }
        
        private static final int getOpenBraceCount(final RSyntaxDocument doc, final int languageIndex) {
            int openCount = 0;
            for (final Token t : doc) {
                if (t.getType() == 22 && t.length() == 1 && t.getLanguageIndex() == languageIndex) {
                    final char ch = t.charAt(0);
                    if (ch == '{') {
                        ++openCount;
                    }
                    else {
                        if (ch != '}') {
                            continue;
                        }
                        --openCount;
                    }
                }
            }
            return openCount;
        }
        
        protected void handleInsertBreak(final RSyntaxTextArea textArea, final boolean noSelection) {
            if (noSelection && textArea.isAutoIndentEnabled()) {
                this.insertNewlineWithAutoIndent(textArea);
            }
            else {
                textArea.replaceSelection("\n");
                if (noSelection) {
                    this.possiblyCloseCurlyBrace(textArea, null);
                }
            }
        }
        
        private void insertNewlineWithAutoIndent(final RSyntaxTextArea sta) {
            try {
                final int caretPos = sta.getCaretPosition();
                final Document doc = sta.getDocument();
                final Element map = doc.getDefaultRootElement();
                final int lineNum = map.getElementIndex(caretPos);
                final Element line = map.getElement(lineNum);
                final int start = line.getStartOffset();
                final int end = line.getEndOffset() - 1;
                final int len = end - start;
                final String s = doc.getText(start, len);
                final String leadingWS = RSyntaxUtilities.getLeadingWhitespace(s);
                final StringBuilder sb = new StringBuilder("\n");
                sb.append(leadingWS);
                final int nonWhitespacePos = atEndOfLine(caretPos - start, s, len);
                if (nonWhitespacePos == -1) {
                    if (leadingWS.length() == len && sta.isClearWhitespaceLinesEnabled()) {
                        sta.setSelectionStart(start);
                        sta.setSelectionEnd(end);
                    }
                    sta.replaceSelection(sb.toString());
                }
                else {
                    sb.append(s.substring(nonWhitespacePos));
                    sta.replaceRange(sb.toString(), caretPos, end);
                    sta.setCaretPosition(caretPos + leadingWS.length() + 1);
                }
                if (sta.getShouldIndentNextLine(lineNum)) {
                    sta.replaceSelection("\t");
                }
                this.possiblyCloseCurlyBrace(sta, leadingWS);
            }
            catch (BadLocationException ble) {
                sta.replaceSelection("\n");
                ble.printStackTrace();
            }
        }
        
        private void possiblyCloseCurlyBrace(final RSyntaxTextArea textArea, final String leadingWS) {
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            if (textArea.getCloseCurlyBraces()) {
                final int line = textArea.getCaretLineNumber();
                Token t = doc.getTokenListForLine(line - 1);
                t = t.getLastNonCommentNonWhitespaceToken();
                if (t != null && t.isLeftCurly()) {
                    final int languageIndex = t.getLanguageIndex();
                    if (doc.getCurlyBracesDenoteCodeBlocks(languageIndex) && getOpenBraceCount(doc, languageIndex) > 0) {
                        final StringBuilder sb = new StringBuilder();
                        if (line == textArea.getLineCount() - 1) {
                            sb.append('\n');
                        }
                        if (leadingWS != null) {
                            sb.append(leadingWS);
                        }
                        sb.append("}\n");
                        final int dot = textArea.getCaretPosition();
                        final int end = textArea.getLineEndOffsetOfCurrentLine();
                        textArea.insert(sb.toString(), end);
                        textArea.setCaretPosition(dot);
                    }
                }
            }
        }
    }
    
    public static class InsertTabAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        
        public InsertTabAction() {
            super("insert-tab");
        }
        
        public InsertTabAction(final String name) {
            super(name);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final Document document = textArea.getDocument();
            final Element map = document.getDefaultRootElement();
            final Caret c = textArea.getCaret();
            final int dot = c.getDot();
            final int mark = c.getMark();
            final int dotLine = map.getElementIndex(dot);
            final int markLine = map.getElementIndex(mark);
            if (dotLine != markLine) {
                final int first = Math.min(dotLine, markLine);
                final int last = Math.max(dotLine, markLine);
                String replacement = "\t";
                if (textArea.getTabsEmulated()) {
                    final StringBuilder sb = new StringBuilder();
                    for (int temp = textArea.getTabSize(), i = 0; i < temp; ++i) {
                        sb.append(' ');
                    }
                    replacement = sb.toString();
                }
                textArea.beginAtomicEdit();
                try {
                    for (int j = first; j < last; ++j) {
                        final Element elem = map.getElement(j);
                        final int start = elem.getStartOffset();
                        document.insertString(start, replacement, null);
                    }
                    final Element elem = map.getElement(last);
                    final int start = elem.getStartOffset();
                    if (Math.max(c.getDot(), c.getMark()) != start) {
                        document.insertString(start, replacement, null);
                    }
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
                finally {
                    textArea.endAtomicEdit();
                }
            }
            else {
                textArea.replaceSelection("\t");
            }
        }
        
        public final String getMacroID() {
            return "insert-tab";
        }
    }
    
    public static class NextWordAction extends RTextAreaEditorKit.NextWordAction
    {
        private Segment seg;
        
        public NextWordAction(final String nm, final boolean select) {
            super(nm, select);
            this.seg = new Segment();
        }
        
        protected int getNextWord(final RTextArea textArea, int offs) throws BadLocationException {
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            if (offs == doc.getLength()) {
                return offs;
            }
            final Element root = doc.getDefaultRootElement();
            int line = root.getElementIndex(offs);
            final int end = root.getElement(line).getEndOffset() - 1;
            if (offs != end) {
                doc.getText(offs, end - offs, this.seg);
                char ch = this.seg.first();
                if (Character.isLetterOrDigit(ch)) {
                    do {
                        ch = this.seg.next();
                    } while (Character.isLetterOrDigit(ch));
                }
                else if (!Character.isWhitespace(ch)) {
                    do {
                        ch = this.seg.next();
                    } while (ch != '\uffff' && !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch));
                }
                while (Character.isWhitespace(ch)) {
                    ch = this.seg.next();
                }
                offs += this.seg.getIndex() - this.seg.getBeginIndex();
                return offs;
            }
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final FoldManager fm = rsta.getFoldManager();
                final int lineCount = root.getElementCount();
                while (++line < lineCount && fm.isLineHidden(line)) {}
                if (line < lineCount) {
                    offs = root.getElement(line).getStartOffset();
                }
                return offs;
            }
            return offs + 1;
        }
    }
    
    public static class PossiblyInsertTemplateAction extends RecordableTextAction
    {
        private static final long serialVersionUID = 1L;
        
        public PossiblyInsertTemplateAction() {
            super("RSTA.TemplateAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                return;
            }
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (RSyntaxTextArea.getTemplatesEnabled()) {
                final Document doc = textArea.getDocument();
                if (doc != null) {
                    try {
                        final CodeTemplateManager manager = RSyntaxTextArea.getCodeTemplateManager();
                        final CodeTemplate template = (manager == null) ? null : manager.getTemplate(rsta);
                        if (template != null) {
                            template.invoke(rsta);
                        }
                        else {
                            this.doDefaultInsert(rsta);
                        }
                    }
                    catch (BadLocationException ble) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    }
                }
            }
            else {
                this.doDefaultInsert(rsta);
            }
        }
        
        private final void doDefaultInsert(final RTextArea textArea) {
            textArea.replaceSelection(" ");
        }
        
        public final String getMacroID() {
            return "RSTA.TemplateAction";
        }
    }
    
    public static class PreviousWordAction extends RTextAreaEditorKit.PreviousWordAction
    {
        private Segment seg;
        
        public PreviousWordAction(final String nm, final boolean select) {
            super(nm, select);
            this.seg = new Segment();
        }
        
        protected int getPreviousWord(final RTextArea textArea, int offs) throws BadLocationException {
            if (offs == 0) {
                return offs;
            }
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            final Element root = doc.getDefaultRootElement();
            int line = root.getElementIndex(offs);
            final int start = root.getElement(line).getStartOffset();
            if (offs != start) {
                doc.getText(start, offs - start, this.seg);
                char ch;
                for (ch = this.seg.last(); Character.isWhitespace(ch); ch = this.seg.previous()) {}
                if (Character.isLetterOrDigit(ch)) {
                    do {
                        ch = this.seg.previous();
                    } while (Character.isLetterOrDigit(ch));
                }
                else if (!Character.isWhitespace(ch)) {
                    do {
                        ch = this.seg.previous();
                    } while (ch != '\uffff' && !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch));
                }
                offs -= this.seg.getEndIndex() - this.seg.getIndex();
                if (ch != '\uffff') {
                    ++offs;
                }
                return offs;
            }
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final FoldManager fm = rsta.getFoldManager();
                while (--line >= 0 && fm.isLineHidden(line)) {}
                if (line >= 0) {
                    offs = root.getElement(line).getEndOffset() - 1;
                }
                return offs;
            }
            return start - 1;
        }
    }
    
    public static class SelectWordAction extends RTextAreaEditorKit.SelectWordAction
    {
        protected void createActions() {
            this.start = new RSyntaxTextAreaEditorKit.BeginWordAction("pigdog", false);
            this.end = new RSyntaxTextAreaEditorKit.EndWordAction("pigdog", true);
        }
    }
    
    public static class ToggleCommentAction extends RecordableTextAction
    {
        public ToggleCommentAction() {
            super("RSTA.ToggleCommentAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final RSyntaxDocument doc = (RSyntaxDocument)textArea.getDocument();
            final Element map = doc.getDefaultRootElement();
            final Caret c = textArea.getCaret();
            final int dot = c.getDot();
            final int mark = c.getMark();
            int line1 = map.getElementIndex(dot);
            final int line2 = map.getElementIndex(mark);
            final int start = Math.min(line1, line2);
            int end = Math.max(line1, line2);
            final Token t = doc.getTokenListForLine(start);
            final int languageIndex = (t != null) ? t.getLanguageIndex() : 0;
            final String[] startEnd = doc.getLineCommentStartAndEnd(languageIndex);
            if (startEnd == null) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            if (start != end) {
                final Element elem = map.getElement(end);
                if (Math.max(dot, mark) == elem.getStartOffset()) {
                    --end;
                }
            }
            textArea.beginAtomicEdit();
            try {
                final boolean add = this.getDoAdd(doc, map, start, end, startEnd);
                for (line1 = start; line1 <= end; ++line1) {
                    final Element elem2 = map.getElement(line1);
                    this.handleToggleComment(elem2, doc, startEnd, add);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            finally {
                textArea.endAtomicEdit();
            }
        }
        
        private boolean getDoAdd(final Document doc, final Element map, final int startLine, final int endLine, final String[] startEnd) throws BadLocationException {
            boolean doAdd = false;
            for (int i = startLine; i <= endLine; ++i) {
                final Element elem = map.getElement(i);
                final int start = elem.getStartOffset();
                final String t = doc.getText(start, elem.getEndOffset() - start - 1);
                if (!t.startsWith(startEnd[0]) || (startEnd[1] != null && !t.endsWith(startEnd[1]))) {
                    doAdd = true;
                    break;
                }
            }
            return doAdd;
        }
        
        private void handleToggleComment(final Element elem, final Document doc, final String[] startEnd, final boolean add) throws BadLocationException {
            final int start = elem.getStartOffset();
            final int end = elem.getEndOffset() - 1;
            if (add) {
                doc.insertString(start, startEnd[0], null);
                if (startEnd[1] != null) {
                    doc.insertString(end + startEnd[0].length(), startEnd[1], null);
                }
            }
            else {
                doc.remove(start, startEnd[0].length());
                if (startEnd[1] != null) {
                    final int temp = startEnd[1].length();
                    doc.remove(end - startEnd[0].length() - temp, temp);
                }
            }
        }
        
        public final String getMacroID() {
            return "RSTA.ToggleCommentAction";
        }
    }
    
    public static class ToggleCurrentFoldAction extends FoldRelatedAction
    {
        private static final long serialVersionUID = 1L;
        
        public ToggleCurrentFoldAction() {
            super("RSTA.ToggleCurrentFoldAction");
            this.setProperties(RSyntaxTextAreaEditorKit.access$000(), "Action.ToggleCurrentFold");
        }
        
        public ToggleCurrentFoldAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
            if (rsta.isCodeFoldingEnabled()) {
                final Fold fold = this.getClosestFold(rsta);
                if (fold != null) {
                    fold.toggleCollapsedState();
                }
                this.possiblyRepaintGutter(textArea);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(rsta);
            }
        }
        
        public final String getMacroID() {
            return "RSTA.ToggleCurrentFoldAction";
        }
    }
}
