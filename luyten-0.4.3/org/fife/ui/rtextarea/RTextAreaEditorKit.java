package org.fife.ui.rtextarea;

import java.io.*;
import java.awt.event.*;
import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.text.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.text.*;

public class RTextAreaEditorKit extends DefaultEditorKit
{
    public static final String rtaBeginRecordingMacroAction = "RTA.BeginRecordingMacroAction";
    public static final String rtaDecreaseFontSizeAction = "RTA.DecreaseFontSizeAction";
    public static final String rtaDeleteLineAction = "RTA.DeleteLineAction";
    public static final String rtaDeletePrevWordAction = "RTA.DeletePrevWordAction";
    public static final String rtaDeleteRestOfLineAction = "RTA.DeleteRestOfLineAction";
    public static final String rtaDumbCompleteWordAction = "RTA.DumbCompleteWordAction";
    public static final String rtaEndRecordingMacroAction = "RTA.EndRecordingMacroAction";
    public static final String rtaIncreaseFontSizeAction = "RTA.IncreaseFontSizeAction";
    public static final String rtaInvertSelectionCaseAction = "RTA.InvertCaseAction";
    public static final String rtaJoinLinesAction = "RTA.JoinLinesAction";
    public static final String rtaLineDownAction = "RTA.LineDownAction";
    public static final String rtaLineUpAction = "RTA.LineUpAction";
    public static final String rtaLowerSelectionCaseAction = "RTA.LowerCaseAction";
    public static final String rtaNextOccurrenceAction = "RTA.NextOccurrenceAction";
    public static final String rtaPrevOccurrenceAction = "RTA.PrevOccurrenceAction";
    public static final String rtaNextBookmarkAction = "RTA.NextBookmarkAction";
    public static final String rtaPrevBookmarkAction = "RTA.PrevBookmarkAction";
    public static final String rtaPlaybackLastMacroAction = "RTA.PlaybackLastMacroAction";
    public static final String rtaRedoAction = "RTA.RedoAction";
    public static final String rtaScrollDownAction = "RTA.ScrollDownAction";
    public static final String rtaScrollUpAction = "RTA.ScrollUpAction";
    public static final String rtaSelectionPageUpAction = "RTA.SelectionPageUpAction";
    public static final String rtaSelectionPageDownAction = "RTA.SelectionPageDownAction";
    public static final String rtaSelectionPageLeftAction = "RTA.SelectionPageLeftAction";
    public static final String rtaSelectionPageRightAction = "RTA.SelectionPageRightAction";
    public static final String rtaTimeDateAction = "RTA.TimeDateAction";
    public static final String rtaToggleBookmarkAction = "RTA.ToggleBookmarkAction";
    public static final String rtaToggleTextModeAction = "RTA.ToggleTextModeAction";
    public static final String rtaUndoAction = "RTA.UndoAction";
    public static final String rtaUnselectAction = "RTA.UnselectAction";
    public static final String rtaUpperSelectionCaseAction = "RTA.UpperCaseAction";
    private static final RecordableTextAction[] defaultActions;
    private static final int READBUFFER_SIZE = 32768;
    
    public IconRowHeader createIconRowHeader(final RTextArea textArea) {
        return new IconRowHeader(textArea);
    }
    
    public LineNumberList createLineNumberList(final RTextArea textArea) {
        return new LineNumberList(textArea);
    }
    
    public Action[] getActions() {
        return RTextAreaEditorKit.defaultActions;
    }
    
    public void read(final Reader in, final Document doc, int pos) throws IOException, BadLocationException {
        final char[] buff = new char[32768];
        boolean lastWasCR = false;
        boolean isCRLF = false;
        boolean isCR = false;
        final boolean wasEmpty = doc.getLength() == 0;
        int nch;
        while ((nch = in.read(buff, 0, buff.length)) != -1) {
            int last = 0;
            for (int counter = 0; counter < nch; ++counter) {
                switch (buff[counter]) {
                    case '\r': {
                        if (!lastWasCR) {
                            lastWasCR = true;
                            break;
                        }
                        isCR = true;
                        if (counter == 0) {
                            doc.insertString(pos, "\n", null);
                            ++pos;
                            break;
                        }
                        buff[counter - 1] = '\n';
                        break;
                    }
                    case '\n': {
                        if (lastWasCR) {
                            if (counter > last + 1) {
                                doc.insertString(pos, new String(buff, last, counter - last - 1), null);
                                pos += counter - last - 1;
                            }
                            lastWasCR = false;
                            last = counter;
                            isCRLF = true;
                            break;
                        }
                        break;
                    }
                    default: {
                        if (lastWasCR) {
                            isCR = true;
                            if (counter == 0) {
                                doc.insertString(pos, "\n", null);
                                ++pos;
                            }
                            else {
                                buff[counter - 1] = '\n';
                            }
                            lastWasCR = false;
                            break;
                        }
                        break;
                    }
                }
            }
            if (last < nch) {
                if (lastWasCR) {
                    if (last >= nch - 1) {
                        continue;
                    }
                    doc.insertString(pos, new String(buff, last, nch - last - 1), null);
                    pos += nch - last - 1;
                }
                else {
                    doc.insertString(pos, new String(buff, last, nch - last), null);
                    pos += nch - last;
                }
            }
        }
        if (lastWasCR) {
            doc.insertString(pos, "\n", null);
            isCR = true;
        }
        if (wasEmpty) {
            if (isCRLF) {
                doc.putProperty("__EndOfLine__", "\r\n");
            }
            else if (isCR) {
                doc.putProperty("__EndOfLine__", "\r");
            }
            else {
                doc.putProperty("__EndOfLine__", "\n");
            }
        }
    }
    
