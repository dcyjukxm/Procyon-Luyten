package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ParenthesizedExpression extends Expression
{
    public ParenthesizedExpression(final Expression expression) {
        super(expression.getOffset());
        this.setExpression(expression);
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
        return (R)visitor.visitParenthesizedExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ParenthesizedExpression && this.getExpression().matches(((ParenthesizedExpression)other).getExpression(), match);
    }
}
