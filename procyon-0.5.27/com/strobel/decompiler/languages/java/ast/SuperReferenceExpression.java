package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public final class SuperReferenceExpression extends Expression
{
    private static final String SUPER_TEXT = "super";
    private TextLocation _startLocation;
    private TextLocation _endLocation;
    
    public SuperReferenceExpression(final int offset) {
        this(offset, TextLocation.EMPTY);
    }
    
    public SuperReferenceExpression(final int offset, final TextLocation startLocation) {
        super(offset);
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "super".length());
    }
    
    @Override
    public TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    @Override
    public TextLocation getEndLocation() {
        return this._endLocation;
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    public void setStartLocation(final TextLocation startLocation) {
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = new TextLocation(startLocation.line(), startLocation.column() + "super".length());
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitSuperReferenceExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof SuperReferenceExpression && this.getTarget().matches(((SuperReferenceExpression)other).getTarget(), match);
    }
}
