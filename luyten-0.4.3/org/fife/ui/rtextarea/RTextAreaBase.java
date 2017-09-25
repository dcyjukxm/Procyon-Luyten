package org.fife.ui.rtextarea;

import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import java.awt.event.*;

public abstract class RTextAreaBase extends JTextArea
{
    public static final String BACKGROUND_IMAGE_PROPERTY = "background.image";
    public static final String CURRENT_LINE_HIGHLIGHT_COLOR_PROPERTY = "RTA.currentLineHighlightColor";
    public static final String CURRENT_LINE_HIGHLIGHT_FADE_PROPERTY = "RTA.currentLineHighlightFade";
    public static final String HIGHLIGHT_CURRENT_LINE_PROPERTY = "RTA.currentLineHighlight";
    public static final String ROUNDED_SELECTION_PROPERTY = "RTA.roundedSelection";
    private boolean tabsEmulatedWithSpaces;
    private boolean highlightCurrentLine;
    private Color currentLineColor;
    private boolean marginLineEnabled;
    private Color marginLineColor;
    private int marginLineX;
    private int marginSizeInChars;
    private boolean fadeCurrentLineHighlight;
    private boolean roundedSelectionEdges;
    private int previousCaretY;
    int currentCaretY;
    private BackgroundPainterStrategy backgroundPainter;
    private RTAMouseListener mouseListener;
    private static final Color DEFAULT_CARET_COLOR;
    private static final Color DEFAULT_CURRENT_LINE_HIGHLIGHT_COLOR;
    private static final Color DEFAULT_MARGIN_LINE_COLOR;
    private static final int DEFAULT_TAB_SIZE = 4;
    private static final int DEFAULT_MARGIN_LINE_POSITION = 80;
    
    public RTextAreaBase() {
        super();
        this.init();
    }
    
    public RTextAreaBase(final AbstractDocument doc) {
        super(doc);
        this.init();
    }
    
    public RTextAreaBase(final String text) {
        super();
        this.init();
        this.setText(text);
    }
    
    public RTextAreaBase(final int rows, final int cols) {
        super(rows, cols);
        this.init();
    }
    
    public RTextAreaBase(final String text, final int rows, final int cols) {
        super(rows, cols);
        this.init();
        this.setText(text);
    }
    
    public RTextAreaBase(final AbstractDocument doc, final String text, final int rows, final int cols) {
        super(doc, null, rows, cols);
        this.init();
        this.setText(text);
    }
    
    private void addCurrentLineHighlightListeners() {
        boolean add = true;
        final MouseMotionListener[] mouseMotionListeners = this.getMouseMotionListeners();
        for (int i = 0; i < mouseMotionListeners.length; ++i) {
            if (mouseMotionListeners[i] == this.mouseListener) {
                add = false;
                break;
            }
        }
        if (add) {
            this.addMouseMotionListener(this.mouseListener);
        }
        final MouseListener[] mouseListeners = this.getMouseListeners();
        for (int j = 0; j < mouseListeners.length; ++j) {
            if (mouseListeners[j] == this.mouseListener) {
                add = false;
                break;
            }
        }
        if (add) {
            this.addMouseListener(this.mouseListener);
        }
    }
    
    public void convertSpacesToTabs() {
        final int caretPosition = this.getCaretPosition();
        final int tabSize = this.getTabSize();
        String tabInSpaces = "";
        for (int i = 0; i < tabSize; ++i) {
            tabInSpaces += " ";
        }
        final String text = this.getText();
        this.setText(text.replaceAll(tabInSpaces, "\t"));
        final int newDocumentLength = this.getDocument().getLength();
        if (caretPosition < newDocumentLength) {
            this.setCaretPosition(caretPosition);
        }
        else {
            this.setCaretPosition(newDocumentLength - 1);
        }
    }
    
    public void convertTabsToSpaces() {
        final int caretPosition = this.getCaretPosition();
        final int tabSize = this.getTabSize();
        final StringBuilder tabInSpaces = new StringBuilder();
        for (int i = 0; i < tabSize; ++i) {
            tabInSpaces.append(' ');
        }
        final String text = this.getText();
        this.setText(text.replaceAll("\t", tabInSpaces.toString()));
        this.setCaretPosition(caretPosition);
    }
    
    protected abstract RTAMouseListener createMouseListener();
    
    protected abstract RTextAreaUI createRTextAreaUI();
    
    protected void forceCurrentLineHighlightRepaint() {
        if (this.isShowing()) {
            this.previousCaretY = -1;
            this.fireCaretUpdate(this.mouseListener);
        }
    }
    
