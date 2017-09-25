package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public final class NullReferenceExpression extends Expression
{
    private static final String NULL_TEXT = "null";
    private TextLocation _startLocation;
    private TextLocation _endLocation;
    
    public NullReferenceExpression(final int offset) {
        this(offset, TextLocation.EMPTY);
    }
    
    public NullReferenceExpression(final int offset, final TextLocation startLocation) {
        super(offset);
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "null".length());
    }
    
    @Override
    public TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    @Override
    public TextLocation getEndLocation() {
        return this._endLocation;
    }
    
    public void setStartLocation(final TextLocation startLocation) {
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "null".length());
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitNullReferenceExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof NullReferenceExpression;
    }
}