    static {
        defaultActions = new RecordableTextAction[] { new BeginAction("caret-begin", false), new BeginAction("selection-begin", true), new BeginLineAction("caret-begin-line", false), new BeginLineAction("selection-begin-line", true), new BeginRecordingMacroAction(), new BeginWordAction("caret-begin-word", false), new BeginWordAction("selection-begin-word", true), new CopyAction(), new CutAction(), new DefaultKeyTypedAction(), new DeleteLineAction(), new DeleteNextCharAction(), new DeletePrevCharAction(), new DeletePrevWordAction(), new DeleteRestOfLineAction(), new DumbCompleteWordAction(), new EndAction("caret-end", false), new EndAction("selection-end", true), new EndLineAction("caret-end-line", false), new EndLineAction("selection-end-line", true), new EndRecordingMacroAction(), new EndWordAction("caret-end-word", false), new EndWordAction("caret-end-word", true), new InsertBreakAction(), new InsertContentAction(), new InsertTabAction(), new InvertSelectionCaseAction(), new JoinLinesAction(), new LowerSelectionCaseAction(), new LineMoveAction("RTA.LineUpAction", -1), new LineMoveAction("RTA.LineDownAction", 1), new NextBookmarkAction("RTA.NextBookmarkAction", true), new NextBookmarkAction("RTA.PrevBookmarkAction", false), new NextVisualPositionAction("caret-forward", false, 3), new NextVisualPositionAction("caret-backward", false, 7), new NextVisualPositionAction("selection-forward", true, 3), new NextVisualPositionAction("selection-backward", true, 7), new NextVisualPositionAction("caret-up", false, 1), new NextVisualPositionAction("caret-down", false, 5), new NextVisualPositionAction("selection-up", true, 1), new NextVisualPositionAction("selection-down", true, 5), new NextOccurrenceAction("RTA.NextOccurrenceAction"), new PreviousOccurrenceAction("RTA.PrevOccurrenceAction"), new NextWordAction("caret-next-word", false), new NextWordAction("selection-next-word", true), new PageAction("RTA.SelectionPageLeftAction", true, true), new PageAction("RTA.SelectionPageRightAction", false, true), new PasteAction(), new PlaybackLastMacroAction(), new PreviousWordAction("caret-previous-word", false), new PreviousWordAction("selection-previous-word", true), new RedoAction(), new ScrollAction("RTA.ScrollUpAction", -1), new ScrollAction("RTA.ScrollDownAction", 1), new SelectAllAction(), new SelectLineAction(), new SelectWordAction(), new SetReadOnlyAction(), new SetWritableAction(), new ToggleBookmarkAction(), new ToggleTextModeAction(), new UndoAction(), new UnselectAction(), new UpperSelectionCaseAction(), new VerticalPageAction("page-up", -1, false), new VerticalPageAction("page-down", 1, false), new VerticalPageAction("RTA.SelectionPageUpAction", -1, true), new VerticalPageAction("RTA.SelectionPageDownAction", 1, true) };
    }
    
    public static class BeepAction extends RecordableTextAction
    {
        public BeepAction() {
            super("beep");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            UIManager.getLookAndFeel().provideErrorFeedback(textArea);
        }
        
        public final String getMacroID() {
            return "beep";
        }
    }
    
    public static class BeginAction extends RecordableTextAction
    {
        private boolean select;
        
        public BeginAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (this.select) {
                textArea.moveCaretPosition(0);
            }
            else {
                textArea.setCaretPosition(0);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class BeginLineAction extends RecordableTextAction
    {
        private Segment currentLine;
        private boolean select;
        
        public BeginLineAction(final String name, final boolean select) {
            super(name);
            this.currentLine = new Segment();
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            int newPos = 0;
            try {
                if (textArea.getLineWrap()) {
                    final int offs = textArea.getCaretPosition();
                    final int begOffs = newPos = Utilities.getRowStart(textArea, offs);
                }
                else {
                    final int caretPosition = textArea.getCaretPosition();
                    final Document document = textArea.getDocument();
                    final Element map = document.getDefaultRootElement();
                    final int currentLineNum = map.getElementIndex(caretPosition);
                    final Element currentLineElement = map.getElement(currentLineNum);
                    final int currentLineStart = currentLineElement.getStartOffset();
                    final int currentLineEnd = currentLineElement.getEndOffset();
                    final int count = currentLineEnd - currentLineStart;
                    if (count > 0) {
                        document.getText(currentLineStart, count, this.currentLine);
                        int firstNonWhitespace = this.getFirstNonWhitespacePos();
                        firstNonWhitespace = currentLineStart + (firstNonWhitespace - this.currentLine.offset);
                        if (caretPosition != firstNonWhitespace) {
                            newPos = firstNonWhitespace;
                        }
                        else {
                            newPos = currentLineStart;
                        }
                    }
                    else {
                        newPos = currentLineStart;
                    }
                }
                if (this.select) {
                    textArea.moveCaretPosition(newPos);
                }
                else {
                    textArea.setCaretPosition(newPos);
                }
            }
            catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                ble.printStackTrace();
            }
        }
        
        private final int getFirstNonWhitespacePos() {
            final int offset = this.currentLine.offset;
            final int end = offset + this.currentLine.count - 1;
            int pos = offset;
            final char[] array = this.currentLine.array;
            for (char currentChar = array[pos]; (currentChar == '\t' || currentChar == ' ') && ++pos < end; currentChar = array[pos]) {}
            return pos;
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class BeginRecordingMacroAction extends RecordableTextAction
    {
        public BeginRecordingMacroAction() {
            super("RTA.BeginRecordingMacroAction");
        }
        
        public BeginRecordingMacroAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            RTextArea.beginRecordingMacro();
        }
        
        public boolean isRecordable() {
            return false;
        }
        
        public final String getMacroID() {
            return "RTA.BeginRecordingMacroAction";
        }
    }
    
    protected static class BeginWordAction extends RecordableTextAction
    {
        private boolean select;
        
        protected BeginWordAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            try {
                final int offs = textArea.getCaretPosition();
                final int begOffs = this.getWordStart(textArea, offs);
                if (this.select) {
                    textArea.moveCaretPosition(begOffs);
                }
                else {
                    textArea.setCaretPosition(begOffs);
                }
            }
            catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        protected int getWordStart(final RTextArea textArea, final int offs) throws BadLocationException {
            return Utilities.getWordStart(textArea, offs);
        }
    }
    
    public static class CopyAction extends RecordableTextAction
    {
        public CopyAction() {
            super("copy-to-clipboard");
        }
        
        public CopyAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.copy();
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "copy-to-clipboard";
        }
    }
    