    public final Color getBackground() {
        final Object bg = this.getBackgroundObject();
        return (bg instanceof Color) ? ((Color)bg) : null;
    }
    
    public final Image getBackgroundImage() {
        final Object bg = this.getBackgroundObject();
        return (bg instanceof Image) ? ((Image)bg) : null;
    }
    
    public final Object getBackgroundObject() {
        if (this.backgroundPainter == null) {
            return null;
        }
        return (this.backgroundPainter instanceof ImageBackgroundPainterStrategy) ? ((ImageBackgroundPainterStrategy)this.backgroundPainter).getMasterImage() : ((ColorBackgroundPainterStrategy)this.backgroundPainter).getColor();
    }
    
    public final int getCaretLineNumber() {
        try {
            return this.getLineOfOffset(this.getCaretPosition());
        }
        catch (BadLocationException ble) {
            return 0;
        }
    }
    
    public final int getCaretOffsetFromLineStart() {
        try {
            final int pos = this.getCaretPosition();
            return pos - this.getLineStartOffset(this.getLineOfOffset(pos));
        }
        catch (BadLocationException ble) {
            return 0;
        }
    }
    
    public Color getCurrentLineHighlightColor() {
        return this.currentLineColor;
    }
    
    public static final Color getDefaultCaretColor() {
        return RTextAreaBase.DEFAULT_CARET_COLOR;
    }
    
    public static final Color getDefaultCurrentLineHighlightColor() {
        return RTextAreaBase.DEFAULT_CURRENT_LINE_HIGHLIGHT_COLOR;
    }
    
    public static final Font getDefaultFont() {
        final StyleContext sc = StyleContext.getDefaultStyleContext();
        Font font = null;
        if (isOSX()) {
            font = sc.getFont("Menlo", 0, 12);
            if (!"Menlo".equals(font.getFamily())) {
                font = sc.getFont("Monaco", 0, 12);
                if (!"Monaco".equals(font.getFamily())) {
                    font = sc.getFont("Monospaced", 0, 13);
                }
            }
        }
        else {
            font = sc.getFont("Consolas", 0, 13);
            if (!"Consolas".equals(font.getFamily())) {
                font = sc.getFont("Monospaced", 0, 13);
            }
        }
        return font;
    }
    
    public static final Color getDefaultForeground() {
        return Color.BLACK;
    }
    
    public static final Color getDefaultMarginLineColor() {
        return RTextAreaBase.DEFAULT_MARGIN_LINE_COLOR;
    }
    
    public static final int getDefaultMarginLinePosition() {
        return 80;
    }
    
    public static final int getDefaultTabSize() {
        return 4;
    }
    
    public boolean getFadeCurrentLineHighlight() {
        return this.fadeCurrentLineHighlight;
    }
    
    public boolean getHighlightCurrentLine() {
        return this.highlightCurrentLine;
    }
    
    public final int getLineEndOffsetOfCurrentLine() {
        try {
            return this.getLineEndOffset(this.getCaretLineNumber());
        }
        catch (BadLocationException ble) {
            return 0;
        }
    }
    
    public int getLineHeight() {
        return this.getRowHeight();
    }
    
    public final int getLineStartOffsetOfCurrentLine() {
        try {
            return this.getLineStartOffset(this.getCaretLineNumber());
        }
        catch (BadLocationException ble) {
            return 0;
        }
    }
    
    public Color getMarginLineColor() {
        return this.marginLineColor;
    }
    
    public int getMarginLinePixelLocation() {
        return this.marginLineX;
    }
    
    public int getMarginLinePosition() {
        return this.marginSizeInChars;
    }
    
    public boolean getRoundedSelectionEdges() {
        return this.roundedSelectionEdges;
    }
    
    public boolean getTabsEmulated() {
        return this.tabsEmulatedWithSpaces;
    }
    
    protected void init() {
        this.setRTextAreaUI(this.createRTextAreaUI());
        this.enableEvents(9L);
        this.setHighlightCurrentLine(true);
        this.setCurrentLineHighlightColor(getDefaultCurrentLineHighlightColor());
        this.setMarginLineEnabled(false);
        this.setMarginLineColor(getDefaultMarginLineColor());
        this.setMarginLinePosition(getDefaultMarginLinePosition());
        this.setBackgroundObject(Color.WHITE);
        this.setWrapStyleWord(true);
        this.setTabSize(5);
        this.setForeground(Color.BLACK);
        this.setTabsEmulated(false);
        final int loc_0 = this.getInsets().top;
        this.currentCaretY = loc_0;
        this.previousCaretY = loc_0;
        this.addFocusListener(this.mouseListener = this.createMouseListener());
        this.addCurrentLineHighlightListeners();
    }
    
