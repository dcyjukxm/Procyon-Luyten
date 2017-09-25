package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public final class ThisReferenceExpression extends Expression
{
    private static final String THIS_TEXT = "this";
    private TextLocation _startLocation;
    private TextLocation _endLocation;
    
    public ThisReferenceExpression(final int offset) {
        this(offset, TextLocation.EMPTY);
    }
    
    public ThisReferenceExpression(final int offset, final TextLocation startLocation) {
        super(offset);
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "this".length());
    }
    
    @Override
    public final TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    @Override
    public final TextLocation getEndLocation() {
        return this._endLocation;
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    public final void setStartLocation(final TextLocation startLocation) {
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "this".length());
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitThisReferenceExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ThisReferenceExpression && this.getTarget().matches(((ThisReferenceExpression)other).getTarget(), match);
    }
}
