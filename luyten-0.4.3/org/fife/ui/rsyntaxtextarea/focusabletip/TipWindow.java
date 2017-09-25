package org.fife.ui.rsyntaxtextarea.focusabletip;

import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.text.html.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.event.*;

class TipWindow extends JWindow implements ActionListener
{
    private FocusableTip ft;
    private JEditorPane textArea;
    private String text;
    private TipListener tipListener;
    private HyperlinkListener userHyperlinkListener;
    private static TipWindow visibleInstance;
    
    public TipWindow(final Window owner, final FocusableTip ft, String msg) {
        super(owner);
        this.ft = ft;
        if (msg != null && msg.length() >= 6 && !msg.substring(0, 6).toLowerCase().equals("<html>")) {
            msg = "<html>" + RSyntaxUtilities.escapeForHtml(msg, "<br>", false);
        }
        this.text = msg;
        this.tipListener = new TipListener();
        final JPanel cp = new JPanel(new BorderLayout());
        cp.setBorder(TipUtil.getToolTipBorder());
        cp.setBackground(TipUtil.getToolTipBackground());
        TipUtil.tweakTipEditorPane(this.textArea = new JEditorPane("text/html", this.text));
        if (ft.getImageBase() != null) {
            ((HTMLDocument)this.textArea.getDocument()).setBase(ft.getImageBase());
        }
        this.textArea.addMouseListener(this.tipListener);
        this.textArea.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(final HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    TipWindow.access$000(TipWindow.this).possiblyDisposeOfTipWindow();
                }
            }
        });
        cp.add(this.textArea);
        this.setFocusableWindowState(false);
        this.setContentPane(cp);
        this.setBottomPanel();
        this.pack();
        final KeyAdapter ka = new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyCode() == 27) {
                    TipWindow.access$000(TipWindow.this).possiblyDisposeOfTipWindow();
                }
            }
        };
        this.addKeyListener(ka);
        this.textArea.addKeyListener(ka);
        synchronized (TipWindow.class) {
            if (TipWindow.visibleInstance != null) {
                TipWindow.visibleInstance.dispose();
            }
            TipWindow.visibleInstance = this;
        }
    }
    
    public void actionPerformed(final ActionEvent e) {
        if (!this.getFocusableWindowState()) {
            this.setFocusableWindowState(true);
            this.setBottomPanel();
            this.textArea.removeMouseListener(this.tipListener);
            this.pack();
            this.addWindowFocusListener(new WindowAdapter() {
                public void windowLostFocus(final WindowEvent e) {
                    TipWindow.access$000(TipWindow.this).possiblyDisposeOfTipWindow();
                }
            });
            this.ft.removeListeners();
            if (e == null) {
                this.requestFocus();
            }
        }
    }
    
    public void dispose() {
        final Container cp = this.getContentPane();
        for (int i = 0; i < cp.getComponentCount(); ++i) {
            cp.getComponent(i).removeMouseListener(this.tipListener);
        }
        this.ft.removeListeners();
        super.dispose();
    }
    
    void fixSize() {
        Dimension d = this.textArea.getPreferredSize();
        Rectangle r = null;
        try {
            r = this.textArea.modelToView(this.textArea.getDocument().getLength() - 1);
            final Dimension loc_0;
            d = (loc_0 = this.textArea.getPreferredSize());
            loc_0.width += 25;
            final int MAX_WINDOW_W = 600;
            d.width = Math.min(d.width, 600);
            d.height = Math.min(d.height, 400);
            this.textArea.setPreferredSize(d);
            this.textArea.setSize(d);
            r = this.textArea.modelToView(this.textArea.getDocument().getLength() - 1);
            if (r.y + r.height > d.height) {
                d.height = r.y + r.height + 5;
                this.textArea.setPreferredSize(d);
            }
        }
        catch (BadLocationException ble) {
            ble.printStackTrace();
        }
        this.pack();
    }
    
    public String getText() {
        return this.text;
    }
    
    private void setBottomPanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JSeparator(), "North");
        final boolean focusable = this.getFocusableWindowState();
        if (focusable) {
            final SizeGrip sg = new SizeGrip();
            sg.applyComponentOrientation(sg.getComponentOrientation());
            panel.add(sg, "After");
            final MouseInputAdapter adapter = new MouseInputAdapter() {
                private Point lastPoint;
                
                public void mouseDragged(final MouseEvent e) {
                    final Point p = e.getPoint();
                    SwingUtilities.convertPointToScreen(p, panel);
                    if (this.lastPoint == null) {
                        this.lastPoint = p;
                    }
                    else {
                        final int dx = p.x - this.lastPoint.x;
                        final int dy = p.y - this.lastPoint.y;
                        TipWindow.this.setLocation(TipWindow.this.getX() + dx, TipWindow.this.getY() + dy);
                        this.lastPoint = p;
                    }
                }
                
                public void mousePressed(final MouseEvent e) {
                    SwingUtilities.convertPointToScreen(this.lastPoint = e.getPoint(), panel);
                }
            };
            panel.addMouseListener(adapter);
            panel.addMouseMotionListener(adapter);
        }
        else {
            panel.setOpaque(false);
            final JLabel label = new JLabel(FocusableTip.getString("FocusHotkey"));
            Color fg = UIManager.getColor("Label.disabledForeground");
            Font font = this.textArea.getFont();
            font = font.deriveFont(font.getSize2D() - 1.0f);
            label.setFont(font);
            if (fg == null) {
                fg = Color.GRAY;
            }
            label.setOpaque(true);
            final Color bg = TipUtil.getToolTipBackground();
            label.setBackground(bg);
            label.setForeground(fg);
            label.setHorizontalAlignment(11);
            label.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            panel.add(label);
            panel.addMouseListener(this.tipListener);
        }
        final Container cp = this.getContentPane();
        if (cp.getComponentCount() == 2) {
            final Component comp = cp.getComponent(0);
            cp.remove(0);
            final JScrollPane sp = new JScrollPane(comp);
            final Border emptyBorder = BorderFactory.createEmptyBorder();
            sp.setBorder(emptyBorder);
            sp.setViewportBorder(emptyBorder);
            sp.setBackground(this.textArea.getBackground());
            sp.getViewport().setBackground(this.textArea.getBackground());
            cp.add(sp);
            cp.getComponent(0).removeMouseListener(this.tipListener);
            cp.remove(0);
        }
        cp.add(panel, "South");
    }
    
    public void setHyperlinkListener(final HyperlinkListener listener) {
        if (this.userHyperlinkListener != null) {
            this.textArea.removeHyperlinkListener(this.userHyperlinkListener);
        }
        this.userHyperlinkListener = listener;
        if (this.userHyperlinkListener != null) {
            this.textArea.addHyperlinkListener(this.userHyperlinkListener);
        }
    }
    
    static /* synthetic */ FocusableTip access$000(final TipWindow x0) {
        return x0.ft;
    }
    
    private class TipListener extends MouseAdapter
    {
        public void mousePressed(final MouseEvent e) {
            TipWindow.this.actionPerformed(null);
        }
        
        public void mouseExited(final MouseEvent e) {
            final Component source = (Component)e.getSource();
            final Point p = e.getPoint();
            SwingUtilities.convertPointToScreen(p, source);
            if (!TipWindow.this.getBounds().contains(p)) {
                TipWindow.access$000(TipWindow.this).possiblyDisposeOfTipWindow();
            }
        }
    }
}
