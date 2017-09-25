package org.fife.ui.rsyntaxtextarea.modes;

import org.fife.ui.rsyntaxtextarea.*;

public abstract class AbstractMarkupTokenMaker extends AbstractJFlexTokenMaker
{
    public abstract boolean getCompleteCloseTags();
    
    public String[] getLineCommentStartAndEnd(final int languageIndex) {
        return new String[] { "<!--", "-->" };
    }
    
    public final boolean isMarkupLanguage() {
        return true;
    }
}
