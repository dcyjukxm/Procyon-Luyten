package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rsyntaxtextarea.focusabletip.*;
import java.awt.datatransfer.*;
import org.fife.ui.rtextarea.*;
import javax.swing.event.*;
import org.fife.ui.rsyntaxtextarea.folding.*;
import org.fife.ui.rsyntaxtextarea.parser.*;
import java.util.*;
import java.awt.font.*;
import java.lang.reflect.*;
import javax.swing.text.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.net.*;

public class RSyntaxTextArea extends RTextArea implements SyntaxConstants
{
    public static final String ANIMATE_BRACKET_MATCHING_PROPERTY = "RSTA.animateBracketMatching";
    public static final String ANTIALIAS_PROPERTY = "RSTA.antiAlias";
    public static final String AUTO_INDENT_PROPERTY = "RSTA.autoIndent";
    public static final String BRACKET_MATCHING_PROPERTY = "RSTA.bracketMatching";
    public static final String CLEAR_WHITESPACE_LINES_PROPERTY = "RSTA.clearWhitespaceLines";
    public static final String CLOSE_CURLY_BRACES_PROPERTY = "RSTA.closeCurlyBraces";
    public static final String CLOSE_MARKUP_TAGS_PROPERTY = "RSTA.closeMarkupTags";
    public static final String CODE_FOLDING_PROPERTY = "RSTA.codeFolding";
    public static final String EOL_VISIBLE_PROPERTY = "RSTA.eolMarkersVisible";
    public static final String FOCUSABLE_TIPS_PROPERTY = "RSTA.focusableTips";
    public static final String FRACTIONAL_FONTMETRICS_PROPERTY = "RSTA.fractionalFontMetrics";
    public static final String HIGHLIGHT_SECONDARY_LANGUAGES_PROPERTY = "RSTA.highlightSecondaryLanguages";
    public static final String HYPERLINKS_ENABLED_PROPERTY = "RSTA.hyperlinksEnabled";
    public static final String MARK_OCCURRENCES_PROPERTY = "RSTA.markOccurrences";
    public static final String MARKED_OCCURRENCES_CHANGED_PROPERTY = "RSTA.markedOccurrencesChanged";
    public static final String PAINT_MATCHED_BRACKET_PAIR_PROPERTY = "RSTA.paintMatchedBracketPair";
    public static final String PARSER_NOTICES_PROPERTY = "RSTA.parserNotices";
    public static final String SYNTAX_SCHEME_PROPERTY = "RSTA.syntaxScheme";
    public static final String SYNTAX_STYLE_PROPERTY = "RSTA.syntaxStyle";
    public static final String TAB_LINE_COLOR_PROPERTY = "RSTA.tabLineColor";
    public static final String TAB_LINES_PROPERTY = "RSTA.tabLines";
    public static final String USE_SELECTED_TEXT_COLOR_PROPERTY = "RSTA.useSelectedTextColor";
    public static final String VISIBLE_WHITESPACE_PROPERTY = "RSTA.visibleWhitespace";
    private static final Color DEFAULT_BRACKET_MATCH_BG_COLOR;
    private static final Color DEFAULT_BRACKET_MATCH_BORDER_COLOR;
    private static final Color DEFAULT_SELECTION_COLOR;
    private static final String MSG = "org.fife.ui.rsyntaxtextarea.RSyntaxTextArea";
    private JMenu foldingMenu;
    private static RecordableTextAction toggleCurrentFoldAction;
    private static RecordableTextAction collapseAllCommentFoldsAction;
    private static RecordableTextAction collapseAllFoldsAction;
    private static RecordableTextAction expandAllFoldsAction;
    private String syntaxStyleKey;
    private SyntaxScheme syntaxScheme;
    private static CodeTemplateManager codeTemplateManager;
    private static boolean templatesEnabled;
    private Rectangle match;
    private Rectangle dotRect;
    private Point bracketInfo;
    private Color matchedBracketBGColor;
    private Color matchedBracketBorderColor;
    private int lastBracketMatchPos;
    private boolean bracketMatchingEnabled;
    private boolean animateBracketMatching;
    private boolean paintMatchedBracketPair;
    private BracketMatchingTimer bracketRepaintTimer;
    private boolean metricsNeverRefreshed;
    private boolean autoIndentEnabled;
    private boolean closeCurlyBraces;
    private boolean closeMarkupTags;
    private boolean clearWhitespaceLines;
    private boolean whitespaceVisible;
    private boolean eolMarkersVisible;
    private boolean paintTabLines;
    private Color tabLineColor;
    private boolean hyperlinksEnabled;
    private Color hyperlinkFG;
    private int linkScanningMask;
    private boolean highlightSecondaryLanguages;
    private boolean useSelectedTextColor;
    private MarkOccurrencesSupport markOccurrencesSupport;
    private Color markOccurrencesColor;
    private boolean paintMarkOccurrencesBorder;
    private FontMetrics defaultFontMetrics;
    private ParserManager parserManager;
    private String cachedTip;
    private Point cachedTipLoc;
    private boolean isScanningForLinks;
    private int hoveredOverLinkOffset;
    private LinkGenerator linkGenerator;
    private LinkGeneratorResult linkGeneratorResult;
    private int rhsCorrection;
    private FoldManager foldManager;
    private boolean useFocusableTips;
    private FocusableTip focusableTip;
    private Map<?, ?> aaHints;
    private TokenPainter tokenPainter;
    private int lineHeight;
    private int maxAscent;
    private boolean fractionalFontMetricsEnabled;
    private Color[] secondaryLanguageBackgrounds;
    
    public RSyntaxTextArea() {
        super();
    }
    
    public RSyntaxTextArea(final RSyntaxDocument doc) {
        super(doc);
    }
    
    public RSyntaxTextArea(final String text) {
        super(text);
    }
    
    public RSyntaxTextArea(final int rows, final int cols) {
        super(rows, cols);
    }
    
    public RSyntaxTextArea(final String text, final int rows, final int cols) {
        super(text, rows, cols);
    }
    
    public RSyntaxTextArea(final RSyntaxDocument doc, final String text, final int rows, final int cols) {
        super(doc, text, rows, cols);
    }
    
    public RSyntaxTextArea(final int textMode) {
        super(textMode);
    }
    
    public void addActiveLineRangeListener(final ActiveLineRangeListener l) {
        this.listenerList.add(ActiveLineRangeListener.class, l);
    }
    
    public void addHyperlinkListener(final HyperlinkListener l) {
        this.listenerList.add(HyperlinkListener.class, l);
    }
    
