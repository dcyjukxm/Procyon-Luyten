package org.fife.ui.rtextarea;

import javax.swing.event.*;
import org.fife.ui.rsyntaxtextarea.*;
import java.util.*;
import java.awt.print.*;
import org.fife.print.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.plaf.*;
import javax.swing.*;
import javax.swing.undo.*;
import java.io.*;
import java.awt.event.*;

public class RTextArea extends RTextAreaBase implements Printable
{
    public static final int INSERT_MODE = 0;
    public static final int OVERWRITE_MODE = 1;
    public static final String MARK_ALL_COLOR_PROPERTY = "RTA.markAllColor";
    public static final String MARK_ALL_OCCURRENCES_CHANGED_PROPERTY = "RTA.markAllOccurrencesChanged";
    private static final int MIN_ACTION_CONSTANT = 0;
    public static final int COPY_ACTION = 0;
    public static final int CUT_ACTION = 1;
    public static final int DELETE_ACTION = 2;
    public static final int PASTE_ACTION = 3;
    public static final int REDO_ACTION = 4;
    public static final int SELECT_ALL_ACTION = 5;
    public static final int UNDO_ACTION = 6;
    private static final int MAX_ACTION_CONSTANT = 6;
    private static final Color DEFAULT_MARK_ALL_COLOR;
    private int textMode;
    private static boolean recordingMacro;
    private static Macro currentMacro;
    private JPopupMenu popupMenu;
    private JMenuItem undoMenuItem;
    private JMenuItem redoMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem deleteMenuItem;
    private boolean popupMenuCreated;
    private static String selectedOccurrenceText;
    private ToolTipSupplier toolTipSupplier;
    private static RecordableTextAction cutAction;
    private static RecordableTextAction copyAction;
    private static RecordableTextAction pasteAction;
    private static RecordableTextAction deleteAction;
    private static RecordableTextAction undoAction;
    private static RecordableTextAction redoAction;
    private static RecordableTextAction selectAllAction;
    private static IconGroup iconGroup;
    private transient RUndoManager undoManager;
    private transient LineHighlightManager lineHighlightManager;
    private SmartHighlightPainter markAllHighlightPainter;
    private CaretStyle[] carets;
    private static final String MSG = "org.fife.ui.rtextarea.RTextArea";
    private static StringBuilder repTabsSB;
    private static Segment repTabsSeg;
    
    public RTextArea() {
        super();
    }
    
    public RTextArea(final AbstractDocument doc) {
        super(doc);
    }
    
    public RTextArea(final String text) {
        super(text);
    }
    
    public RTextArea(final int rows, final int cols) {
        super(rows, cols);
    }
    
    public RTextArea(final String text, final int rows, final int cols) {
        super(text, rows, cols);
    }
    
    public RTextArea(final AbstractDocument doc, final String text, final int rows, final int cols) {
        super(doc, text, rows, cols);
    }
    
    public RTextArea(final int textMode) {
        super();
        this.setTextMode(textMode);
    }
    
    static synchronized void addToCurrentMacro(final String id, final String actionCommand) {
        RTextArea.currentMacro.addMacroRecord(new Macro.MacroRecord(id, actionCommand));
    }
    
    public Object addLineHighlight(final int line, final Color color) throws BadLocationException {
        if (this.lineHighlightManager == null) {
            this.lineHighlightManager = new LineHighlightManager(this);
        }
        return this.lineHighlightManager.addLineHighlight(line, color);
    }
    
    public void beginAtomicEdit() {
        this.undoManager.beginInternalAtomicEdit();
    }
    
    public static synchronized void beginRecordingMacro() {
        if (isRecordingMacro()) {
            return;
        }
        if (RTextArea.currentMacro != null) {
            RTextArea.currentMacro = null;
        }
        RTextArea.currentMacro = new Macro();
        RTextArea.recordingMacro = true;
    }
    
    public boolean canUndo() {
        return this.undoManager.canUndo();
    }
    
    public boolean canRedo() {
        return this.undoManager.canRedo();
    }
    
