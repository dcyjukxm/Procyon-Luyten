package org.fife.ui.rtextarea;

import javax.swing.*;
import java.awt.*;

public class RTADefaultInputMap extends InputMap
{
    public RTADefaultInputMap() {
        super();
        final int defaultModifier = getDefaultModifier();
        final int alt = 8;
        final int shift = 1;
        this.put(KeyStroke.getKeyStroke(36, 0), "caret-begin-line");
        this.put(KeyStroke.getKeyStroke(36, shift), "selection-begin-line");
        this.put(KeyStroke.getKeyStroke(36, defaultModifier), "caret-begin");
        this.put(KeyStroke.getKeyStroke(36, defaultModifier | shift), "selection-begin");
        this.put(KeyStroke.getKeyStroke(35, 0), "caret-end-line");
        this.put(KeyStroke.getKeyStroke(35, shift), "selection-end-line");
        this.put(KeyStroke.getKeyStroke(35, defaultModifier), "caret-end");
        this.put(KeyStroke.getKeyStroke(35, defaultModifier | shift), "selection-end");
        this.put(KeyStroke.getKeyStroke(37, 0), "caret-backward");
        this.put(KeyStroke.getKeyStroke(37, shift), "selection-backward");
        this.put(KeyStroke.getKeyStroke(37, defaultModifier), "caret-previous-word");
        this.put(KeyStroke.getKeyStroke(37, defaultModifier | shift), "selection-previous-word");
        this.put(KeyStroke.getKeyStroke(40, 0), "caret-down");
        this.put(KeyStroke.getKeyStroke(40, shift), "selection-down");
        this.put(KeyStroke.getKeyStroke(40, defaultModifier), "RTA.ScrollDownAction");
        this.put(KeyStroke.getKeyStroke(40, alt), "RTA.LineDownAction");
        this.put(KeyStroke.getKeyStroke(39, 0), "caret-forward");
        this.put(KeyStroke.getKeyStroke(39, shift), "selection-forward");
        this.put(KeyStroke.getKeyStroke(39, defaultModifier), "caret-next-word");
        this.put(KeyStroke.getKeyStroke(39, defaultModifier | shift), "selection-next-word");
        this.put(KeyStroke.getKeyStroke(38, 0), "caret-up");
        this.put(KeyStroke.getKeyStroke(38, shift), "selection-up");
        this.put(KeyStroke.getKeyStroke(38, defaultModifier), "RTA.ScrollUpAction");
        this.put(KeyStroke.getKeyStroke(38, alt), "RTA.LineUpAction");
        this.put(KeyStroke.getKeyStroke(33, 0), "page-up");
        this.put(KeyStroke.getKeyStroke(33, shift), "RTA.SelectionPageUpAction");
        this.put(KeyStroke.getKeyStroke(33, defaultModifier | shift), "RTA.SelectionPageLeftAction");
        this.put(KeyStroke.getKeyStroke(34, 0), "page-down");
        this.put(KeyStroke.getKeyStroke(34, shift), "RTA.SelectionPageDownAction");
        this.put(KeyStroke.getKeyStroke(34, defaultModifier | shift), "RTA.SelectionPageRightAction");
        this.put(KeyStroke.getKeyStroke(65489, 0), "cut-to-clipboard");
        this.put(KeyStroke.getKeyStroke(65485, 0), "copy-to-clipboard");
        this.put(KeyStroke.getKeyStroke(65487, 0), "paste-from-clipboard");
        this.put(KeyStroke.getKeyStroke(88, defaultModifier), "cut-to-clipboard");
        this.put(KeyStroke.getKeyStroke(67, defaultModifier), "copy-to-clipboard");
        this.put(KeyStroke.getKeyStroke(86, defaultModifier), "paste-from-clipboard");
        this.put(KeyStroke.getKeyStroke(127, 0), "delete-next");
        this.put(KeyStroke.getKeyStroke(127, shift), "cut-to-clipboard");
        this.put(KeyStroke.getKeyStroke(127, defaultModifier), "RTA.DeleteRestOfLineAction");
        this.put(KeyStroke.getKeyStroke(155, 0), "RTA.ToggleTextModeAction");
        this.put(KeyStroke.getKeyStroke(155, shift), "paste-from-clipboard");
        this.put(KeyStroke.getKeyStroke(155, defaultModifier), "copy-to-clipboard");
        this.put(KeyStroke.getKeyStroke(65, defaultModifier), "select-all");
        this.put(KeyStroke.getKeyStroke(68, defaultModifier), "RTA.DeleteLineAction");
        this.put(KeyStroke.getKeyStroke(74, defaultModifier), "RTA.JoinLinesAction");
        this.put(KeyStroke.getKeyStroke(8, shift), "delete-previous");
        this.put(KeyStroke.getKeyStroke(8, defaultModifier), "RTA.DeletePrevWordAction");
        this.put(KeyStroke.getKeyStroke(9, 0), "insert-tab");
        this.put(KeyStroke.getKeyStroke(10, 0), "insert-break");
        this.put(KeyStroke.getKeyStroke(10, shift), "insert-break");
        this.put(KeyStroke.getKeyStroke(10, defaultModifier), "RTA.DumbCompleteWordAction");
        this.put(KeyStroke.getKeyStroke(90, defaultModifier), "RTA.UndoAction");
        this.put(KeyStroke.getKeyStroke(89, defaultModifier), "RTA.RedoAction");
        this.put(KeyStroke.getKeyStroke(113, 0), "RTA.NextBookmarkAction");
        this.put(KeyStroke.getKeyStroke(113, shift), "RTA.PrevBookmarkAction");
        this.put(KeyStroke.getKeyStroke(113, defaultModifier), "RTA.ToggleBookmarkAction");
        this.put(KeyStroke.getKeyStroke(75, defaultModifier | shift), "RTA.PrevOccurrenceAction");
        this.put(KeyStroke.getKeyStroke(75, defaultModifier), "RTA.NextOccurrenceAction");
    }
    
    protected static final int getDefaultModifier() {
        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    }
}