    public void addNotify() {
        super.addNotify();
        if (this.metricsNeverRefreshed) {
            final Window parent = SwingUtilities.getWindowAncestor(this);
            if (parent != null && parent.getWidth() > 0 && parent.getHeight() > 0) {
                this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
                this.metricsNeverRefreshed = false;
            }
        }
        if (this.parserManager != null) {
            this.parserManager.restartParsing();
        }
    }
    
    public void addParser(final Parser parser) {
        if (this.parserManager == null) {
            this.parserManager = new ParserManager(this);
        }
        this.parserManager.addParser(parser);
    }
    
    protected void appendFoldingMenu(final JPopupMenu popup) {
        popup.addSeparator();
        final ResourceBundle bundle = ResourceBundle.getBundle("org.fife.ui.rsyntaxtextarea.RSyntaxTextArea");
        (this.foldingMenu = new JMenu(bundle.getString("ContextMenu.Folding"))).add(this.createPopupMenuItem(RSyntaxTextArea.toggleCurrentFoldAction));
        this.foldingMenu.add(this.createPopupMenuItem(RSyntaxTextArea.collapseAllCommentFoldsAction));
        this.foldingMenu.add(this.createPopupMenuItem(RSyntaxTextArea.collapseAllFoldsAction));
        this.foldingMenu.add(this.createPopupMenuItem(RSyntaxTextArea.expandAllFoldsAction));
        popup.add(this.foldingMenu);
    }
    
    private void calculateLineHeight() {
        final boolean loc_0 = false;
        this.maxAscent = (loc_0 ? 1 : 0);
        this.lineHeight = (loc_0 ? 1 : 0);
        for (int i = 0; i < this.syntaxScheme.getStyleCount(); ++i) {
            final Style ss = this.syntaxScheme.getStyle(i);
            if (ss != null && ss.font != null) {
                final FontMetrics fm = this.getFontMetrics(ss.font);
                final int height = fm.getHeight();
                if (height > this.lineHeight) {
                    this.lineHeight = height;
                }
                final int ascent = fm.getMaxAscent();
                if (ascent > this.maxAscent) {
                    this.maxAscent = ascent;
                }
            }
        }
        final Font temp = this.getFont();
        final FontMetrics fm2 = this.getFontMetrics(temp);
        final int height2 = fm2.getHeight();
        if (height2 > this.lineHeight) {
            this.lineHeight = height2;
        }
        final int ascent2 = fm2.getMaxAscent();
        if (ascent2 > this.maxAscent) {
            this.maxAscent = ascent2;
        }
    }
    
    public void clearParsers() {
        if (this.parserManager != null) {
            this.parserManager.clearParsers();
        }
    }
    
    private TokenImpl cloneTokenList(Token t) {
        if (t == null) {
            return null;
        }
        TokenImpl cloneEnd;
        final TokenImpl clone = cloneEnd = new TokenImpl(t);
        while ((t = t.getNextToken()) != null) {
            final TokenImpl temp = new TokenImpl(t);
            cloneEnd.setNextToken(temp);
            cloneEnd = temp;
        }
        return clone;
    }
    
    protected void configurePopupMenu(final JPopupMenu popupMenu) {
        super.configurePopupMenu(popupMenu);
        if (popupMenu != null && popupMenu.getComponentCount() > 0 && this.foldingMenu != null) {
            this.foldingMenu.setEnabled(this.foldManager.isCodeFoldingSupportedAndEnabled());
        }
    }
    
