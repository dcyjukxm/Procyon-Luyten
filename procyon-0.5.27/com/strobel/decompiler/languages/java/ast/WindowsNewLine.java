package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public final class WindowsNewLine extends NewLineNode
{
    @Override
    public NewLineType getNewLineType() {
        return NewLineType.WINDOWS;
    }
    
    public WindowsNewLine() {
        super();
    }
    
    public WindowsNewLine(final TextLocation startLocation) {
        super(startLocation);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof UnixNewLine;
    }
}
