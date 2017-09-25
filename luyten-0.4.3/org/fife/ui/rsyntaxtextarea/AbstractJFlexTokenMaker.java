package org.fife.ui.rsyntaxtextarea;

import javax.swing.text.*;

public abstract class AbstractJFlexTokenMaker extends TokenMakerBase
{
    protected Segment s;
    protected int start;
    protected int offsetShift;
    
    public abstract void yybegin(final int param_0);
    
    protected void yybegin(final int state, final int languageIndex) {
        this.yybegin(state);
        this.setLanguageIndex(languageIndex);
    }
}
