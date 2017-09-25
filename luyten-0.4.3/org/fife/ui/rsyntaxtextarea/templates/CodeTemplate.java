package org.fife.ui.rsyntaxtextarea.templates;

import java.io.*;
import org.fife.ui.rsyntaxtextarea.*;
import javax.swing.text.*;

public interface CodeTemplate extends Cloneable, Comparable<CodeTemplate>, Serializable
{
    Object clone();
    
    String getID();
    
    void invoke(RSyntaxTextArea param_0) throws BadLocationException;
}
