package org.fife.ui.rsyntaxtextarea.folding;

import org.fife.ui.rsyntaxtextarea.*;

public class LispFoldParser extends CurlyFoldParser
{
    public boolean isLeftCurly(final Token t) {
        return t.isSingleChar(22, '(');
    }
    
    public boolean isRightCurly(final Token t) {
        return t.isSingleChar(22, ')');
    }
}
