package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;

public abstract class NewLineNode extends AstNode
{
    private final TextLocation _startLocation;
    private final TextLocation _endLocation;
    
    protected NewLineNode() {
        this(TextLocation.EMPTY);
    }
    
    protected NewLineNode(final TextLocation startLocation) {
        super();
        this._startLocation = ((startLocation != null) ? startLocation : TextLocation.EMPTY);
        this._endLocation = new TextLocation(this._startLocation.line() + 1, 1);
    }
    
    public abstract NewLineType getNewLineType();
    
    @Override
    public TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    @Override
    public TextLocation getEndLocation() {
        return this._endLocation;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitNewLine(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.WHITESPACE;
    }
    
    public static NewLineNode create() {
        if (Environment.isWindows() || Environment.isOS2()) {
            return new WindowsNewLine();
        }
        if (Environment.isMac()) {
            return new MacNewLine();
        }
        return new UnixNewLine();
    }
}
