package org.fife.ui.rsyntaxtextarea.focusabletip;

import java.net.*;
import java.util.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;

public class FocusableTip
{
    private JTextArea textArea;
    private TipWindow tipWindow;
    private URL imageBase;
    private TextAreaListener textAreaListener;
    private HyperlinkListener hyperlinkListener;
    private String lastText;
    private Rectangle tipVisibleBounds;
    private static final int X_MARGIN = 18;
    private static final int Y_MARGIN = 12;
    private static final String MSG = "org.fife.ui.rsyntaxtextarea.focusabletip.FocusableTip";
    private static final ResourceBundle msg;
    
    public FocusableTip(final JTextArea textArea, final HyperlinkListener listener) {
        super();
        this.setTextArea(textArea);
        this.hyperlinkListener = listener;
        this.textAreaListener = new TextAreaListener();
        this.tipVisibleBounds = new Rectangle();
    }
    
    private void computeTipVisibleBounds() {
        final Rectangle r = this.tipWindow.getBounds();
        final Point p = r.getLocation();
        SwingUtilities.convertPointFromScreen(p, this.textArea);
        r.setLocation(p);
        this.tipVisibleBounds.setBounds(r.x, r.y - 15, r.width, r.height + 30);
    }
    
    private void createAndShowTipWindow(final MouseEvent e, final String text) {
        final Window owner = SwingUtilities.getWindowAncestor(this.textArea);
        (this.tipWindow = new TipWindow(owner, this, text)).setHyperlinkListener(this.hyperlinkListener);
        final PopupWindowDecorator decorator = PopupWindowDecorator.get();
        if (decorator != null) {
            decorator.decorate(this.tipWindow);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (FocusableTip.access$100(FocusableTip.this) == null) {
                    return;
                }
                FocusableTip.access$100(FocusableTip.this).fixSize();
                final ComponentOrientation o = FocusableTip.access$200(FocusableTip.this).getComponentOrientation();
                final Point p = e.getPoint();
                SwingUtilities.convertPointToScreen(p, FocusableTip.access$200(FocusableTip.this));
                final Rectangle sb = TipUtil.getScreenBoundsForPoint(p.x, p.y);
                int y = p.y + 12;
                if (y + FocusableTip.access$100(FocusableTip.this).getHeight() >= sb.y + sb.height) {
                    y = p.y - 12 - FocusableTip.access$100(FocusableTip.this).getHeight();
                }
                int x = p.x - 18;
                if (!o.isLeftToRight()) {
                    x = p.x - FocusableTip.access$100(FocusableTip.this).getWidth() + 18;
                }
                if (x < sb.x) {
                    x = sb.x;
                }
                else if (x + FocusableTip.access$100(FocusableTip.this).getWidth() > sb.x + sb.width) {
                    x = sb.x + sb.width - FocusableTip.access$100(FocusableTip.this).getWidth();
                }
                FocusableTip.access$100(FocusableTip.this).setLocation(x, y);
                FocusableTip.access$100(FocusableTip.this).setVisible(true);
                FocusableTip.access$300(FocusableTip.this);
                FocusableTip.access$400(FocusableTip.this).install(FocusableTip.access$200(FocusableTip.this));
                FocusableTip.access$502(FocusableTip.this, text);
            }
        });
    }
    
    public URL getImageBase() {
        return this.imageBase;
    }
    
    static String getString(final String key) {
        return FocusableTip.msg.getString(key);
    }
    
    public void possiblyDisposeOfTipWindow() {
        if (this.tipWindow != null) {
            this.tipWindow.dispose();
            this.tipWindow = null;
            this.textAreaListener.uninstall();
            this.tipVisibleBounds.setBounds(-1, -1, 0, 0);
            this.lastText = null;
            this.textArea.requestFocus();
        }
    }
    
    void removeListeners() {
        this.textAreaListener.uninstall();
    }
    
    public void setImageBase(final URL url) {
        this.imageBase = url;
    }
    
    private void setTextArea(final JTextArea textArea) {
        this.textArea = textArea;
        ToolTipManager.sharedInstance().registerComponent(textArea);
    }
    
    public void toolTipRequested(final MouseEvent e, final String text) {
        if (text == null || text.length() == 0) {
            this.possiblyDisposeOfTipWindow();
            this.lastText = text;
            return;
        }
        if (this.lastText == null || text.length() != this.lastText.length() || !text.equals(this.lastText)) {
            this.possiblyDisposeOfTipWindow();
            this.createAndShowTipWindow(e, text);
        }
    }
    
    static /* synthetic */ TipWindow access$100(final FocusableTip x0) {
        return x0.tipWindow;
    }
    
    static /* synthetic */ JTextArea access$200(final FocusableTip x0) {
        return x0.textArea;
    }
    
    static /* synthetic */ void access$300(final FocusableTip x0) {
        x0.computeTipVisibleBounds();
    }
    
    static /* synthetic */ TextAreaListener access$400(final FocusableTip x0) {
        return x0.textAreaListener;
    }
    
    static /* synthetic */ String access$502(final FocusableTip x0, final String x1) {
        return x0.lastText = x1;
    }
    
    static /* synthetic */ Rectangle access$600(final FocusableTip x0) {
        return x0.tipVisibleBounds;
    }
    
    static {
        msg = ResourceBundle.getBundle("org.fife.ui.rsyntaxtextarea.focusabletip.FocusableTip");
    }
    
    private class TextAreaListener extends MouseInputAdapter implements CaretListener, ComponentListener, FocusListener, KeyListener
    {
        public void caretUpdate(final CaretEvent e) {
            final Object source = e.getSource();
            if (source == FocusableTip.access$200(FocusableTip.this)) {
                FocusableTip.this.possiblyDisposeOfTipWindow();
            }
        }
        
        public void componentHidden(final ComponentEvent e) {
            this.handleComponentEvent(e);
        }
        
        public void componentMoved(final ComponentEvent e) {
            this.handleComponentEvent(e);
        }
        
        public void componentResized(final ComponentEvent e) {
            this.handleComponentEvent(e);
        }
        
        public void componentShown(final ComponentEvent e) {
            this.handleComponentEvent(e);
        }
        
        public void focusGained(final FocusEvent e) {
        }
        
        public void focusLost(final FocusEvent e) {
            final Component c = e.getOppositeComponent();
            final boolean tipClicked = c instanceof TipWindow || (c != null && SwingUtilities.getWindowAncestor(c) instanceof TipWindow);
            if (!tipClicked) {
                FocusableTip.this.possiblyDisposeOfTipWindow();
            }
        }
        
        private void handleComponentEvent(final ComponentEvent e) {
            FocusableTip.this.possiblyDisposeOfTipWindow();
        }
        
        public void install(final JTextArea textArea) {
            textArea.addCaretListener(this);
            textArea.addComponentListener(this);
            textArea.addFocusListener(this);
            textArea.addKeyListener(this);
            textArea.addMouseListener(this);
            textArea.addMouseMotionListener(this);
        }
        
        public void keyPressed(final KeyEvent e) {
            if (e.getKeyCode() == 27) {
                FocusableTip.this.possiblyDisposeOfTipWindow();
            }
            else if (e.getKeyCode() == 113 && FocusableTip.access$100(FocusableTip.this) != null && !FocusableTip.access$100(FocusableTip.this).getFocusableWindowState()) {
                FocusableTip.access$100(FocusableTip.this).actionPerformed(null);
                e.consume();
            }
        }
        
        public void keyReleased(final KeyEvent e) {
        }
        
        public void keyTyped(final KeyEvent e) {
        }
        
        public void mouseExited(final MouseEvent e) {
        }
        
        public void mouseMoved(final MouseEvent e) {
            if (FocusableTip.access$600(FocusableTip.this) == null || !FocusableTip.access$600(FocusableTip.this).contains(e.getPoint())) {
                FocusableTip.this.possiblyDisposeOfTipWindow();
            }
        }
        
        public void uninstall() {
            FocusableTip.access$200(FocusableTip.this).removeCaretListener(this);
            FocusableTip.access$200(FocusableTip.this).removeComponentListener(this);
            FocusableTip.access$200(FocusableTip.this).removeFocusListener(this);
            FocusableTip.access$200(FocusableTip.this).removeKeyListener(this);
            FocusableTip.access$200(FocusableTip.this).removeMouseListener(this);
            FocusableTip.access$200(FocusableTip.this).removeMouseMotionListener(this);
        }
    }
}