    public static class CutAction extends RecordableTextAction
    {
        public CutAction() {
            super("cut-to-clipboard");
        }
        
        public CutAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.cut();
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "cut-to-clipboard";
        }
    }
    
    public static class DecreaseFontSizeAction extends RecordableTextAction
    {
        protected float decreaseAmount;
        protected static final float MINIMUM_SIZE = 2.0f;
        
        public DecreaseFontSizeAction() {
            super("RTA.DecreaseFontSizeAction");
            this.initialize();
        }
        
        public DecreaseFontSizeAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
            this.initialize();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            Font font = textArea.getFont();
            final float oldSize = font.getSize2D();
            final float newSize = oldSize - this.decreaseAmount;
            if (newSize >= 2.0f) {
                font = font.deriveFont(newSize);
                textArea.setFont(font);
            }
            else if (oldSize > 2.0f) {
                font = font.deriveFont(2.0f);
                textArea.setFont(font);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "RTA.DecreaseFontSizeAction";
        }
        
        protected void initialize() {
            this.decreaseAmount = 1.0f;
        }
    }
    
    public static class DefaultKeyTypedAction extends RecordableTextAction
    {
        private Action delegate;
        
        public DefaultKeyTypedAction() {
            super("default-typed", null, null, null, null);
            this.delegate = new DefaultEditorKit.DefaultKeyTypedAction();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            this.delegate.actionPerformed(e);
        }
        
        public final String getMacroID() {
            return "default-typed";
        }
    }
    
    public static class DeleteLineAction extends RecordableTextAction
    {
        public DeleteLineAction() {
            super("RTA.DeleteLineAction", null, null, null, null);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final int selStart = textArea.getSelectionStart();
            final int selEnd = textArea.getSelectionEnd();
            try {
                final int line1 = textArea.getLineOfOffset(selStart);
                final int startOffs = textArea.getLineStartOffset(line1);
                final int line2 = textArea.getLineOfOffset(selEnd);
                int endOffs = textArea.getLineEndOffset(line2);
                if (line2 > line1 && selEnd == textArea.getLineStartOffset(line2)) {
                    endOffs = selEnd;
                }
                textArea.replaceRange(null, startOffs, endOffs);
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        
        public final String getMacroID() {
            return "RTA.DeleteLineAction";
        }
    }
    
    public static class DeleteNextCharAction extends RecordableTextAction
    {
        public DeleteNextCharAction() {
            super("delete-next", null, null, null, null);
        }
        
        public DeleteNextCharAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            boolean beep = true;
            if (textArea != null && textArea.isEditable()) {
                try {
                    final Document doc = textArea.getDocument();
                    final Caret caret = textArea.getCaret();
                    final int dot = caret.getDot();
                    final int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    }
                    else if (dot < doc.getLength()) {
                        int delChars = 1;
                        if (dot < doc.getLength() - 1) {
                            final String dotChars = doc.getText(dot, 2);
                            final char c0 = dotChars.charAt(0);
                            final char c = dotChars.charAt(1);
                            if (c0 >= '\ud800' && c0 <= '\udbff' && c >= '\udc00' && c <= '\udfff') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot, delChars);
                        beep = false;
                    }
                }
                catch (BadLocationException loc_0) {}
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "delete-next";
        }
    }
    
    public static class DeletePrevCharAction extends RecordableTextAction
    {
        public DeletePrevCharAction() {
            super("delete-previous");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            boolean beep = true;
            if (textArea != null && textArea.isEditable()) {
                try {
                    final Document doc = textArea.getDocument();
                    final Caret caret = textArea.getCaret();
                    final int dot = caret.getDot();
                    final int mark = caret.getMark();
                    if (dot != mark) {
                        doc.remove(Math.min(dot, mark), Math.abs(dot - mark));
                        beep = false;
                    }
                    else if (dot > 0) {
                        int delChars = 1;
                        if (dot > 1) {
                            final String dotChars = doc.getText(dot - 2, 2);
                            final char c0 = dotChars.charAt(0);
                            final char c = dotChars.charAt(1);
                            if (c0 >= '\ud800' && c0 <= '\udbff' && c >= '\udc00' && c <= '\udfff') {
                                delChars = 2;
                            }
                        }
                        doc.remove(dot - delChars, delChars);
                        beep = false;
                    }
                }
                catch (BadLocationException loc_0) {}
            }
            if (beep) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return "delete-previous";
        }
    }
    
    public static class DeletePrevWordAction extends RecordableTextAction
    {
        public DeletePrevWordAction() {
            super("RTA.DeletePrevWordAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            try {
                final int end = textArea.getSelectionStart();
                final int start = this.getPreviousWordStart(textArea, end);
                if (end > start) {
                    textArea.getDocument().remove(start, end - start);
                }
            }
            catch (BadLocationException ex) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public String getMacroID() {
            return "RTA.DeletePrevWordAction";
        }
        
        protected int getPreviousWordStart(final RTextArea textArea, final int end) throws BadLocationException {
            return Utilities.getPreviousWord(textArea, end);
        }
    }
    