    public void copyAsRtf() {
        final int selStart = this.getSelectionStart();
        final int selEnd = this.getSelectionEnd();
        if (selStart == selEnd) {
            return;
        }
        final SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            try {
                sm.checkSystemClipboardAccess();
            }
            catch (SecurityException se) {
                UIManager.getLookAndFeel().provideErrorFeedback(null);
                return;
            }
        }
        final Clipboard cb = this.getToolkit().getSystemClipboard();
        final RtfGenerator gen = new RtfGenerator();
        Token t;
        for (Token tokenList = t = this.getTokenListFor(selStart, selEnd); t != null; t = t.getNextToken()) {
            if (t.isPaintable()) {
                if (t.length() == 1 && t.charAt(0) == '\n') {
                    gen.appendNewline();
                }
                else {
                    final Font font = this.getFontForTokenType(t.getType());
                    final Color bg = this.getBackgroundForToken(t);
                    final boolean underline = this.getUnderlineForToken(t);
                    if (t.isWhitespace()) {
                        gen.appendToDocNoFG(t.getLexeme(), font, bg, underline);
                    }
                    else {
                        final Color fg = this.getForegroundForToken(t);
                        gen.appendToDoc(t.getLexeme(), font, fg, bg, underline);
                    }
                }
            }
        }
        final RtfTransferable contents = new RtfTransferable(gen.getRtf().getBytes());
        try {
            cb.setContents(contents, null);
        }
        catch (IllegalStateException ise) {
            UIManager.getLookAndFeel().provideErrorFeedback(null);
        }
    }
    
    protected Document createDefaultModel() {
        return new RSyntaxDocument("text/plain");
    }
    
    protected RTAMouseListener createMouseListener() {
        return new RSyntaxTextAreaMutableCaretEvent(this);
    }
    
    protected JPopupMenu createPopupMenu() {
        final JPopupMenu popup = super.createPopupMenu();
        this.appendFoldingMenu(popup);
        return popup;
    }
    
    private static void createRstaPopupMenuActions() {
        final ResourceBundle msg = ResourceBundle.getBundle("org.fife.ui.rsyntaxtextarea.RSyntaxTextArea");
        (RSyntaxTextArea.toggleCurrentFoldAction = new RSyntaxTextAreaEditorKit.ToggleCurrentFoldAction()).setProperties(msg, "Action.ToggleCurrentFold");
        (RSyntaxTextArea.collapseAllCommentFoldsAction = new RSyntaxTextAreaEditorKit.CollapseAllCommentFoldsAction()).setProperties(msg, "Action.CollapseCommentFolds");
        RSyntaxTextArea.collapseAllFoldsAction = new RSyntaxTextAreaEditorKit.CollapseAllFoldsAction(true);
        RSyntaxTextArea.expandAllFoldsAction = new RSyntaxTextAreaEditorKit.ExpandAllFoldsAction(true);
    }
    
    protected RTextAreaUI createRTextAreaUI() {
        return new RSyntaxTextAreaUI(this);
    }
    
    protected final void doBracketMatching() {
        if (this.match != null) {
            this.repaint(this.match);
            if (this.dotRect != null) {
                this.repaint(this.dotRect);
            }
        }
        final int lastCaretBracketPos = (this.bracketInfo == null) ? -1 : this.bracketInfo.x;
        this.bracketInfo = RSyntaxUtilities.getMatchingBracketPosition(this, this.bracketInfo);
        Label_0225: {
            Label_0197: {
                if (this.bracketInfo.y > -1) {
                    if (this.bracketInfo.y == this.lastBracketMatchPos) {
                        if (this.bracketInfo.x == lastCaretBracketPos) {
                            break Label_0197;
                        }
                    }
                    try {
                        this.match = this.modelToView(this.bracketInfo.y);
                        if (this.match != null) {
                            if (this.getPaintMatchedBracketPair()) {
                                this.dotRect = this.modelToView(this.bracketInfo.x);
                            }
                            else {
                                this.dotRect = null;
                            }
                            if (this.getAnimateBracketMatching()) {
                                this.bracketRepaintTimer.restart();
                            }
                            this.repaint(this.match);
                            if (this.dotRect != null) {
                                this.repaint(this.dotRect);
                            }
                        }
                    }
                    catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                    break Label_0225;
                }
            }
            if (this.bracketInfo.y == -1) {
                this.match = null;
                this.dotRect = null;
                this.bracketRepaintTimer.stop();
            }
        }
        this.lastBracketMatchPos = this.bracketInfo.y;
    }
    
    protected void fireCaretUpdate(final CaretEvent e) {
        super.fireCaretUpdate(e);
        if (this.isBracketMatchingEnabled()) {
            this.doBracketMatching();
        }
    }
    
    private void fireActiveLineRangeEvent(final int min, final int max) {
        ActiveLineRangeEvent e = null;
        final Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ActiveLineRangeListener.class) {
                if (e == null) {
                    e = new ActiveLineRangeEvent(this, min, max);
                }
                ((ActiveLineRangeListener)listeners[i + 1]).activeLineRangeChanged(e);
            }
        }
    }
    
    private void fireHyperlinkUpdate(final HyperlinkEvent e) {
        final Object[] listeners = this.listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == HyperlinkListener.class) {
                ((HyperlinkListener)listeners[i + 1]).hyperlinkUpdate(e);
            }
        }
    }
    
    void fireMarkedOccurrencesChanged() {
        this.firePropertyChange("RSTA.markedOccurrencesChanged", null, null);
    }
    
    void fireParserNoticesChange() {
        this.firePropertyChange("RSTA.parserNotices", null, null);
    }
    
    public void foldToggled(final Fold fold) {
        this.match = null;
        this.dotRect = null;
        if (this.getLineWrap()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    RSyntaxTextArea.access$000(RSyntaxTextArea.this);
                }
            });
        }
        else {
            this.possiblyUpdateCurrentLineHighlightLocation();
        }
        this.revalidate();
        this.repaint();
    }
    
    public void forceReparsing(final int parser) {
        this.parserManager.forceReparsing(parser);
    }
    
    public boolean forceReparsing(final Parser parser) {
        for (int i = 0; i < this.getParserCount(); ++i) {
            if (this.getParser(i) == parser) {
                this.forceReparsing(i);
                return true;
            }
        }
        return false;
    }
    
    public boolean getAnimateBracketMatching() {
        return this.animateBracketMatching;
    }
    
    public boolean getAntiAliasingEnabled() {
        return this.aaHints != null;
    }
    
    public Color getBackgroundForToken(final Token token) {
        Color c = null;
        if (this.getHighlightSecondaryLanguages()) {
            final int languageIndex = token.getLanguageIndex() - 1;
            if (languageIndex >= 0 && languageIndex < this.secondaryLanguageBackgrounds.length) {
                c = this.secondaryLanguageBackgrounds[languageIndex];
            }
        }
        if (c == null) {
            c = this.syntaxScheme.getStyle(token.getType()).background;
        }
        return c;
    }
    
    public boolean getCloseCurlyBraces() {
        return this.closeCurlyBraces;
    }
    
    public boolean getCloseMarkupTags() {
        return this.closeMarkupTags;
    }
    
    public static synchronized CodeTemplateManager getCodeTemplateManager() {
        if (RSyntaxTextArea.codeTemplateManager == null) {
            RSyntaxTextArea.codeTemplateManager = new CodeTemplateManager();
        }
        return RSyntaxTextArea.codeTemplateManager;
    }
    
    public static final Color getDefaultBracketMatchBGColor() {
        return RSyntaxTextArea.DEFAULT_BRACKET_MATCH_BG_COLOR;
    }
    
    public static final Color getDefaultBracketMatchBorderColor() {
        return RSyntaxTextArea.DEFAULT_BRACKET_MATCH_BORDER_COLOR;
    }
    
    public static Color getDefaultSelectionColor() {
        return RSyntaxTextArea.DEFAULT_SELECTION_COLOR;
    }
    
    public SyntaxScheme getDefaultSyntaxScheme() {
        return new SyntaxScheme(this.getFont());
    }
    
    public boolean getEOLMarkersVisible() {
        return this.eolMarkersVisible;
    }
    
    public FoldManager getFoldManager() {
        return this.foldManager;
    }
    
    public Font getFontForTokenType(final int type) {
        final Font f = this.syntaxScheme.getStyle(type).font;
        return (f != null) ? f : this.getFont();
    }
    
    public FontMetrics getFontMetricsForTokenType(final int type) {
        final FontMetrics fm = this.syntaxScheme.getStyle(type).fontMetrics;
        return (fm != null) ? fm : this.defaultFontMetrics;
    }
    
    public Color getForegroundForToken(final Token t) {
        if (this.getHyperlinksEnabled() && this.hoveredOverLinkOffset == t.getOffset() && (t.isHyperlink() || this.linkGeneratorResult != null)) {
            return this.hyperlinkFG;
        }
        return this.getForegroundForTokenType(t.getType());
    }
    
    public Color getForegroundForTokenType(final int type) {
        final Color fg = this.syntaxScheme.getStyle(type).foreground;
        return (fg != null) ? fg : this.getForeground();
    }
    
    public boolean getFractionalFontMetricsEnabled() {
        return this.fractionalFontMetricsEnabled;
    }
    
    private final Graphics2D getGraphics2D(final Graphics g) {
        final Graphics2D g2d = (Graphics2D)g;
        if (this.aaHints != null) {
            g2d.addRenderingHints(this.aaHints);
        }
        if (this.fractionalFontMetricsEnabled) {
            g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        return g2d;
    }
    
    public boolean getHighlightSecondaryLanguages() {
        return this.highlightSecondaryLanguages;
    }
    
    public Color getHyperlinkForeground() {
        return this.hyperlinkFG;
    }
    
    public boolean getHyperlinksEnabled() {
        return this.hyperlinksEnabled;
    }
    
    public int getLastVisibleOffset() {
        if (this.isCodeFoldingEnabled()) {
            final int lastVisibleLine = this.foldManager.getLastVisibleLine();
            if (lastVisibleLine < this.getLineCount() - 1) {
                try {
                    return this.getLineEndOffset(lastVisibleLine) - 1;
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
        return this.getDocument().getLength();
    }
    
    public int getLineHeight() {
        return this.lineHeight;
    }
    
    public LinkGenerator getLinkGenerator() {
        return this.linkGenerator;
    }
    
    public List<DocumentRange> getMarkAllHighlightRanges() {
        return ((RSyntaxTextAreaHighlighter)this.getHighlighter()).getMarkAllHighlightRanges();
    }
    
    public List<DocumentRange> getMarkedOccurrences() {
        return ((RSyntaxTextAreaHighlighter)this.getHighlighter()).getMarkedOccurrences();
    }
    
    public boolean getMarkOccurrences() {
        return this.markOccurrencesSupport != null;
    }
    
    public Color getMarkOccurrencesColor() {
        return this.markOccurrencesColor;
    }
    
    boolean getMarkOccurrencesOfTokenType(final int type) {
        final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
        return doc.getMarkOccurrencesOfTokenType(type);
    }
    
    public Color getMatchedBracketBGColor() {
        return this.matchedBracketBGColor;
    }
    
    public Color getMatchedBracketBorderColor() {
        return this.matchedBracketBorderColor;
    }
    
    Rectangle getDotRectangle() {
        return this.dotRect;
    }
    
    Rectangle getMatchRectangle() {
        return this.match;
    }
    
    public int getMaxAscent() {
        return this.maxAscent;
    }
    
    public boolean getPaintMatchedBracketPair() {
        return this.paintMatchedBracketPair;
    }
    
    public boolean getPaintTabLines() {
        return this.paintTabLines;
    }
    
    public Parser getParser(final int index) {
        return this.parserManager.getParser(index);
    }
    
    public int getParserCount() {
        return (this.parserManager == null) ? 0 : this.parserManager.getParserCount();
    }
    
    public List<ParserNotice> getParserNotices() {
        if (this.parserManager == null) {
            return Collections.emptyList();
        }
        return this.parserManager.getParserNotices();
    }
    
    public int getRightHandSideCorrection() {
        return this.rhsCorrection;
    }
    
    public boolean getShouldIndentNextLine(final int line) {
        if (this.isAutoIndentEnabled()) {
            final RSyntaxDocument doc = (RSyntaxDocument)this.getDocument();
            return doc.getShouldIndentNextLine(line);
        }
        return false;
    }
    
    public String getSyntaxEditingStyle() {
        return this.syntaxStyleKey;
    }
    
    public SyntaxScheme getSyntaxScheme() {
        return this.syntaxScheme;
    }
    
    public Color getTabLineColor() {
        return this.tabLineColor;
    }
    
    public boolean getPaintMarkOccurrencesBorder() {
        return this.paintMarkOccurrencesBorder;
    }
    
    public Color getSecondaryLanguageBackground(final int index) {
        return this.secondaryLanguageBackgrounds[index];
    }
    
    public int getSecondaryLanguageCount() {
        return this.secondaryLanguageBackgrounds.length;
    }
    
    public static synchronized boolean getTemplatesEnabled() {
        return RSyntaxTextArea.templatesEnabled;
    }
    
    private Token getTokenListFor(final int startOffs, final int endOffs) {
        TokenImpl tokenList = null;
        TokenImpl lastToken = null;
        final Element map = this.getDocument().getDefaultRootElement();
        final int startLine = map.getElementIndex(startOffs);
        for (int endLine = map.getElementIndex(endOffs), line = startLine; line <= endLine; ++line) {
            TokenImpl t = (TokenImpl)this.getTokenListForLine(line);
            t = this.cloneTokenList(t);
            if (tokenList == null) {
                tokenList = (lastToken = t);
            }
            else {
                lastToken.setNextToken(t);
            }
            while (lastToken.getNextToken() != null && lastToken.getNextToken().isPaintable()) {
                lastToken = (TokenImpl)lastToken.getNextToken();
            }
            if (line < endLine) {
                final int docOffs = map.getElement(line).getEndOffset() - 1;
                t = new TokenImpl(new char[] { '\n' }, 0, 0, docOffs, 21);
                lastToken.setNextToken(t);
                lastToken = t;
            }
        }
        if (startOffs >= tokenList.getOffset()) {
            while (!tokenList.containsPosition(startOffs)) {
                tokenList = (TokenImpl)tokenList.getNextToken();
            }
            tokenList.makeStartAt(startOffs);
        }
        TokenImpl temp;
        for (temp = tokenList; temp != null && !temp.containsPosition(endOffs); temp = (TokenImpl)temp.getNextToken()) {}
        if (temp != null) {
            temp.textCount = endOffs - temp.getOffset();
            temp.setNextToken(null);
        }
        return tokenList;
    }
    
    public Token getTokenListForLine(final int line) {
        return ((RSyntaxDocument)this.getDocument()).getTokenListForLine(line);
    }
    
    TokenPainter getTokenPainter() {
        return this.tokenPainter;
    }
    
    public String getToolTipText(final MouseEvent e) {
        if (RSyntaxUtilities.getOS() == 2) {
            final Point newLoc = e.getPoint();
            if (newLoc != null && newLoc.equals(this.cachedTipLoc)) {
                return this.cachedTip;
            }
            this.cachedTipLoc = newLoc;
        }
        return this.cachedTip = this.getToolTipTextImpl(e);
    }
    
    protected String getToolTipTextImpl(final MouseEvent e) {
        String text = null;
        URL imageBase = null;
        if (this.parserManager != null) {
            final ToolTipInfo info = this.parserManager.getToolTipText(e);
            if (info != null) {
                text = info.getToolTipText();
                imageBase = info.getImageBase();
            }
        }
        if (text == null) {
            text = super.getToolTipText(e);
        }
        if (this.getUseFocusableTips()) {
            if (text != null) {
                if (this.focusableTip == null) {
                    this.focusableTip = new FocusableTip(this, this.parserManager);
                }
                this.focusableTip.setImageBase(imageBase);
                this.focusableTip.toolTipRequested(e, text);
            }
            else if (this.focusableTip != null) {
                this.focusableTip.possiblyDisposeOfTipWindow();
            }
            return null;
        }
        return text;
    }
    
    public boolean getUnderlineForToken(final Token t) {
        return (this.getHyperlinksEnabled() && (t.isHyperlink() || (this.linkGeneratorResult != null && this.linkGeneratorResult.getSourceOffset() == t.getOffset()))) || this.syntaxScheme.getStyle(t.getType()).underline;
    }
    
    public boolean getUseFocusableTips() {
        return this.useFocusableTips;
    }
    
    public boolean getUseSelectedTextColor() {
        return this.useSelectedTextColor;
    }
    
    protected void init() {
        super.init();
        this.metricsNeverRefreshed = true;
        this.tokenPainter = new DefaultTokenPainter();
        if (RSyntaxTextArea.toggleCurrentFoldAction == null) {
            createRstaPopupMenuActions();
        }
        this.syntaxStyleKey = "text/plain";
        this.setMatchedBracketBGColor(getDefaultBracketMatchBGColor());
        this.setMatchedBracketBorderColor(getDefaultBracketMatchBorderColor());
        this.setBracketMatchingEnabled(true);
        this.setAnimateBracketMatching(true);
        this.lastBracketMatchPos = -1;
        this.setSelectionColor(getDefaultSelectionColor());
        this.setTabLineColor(null);
        this.setMarkOccurrencesColor(MarkOccurrencesSupport.DEFAULT_COLOR);
        this.foldManager = new FoldManager(this);
        this.setAutoIndentEnabled(true);
        this.setCloseCurlyBraces(true);
        this.setCloseMarkupTags(true);
        this.setClearWhitespaceLinesEnabled(true);
        this.setHyperlinksEnabled(true);
        this.setLinkScanningMask(128);
        this.setHyperlinkForeground(Color.BLUE);
        this.isScanningForLinks = false;
        this.setUseFocusableTips(true);
        this.setDefaultAntiAliasingState();
        this.restoreDefaultSyntaxScheme();
        this.setHighlightSecondaryLanguages(true);
        (this.secondaryLanguageBackgrounds = new Color[3])[0] = new Color(16773324);
        this.secondaryLanguageBackgrounds[1] = new Color(14352090);
        this.secondaryLanguageBackgrounds[2] = new Color(16769264);
        this.setRightHandSideCorrection(0);
    }
    
    public boolean isAutoIndentEnabled() {
        return this.autoIndentEnabled;
    }
    
    public final boolean isBracketMatchingEnabled() {
        return this.bracketMatchingEnabled;
    }
    
    public boolean isClearWhitespaceLinesEnabled() {
        return this.clearWhitespaceLines;
    }
    
    public boolean isCodeFoldingEnabled() {
        return this.foldManager.isCodeFoldingEnabled();
    }
    
    public boolean isWhitespaceVisible() {
        return this.whitespaceVisible;
    }
    
    public Token modelToToken(final int offs) {
        if (offs >= 0) {
            try {
                final int line = this.getLineOfOffset(offs);
                final Token t = this.getTokenListForLine(line);
                return RSyntaxUtilities.getTokenAtOffset(t, offs);
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        return null;
    }
    
    protected void paintComponent(final Graphics g) {
        if (this.metricsNeverRefreshed) {
            this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
            this.metricsNeverRefreshed = false;
        }
        super.paintComponent(this.getGraphics2D(g));
    }
    
    private void refreshFontMetrics(final Graphics2D g2d) {
        this.defaultFontMetrics = g2d.getFontMetrics(this.getFont());
        this.syntaxScheme.refreshFontMetrics(g2d);
        if (!this.getLineWrap()) {
            final SyntaxView sv = (SyntaxView)this.getUI().getRootView(this).getView(0);
            sv.calculateLongestLine();
        }
    }
    
    public void removeActiveLineRangeListener(final ActiveLineRangeListener l) {
        this.listenerList.remove(ActiveLineRangeListener.class, l);
    }
    
    public void removeHyperlinkListener(final HyperlinkListener l) {
        this.listenerList.remove(HyperlinkListener.class, l);
    }
    
    public void removeNotify() {
        if (this.parserManager != null) {
            this.parserManager.stopParsing();
        }
        super.removeNotify();
    }
    
    public boolean removeParser(final Parser parser) {
        boolean removed = false;
        if (this.parserManager != null) {
            removed = this.parserManager.removeParser(parser);
        }
        return removed;
    }
    
    public void restoreDefaultSyntaxScheme() {
        this.setSyntaxScheme(this.getDefaultSyntaxScheme());
    }
    
    public static synchronized boolean saveTemplates() {
        return getTemplatesEnabled() && getCodeTemplateManager().saveTemplates();
    }
    
    public void setActiveLineRange(final int min, int max) {
        if (min == -1) {
            max = -1;
        }
        this.fireActiveLineRangeEvent(min, max);
    }
    
    public void setAnimateBracketMatching(final boolean animate) {
        if (animate != this.animateBracketMatching) {
            this.animateBracketMatching = animate;
            if (animate && this.bracketRepaintTimer == null) {
                this.bracketRepaintTimer = new BracketMatchingTimer();
            }
            this.firePropertyChange("RSTA.animateBracketMatching", !animate, animate);
        }
    }
    
    public void setAntiAliasingEnabled(final boolean enabled) {
        final boolean currentlyEnabled = this.aaHints != null;
        if (enabled != currentlyEnabled) {
            if (enabled) {
                this.aaHints = RSyntaxUtilities.getDesktopAntiAliasHints();
                if (this.aaHints == null) {
                    final Map<RenderingHints.Key, Object> temp = new HashMap<RenderingHints.Key, Object>();
                    temp.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    this.aaHints = temp;
                }
            }
            else {
                this.aaHints = null;
            }
            if (this.isDisplayable()) {
                this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
            }
            this.firePropertyChange("RSTA.antiAlias", !enabled, enabled);
            this.repaint();
        }
    }
    
    public void setAutoIndentEnabled(final boolean enabled) {
        if (this.autoIndentEnabled != enabled) {
            this.autoIndentEnabled = enabled;
            this.firePropertyChange("RSTA.autoIndent", !enabled, enabled);
        }
    }
    
    public void setBracketMatchingEnabled(final boolean enabled) {
        if (enabled != this.bracketMatchingEnabled) {
            this.bracketMatchingEnabled = enabled;
            this.repaint();
            this.firePropertyChange("RSTA.bracketMatching", !enabled, enabled);
        }
    }
    
    public void setClearWhitespaceLinesEnabled(final boolean enabled) {
        if (enabled != this.clearWhitespaceLines) {
            this.clearWhitespaceLines = enabled;
            this.firePropertyChange("RSTA.clearWhitespaceLines", !enabled, enabled);
        }
    }
    
    public void setCloseCurlyBraces(final boolean close) {
        if (close != this.closeCurlyBraces) {
            this.closeCurlyBraces = close;
            this.firePropertyChange("RSTA.closeCurlyBraces", !close, close);
        }
    }
    
    public void setCloseMarkupTags(final boolean close) {
        if (close != this.closeMarkupTags) {
            this.closeMarkupTags = close;
            this.firePropertyChange("RSTA.closeMarkupTags", !close, close);
        }
    }
    
    public void setCodeFoldingEnabled(final boolean enabled) {
        if (enabled != this.foldManager.isCodeFoldingEnabled()) {
            this.foldManager.setCodeFoldingEnabled(enabled);
            this.firePropertyChange("RSTA.codeFolding", !enabled, enabled);
        }
    }
    
    private final void setDefaultAntiAliasingState() {
        this.aaHints = RSyntaxUtilities.getDesktopAntiAliasHints();
        if (this.aaHints == null) {
            final Map<RenderingHints.Key, Object> temp = new HashMap<RenderingHints.Key, Object>();
            final JLabel label = new JLabel();
            final FontMetrics fm = label.getFontMetrics(label.getFont());
            Object hint = null;
            try {
                Method m = FontMetrics.class.getMethod("getFontRenderContext", (Class<?>[])new Class[0]);
                final FontRenderContext frc = (FontRenderContext)m.invoke(fm, new Object[0]);
                m = FontRenderContext.class.getMethod("getAntiAliasingHint", (Class<?>[])new Class[0]);
                hint = m.invoke(frc, new Object[0]);
            }
            catch (RuntimeException re) {
                throw re;
            }
            catch (Exception loc_0) {}
            if (hint == null) {
                final String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("windows")) {
                    hint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                }
                else {
                    hint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
                }
            }
            temp.put(RenderingHints.KEY_TEXT_ANTIALIASING, hint);
            this.aaHints = temp;
        }
        if (this.isDisplayable()) {
            this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
        }
        this.repaint();
    }
    
    public void setDocument(final Document document) {
        if (!(document instanceof RSyntaxDocument)) {
            throw new IllegalArgumentException("Documents for RSyntaxTextArea must be instances of RSyntaxDocument!");
        }
        super.setDocument(document);
    }
    
    public void setEOLMarkersVisible(final boolean visible) {
        if (visible != this.eolMarkersVisible) {
            this.eolMarkersVisible = visible;
            this.repaint();
            this.firePropertyChange("RSTA.eolMarkersVisible", !visible, visible);
        }
    }
    
    public void setFont(final Font font) {
        final Font old = super.getFont();
        super.setFont(font);
        final SyntaxScheme scheme = this.getSyntaxScheme();
        if (scheme != null && old != null) {
            scheme.changeBaseFont(old, font);
            this.calculateLineHeight();
        }
        if (this.isDisplayable()) {
            this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
            this.updateMarginLineX();
            this.forceCurrentLineHighlightRepaint();
            this.firePropertyChange("font", old, font);
            this.revalidate();
        }
    }
    
    public void setFractionalFontMetricsEnabled(final boolean enabled) {
        if (this.fractionalFontMetricsEnabled != enabled) {
            this.fractionalFontMetricsEnabled = enabled;
            if (this.isDisplayable()) {
                this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
            }
            this.firePropertyChange("RSTA.fractionalFontMetrics", !enabled, enabled);
        }
    }
    
    public void setHighlighter(final Highlighter h) {
        if (!(h instanceof RSyntaxTextAreaHighlighter)) {
            throw new IllegalArgumentException("RSyntaxTextArea requires an RSyntaxTextAreaHighlighter for its Highlighter");
        }
        super.setHighlighter(h);
    }
    
    public void setHighlightSecondaryLanguages(final boolean highlight) {
        if (this.highlightSecondaryLanguages != highlight) {
            this.highlightSecondaryLanguages = highlight;
            this.repaint();
            this.firePropertyChange("RSTA.highlightSecondaryLanguages", !highlight, highlight);
        }
    }
    
    public void setHyperlinkForeground(final Color fg) {
        if (fg == null) {
            throw new NullPointerException("fg cannot be null");
        }
        this.hyperlinkFG = fg;
    }
    
    public void setHyperlinksEnabled(final boolean enabled) {
        if (this.hyperlinksEnabled != enabled) {
            this.hyperlinksEnabled = enabled;
            this.repaint();
            this.firePropertyChange("RSTA.hyperlinksEnabled", !enabled, enabled);
        }
    }
    
    public void setLinkGenerator(final LinkGenerator generator) {
        this.linkGenerator = generator;
    }
    
    public void setLinkScanningMask(int mask) {
        mask &= 0x3C0;
        if (mask == 0) {
            throw new IllegalArgumentException("mask argument should be some combination of InputEvent.*_DOWN_MASK fields");
        }
        this.linkScanningMask = mask;
    }
    
    public void setMarkOccurrences(final boolean markOccurrences) {
        if (markOccurrences) {
            if (this.markOccurrencesSupport == null) {
                (this.markOccurrencesSupport = new MarkOccurrencesSupport()).install(this);
                this.firePropertyChange("RSTA.markOccurrences", false, true);
            }
        }
        else if (this.markOccurrencesSupport != null) {
            this.markOccurrencesSupport.uninstall();
            this.markOccurrencesSupport = null;
            this.firePropertyChange("RSTA.markOccurrences", true, false);
        }
    }
    
    public void setMarkOccurrencesColor(final Color color) {
        this.markOccurrencesColor = color;
        if (this.markOccurrencesSupport != null) {
            this.markOccurrencesSupport.setColor(color);
        }
    }
    
    public void setMatchedBracketBGColor(final Color color) {
        this.matchedBracketBGColor = color;
        if (this.match != null) {
            this.repaint();
        }
    }
    
    public void setMatchedBracketBorderColor(final Color color) {
        this.matchedBracketBorderColor = color;
        if (this.match != null) {
            this.repaint();
        }
    }
    
    public void setPaintMarkOccurrencesBorder(final boolean paintBorder) {
        this.paintMarkOccurrencesBorder = paintBorder;
        if (this.markOccurrencesSupport != null) {
            this.markOccurrencesSupport.setPaintBorder(paintBorder);
        }
    }
    
    public void setPaintMatchedBracketPair(final boolean paintPair) {
        if (paintPair != this.paintMatchedBracketPair) {
            this.paintMatchedBracketPair = paintPair;
            this.doBracketMatching();
            this.repaint();
            this.firePropertyChange("RSTA.paintMatchedBracketPair", !this.paintMatchedBracketPair, this.paintMatchedBracketPair);
        }
    }
    
    public void setPaintTabLines(final boolean paint) {
        if (paint != this.paintTabLines) {
            this.paintTabLines = paint;
            this.repaint();
            this.firePropertyChange("RSTA.tabLines", !paint, paint);
        }
    }
    
    public void setRightHandSideCorrection(final int rhsCorrection) {
        if (rhsCorrection < 0) {
            throw new IllegalArgumentException("correction should be > 0");
        }
        if (rhsCorrection != this.rhsCorrection) {
            this.rhsCorrection = rhsCorrection;
            this.revalidate();
            this.repaint();
        }
    }
    
    public void setSecondaryLanguageBackground(int index, final Color color) {
        --index;
        final Color old = this.secondaryLanguageBackgrounds[index];
        if ((color == null && old != null) || (color != null && !color.equals(old))) {
            this.secondaryLanguageBackgrounds[index] = color;
            if (this.getHighlightSecondaryLanguages()) {
                this.repaint();
            }
        }
    }
    
    public void setSyntaxEditingStyle(String styleKey) {
        if (styleKey == null) {
            styleKey = "text/plain";
        }
        if (!styleKey.equals(this.syntaxStyleKey)) {
            final String oldStyle = this.syntaxStyleKey;
            this.syntaxStyleKey = styleKey;
            ((RSyntaxDocument)this.getDocument()).setSyntaxStyle(styleKey);
            this.firePropertyChange("RSTA.syntaxStyle", oldStyle, styleKey);
            this.setActiveLineRange(-1, -1);
        }
    }
    
    public void setSyntaxScheme(final SyntaxScheme scheme) {
        final SyntaxScheme old = this.syntaxScheme;
        this.syntaxScheme = scheme;
        this.calculateLineHeight();
        if (this.isDisplayable()) {
            this.refreshFontMetrics(this.getGraphics2D(this.getGraphics()));
        }
        this.updateMarginLineX();
        this.forceCurrentLineHighlightRepaint();
        this.revalidate();
        this.firePropertyChange("RSTA.syntaxScheme", old, this.syntaxScheme);
    }
    
    public static synchronized boolean setTemplateDirectory(final String dir) {
        if (getTemplatesEnabled() && dir != null) {
            final File directory = new File(dir);
            if (directory.isDirectory()) {
                return getCodeTemplateManager().setTemplateDirectory(directory) > -1;
            }
            final boolean created = directory.mkdir();
            if (created) {
                return getCodeTemplateManager().setTemplateDirectory(directory) > -1;
            }
        }
        return false;
    }
    
    public static synchronized void setTemplatesEnabled(final boolean enabled) {
        RSyntaxTextArea.templatesEnabled = enabled;
    }
    
    public void setTabLineColor(Color c) {
        if (c == null) {
            c = Color.gray;
        }
        if (!c.equals(this.tabLineColor)) {
            final Color old = this.tabLineColor;
            this.tabLineColor = c;
            if (this.getPaintTabLines()) {
                this.repaint();
            }
            this.firePropertyChange("RSTA.tabLineColor", old, this.tabLineColor);
        }
    }
    
    public void setUseFocusableTips(final boolean use) {
        if (use != this.useFocusableTips) {
            this.useFocusableTips = use;
            this.firePropertyChange("RSTA.focusableTips", !use, use);
        }
    }
    
    public void setUseSelectedTextColor(final boolean use) {
        if (use != this.useSelectedTextColor) {
            this.useSelectedTextColor = use;
            this.firePropertyChange("RSTA.useSelectedTextColor", !use, use);
        }
    }
    
    public void setWhitespaceVisible(final boolean visible) {
        if (this.whitespaceVisible != visible) {
            this.whitespaceVisible = visible;
            this.tokenPainter = (visible ? new VisibleWhitespaceTokenPainter() : new DefaultTokenPainter());
            this.repaint();
            this.firePropertyChange("RSTA.visibleWhitespace", !visible, visible);
        }
    }
    
    private void stopScanningForLinks() {
        if (this.isScanningForLinks) {
            final Cursor c = this.getCursor();
            this.isScanningForLinks = false;
            this.linkGeneratorResult = null;
            this.hoveredOverLinkOffset = -1;
            if (c != null && c.getType() == 12) {
                this.setCursor(Cursor.getPredefinedCursor(2));
                this.repaint();
            }
        }
    }
    
    public Token viewToToken(final Point p) {
        return this.modelToToken(this.viewToModel(p));
    }
    
    static /* synthetic */ void access$000(final RSyntaxTextArea x0) {
        x0.possiblyUpdateCurrentLineHighlightLocation();
    }
    
    static /* synthetic */ Rectangle access$100(final RSyntaxTextArea x0) {
        return x0.match;
    }
    
    static /* synthetic */ Rectangle access$200(final RSyntaxTextArea x0) {
        return x0.dotRect;
    }
    
    static /* synthetic */ LinkGeneratorResult access$300(final RSyntaxTextArea x0) {
        return x0.linkGeneratorResult;
    }
    
    static /* synthetic */ LinkGeneratorResult access$302(final RSyntaxTextArea x0, final LinkGeneratorResult x1) {
        return x0.linkGeneratorResult = x1;
    }
    
    static /* synthetic */ int access$400(final RSyntaxTextArea x0) {
        return x0.hoveredOverLinkOffset;
    }
    
    static /* synthetic */ boolean access$500(final RSyntaxTextArea x0) {
        return x0.isScanningForLinks;
    }
    
    static /* synthetic */ void access$600(final RSyntaxTextArea x0, final HyperlinkEvent x1) {
        x0.fireHyperlinkUpdate(x1);
    }
    
    static /* synthetic */ void access$700(final RSyntaxTextArea x0) {
        x0.stopScanningForLinks();
    }
    
    static /* synthetic */ int access$800(final RSyntaxTextArea x0) {
        return x0.linkScanningMask;
    }
    
    static /* synthetic */ boolean access$502(final RSyntaxTextArea x0, final boolean x1) {
        return x0.isScanningForLinks = x1;
    }
    
    static /* synthetic */ int access$402(final RSyntaxTextArea x0, final int x1) {
        return x0.hoveredOverLinkOffset = x1;
    }
    
    static /* synthetic */ LinkGenerator access$900(final RSyntaxTextArea x0) {
        return x0.linkGenerator;
    }
    
    static {
        DEFAULT_BRACKET_MATCH_BG_COLOR = new Color(234, 234, 255);
        DEFAULT_BRACKET_MATCH_BORDER_COLOR = new Color(0, 0, 128);
        DEFAULT_SELECTION_COLOR = new Color(200, 200, 255);
    }
    
    private class BracketMatchingTimer extends Timer implements ActionListener
    {
        private int pulseCount;
        
        public BracketMatchingTimer() {
            super(20, null);
            this.addActionListener(this);
            this.setCoalesce(false);
        }
        
        public void actionPerformed(final ActionEvent e) {
            if (RSyntaxTextArea.this.isBracketMatchingEnabled()) {
                if (RSyntaxTextArea.access$100(RSyntaxTextArea.this) != null) {
                    this.updateAndInvalidate(RSyntaxTextArea.access$100(RSyntaxTextArea.this));
                }
                if (RSyntaxTextArea.access$200(RSyntaxTextArea.this) != null && RSyntaxTextArea.this.getPaintMatchedBracketPair()) {
                    this.updateAndInvalidate(RSyntaxTextArea.access$200(RSyntaxTextArea.this));
                }
                if (++this.pulseCount == 8) {
                    this.pulseCount = 0;
                    this.stop();
                }
            }
        }
        
        private void init(final Rectangle r) {
            r.x += 3;
            r.y += 3;
            r.width -= 6;
            r.height -= 6;
        }
        
        public void start() {
            this.init(RSyntaxTextArea.access$100(RSyntaxTextArea.this));
            if (RSyntaxTextArea.access$200(RSyntaxTextArea.this) != null && RSyntaxTextArea.this.getPaintMatchedBracketPair()) {
                this.init(RSyntaxTextArea.access$200(RSyntaxTextArea.this));
            }
            this.pulseCount = 0;
            super.start();
        }
        
        private void updateAndInvalidate(final Rectangle r) {
            if (this.pulseCount < 5) {
                --r.x;
                --r.y;
                r.width += 2;
                r.height += 2;
                RSyntaxTextArea.this.repaint(r.x, r.y, r.width, r.height);
            }
            else if (this.pulseCount < 7) {
                ++r.x;
                ++r.y;
                r.width -= 2;
                r.height -= 2;
                RSyntaxTextArea.this.repaint(r.x - 2, r.y - 2, r.width + 5, r.height + 5);
            }
        }
    }
    
    private class RSyntaxTextAreaMutableCaretEvent extends RTextAreaMutableCaretEvent
    {
        private Insets insets;
        
        protected RSyntaxTextAreaMutableCaretEvent(final RTextArea textArea) {
            super(textArea);
            this.insets = new Insets(0, 0, 0, 0);
        }
        
        private HyperlinkEvent createHyperlinkEvent() {
            HyperlinkEvent he = null;
            if (RSyntaxTextArea.access$300(RSyntaxTextArea.this) != null) {
                he = RSyntaxTextArea.access$300(RSyntaxTextArea.this).execute();
                RSyntaxTextArea.access$302(RSyntaxTextArea.this, null);
            }
            else {
                final Token t = RSyntaxTextArea.this.modelToToken(RSyntaxTextArea.access$400(RSyntaxTextArea.this));
                URL url = null;
                String desc = null;
                try {
                    String temp = t.getLexeme();
                    if (temp.startsWith("www.")) {
                        temp = "http://" + temp;
                    }
                    url = new URL(temp);
                }
                catch (MalformedURLException mue) {
                    desc = mue.getMessage();
                }
                he = new HyperlinkEvent(RSyntaxTextArea.this, HyperlinkEvent.EventType.ACTIVATED, url, desc);
            }
            return he;
        }
        
        private final boolean equal(final LinkGeneratorResult e1, final LinkGeneratorResult e2) {
            return e1.getSourceOffset() == e2.getSourceOffset();
        }
        
        public void mouseClicked(final MouseEvent e) {
            if (RSyntaxTextArea.this.getHyperlinksEnabled() && RSyntaxTextArea.access$500(RSyntaxTextArea.this) && RSyntaxTextArea.access$400(RSyntaxTextArea.this) > -1) {
                final HyperlinkEvent he = this.createHyperlinkEvent();
                if (he != null) {
                    RSyntaxTextArea.access$600(RSyntaxTextArea.this, he);
                }
                RSyntaxTextArea.access$700(RSyntaxTextArea.this);
            }
        }
        
        public void mouseMoved(final MouseEvent e) {
            super.mouseMoved(e);
            if (!RSyntaxTextArea.this.getHyperlinksEnabled()) {
                return;
            }
            if ((e.getModifiersEx() & RSyntaxTextArea.access$800(RSyntaxTextArea.this)) == RSyntaxTextArea.access$800(RSyntaxTextArea.this)) {
                this.insets = RSyntaxTextArea.this.getInsets(this.insets);
                if (this.insets != null) {
                    final int x = e.getX();
                    final int y = e.getY();
                    if (x <= this.insets.left || y < this.insets.top) {
                        if (RSyntaxTextArea.access$500(RSyntaxTextArea.this)) {
                            RSyntaxTextArea.access$700(RSyntaxTextArea.this);
                        }
                        return;
                    }
                }
                RSyntaxTextArea.access$502(RSyntaxTextArea.this, true);
                Token t = RSyntaxTextArea.this.viewToToken(e.getPoint());
                if (t != null) {
                    t = new TokenImpl(t);
                }
                Cursor c2 = null;
                if (t != null && t.isHyperlink()) {
                    if (RSyntaxTextArea.access$400(RSyntaxTextArea.this) == -1 || RSyntaxTextArea.access$400(RSyntaxTextArea.this) != t.getOffset()) {
                        RSyntaxTextArea.access$402(RSyntaxTextArea.this, t.getOffset());
                        RSyntaxTextArea.this.repaint();
                    }
                    c2 = Cursor.getPredefinedCursor(12);
                }
                else if (t != null && RSyntaxTextArea.access$900(RSyntaxTextArea.this) != null) {
                    final int offs = RSyntaxTextArea.this.viewToModel(e.getPoint());
                    final LinkGeneratorResult newResult = RSyntaxTextArea.access$900(RSyntaxTextArea.this).isLinkAtOffset(RSyntaxTextArea.this, offs);
                    if (newResult != null) {
                        if (RSyntaxTextArea.access$300(RSyntaxTextArea.this) == null || !this.equal(newResult, RSyntaxTextArea.access$300(RSyntaxTextArea.this))) {
                            RSyntaxTextArea.this.repaint();
                        }
                        RSyntaxTextArea.access$302(RSyntaxTextArea.this, newResult);
                        RSyntaxTextArea.access$402(RSyntaxTextArea.this, t.getOffset());
                        c2 = Cursor.getPredefinedCursor(12);
                    }
                    else {
                        if (RSyntaxTextArea.access$300(RSyntaxTextArea.this) != null) {
                            RSyntaxTextArea.this.repaint();
                        }
                        c2 = Cursor.getPredefinedCursor(2);
                        RSyntaxTextArea.access$402(RSyntaxTextArea.this, -1);
                        RSyntaxTextArea.access$302(RSyntaxTextArea.this, null);
                    }
                }
                else {
                    c2 = Cursor.getPredefinedCursor(2);
                    RSyntaxTextArea.access$402(RSyntaxTextArea.this, -1);
                    RSyntaxTextArea.access$302(RSyntaxTextArea.this, null);
                }
                if (RSyntaxTextArea.this.getCursor() != c2) {
                    RSyntaxTextArea.this.setCursor(c2);
                    RSyntaxTextArea.this.repaint();
                }
            }
            else if (RSyntaxTextArea.access$500(RSyntaxTextArea.this)) {
                RSyntaxTextArea.access$700(RSyntaxTextArea.this);
            }
        }
    }
}
