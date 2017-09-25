package org.fife.ui.rtextarea;

import java.awt.event.*;
import java.awt.datatransfer.*;
import javax.swing.plaf.*;
import javax.swing.text.*;
import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.*;
import java.awt.*;
import org.fife.ui.rsyntaxtextarea.folding.*;

public class ConfigurableCaret extends DefaultCaret
{
    private static transient Action selectWord;
    private static transient Action selectLine;
    private transient MouseEvent selectedWordEvent;
    private transient Segment seg;
    private CaretStyle style;
    private ChangeableHighlightPainter selectionPainter;
    
    public ConfigurableCaret() {
        this(CaretStyle.THICK_VERTICAL_LINE_STYLE);
    }
    
    public ConfigurableCaret(final CaretStyle style) {
        super();
        this.selectedWordEvent = null;
        this.seg = new Segment();
        this.setStyle(style);
        this.selectionPainter = new ChangeableHighlightPainter();
    }
    
    private void adjustCaret(final MouseEvent e) {
        if ((e.getModifiers() & 0x1) != 0x0 && this.getDot() != -1) {
            this.moveCaret(e);
        }
        else {
            this.positionCaret(e);
        }
    }
    
    private void adjustFocus(final boolean inWindow) {
        final RTextArea textArea = this.getTextArea();
        if (textArea != null && textArea.isEnabled() && textArea.isRequestFocusEnabled()) {
            if (inWindow) {
                textArea.requestFocusInWindow();
            }
            else {
                textArea.requestFocus();
            }
        }
    }
    
    protected synchronized void damage(final Rectangle r) {
        if (r != null) {
            this.validateWidth(r);
            this.x = r.x - 1;
            this.y = r.y;
            this.width = r.width + 4;
            this.height = r.height;
            this.repaint();
        }
    }
    
    public void deinstall(final JTextComponent c) {
        if (!(c instanceof RTextArea)) {
            throw new IllegalArgumentException("c must be instance of RTextArea");
        }
        super.deinstall(c);
        c.setNavigationFilter(null);
    }
    
    protected RTextArea getTextArea() {
        return (RTextArea)this.getComponent();
    }
    
    public boolean getRoundedSelectionEdges() {
        return ((ChangeableHighlightPainter)this.getSelectionPainter()).getRoundedEdges();
    }
    
    protected Highlighter.HighlightPainter getSelectionPainter() {
        return this.selectionPainter;
    }
    
    public CaretStyle getStyle() {
        return this.style;
    }
    
    public void install(final JTextComponent c) {
        if (!(c instanceof RTextArea)) {
            throw new IllegalArgumentException("c must be instance of RTextArea");
        }
        super.install(c);
        c.setNavigationFilter(new FoldAwareNavigationFilter());
    }
    
    public void mouseClicked(final MouseEvent e) {
        if (!e.isConsumed()) {
            final RTextArea textArea = this.getTextArea();
            int nclicks = e.getClickCount();
            if (SwingUtilities.isLeftMouseButton(e)) {
                if (nclicks > 2) {
                    nclicks %= 2;
                    switch (nclicks) {
                        case 0: {
                            this.selectWord(e);
                            this.selectedWordEvent = null;
                            break;
                        }
                        case 1: {
                            Action a = null;
                            final ActionMap map = textArea.getActionMap();
                            if (map != null) {
                                a = map.get("select-line");
                            }
                            if (a == null) {
                                if (ConfigurableCaret.selectLine == null) {
                                    ConfigurableCaret.selectLine = new RTextAreaEditorKit.SelectLineAction();
                                }
                                a = ConfigurableCaret.selectLine;
                            }
                            a.actionPerformed(new ActionEvent(textArea, 1001, null, e.getWhen(), e.getModifiers()));
                            break;
                        }
                    }
                }
            }
            else if (SwingUtilities.isMiddleMouseButton(e) && nclicks == 1 && textArea.isEditable() && textArea.isEnabled()) {
                final JTextComponent c = (JTextComponent)e.getSource();
                if (c != null) {
                    try {
                        final Toolkit tk = c.getToolkit();
                        final Clipboard buffer = tk.getSystemSelection();
                        if (buffer != null) {
                            this.adjustCaret(e);
                            final TransferHandler th = c.getTransferHandler();
                            if (th != null) {
                                final Transferable trans = buffer.getContents(null);
                                if (trans != null) {
                                    th.importData(c, trans);
                                }
                            }
                            this.adjustFocus(true);
                        }
                        else {
                            textArea.paste();
                        }
                    }
                    catch (HeadlessException loc_0) {}
                }
            }
        }
    }
    
    public void mousePressed(final MouseEvent e) {
        super.mousePressed(e);
        if (!e.isConsumed() && SwingUtilities.isRightMouseButton(e)) {
            final JTextComponent c = this.getComponent();
            if (c != null && c.isEnabled() && c.isRequestFocusEnabled()) {
                c.requestFocusInWindow();
            }
        }
    }
    
