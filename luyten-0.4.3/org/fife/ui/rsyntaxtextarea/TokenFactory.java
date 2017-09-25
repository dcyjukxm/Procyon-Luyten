package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;

interface TokenFactory
{
    TokenImpl createToken();
    
    TokenImpl createToken(Segment param_0, int param_1, int param_2, int param_3, int param_4);
    
    TokenImpl createToken(char[] param_0, int param_1, int param_2, int param_3, int param_4);
    
    void resetAllTokens();
}
