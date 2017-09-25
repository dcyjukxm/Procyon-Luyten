package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ExpressionStatement extends Statement
{
    public ExpressionStatement(final Expression expression) {
        super(expression.getOffset());
        this.setExpression(expression);
    }
    
    @Override
    public boolean isEmbeddable() {
        return true;
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitExpressionStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ExpressionStatement && !other.isNull() && this.getExpression().matches(((ExpressionStatement)other).getExpression(), match);
    }
}