    public void paint(final Graphics g) {
        if (this.isVisible()) {
            try {
                final RTextArea textArea = this.getTextArea();
                g.setColor(textArea.getCaretColor());
                final TextUI mapper = textArea.getUI();
                final Rectangle r = mapper.modelToView(textArea, this.getDot());
                this.validateWidth(r);
                if (this.width > 0 && this.height > 0 && !this.contains(r.x, r.y, r.width, r.height)) {
                    final Rectangle clip = g.getClipBounds();
                    if (clip != null && !clip.contains(this)) {
                        this.repaint();
                    }
                    this.damage(r);
                }
                final Rectangle loc_0 = r;
                loc_0.height -= 2;
                switch (this.style) {
                    case BLOCK_STYLE: {
                        Color textAreaBg = textArea.getBackground();
                        if (textAreaBg == null) {
                            textAreaBg = Color.white;
                        }
                        g.setXORMode(textAreaBg);
                        g.fillRect(r.x, r.y, r.width, r.height);
                        break;
                    }
                    case BLOCK_BORDER_STYLE: {
                        g.drawRect(r.x, r.y, r.width - 1, r.height);
                        break;
                    }
                    case UNDERLINE_STYLE: {
                        Color textAreaBg = textArea.getBackground();
                        if (textAreaBg == null) {
                            textAreaBg = Color.white;
                        }
                        g.setXORMode(textAreaBg);
                        final int y = r.y + r.height;
                        g.drawLine(r.x, y, r.x + r.width - 1, y);
                        break;
                    }
                    default: {
                        g.drawLine(r.x, r.y, r.x, r.y + r.height);
                        break;
                    }
                    case THICK_VERTICAL_LINE_STYLE: {
                        g.drawLine(r.x, r.y, r.x, r.y + r.height);
                        final Rectangle loc_1 = r;
                        ++loc_1.x;
                        g.drawLine(r.x, r.y, r.x, r.y + r.height);
                        break;
                    }
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
    }
    
    private void selectWord(final MouseEvent e) {
        if (this.selectedWordEvent != null && this.selectedWordEvent.getX() == e.getX() && this.selectedWordEvent.getY() == e.getY()) {
            return;
        }
        Action a = null;
        final RTextArea textArea = this.getTextArea();
        final ActionMap map = textArea.getActionMap();
        if (map != null) {
            a = map.get("select-word");
        }
        if (a == null) {
            if (ConfigurableCaret.selectWord == null) {
                ConfigurableCaret.selectWord = new RTextAreaEditorKit.SelectWordAction();
            }
            a = ConfigurableCaret.selectWord;
        }
        a.actionPerformed(new ActionEvent(textArea, 1001, null, e.getWhen(), e.getModifiers()));
        this.selectedWordEvent = e;
    }
    
    public void setRoundedSelectionEdges(final boolean rounded) {
        ((ChangeableHighlightPainter)this.getSelectionPainter()).setRoundedEdges(rounded);
    }
    
    public void setSelectionVisible(final boolean visible) {
        super.setSelectionVisible(true);
    }
    
    public void setStyle(CaretStyle style) {
        if (style == null) {
            style = CaretStyle.THICK_VERTICAL_LINE_STYLE;
        }
        if (style != this.style) {
            this.style = style;
            this.repaint();
        }
    }
    
    private void validateWidth(final Rectangle rect) {
        if (rect != null && rect.width <= 1) {
            try {
                final RTextArea textArea = this.getTextArea();
                textArea.getDocument().getText(this.getDot(), 1, this.seg);
                final Font font = textArea.getFont();
                final FontMetrics fm = textArea.getFontMetrics(font);
                rect.width = fm.charWidth(this.seg.array[this.seg.offset]);
                if (rect.width == 0) {
                    rect.width = fm.charWidth(' ');
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                rect.width = 8;
            }
        }
    }
    
    static {
        ConfigurableCaret.selectWord = null;
        ConfigurableCaret.selectLine = null;
    }
    
    private class FoldAwareNavigationFilter extends NavigationFilter
    {
        public void setDot(final FilterBypass fb, int dot, final Position.Bias bias) {
            final RTextArea textArea = ConfigurableCaret.this.getTextArea();
            if (textArea instanceof RSyntaxTextArea) {
                final RSyntaxTextArea rsta = (RSyntaxTextArea)ConfigurableCaret.this.getTextArea();
                if (rsta.isCodeFoldingEnabled()) {
                    final int lastDot = ConfigurableCaret.this.getDot();
                    final FoldManager fm = rsta.getFoldManager();
                    int line = 0;
                    try {
                        line = textArea.getLineOfOffset(dot);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (fm.isLineHidden(line)) {
                        try {
                            if (dot > lastDot) {
                                final int lineCount = textArea.getLineCount();
                                while (++line < lineCount && fm.isLineHidden(line)) {}
                                if (line >= lineCount) {
                                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                                    return;
                                }
                                dot = textArea.getLineStartOffset(line);
                            }
                            else if (dot < lastDot) {
                                while (--line >= 0 && fm.isLineHidden(line)) {}
                                if (line >= 0) {
                                    dot = textArea.getLineEndOffset(line) - 1;
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                }
            }
            super.setDot(fb, dot, bias);
        }
        
        public void moveDot(final FilterBypass fb, final int dot, final Position.Bias bias) {
            super.moveDot(fb, dot, bias);
        }
    }
}
