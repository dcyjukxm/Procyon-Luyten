package org.fife.ui.rsyntaxtextarea;

import java.awt.*;
import javax.swing.text.*;

interface RSTAView
{
    int yForLine(Rectangle param_0, int param_1) throws BadLocationException;
    
    int yForLineContaining(Rectangle param_0, int param_1) throws BadLocationException;
}