    void clearMarkAllHighlights() {
        ((RTextAreaHighlighter)this.getHighlighter()).clearMarkAllHighlights();
        this.repaint();
    }
    
    protected void configurePopupMenu(final JPopupMenu popupMenu) {
        final boolean canType = this.isEditable() && this.isEnabled();
        if (this.undoMenuItem != null) {
            this.undoMenuItem.setEnabled(RTextArea.undoAction.isEnabled() && canType);
            this.redoMenuItem.setEnabled(RTextArea.redoAction.isEnabled() && canType);
            this.cutMenuItem.setEnabled(RTextArea.cutAction.isEnabled() && canType);
            this.pasteMenuItem.setEnabled(RTextArea.pasteAction.isEnabled() && canType);
            this.deleteMenuItem.setEnabled(RTextArea.deleteAction.isEnabled() && canType);
        }
    }
    
    protected Document createDefaultModel() {
        return new RDocument();
    }
    
    protected RTAMouseListener createMouseListener() {
        return new RTextAreaMutableCaretEvent(this);
    }
    
    protected JPopupMenu createPopupMenu() {
        final JPopupMenu menu = new JPopupMenu();
        menu.add(this.undoMenuItem = this.createPopupMenuItem(RTextArea.undoAction));
        menu.add(this.redoMenuItem = this.createPopupMenuItem(RTextArea.redoAction));
        menu.addSeparator();
        menu.add(this.cutMenuItem = this.createPopupMenuItem(RTextArea.cutAction));
        menu.add(this.createPopupMenuItem(RTextArea.copyAction));
        menu.add(this.pasteMenuItem = this.createPopupMenuItem(RTextArea.pasteAction));
        menu.add(this.deleteMenuItem = this.createPopupMenuItem(RTextArea.deleteAction));
        menu.addSeparator();
        menu.add(this.createPopupMenuItem(RTextArea.selectAllAction));
        return menu;
    }
    
    private static void createPopupMenuActions() {
        final int mod = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        final ResourceBundle msg = ResourceBundle.getBundle("org.fife.ui.rtextarea.RTextArea");
        (RTextArea.cutAction = new RTextAreaEditorKit.CutAction()).setProperties(msg, "Action.Cut");
        RTextArea.cutAction.setAccelerator(KeyStroke.getKeyStroke(88, mod));
        (RTextArea.copyAction = new RTextAreaEditorKit.CopyAction()).setProperties(msg, "Action.Copy");
        RTextArea.copyAction.setAccelerator(KeyStroke.getKeyStroke(67, mod));
        (RTextArea.pasteAction = new RTextAreaEditorKit.PasteAction()).setProperties(msg, "Action.Paste");
        RTextArea.pasteAction.setAccelerator(KeyStroke.getKeyStroke(86, mod));
        (RTextArea.deleteAction = new RTextAreaEditorKit.DeleteNextCharAction()).setProperties(msg, "Action.Delete");
        RTextArea.deleteAction.setAccelerator(KeyStroke.getKeyStroke(127, 0));
        (RTextArea.undoAction = new RTextAreaEditorKit.UndoAction()).setProperties(msg, "Action.Undo");
        RTextArea.undoAction.setAccelerator(KeyStroke.getKeyStroke(90, mod));
        (RTextArea.redoAction = new RTextAreaEditorKit.RedoAction()).setProperties(msg, "Action.Redo");
        RTextArea.redoAction.setAccelerator(KeyStroke.getKeyStroke(89, mod));
        (RTextArea.selectAllAction = new RTextAreaEditorKit.SelectAllAction()).setProperties(msg, "Action.SelectAll");
        RTextArea.selectAllAction.setAccelerator(KeyStroke.getKeyStroke(65, mod));
    }
    
    protected JMenuItem createPopupMenuItem(final Action a) {
        final JMenuItem item = new JMenuItem(a) {
            public void setToolTipText(final String text) {
            }
        };
        item.setAccelerator(null);
        return item;
    }
    
