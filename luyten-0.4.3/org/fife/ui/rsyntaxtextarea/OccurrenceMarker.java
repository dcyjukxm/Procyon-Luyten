package org.fife.ui.rsyntaxtextarea;

import org.fife.ui.rtextarea.*;

public interface OccurrenceMarker
{
    Token getTokenToMark(RSyntaxTextArea param_0);
    
    boolean isValidType(RSyntaxTextArea param_0, Token param_1);
    
    void markOccurrences(RSyntaxDocument param_0, Token param_1, RSyntaxTextAreaHighlighter param_2, SmartHighlightPainter param_3);
}
