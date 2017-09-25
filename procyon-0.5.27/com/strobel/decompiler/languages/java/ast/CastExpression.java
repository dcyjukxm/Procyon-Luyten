package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class CastExpression extends Expression
{
    public CastExpression(final AstType castToType, final Expression expression) {
        super(expression.getOffset());
        this.setType(castToType);
        this.setExpression(expression);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitCastExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof CastExpression) {
            final CastExpression otherCast = (CastExpression)other;
            return !otherCast.isNull() && this.getType().matches(otherCast.getType(), match) && this.getExpression().matches(otherCast.getExpression(), match);
        }
        return false;
    }
}