    public boolean isMarginLineEnabled() {
        return this.marginLineEnabled;
    }
    
    public static boolean isOSX() {
        final String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("mac os x");
    }
    
    protected void paintComponent(final Graphics g) {
        this.backgroundPainter.paint(g, this.getVisibleRect());
        final TextUI ui = this.getUI();
        if (ui != null) {
            final Graphics scratchGraphics = g.create();
            try {
                ui.update(scratchGraphics, this);
            }
            finally {
                scratchGraphics.dispose();
            }
        }
    }
    
    protected void possiblyUpdateCurrentLineHighlightLocation() {
        final int width = this.getWidth();
        final int lineHeight = this.getLineHeight();
        final int dot = this.getCaretPosition();
        if (this.getLineWrap()) {
            try {
                final Rectangle temp = this.modelToView(dot);
                if (temp != null) {
                    this.currentCaretY = temp.y;
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        else {
            try {
                final Rectangle temp = this.modelToView(dot);
                if (temp != null) {
                    this.currentCaretY = temp.y;
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        this.repaint(0, this.currentCaretY, width, lineHeight);
        if (this.previousCaretY != this.currentCaretY) {
            this.repaint(0, this.previousCaretY, width, lineHeight);
        }
        this.previousCaretY = this.currentCaretY;
    }
    
    protected void processComponentEvent(final ComponentEvent e) {
        if (e.getID() == 101 && this.getLineWrap() && this.getHighlightCurrentLine()) {
            this.previousCaretY = -1;
            this.fireCaretUpdate(this.mouseListener);
        }
        super.processComponentEvent(e);
    }
    
    public void setBackground(final Color bg) {
        final Object oldBG = this.getBackgroundObject();
        if (oldBG instanceof Color) {
            ((ColorBackgroundPainterStrategy)this.backgroundPainter).setColor(bg);
        }
        else {
            this.backgroundPainter = new ColorBackgroundPainterStrategy(bg);
        }
        this.setOpaque(true);
        this.firePropertyChange("background", oldBG, bg);
        this.repaint();
    }
    
    public void setBackgroundImage(final Image image) {
        final Object oldBG = this.getBackgroundObject();
        if (oldBG instanceof Image) {
            ((ImageBackgroundPainterStrategy)this.backgroundPainter).setImage(image);
        }
        else {
            final ImageBackgroundPainterStrategy strategy = new BufferedImageBackgroundPainterStrategy(this);
            strategy.setImage(image);
            this.backgroundPainter = strategy;
        }
        this.setOpaque(false);
        this.firePropertyChange("background.image", oldBG, image);
        this.repaint();
    }
    
    public void setBackgroundObject(final Object newBackground) {
        if (newBackground instanceof Color) {
            this.setBackground((Color)newBackground);
        }
        else if (newBackground instanceof Image) {
            this.setBackgroundImage((Image)newBackground);
        }
        else {
            this.setBackground(Color.WHITE);
        }
    }
    
    public void setCurrentLineHighlightColor(final Color color) {
        if (color == null) {
            throw new NullPointerException();
        }
        if (!color.equals(this.currentLineColor)) {
            final Color old = this.currentLineColor;
            this.firePropertyChange("RTA.currentLineHighlightColor", old, this.currentLineColor = color);
        }
    }
    
    public void setFadeCurrentLineHighlight(final boolean fade) {
        if (fade != this.fadeCurrentLineHighlight) {
            this.fadeCurrentLineHighlight = fade;
            if (this.getHighlightCurrentLine()) {
                this.forceCurrentLineHighlightRepaint();
            }
            this.firePropertyChange("RTA.currentLineHighlightFade", !fade, fade);
        }
    }
    
    public void setFont(final Font font) {
        super.setFont(font);
        if (font != null) {
            this.updateMarginLineX();
            if (this.highlightCurrentLine) {
                this.possiblyUpdateCurrentLineHighlightLocation();
            }
        }
    }
    
    public void setHighlightCurrentLine(final boolean highlight) {
        if (highlight != this.highlightCurrentLine) {
            this.highlightCurrentLine = highlight;
            this.firePropertyChange("RTA.currentLineHighlight", !highlight, highlight);
            this.repaint();
        }
    }
    
    public void setLineWrap(final boolean wrap) {
        super.setLineWrap(wrap);
        this.forceCurrentLineHighlightRepaint();
    }
    
    public void setMargin(final Insets insets) {
        final Insets old = this.getInsets();
        final int oldTop = (old != null) ? old.top : 0;
        final int newTop = (insets != null) ? insets.top : 0;
        if (oldTop != newTop) {
            final int loc_0 = newTop;
            this.currentCaretY = loc_0;
            this.previousCaretY = loc_0;
        }
        super.setMargin(insets);
    }
    
    public void setMarginLineColor(final Color color) {
        this.marginLineColor = color;
        if (this.marginLineEnabled) {
            final Rectangle visibleRect = this.getVisibleRect();
            this.repaint(this.marginLineX, visibleRect.y, 1, visibleRect.height);
        }
    }
    
    public void setMarginLineEnabled(final boolean enabled) {
        if (enabled != this.marginLineEnabled) {
            this.marginLineEnabled = enabled;
            if (this.marginLineEnabled) {
                final Rectangle visibleRect = this.getVisibleRect();
                this.repaint(this.marginLineX, visibleRect.y, 1, visibleRect.height);
            }
        }
    }
    
    public void setMarginLinePosition(final int size) {
        this.marginSizeInChars = size;
        if (this.marginLineEnabled) {
            final Rectangle visibleRect = this.getVisibleRect();
            this.repaint(this.marginLineX, visibleRect.y, 1, visibleRect.height);
            this.updateMarginLineX();
            this.repaint(this.marginLineX, visibleRect.y, 1, visibleRect.height);
        }
    }
    
    public void setRoundedSelectionEdges(final boolean rounded) {
        if (this.roundedSelectionEdges != rounded) {
            this.roundedSelectionEdges = rounded;
            final Caret c = this.getCaret();
            if (c instanceof ConfigurableCaret) {
                ((ConfigurableCaret)c).setRoundedSelectionEdges(rounded);
                if (c.getDot() != c.getMark()) {
                    this.repaint();
                }
            }
            this.firePropertyChange("RTA.roundedSelection", !rounded, rounded);
        }
    }
    
    protected void setRTextAreaUI(final RTextAreaUI ui) {
        super.setUI(ui);
        this.setOpaque(this.getBackgroundObject() instanceof Color);
    }
    
    public void setTabsEmulated(final boolean areEmulated) {
        this.tabsEmulatedWithSpaces = areEmulated;
    }
    
    public void setTabSize(final int size) {
        super.setTabSize(size);
        final boolean b = this.getLineWrap();
        this.setLineWrap(!b);
        this.setLineWrap(b);
    }
    
    protected void updateMarginLineX() {
        final Font font = this.getFont();
        if (font == null) {
            this.marginLineX = 0;
            return;
        }
        this.marginLineX = this.getFontMetrics(font).charWidth('m') * this.marginSizeInChars;
    }
    
    public int yForLine(final int line) throws BadLocationException {
        return ((RTextAreaUI)this.getUI()).yForLine(line);
    }
    
    public int yForLineContaining(final int offs) throws BadLocationException {
        return ((RTextAreaUI)this.getUI()).yForLineContaining(offs);
    }
    
    static {
        DEFAULT_CARET_COLOR = new ColorUIResource(255, 51, 51);
        DEFAULT_CURRENT_LINE_HIGHLIGHT_COLOR = new Color(255, 255, 170);
        DEFAULT_MARGIN_LINE_COLOR = new Color(255, 224, 224);
    }
    
    protected class RTAMouseListener extends CaretEvent implements MouseListener, MouseMotionListener, FocusListener
    {
        protected int dot;
        protected int mark;
        
        RTAMouseListener(final RTextAreaBase textArea) {
            super(textArea);
        }
        
        public void focusGained(final FocusEvent e) {
        }
        
        public void focusLost(final FocusEvent e) {
        }
        
        public void mouseDragged(final MouseEvent e) {
        }
        
        public void mouseMoved(final MouseEvent e) {
        }
        
        public void mouseClicked(final MouseEvent e) {
        }
        
        public void mousePressed(final MouseEvent e) {
        }
        
        public void mouseReleased(final MouseEvent e) {
        }
        
        public void mouseEntered(final MouseEvent e) {
        }
        
        public void mouseExited(final MouseEvent e) {
        }
        
        public int getDot() {
            return this.dot;
        }
        
        public int getMark() {
            return this.mark;
        }
    }
}
