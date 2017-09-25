package org.fife.ui.rtextarea;

import javax.swing.*;
import java.awt.event.*;
import javax.swing.text.*;
import java.util.*;

public abstract class RecordableTextAction extends TextAction
{
    private boolean isRecordable;
    
    public RecordableTextAction(final String text) {
        this(text, null, null, null, null);
    }
    
    public RecordableTextAction(final String text, final Icon icon, final String desc, final Integer mnemonic, final KeyStroke accelerator) {
        super(text);
        this.putValue("SmallIcon", icon);
        this.putValue("ShortDescription", desc);
        this.putValue("AcceleratorKey", accelerator);
        this.putValue("MnemonicKey", mnemonic);
        this.setRecordable(true);
    }
    
    public final void actionPerformed(final ActionEvent e) {
        final JTextComponent textComponent = this.getTextComponent(e);
        if (textComponent instanceof RTextArea) {
            final RTextArea textArea = (RTextArea)textComponent;
            if (RTextArea.isRecordingMacro() && this.isRecordable()) {
                final int mod = e.getModifiers();
                final String macroID = this.getMacroID();
                if (!"default-typed".equals(macroID) || ((mod & 0x8) == 0x0 && (mod & 0x2) == 0x0 && (mod & 0x4) == 0x0)) {
                    final String command = e.getActionCommand();
                    RTextArea.addToCurrentMacro(macroID, command);
                }
            }
            this.actionPerformedImpl(e, textArea);
        }
    }
    
    public abstract void actionPerformedImpl(final ActionEvent param_0, final RTextArea param_1);
    
    public KeyStroke getAccelerator() {
        return (KeyStroke)this.getValue("AcceleratorKey");
    }
    
    public String getDescription() {
        return (String)this.getValue("ShortDescription");
    }
    
    public Icon getIcon() {
        return (Icon)this.getValue("SmallIcon");
    }
    
    public abstract String getMacroID();
    
    public int getMnemonic() {
        final Integer i = (Integer)this.getValue("MnemonicKey");
        return (i != null) ? i : -1;
    }
    
    public String getName() {
        return (String)this.getValue("Name");
    }
    
    public boolean isRecordable() {
        return this.isRecordable;
    }
    
    public void setAccelerator(final KeyStroke accelerator) {
        this.putValue("AcceleratorKey", accelerator);
    }
    
    public void setMnemonic(final char mnemonic) {
        this.setMnemonic(Integer.valueOf(mnemonic));
    }
    
    public void setMnemonic(final Integer mnemonic) {
        this.putValue("MnemonicKey", mnemonic);
    }
    
    public void setName(final String name) {
        this.putValue("Name", name);
    }
    
    public void setProperties(final ResourceBundle msg, final String keyRoot) {
        this.setName(msg.getString(keyRoot + ".Name"));
        this.setMnemonic(msg.getString(keyRoot + ".Mnemonic").charAt(0));
        this.setShortDescription(msg.getString(keyRoot + ".Desc"));
    }
    
    public void setRecordable(final boolean recordable) {
        this.isRecordable = recordable;
    }
    
    public void setShortDescription(final String shortDesc) {
        this.putValue("ShortDescription", shortDesc);
    }
}
