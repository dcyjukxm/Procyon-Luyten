package org.fife.ui.rtextarea;

import java.util.*;
import javax.swing.event.*;
import javax.swing.undo.*;
import javax.swing.*;

public class RUndoManager extends UndoManager
{
    private RCompoundEdit compoundEdit;
    private RTextArea textArea;
    private int lastOffset;
    private String cantUndoText;
    private String cantRedoText;
    private int internalAtomicEditDepth;
    private static final String MSG = "org.fife.ui.rtextarea.RTextArea";
    
    public RUndoManager(final RTextArea textArea) {
        super();
        this.textArea = textArea;
        final ResourceBundle msg = ResourceBundle.getBundle("org.fife.ui.rtextarea.RTextArea");
        this.cantUndoText = msg.getString("Action.CantUndo.Name");
        this.cantRedoText = msg.getString("Action.CantRedo.Name");
    }
    
    public void beginInternalAtomicEdit() {
        if (++this.internalAtomicEditDepth == 1) {
            if (this.compoundEdit != null) {
                this.compoundEdit.end();
            }
            this.compoundEdit = new RCompoundEdit();
        }
    }
    
    public void endInternalAtomicEdit() {
        if (this.internalAtomicEditDepth > 0 && --this.internalAtomicEditDepth == 0) {
            this.addEdit(this.compoundEdit);
            this.compoundEdit.end();
            this.compoundEdit = null;
            this.updateActions();
        }
    }
    
    public String getCantRedoText() {
        return this.cantRedoText;
    }
    
    public String getCantUndoText() {
        return this.cantUndoText;
    }
    
    public void redo() throws CannotRedoException {
        super.redo();
        this.updateActions();
    }
    
    private RCompoundEdit startCompoundEdit(final UndoableEdit edit) {
        this.lastOffset = this.textArea.getCaretPosition();
        (this.compoundEdit = new RCompoundEdit()).addEdit(edit);
        this.addEdit(this.compoundEdit);
        return this.compoundEdit;
    }
    
    public void undo() throws CannotUndoException {
        super.undo();
        this.updateActions();
    }
    
    public void undoableEditHappened(final UndoableEditEvent e) {
        if (this.compoundEdit == null) {
            this.compoundEdit = this.startCompoundEdit(e.getEdit());
            this.updateActions();
            return;
        }
        if (this.internalAtomicEditDepth > 0) {
            this.compoundEdit.addEdit(e.getEdit());
            return;
        }
        final int diff = this.textArea.getCaretPosition() - this.lastOffset;
        if (Math.abs(diff) <= 1) {
            this.compoundEdit.addEdit(e.getEdit());
            this.lastOffset += diff;
            return;
        }
        this.compoundEdit.end();
        this.compoundEdit = this.startCompoundEdit(e.getEdit());
    }
    
    public void updateActions() {
        Action a = RTextArea.getAction(6);
        if (this.canUndo()) {
            a.setEnabled(true);
            final String text = this.getUndoPresentationName();
            a.putValue("Name", text);
            a.putValue("ShortDescription", text);
        }
        else if (a.isEnabled()) {
            a.setEnabled(false);
            final String text = this.cantUndoText;
            a.putValue("Name", text);
            a.putValue("ShortDescription", text);
        }
        a = RTextArea.getAction(4);
        if (this.canRedo()) {
            a.setEnabled(true);
            final String text = this.getRedoPresentationName();
            a.putValue("Name", text);
            a.putValue("ShortDescription", text);
        }
        else if (a.isEnabled()) {
            a.setEnabled(false);
            final String text = this.cantRedoText;
            a.putValue("Name", text);
            a.putValue("ShortDescription", text);
        }
    }
    
    static /* synthetic */ RCompoundEdit access$000(final RUndoManager x0) {
        return x0.compoundEdit;
    }
    
    static /* synthetic */ RCompoundEdit access$002(final RUndoManager x0, final RCompoundEdit x1) {
        return x0.compoundEdit = x1;
    }
    
    class RCompoundEdit extends CompoundEdit
    {
        public String getUndoPresentationName() {
            return UIManager.getString("AbstractUndoableEdit.undoText");
        }
        
        public String getRedoPresentationName() {
            return UIManager.getString("AbstractUndoableEdit.redoText");
        }
        
        public boolean isInProgress() {
            return false;
        }
        
        public void undo() throws CannotUndoException {
            if (RUndoManager.access$000(RUndoManager.this) != null) {
                RUndoManager.access$000(RUndoManager.this).end();
            }
            super.undo();
            RUndoManager.access$002(RUndoManager.this, null);
        }
    }
}
