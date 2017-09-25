package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;
import javax.swing.*;

public interface TokenMaker
{
    void addNullToken();
    
    void addToken(char[] param_0, int param_1, int param_2, int param_3, int param_4);
    
    int getClosestStandardTokenTypeForInternalType(int param_0);
    
    boolean getCurlyBracesDenoteCodeBlocks(int param_0);
    
    int getLastTokenTypeOnLine(Segment param_0, int param_1);
    
    String[] getLineCommentStartAndEnd(int param_0);
    
    Action getInsertBreakAction();
    
    boolean getMarkOccurrencesOfTokenType(int param_0);
    
    OccurrenceMarker getOccurrenceMarker();
    
    boolean getShouldIndentNextLineAfter(Token param_0);
    
    Token getTokenList(Segment param_0, int param_1, int param_2);
    
    boolean isMarkupLanguage();
}
