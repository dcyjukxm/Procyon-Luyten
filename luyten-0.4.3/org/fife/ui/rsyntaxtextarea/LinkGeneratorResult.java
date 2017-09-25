package org.fife.ui.rsyntaxtextarea;

import javax.swing.event.*;

public interface LinkGeneratorResult
{
    HyperlinkEvent execute();
    
    int getSourceOffset();
}