    public static class DeleteRestOfLineAction extends RecordableTextAction
    {
        public DeleteRestOfLineAction() {
            super("RTA.DeleteRestOfLineAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            try {
                final Document document = textArea.getDocument();
                final int caretPosition = textArea.getCaretPosition();
                final Element map = document.getDefaultRootElement();
                final int currentLineNum = map.getElementIndex(caretPosition);
                final Element currentLineElement = map.getElement(currentLineNum);
                final int currentLineEnd = currentLineElement.getEndOffset() - 1;
                if (caretPosition < currentLineEnd) {
                    document.remove(caretPosition, currentLineEnd - caretPosition);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        
        public final String getMacroID() {
            return "RTA.DeleteRestOfLineAction";
        }
    }
    
    public static class DumbCompleteWordAction extends RecordableTextAction
    {
        private int lastWordStart;
        private int lastDot;
        private int searchOffs;
        private String lastPrefix;
        
        public DumbCompleteWordAction() {
            super("RTA.DumbCompleteWordAction");
            final int loc_0 = -1;
            this.lastDot = loc_0;
            this.searchOffs = loc_0;
            this.lastWordStart = loc_0;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                return;
            }
            try {
                final int dot = textArea.getCaretPosition();
                if (dot == 0) {
                    return;
                }
                final int curWordStart = Utilities.getWordStart(textArea, dot - 1);
                if (this.lastWordStart != curWordStart || dot != this.lastDot) {
                    this.lastPrefix = textArea.getText(curWordStart, dot - curWordStart);
                    if (this.lastPrefix.length() == 0 || !Character.isLetter(this.lastPrefix.charAt(this.lastPrefix.length() - 1))) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                        return;
                    }
                    this.lastWordStart = dot - this.lastPrefix.length();
                    this.searchOffs = this.lastWordStart;
                }
                while (this.searchOffs > 0) {
                    int wordStart = 0;
                    try {
                        wordStart = Utilities.getPreviousWord(textArea, this.searchOffs);
                    }
                    catch (BadLocationException ble2) {
                        wordStart = -1;
                    }
                    if (wordStart == -1) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                        break;
                    }
                    final int end = Utilities.getWordEnd(textArea, wordStart);
                    final String word = textArea.getText(wordStart, end - wordStart);
                    this.searchOffs = wordStart;
                    if (word.startsWith(this.lastPrefix)) {
                        textArea.replaceRange(word, this.lastWordStart, dot);
                        this.lastDot = textArea.getCaretPosition();
                        break;
                    }
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class EndAction extends RecordableTextAction
    {
        private boolean select;
        
        public EndAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final int dot = this.getVisibleEnd(textArea);
            if (this.select) {
                textArea.moveCaretPosition(dot);
            }
            else {
                textArea.setCaretPosition(dot);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        protected int getVisibleEnd(final RTextArea textArea) {
            return textArea.getDocument().getLength();
        }
    }
    
    public static class EndLineAction extends RecordableTextAction
    {
        private boolean select;
        
        public EndLineAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final int offs = textArea.getCaretPosition();
            int endOffs = 0;
            try {
                if (textArea.getLineWrap()) {
                    endOffs = Utilities.getRowEnd(textArea, offs);
                }
                else {
                    final Element root = textArea.getDocument().getDefaultRootElement();
                    final int line = root.getElementIndex(offs);
                    endOffs = root.getElement(line).getEndOffset() - 1;
                }
                if (this.select) {
                    textArea.moveCaretPosition(endOffs);
                }
                else {
                    textArea.setCaretPosition(endOffs);
                }
            }
            catch (Exception ex) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class EndRecordingMacroAction extends RecordableTextAction
    {
        public EndRecordingMacroAction() {
            super("RTA.EndRecordingMacroAction");
        }
        
        public EndRecordingMacroAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            RTextArea.endRecordingMacro();
        }
        
        public final String getMacroID() {
            return "RTA.EndRecordingMacroAction";
        }
        
        public boolean isRecordable() {
            return false;
        }
    }
    
    protected static class EndWordAction extends RecordableTextAction
    {
        private boolean select;
        
        protected EndWordAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            try {
                final int offs = textArea.getCaretPosition();
                final int endOffs = this.getWordEnd(textArea, offs);
                if (this.select) {
                    textArea.moveCaretPosition(endOffs);
                }
                else {
                    textArea.setCaretPosition(endOffs);
                }
            }
            catch (BadLocationException ble) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        protected int getWordEnd(final RTextArea textArea, final int offs) throws BadLocationException {
            return Utilities.getWordEnd(textArea, offs);
        }
    }
    
    public static class IncreaseFontSizeAction extends RecordableTextAction
    {
        protected float increaseAmount;
        protected static final float MAXIMUM_SIZE = 40.0f;
        
        public IncreaseFontSizeAction() {
            super("RTA.IncreaseFontSizeAction");
            this.initialize();
        }
        
        public IncreaseFontSizeAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
            this.initialize();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            Font font = textArea.getFont();
            final float oldSize = font.getSize2D();
            final float newSize = oldSize + this.increaseAmount;
            if (newSize <= 40.0f) {
                font = font.deriveFont(newSize);
                textArea.setFont(font);
            }
            else if (oldSize < 40.0f) {
                font = font.deriveFont(40.0f);
                textArea.setFont(font);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "RTA.IncreaseFontSizeAction";
        }
        
        protected void initialize() {
            this.increaseAmount = 1.0f;
        }
    }
    
    public static class InsertBreakAction extends RecordableTextAction
    {
        public InsertBreakAction() {
            super("insert-break");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            textArea.replaceSelection("\n");
        }
        
        public final String getMacroID() {
            return "insert-break";
        }
        
        public boolean isEnabled() {
            final JTextComponent tc = this.getTextComponent(null);
            return (tc == null || tc.isEditable()) && super.isEnabled();
        }
    }
    
    public static class InsertContentAction extends RecordableTextAction
    {
        public InsertContentAction() {
            super("insert-content", null, null, null, null);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final String content = e.getActionCommand();
            if (content != null) {
                textArea.replaceSelection(content);
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return "insert-content";
        }
    }
    
    public static class InsertTabAction extends RecordableTextAction
    {
        public InsertTabAction() {
            super("insert-tab");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            textArea.replaceSelection("\t");
        }
        
        public final String getMacroID() {
            return "insert-tab";
        }
    }
    
    public static class InvertSelectionCaseAction extends RecordableTextAction
    {
        public InvertSelectionCaseAction() {
            super("RTA.InvertCaseAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final String selection = textArea.getSelectedText();
            if (selection != null) {
                final StringBuilder buffer = new StringBuilder(selection);
                for (int length = buffer.length(), i = 0; i < length; ++i) {
                    final char c = buffer.charAt(i);
                    if (Character.isUpperCase(c)) {
                        buffer.setCharAt(i, Character.toLowerCase(c));
                    }
                    else if (Character.isLowerCase(c)) {
                        buffer.setCharAt(i, Character.toUpperCase(c));
                    }
                }
                textArea.replaceSelection(buffer.toString());
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class JoinLinesAction extends RecordableTextAction
    {
        public JoinLinesAction() {
            super("RTA.JoinLinesAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            try {
                final Caret c = textArea.getCaret();
                int caretPos = c.getDot();
                final Document doc = textArea.getDocument();
                final Element map = doc.getDefaultRootElement();
                final int lineCount = map.getElementCount();
                final int line = map.getElementIndex(caretPos);
                if (line == lineCount - 1) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    return;
                }
                final Element lineElem = map.getElement(line);
                caretPos = lineElem.getEndOffset() - 1;
                c.setDot(caretPos);
                doc.remove(caretPos, 1);
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class LineMoveAction extends RecordableTextAction
    {
        private int moveAmt;
        
        public LineMoveAction(final String name, final int moveAmt) {
            super(name);
            this.moveAmt = moveAmt;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            try {
                final int caret = textArea.getCaretPosition();
                final Document doc = textArea.getDocument();
                final Element root = doc.getDefaultRootElement();
                final int line = root.getElementIndex(caret);
                if (this.moveAmt == -1 && line > 0) {
                    this.moveLineUp(textArea, line);
                }
                else {
                    if (this.moveAmt != 1 || line >= root.getElementCount() - 1) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                        return;
                    }
                    this.moveLineDown(textArea, line);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        private final void moveLineDown(final RTextArea textArea, final int line) throws BadLocationException {
            final Document doc = textArea.getDocument();
            final Element root = doc.getDefaultRootElement();
            Element elem = root.getElement(line);
            final int start = elem.getStartOffset();
            final int end = elem.getEndOffset();
            final int caret = textArea.getCaretPosition();
            final int caretOffset = caret - start;
            final String text = doc.getText(start, end - start);
            doc.remove(start, end - start);
            final Element elem2 = root.getElement(line);
            final int end2 = elem2.getEndOffset();
            doc.insertString(end2, text, null);
            elem = root.getElement(line + 1);
            textArea.setCaretPosition(elem.getStartOffset() + caretOffset);
        }
        
        private final void moveLineUp(final RTextArea textArea, final int line) throws BadLocationException {
            final Document doc = textArea.getDocument();
            final Element root = doc.getDefaultRootElement();
            final int lineCount = root.getElementCount();
            final Element elem = root.getElement(line);
            int start = elem.getStartOffset();
            final int end = (line == lineCount - 1) ? (elem.getEndOffset() - 1) : elem.getEndOffset();
            final int caret = textArea.getCaretPosition();
            final int caretOffset = caret - start;
            String text = doc.getText(start, end - start);
            if (line == lineCount - 1) {
                --start;
            }
            doc.remove(start, end - start);
            final Element elem2 = root.getElement(line - 1);
            final int start2 = elem2.getStartOffset();
            if (line == lineCount - 1) {
                text += '\n';
            }
            doc.insertString(start2, text, null);
            textArea.setCaretPosition(start2 + caretOffset);
        }
    }
    
    public static class LowerSelectionCaseAction extends RecordableTextAction
    {
        public LowerSelectionCaseAction() {
            super("RTA.LowerCaseAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final String selection = textArea.getSelectedText();
            if (selection != null) {
                textArea.replaceSelection(selection.toLowerCase());
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class NextBookmarkAction extends RecordableTextAction
    {
        private boolean forward;
        
        public NextBookmarkAction(final String name, final boolean forward) {
            super(name);
            this.forward = forward;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
            if (gutter != null) {
                try {
                    final GutterIconInfo[] bookmarks = gutter.getBookmarks();
                    if (bookmarks.length == 0) {
                        UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                        return;
                    }
                    GutterIconInfo moveTo = null;
                    final int curLine = textArea.getCaretLineNumber();
                    if (this.forward) {
                        for (int i = 0; i < bookmarks.length; ++i) {
                            final GutterIconInfo bookmark = bookmarks[i];
                            final int offs = bookmark.getMarkedOffset();
                            final int line = textArea.getLineOfOffset(offs);
                            if (line > curLine) {
                                moveTo = bookmark;
                                break;
                            }
                        }
                        if (moveTo == null) {
                            moveTo = bookmarks[0];
                        }
                    }
                    else {
                        for (int i = bookmarks.length - 1; i >= 0; --i) {
                            final GutterIconInfo bookmark = bookmarks[i];
                            final int offs = bookmark.getMarkedOffset();
                            final int line = textArea.getLineOfOffset(offs);
                            if (line < curLine) {
                                moveTo = bookmark;
                                break;
                            }
                        }
                        if (moveTo == null) {
                            moveTo = bookmarks[bookmarks.length - 1];
                        }
                    }
                    int offs2 = moveTo.getMarkedOffset();
                    if (textArea instanceof RSyntaxTextArea) {
                        final RSyntaxTextArea rsta = (RSyntaxTextArea)textArea;
                        if (rsta.isCodeFoldingEnabled()) {
                            rsta.getFoldManager().ensureOffsetNotInClosedFold(offs2);
                        }
                    }
                    final int line2 = textArea.getLineOfOffset(offs2);
                    offs2 = textArea.getLineStartOffset(line2);
                    textArea.setCaretPosition(offs2);
                }
                catch (BadLocationException ble) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    ble.printStackTrace();
                }
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class NextOccurrenceAction extends RecordableTextAction
    {
        public NextOccurrenceAction(final String name) {
            super(name);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            String selectedText = textArea.getSelectedText();
            if (selectedText == null || selectedText.length() == 0) {
                selectedText = RTextArea.getSelectedOccurrenceText();
                if (selectedText == null || selectedText.length() == 0) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    return;
                }
            }
            final SearchContext context = new SearchContext(selectedText);
            if (!SearchEngine.find(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            RTextArea.setSelectedOccurrenceText(selectedText);
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class NextVisualPositionAction extends RecordableTextAction
    {
        private boolean select;
        private int direction;
        
        public NextVisualPositionAction(final String nm, final boolean select, final int dir) {
            super(nm);
            this.select = select;
            this.direction = dir;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Caret caret = textArea.getCaret();
            int dot = caret.getDot();
            if (!this.select) {
                switch (this.direction) {
                    case 3: {
                        final int mark = caret.getMark();
                        if (dot != mark) {
                            caret.setDot(Math.max(dot, mark));
                            return;
                        }
                        break;
                    }
                    case 7: {
                        final int mark = caret.getMark();
                        if (dot != mark) {
                            caret.setDot(Math.min(dot, mark));
                            return;
                        }
                        break;
                    }
                }
            }
            final Position.Bias[] bias = { null };
            Point magicPosition = caret.getMagicCaretPosition();
            try {
                if (magicPosition == null && (this.direction == 1 || this.direction == 5)) {
                    final Rectangle r = textArea.modelToView(dot);
                    magicPosition = new Point(r.x, r.y);
                }
                final NavigationFilter filter = textArea.getNavigationFilter();
                if (filter != null) {
                    dot = filter.getNextVisualPositionFrom(textArea, dot, Position.Bias.Forward, this.direction, bias);
                }
                else {
                    dot = textArea.getUI().getNextVisualPositionFrom(textArea, dot, Position.Bias.Forward, this.direction, bias);
                }
                if (this.select) {
                    caret.moveDot(dot);
                }
                else {
                    caret.setDot(dot);
                }
                if (magicPosition != null && (this.direction == 1 || this.direction == 5)) {
                    caret.setMagicCaretPosition(magicPosition);
                }
            }
            catch (BadLocationException ble) {
                ble.printStackTrace();
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class NextWordAction extends RecordableTextAction
    {
        private boolean select;
        
        public NextWordAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final int oldOffs;
            int offs = oldOffs = textArea.getCaretPosition();
            final Element curPara = Utilities.getParagraphElement(textArea, offs);
            try {
                offs = this.getNextWord(textArea, offs);
                if (offs >= curPara.getEndOffset() && oldOffs != curPara.getEndOffset() - 1) {
                    offs = curPara.getEndOffset() - 1;
                }
            }
            catch (BadLocationException ble) {
                final int end = textArea.getDocument().getLength();
                if (offs != end) {
                    if (oldOffs != curPara.getEndOffset() - 1) {
                        offs = curPara.getEndOffset() - 1;
                    }
                    else {
                        offs = end;
                    }
                }
            }
            if (this.select) {
                textArea.moveCaretPosition(offs);
            }
            else {
                textArea.setCaretPosition(offs);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        protected int getNextWord(final RTextArea textArea, final int offs) throws BadLocationException {
            return Utilities.getNextWord(textArea, offs);
        }
    }
    
    static class PageAction extends RecordableTextAction
    {
        private boolean select;
        private boolean left;
        
        public PageAction(final String name, final boolean left, final boolean select) {
            super(name);
            this.select = select;
            this.left = left;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Rectangle visible = new Rectangle();
            textArea.computeVisibleRect(visible);
            if (this.left) {
                visible.x = Math.max(0, visible.x - visible.width);
            }
            else {
                final Rectangle loc_0 = visible;
                loc_0.x += visible.width;
            }
            int selectedIndex = textArea.getCaretPosition();
            if (selectedIndex != -1) {
                if (this.left) {
                    selectedIndex = textArea.viewToModel(new Point(visible.x, visible.y));
                }
                else {
                    selectedIndex = textArea.viewToModel(new Point(visible.x + visible.width - 1, visible.y + visible.height - 1));
                }
                final Document doc = textArea.getDocument();
                if (selectedIndex != 0 && selectedIndex > doc.getLength() - 1) {
                    selectedIndex = doc.getLength() - 1;
                }
                else if (selectedIndex < 0) {
                    selectedIndex = 0;
                }
                if (this.select) {
                    textArea.moveCaretPosition(selectedIndex);
                }
                else {
                    textArea.setCaretPosition(selectedIndex);
                }
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class PasteAction extends RecordableTextAction
    {
        public PasteAction() {
            super("paste-from-clipboard");
        }
        
        public PasteAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.paste();
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return "paste-from-clipboard";
        }
    }
    
    public static class PlaybackLastMacroAction extends RecordableTextAction
    {
        public PlaybackLastMacroAction() {
            super("RTA.PlaybackLastMacroAction");
        }
        
        public PlaybackLastMacroAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.playbackLastMacro();
        }
        
        public boolean isRecordable() {
            return false;
        }
        
        public final String getMacroID() {
            return "RTA.PlaybackLastMacroAction";
        }
    }
    
    public static class PreviousOccurrenceAction extends RecordableTextAction
    {
        public PreviousOccurrenceAction(final String name) {
            super(name);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            String selectedText = textArea.getSelectedText();
            if (selectedText == null || selectedText.length() == 0) {
                selectedText = RTextArea.getSelectedOccurrenceText();
                if (selectedText == null || selectedText.length() == 0) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    return;
                }
            }
            final SearchContext context = new SearchContext(selectedText);
            context.setSearchForward(false);
            if (!SearchEngine.find(textArea, context).wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
            RTextArea.setSelectedOccurrenceText(selectedText);
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class PreviousWordAction extends RecordableTextAction
    {
        private boolean select;
        
        public PreviousWordAction(final String name, final boolean select) {
            super(name);
            this.select = select;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            int offs = textArea.getCaretPosition();
            boolean failed = false;
            try {
                final Element curPara = Utilities.getParagraphElement(textArea, offs);
                offs = this.getPreviousWord(textArea, offs);
                if (offs < curPara.getStartOffset()) {
                    offs = Utilities.getParagraphElement(textArea, offs).getEndOffset() - 1;
                }
            }
            catch (BadLocationException bl) {
                if (offs != 0) {
                    offs = 0;
                }
                else {
                    failed = true;
                }
            }
            if (!failed) {
                if (this.select) {
                    textArea.moveCaretPosition(offs);
                }
                else {
                    textArea.setCaretPosition(offs);
                }
            }
            else {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
        
        protected int getPreviousWord(final RTextArea textArea, final int offs) throws BadLocationException {
            return Utilities.getPreviousWord(textArea, offs);
        }
    }
    
    public static class RedoAction extends RecordableTextAction
    {
        public RedoAction() {
            super("RTA.RedoAction");
        }
        
        public RedoAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (textArea.isEnabled() && textArea.isEditable()) {
                textArea.redoLastAction();
                textArea.requestFocusInWindow();
            }
        }
        
        public final String getMacroID() {
            return "RTA.RedoAction";
        }
    }
    
    public static class ScrollAction extends RecordableTextAction
    {
        private int delta;
        
        public ScrollAction(final String name, final int delta) {
            super(name);
            this.delta = delta;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Container parent = textArea.getParent();
            if (parent instanceof JViewport) {
                final JViewport viewport = (JViewport)parent;
                final Point loc_0;
                final Point p = loc_0 = viewport.getViewPosition();
                loc_0.y += this.delta * textArea.getLineHeight();
                if (p.y < 0) {
                    p.y = 0;
                }
                else {
                    final Rectangle viewRect = viewport.getViewRect();
                    final int visibleEnd = p.y + viewRect.height;
                    if (visibleEnd >= textArea.getHeight()) {
                        p.y = textArea.getHeight() - viewRect.height;
                    }
                }
                viewport.setViewPosition(p);
            }
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class SelectAllAction extends RecordableTextAction
    {
        public SelectAllAction() {
            super("select-all");
        }
        
        public SelectAllAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Document doc = textArea.getDocument();
            textArea.setCaretPosition(0);
            textArea.moveCaretPosition(doc.getLength());
        }
        
        public final String getMacroID() {
            return "select-all";
        }
    }
    
    public static class SelectLineAction extends RecordableTextAction
    {
        private Action start;
        private Action end;
        
        public SelectLineAction() {
            super("select-line");
            this.start = new BeginLineAction("pigdog", false);
            this.end = new EndLineAction("pigdog", true);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
        }
        
        public final String getMacroID() {
            return "select-line";
        }
    }
    
    public static class SelectWordAction extends RecordableTextAction
    {
        protected Action start;
        protected Action end;
        
        public SelectWordAction() {
            super("select-word");
            this.createActions();
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            this.start.actionPerformed(e);
            this.end.actionPerformed(e);
        }
        
        protected void createActions() {
            this.start = new BeginWordAction("pigdog", false);
            this.end = new EndWordAction("pigdog", true);
        }
        
        public final String getMacroID() {
            return "select-word";
        }
    }
    
    public static class SetReadOnlyAction extends RecordableTextAction
    {
        public SetReadOnlyAction() {
            super("set-read-only");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.setEditable(false);
        }
        
        public final String getMacroID() {
            return "set-read-only";
        }
        
        public boolean isRecordable() {
            return false;
        }
    }
    
    public static class SetWritableAction extends RecordableTextAction
    {
        public SetWritableAction() {
            super("set-writable");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.setEditable(true);
        }
        
        public final String getMacroID() {
            return "set-writable";
        }
        
        public boolean isRecordable() {
            return false;
        }
    }
    
    public static class TimeDateAction extends RecordableTextAction
    {
        public TimeDateAction() {
            super("RTA.TimeDateAction");
        }
        
        public TimeDateAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final Date today = new Date();
            final DateFormat timeDateStamp = DateFormat.getDateTimeInstance();
            final String dateString = timeDateStamp.format(today);
            textArea.replaceSelection(dateString);
        }
        
        public final String getMacroID() {
            return "RTA.TimeDateAction";
        }
    }
    
    public static class ToggleBookmarkAction extends RecordableTextAction
    {
        public ToggleBookmarkAction() {
            super("RTA.ToggleBookmarkAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Gutter gutter = RSyntaxUtilities.getGutter(textArea);
            if (gutter != null) {
                final int line = textArea.getCaretLineNumber();
                try {
                    gutter.toggleBookmark(line);
                }
                catch (BadLocationException ble) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                    ble.printStackTrace();
                }
            }
        }
        
        public final String getMacroID() {
            return "RTA.ToggleBookmarkAction";
        }
    }
    
    public static class ToggleTextModeAction extends RecordableTextAction
    {
        public ToggleTextModeAction() {
            super("RTA.ToggleTextModeAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final int textMode = textArea.getTextMode();
            if (textMode == 0) {
                textArea.setTextMode(1);
            }
            else {
                textArea.setTextMode(0);
            }
        }
        
        public final String getMacroID() {
            return "RTA.ToggleTextModeAction";
        }
    }
    
    public static class UndoAction extends RecordableTextAction
    {
        public UndoAction() {
            super("RTA.UndoAction");
        }
        
        public UndoAction(final String name, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
            super(name, icon, desc, mnemonic, accelerator);
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (textArea.isEnabled() && textArea.isEditable()) {
                textArea.undoLastAction();
                textArea.requestFocusInWindow();
            }
        }
        
        public final String getMacroID() {
            return "RTA.UndoAction";
        }
    }
    
    public static class UnselectAction extends RecordableTextAction
    {
        public UnselectAction() {
            super("RTA.UnselectAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            textArea.setCaretPosition(textArea.getCaretPosition());
        }
        
        public final String getMacroID() {
            return "RTA.UnselectAction";
        }
    }
    
    public static class UpperSelectionCaseAction extends RecordableTextAction
    {
        public UpperSelectionCaseAction() {
            super("RTA.UpperCaseAction");
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            if (!textArea.isEditable() || !textArea.isEnabled()) {
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                return;
            }
            final String selection = textArea.getSelectedText();
            if (selection != null) {
                textArea.replaceSelection(selection.toUpperCase());
            }
            textArea.requestFocusInWindow();
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
    
    public static class VerticalPageAction extends RecordableTextAction
    {
        private boolean select;
        private int direction;
        
        public VerticalPageAction(final String name, final int direction, final boolean select) {
            super(name);
            this.select = select;
            this.direction = direction;
        }
        
        public void actionPerformedImpl(final ActionEvent e, final RTextArea textArea) {
            final Rectangle visible = textArea.getVisibleRect();
            final Rectangle newVis = new Rectangle(visible);
            final int selectedIndex = textArea.getCaretPosition();
            final int scrollAmount = textArea.getScrollableBlockIncrement(visible, 1, this.direction);
            final int initialY = visible.y;
            final Caret caret = textArea.getCaret();
            final Point magicPosition = caret.getMagicCaretPosition();
            if (selectedIndex != -1) {
                try {
                    final Rectangle dotBounds = textArea.modelToView(selectedIndex);
                    final int x = (magicPosition != null) ? magicPosition.x : dotBounds.x;
                    final int h = dotBounds.height;
                    final int yOffset = this.direction * ((int)Math.ceil(scrollAmount / h) - 1) * h;
                    newVis.y = this.constrainY(textArea, initialY + yOffset, yOffset, visible.height);
                    int newIndex;
                    if (visible.contains(dotBounds.x, dotBounds.y)) {
                        newIndex = textArea.viewToModel(new Point(x, this.constrainY(textArea, dotBounds.y + yOffset, 0, 0)));
                    }
                    else if (this.direction == -1) {
                        newIndex = textArea.viewToModel(new Point(x, newVis.y));
                    }
                    else {
                        newIndex = textArea.viewToModel(new Point(x, newVis.y + visible.height));
                    }
                    newIndex = this.constrainOffset(textArea, newIndex);
                    if (newIndex != selectedIndex) {
                        this.adjustScrollIfNecessary(textArea, newVis, initialY, newIndex);
                        if (this.select) {
                            textArea.moveCaretPosition(newIndex);
                        }
                        else {
                            textArea.setCaretPosition(newIndex);
                        }
                    }
                }
                catch (BadLocationException ble) {}
            }
            else {
                final int yOffset = this.direction * scrollAmount;
                newVis.y = this.constrainY(textArea, initialY + yOffset, yOffset, visible.height);
            }
            if (magicPosition != null) {
                caret.setMagicCaretPosition(magicPosition);
            }
            textArea.scrollRectToVisible(newVis);
        }
        
        private int constrainY(final JTextComponent textArea, int y, final int vis, final int screenHeight) {
            if (y < 0) {
                y = 0;
            }
            else if (y + vis > textArea.getHeight()) {
                y = Math.max(0, textArea.getHeight() - screenHeight);
            }
            return y;
        }
        
        private int constrainOffset(final JTextComponent text, int offset) {
            final Document doc = text.getDocument();
            if (offset != 0 && offset > doc.getLength()) {
                offset = doc.getLength();
            }
            if (offset < 0) {
                offset = 0;
            }
            return offset;
        }
        
        private void adjustScrollIfNecessary(final JTextComponent text, final Rectangle visible, final int initialY, final int index) {
            try {
                final Rectangle dotBounds = text.modelToView(index);
                if (dotBounds.y < visible.y || dotBounds.y > visible.y + visible.height || dotBounds.y + dotBounds.height > visible.y + visible.height) {
                    int y;
                    if (dotBounds.y < visible.y) {
                        y = dotBounds.y;
                    }
                    else {
                        y = dotBounds.y + dotBounds.height - visible.height;
                    }
                    if ((this.direction == -1 && y < initialY) || (this.direction == 1 && y > initialY)) {
                        visible.y = y;
                    }
                }
            }
            catch (BadLocationException loc_0) {}
        }
        
        public final String getMacroID() {
            return this.getName();
        }
    }
}