    protected RTextAreaUI createRTextAreaUI() {
        return new RTextAreaUI(this);
    }
    
    private final String createSpacer(final int size) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }
    
    protected RUndoManager createUndoManager() {
        return new RUndoManager(this);
    }
    
    public void discardAllEdits() {
        this.undoManager.discardAllEdits();
        this.getDocument().removeUndoableEditListener(this.undoManager);
        this.undoManager = this.createUndoManager();
        this.getDocument().addUndoableEditListener(this.undoManager);
        this.undoManager.updateActions();
    }
    
    public void endAtomicEdit() {
        this.undoManager.endInternalAtomicEdit();
    }
    
    public static synchronized void endRecordingMacro() {
        if (!isRecordingMacro()) {
            return;
        }
        RTextArea.recordingMacro = false;
    }
    
    protected void fireCaretUpdate(final CaretEvent e) {
        this.possiblyUpdateCurrentLineHighlightLocation();
        if (e != null && e.getDot() != e.getMark()) {
            RTextArea.cutAction.setEnabled(true);
            RTextArea.copyAction.setEnabled(true);
        }
        else if (RTextArea.cutAction.isEnabled()) {
            RTextArea.cutAction.setEnabled(false);
            RTextArea.copyAction.setEnabled(false);
        }
        super.fireCaretUpdate(e);
    }
    
    private void fixCtrlH() {
        final InputMap inputMap = this.getInputMap();
        final KeyStroke char010 = KeyStroke.getKeyStroke("typed \b");
        for (InputMap parent = inputMap; parent != null; parent = parent.getParent()) {
            parent.remove(char010);
        }
        final KeyStroke backspace = KeyStroke.getKeyStroke("BACK_SPACE");
        inputMap.put(backspace, "delete-previous");
    }
    
    public static RecordableTextAction getAction(final int action) {
        if (action < 0 || action > 6) {
            return null;
        }
        switch (action) {
            case 0: {
                return RTextArea.copyAction;
            }
            case 1: {
                return RTextArea.cutAction;
            }
            case 2: {
                return RTextArea.deleteAction;
            }
            case 3: {
                return RTextArea.pasteAction;
            }
            case 4: {
                return RTextArea.redoAction;
            }
            case 5: {
                return RTextArea.selectAllAction;
            }
            case 6: {
                return RTextArea.undoAction;
            }
            default: {
                return null;
            }
        }
    }
    
    public static synchronized Macro getCurrentMacro() {
        return RTextArea.currentMacro;
    }
    
    public static final Color getDefaultMarkAllHighlightColor() {
        return RTextArea.DEFAULT_MARK_ALL_COLOR;
    }
    
    public static IconGroup getIconGroup() {
        return RTextArea.iconGroup;
    }
    
    LineHighlightManager getLineHighlightManager() {
        return this.lineHighlightManager;
    }
    
    public Color getMarkAllHighlightColor() {
        return (Color)this.markAllHighlightPainter.getPaint();
    }
    
    public int getMaxAscent() {
        return this.getFontMetrics(this.getFont()).getAscent();
    }
    
    public JPopupMenu getPopupMenu() {
        if (!this.popupMenuCreated) {
            this.popupMenu = this.createPopupMenu();
            if (this.popupMenu != null) {
                final ComponentOrientation orientation = ComponentOrientation.getOrientation(Locale.getDefault());
                this.popupMenu.applyComponentOrientation(orientation);
            }
            this.popupMenuCreated = true;
        }
        return this.popupMenu;
    }
    
    public static String getSelectedOccurrenceText() {
        return RTextArea.selectedOccurrenceText;
    }
    
    public final int getTextMode() {
        return this.textMode;
    }
    
    public ToolTipSupplier getToolTipSupplier() {
        return this.toolTipSupplier;
    }
    
    public String getToolTipText(final MouseEvent e) {
        String tip = null;
        if (this.getToolTipSupplier() != null) {
            tip = this.getToolTipSupplier().getToolTipText(this, e);
        }
        return (tip != null) ? tip : super.getToolTipText();
    }
    
    protected void handleReplaceSelection(final String content) {
        super.replaceSelection(content);
    }
    
    protected void init() {
        super.init();
        if (RTextArea.cutAction == null) {
            createPopupMenuActions();
        }
        this.undoManager = this.createUndoManager();
        this.getDocument().addUndoableEditListener(this.undoManager);
        final Color markAllHighlightColor = getDefaultMarkAllHighlightColor();
        this.markAllHighlightPainter = new SmartHighlightPainter(markAllHighlightColor);
        this.setMarkAllHighlightColor(markAllHighlightColor);
        this.carets = new CaretStyle[2];
        this.setCaretStyle(0, CaretStyle.THICK_VERTICAL_LINE_STYLE);
        this.setCaretStyle(1, CaretStyle.BLOCK_STYLE);
        this.setDragEnabled(true);
        this.setTextMode(0);
        this.fixCtrlH();
    }
    
    public static synchronized boolean isRecordingMacro() {
        return RTextArea.recordingMacro;
    }
    
    public static synchronized void loadMacro(final Macro macro) {
        RTextArea.currentMacro = macro;
    }
    
    void markAll(final List<DocumentRange> ranges) {
        final RTextAreaHighlighter h = (RTextAreaHighlighter)this.getHighlighter();
        if (h != null) {
            if (ranges != null) {
                for (final DocumentRange range : ranges) {
                    try {
                        h.addMarkAllHighlight(range.getStartOffset(), range.getEndOffset(), this.markAllHighlightPainter);
                    }
                    catch (BadLocationException ble) {
                        ble.printStackTrace();
                    }
                }
            }
            this.repaint();
            this.firePropertyChange("RTA.markAllOccurrencesChanged", null, ranges);
        }
    }
    
    public void paste() {
        this.beginAtomicEdit();
        try {
            super.paste();
        }
        finally {
            this.endAtomicEdit();
        }
    }
    
    public synchronized void playbackLastMacro() {
        if (RTextArea.currentMacro != null) {
            final List<Macro.MacroRecord> macroRecords = RTextArea.currentMacro.getMacroRecords();
            if (!macroRecords.isEmpty()) {
                final Action[] actions = this.getActions();
                this.undoManager.beginInternalAtomicEdit();
                try {
                    for (final Macro.MacroRecord record : macroRecords) {
                        for (int i = 0; i < actions.length; ++i) {
                            if (actions[i] instanceof RecordableTextAction && record.id.equals(((RecordableTextAction)actions[i]).getMacroID())) {
                                actions[i].actionPerformed(new ActionEvent(this, 1001, record.actionCommand));
                                break;
                            }
                        }
                    }
                }
                finally {
                    this.undoManager.endInternalAtomicEdit();
                }
            }
        }
    }
    
    public int print(final Graphics g, final PageFormat pageFormat, final int pageIndex) {
        return RPrintUtilities.printDocumentWordWrap(g, this, this.getFont(), pageIndex, pageFormat, this.getTabSize());
    }
    
    public void read(final Reader in, final Object desc) throws IOException {
        final RTextAreaEditorKit kit = (RTextAreaEditorKit)this.getUI().getEditorKit(this);
        this.setText(null);
        final Document doc = this.getDocument();
        if (desc != null) {
            doc.putProperty("stream", desc);
        }
        try {
            kit.read(in, doc, 0);
        }
        catch (BadLocationException e) {
            throw new IOException(e.getMessage());
        }
    }
    
    private void readObject(final ObjectInputStream s) throws ClassNotFoundException, IOException {
        s.defaultReadObject();
        this.undoManager = this.createUndoManager();
        this.getDocument().addUndoableEditListener(this.undoManager);
        this.lineHighlightManager = null;
    }
    
    public void redoLastAction() {
        try {
            if (this.undoManager.canRedo()) {
                this.undoManager.redo();
            }
        }
        catch (CannotRedoException cre) {
            cre.printStackTrace();
        }
    }
    
    public void removeAllLineHighlights() {
        if (this.lineHighlightManager != null) {
            this.lineHighlightManager.removeAllLineHighlights();
        }
    }
    
    public void removeLineHighlight(final Object tag) {
        if (this.lineHighlightManager != null) {
            this.lineHighlightManager.removeLineHighlight(tag);
        }
    }
    
    public void replaceRange(final String str, final int start, final int end) {
        if (end < start) {
            throw new IllegalArgumentException("end before start");
        }
        final Document doc = this.getDocument();
        if (doc != null) {
            try {
                this.undoManager.beginInternalAtomicEdit();
                ((AbstractDocument)doc).replace(start, end - start, str, null);
            }
            catch (BadLocationException e) {
                throw new IllegalArgumentException(e.getMessage());
            }
            finally {
                this.undoManager.endInternalAtomicEdit();
            }
        }
    }
    
    public void replaceSelection(String text) {
        if (text == null) {
            this.handleReplaceSelection(text);
            return;
        }
        if (this.getTabsEmulated()) {
            final int firstTab = text.indexOf(9);
            if (firstTab > -1) {
                final int docOffs = this.getSelectionStart();
                try {
                    text = this.replaceTabsWithSpaces(text, docOffs, firstTab);
                }
                catch (BadLocationException ble) {
                    ble.printStackTrace();
                }
            }
        }
        if (this.textMode == 1 && !"\n".equals(text)) {
            final Caret caret = this.getCaret();
            int caretPos = caret.getDot();
            final Document doc = this.getDocument();
            final Element map = doc.getDefaultRootElement();
            final int curLine = map.getElementIndex(caretPos);
            final int lastLine = map.getElementCount() - 1;
            try {
                final int curLineEnd = this.getLineEndOffset(curLine);
                if (caretPos == caret.getMark() && caretPos != curLineEnd) {
                    if (curLine == lastLine) {
                        caretPos = Math.min(caretPos + text.length(), curLineEnd);
                    }
                    else {
                        caretPos = Math.min(caretPos + text.length(), curLineEnd - 1);
                    }
                    caret.moveDot(caretPos);
                }
            }
            catch (BadLocationException ble2) {
                UIManager.getLookAndFeel().provideErrorFeedback(this);
                ble2.printStackTrace();
            }
        }
        this.handleReplaceSelection(text);
    }
    
    private final String replaceTabsWithSpaces(final String text, final int docOffs, final int firstTab) throws BadLocationException {
        final int tabSize = this.getTabSize();
        final Document doc = this.getDocument();
        final Element root = doc.getDefaultRootElement();
        final int lineIndex = root.getElementIndex(docOffs);
        final Element line = root.getElement(lineIndex);
        final int lineStart = line.getStartOffset();
        int charCount = docOffs - lineStart;
        if (charCount > 0) {
            doc.getText(lineStart, charCount, RTextArea.repTabsSeg);
            charCount = 0;
            for (int i = 0; i < RTextArea.repTabsSeg.count; ++i) {
                final char ch = RTextArea.repTabsSeg.array[RTextArea.repTabsSeg.offset + i];
                if (ch == '\t') {
                    charCount = 0;
                }
                else {
                    charCount = (charCount + 1) % tabSize;
                }
            }
        }
        if (text.length() == 1) {
            return this.createSpacer(tabSize - charCount);
        }
        if (RTextArea.repTabsSB == null) {
            RTextArea.repTabsSB = new StringBuilder();
        }
        RTextArea.repTabsSB.setLength(0);
        final char[] array = text.toCharArray();
        int lastPos = 0;
        int offsInLine = charCount;
        for (int pos = firstTab; pos < array.length; ++pos) {
            final char ch2 = array[pos];
            switch (ch2) {
                case '\t': {
                    if (pos > lastPos) {
                        RTextArea.repTabsSB.append(array, lastPos, pos - lastPos);
                    }
                    final int thisTabSize = tabSize - offsInLine % tabSize;
                    RTextArea.repTabsSB.append(this.createSpacer(thisTabSize));
                    lastPos = pos + 1;
                    offsInLine = 0;
                    break;
                }
                case '\n': {
                    offsInLine = 0;
                    break;
                }
                default: {
                    ++offsInLine;
                    break;
                }
            }
        }
        if (lastPos < array.length) {
            RTextArea.repTabsSB.append(array, lastPos, array.length - lastPos);
        }
        return RTextArea.repTabsSB.toString();
    }
    
    public static void setActionProperties(final int action, final String name, final char mnemonic, final KeyStroke accelerator) {
        setActionProperties(action, name, Integer.valueOf(mnemonic), accelerator);
    }
    
    public static void setActionProperties(final int action, final String name, final Integer mnemonic, final KeyStroke accelerator) {
        Action tempAction = null;
        switch (action) {
            case 1: {
                tempAction = RTextArea.cutAction;
                break;
            }
            case 0: {
                tempAction = RTextArea.copyAction;
                break;
            }
            case 3: {
                tempAction = RTextArea.pasteAction;
                break;
            }
            case 2: {
                tempAction = RTextArea.deleteAction;
                break;
            }
            case 5: {
                tempAction = RTextArea.selectAllAction;
                break;
            }
            default: {
                return;
            }
        }
        tempAction.putValue("Name", name);
        tempAction.putValue("ShortDescription", name);
        tempAction.putValue("AcceleratorKey", accelerator);
        tempAction.putValue("MnemonicKey", mnemonic);
    }
    
    public void setCaret(final Caret caret) {
        super.setCaret(caret);
        if (this.carets != null && caret instanceof ConfigurableCaret) {
            ((ConfigurableCaret)caret).setStyle(this.carets[this.getTextMode()]);
        }
    }
    
    public void setCaretStyle(final int mode, CaretStyle style) {
        if (style == null) {
            style = CaretStyle.THICK_VERTICAL_LINE_STYLE;
        }
        this.carets[mode] = style;
        if (mode == this.getTextMode() && this.getCaret() instanceof ConfigurableCaret) {
            ((ConfigurableCaret)this.getCaret()).setStyle(style);
        }
    }
    
    public void setDocument(final Document document) {
        if (!(document instanceof RDocument)) {
            throw new IllegalArgumentException("RTextArea requires instances of RDocument for its document");
        }
        if (this.undoManager != null) {
            final Document old = this.getDocument();
            if (old != null) {
                old.removeUndoableEditListener(this.undoManager);
            }
        }
        super.setDocument(document);
        if (this.undoManager != null) {
            document.addUndoableEditListener(this.undoManager);
            this.discardAllEdits();
        }
    }
    
    public static synchronized void setIconGroup(final IconGroup group) {
        Icon icon = group.getIcon("cut");
        RTextArea.cutAction.putValue("SmallIcon", icon);
        icon = group.getIcon("copy");
        RTextArea.copyAction.putValue("SmallIcon", icon);
        icon = group.getIcon("paste");
        RTextArea.pasteAction.putValue("SmallIcon", icon);
        icon = group.getIcon("delete");
        RTextArea.deleteAction.putValue("SmallIcon", icon);
        icon = group.getIcon("undo");
        RTextArea.undoAction.putValue("SmallIcon", icon);
        icon = group.getIcon("redo");
        RTextArea.redoAction.putValue("SmallIcon", icon);
        icon = group.getIcon("selectall");
        RTextArea.selectAllAction.putValue("SmallIcon", icon);
        RTextArea.iconGroup = group;
    }
    
    public void setMarkAllHighlightColor(final Color color) {
        final Color old = (Color)this.markAllHighlightPainter.getPaint();
        if (old != null && !old.equals(color)) {
            this.markAllHighlightPainter.setPaint(color);
            final RTextAreaHighlighter h = (RTextAreaHighlighter)this.getHighlighter();
            if (h.getMarkAllHighlightCount() > 0) {
                this.repaint();
            }
            this.firePropertyChange("RTA.markAllColor", old, color);
        }
    }
    
    public void setPopupMenu(final JPopupMenu popupMenu) {
        this.popupMenu = popupMenu;
        this.popupMenuCreated = true;
    }
    
    public void setRoundedSelectionEdges(final boolean rounded) {
        if (this.getRoundedSelectionEdges() != rounded) {
            this.markAllHighlightPainter.setRoundedEdges(rounded);
            super.setRoundedSelectionEdges(rounded);
        }
    }
    
    public static void setSelectedOccurrenceText(final String text) {
        RTextArea.selectedOccurrenceText = text;
    }
    
    public void setTextMode(int mode) {
        if (mode != 0 && mode != 1) {
            mode = 0;
        }
        if (this.textMode != mode) {
            final Caret caret = this.getCaret();
            if (caret instanceof ConfigurableCaret) {
                ((ConfigurableCaret)caret).setStyle(this.carets[mode]);
            }
            this.textMode = mode;
        }
    }
    
    public void setToolTipSupplier(final ToolTipSupplier supplier) {
        this.toolTipSupplier = supplier;
    }
    
    public final void setUI(final TextUI ui) {
        if (this.popupMenu != null) {
            SwingUtilities.updateComponentTreeUI(this.popupMenu);
        }
        final RTextAreaUI rtaui = (RTextAreaUI)this.getUI();
        if (rtaui != null) {
            rtaui.installDefaults();
        }
    }
    
    public void undoLastAction() {
        try {
            if (this.undoManager.canUndo()) {
                this.undoManager.undo();
            }
        }
        catch (CannotUndoException cre) {
            cre.printStackTrace();
        }
    }
    
    private void writeObject(final ObjectOutputStream s) throws IOException {
        this.getDocument().removeUndoableEditListener(this.undoManager);
        s.defaultWriteObject();
        this.getDocument().addUndoableEditListener(this.undoManager);
    }
    
    static /* synthetic */ RecordableTextAction access$000() {
        return RTextArea.cutAction;
    }
    
    static /* synthetic */ RecordableTextAction access$100() {
        return RTextArea.copyAction;
    }
    
    static /* synthetic */ RUndoManager access$200(final RTextArea x0) {
        return x0.undoManager;
    }
    
    static {
        DEFAULT_MARK_ALL_COLOR = new Color(16762880);
        RTextArea.repTabsSeg = new Segment();
    }
    
    protected class RTextAreaMutableCaretEvent extends RTAMouseListener
    {
        protected RTextAreaMutableCaretEvent(final RTextArea textArea) {
            super(textArea);
        }
        
        public void focusGained(final FocusEvent e) {
            final Caret c = RTextArea.this.getCaret();
            final boolean enabled = c.getDot() != c.getMark();
            RTextArea.access$000().setEnabled(enabled);
            RTextArea.access$100().setEnabled(enabled);
            RTextArea.access$200(RTextArea.this).updateActions();
        }
        
        public void focusLost(final FocusEvent e) {
        }
        
        public void mouseDragged(final MouseEvent e) {
            if ((e.getModifiers() & 0x10) != 0x0) {
                final Caret caret = RTextArea.this.getCaret();
                this.dot = caret.getDot();
                this.mark = caret.getMark();
                RTextArea.this.fireCaretUpdate(this);
            }
        }
        
        public void mousePressed(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                this.showPopup(e);
            }
            else if ((e.getModifiers() & 0x10) != 0x0) {
                final Caret caret = RTextArea.this.getCaret();
                this.dot = caret.getDot();
                this.mark = caret.getMark();
                RTextArea.this.fireCaretUpdate(this);
            }
        }
        
        public void mouseReleased(final MouseEvent e) {
            if (e.isPopupTrigger()) {
                this.showPopup(e);
            }
        }
        
        private void showPopup(final MouseEvent e) {
            final JPopupMenu popupMenu = RTextArea.this.getPopupMenu();
            if (popupMenu != null) {
                RTextArea.this.configurePopupMenu(popupMenu);
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
                e.consume();
            }
        }
    }
}
