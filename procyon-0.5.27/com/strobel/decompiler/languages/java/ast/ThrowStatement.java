package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ThrowStatement extends Statement
{
    public static final TokenRole THROW_KEYWORD_ROLE;
    
    static {
        THROW_KEYWORD_ROLE = new TokenRole("throw", 1);
    }
    
    public ThrowStatement(final Expression expression) {
        super(expression.getOffset());
        this.setExpression(expression);
    }
    
    public final JavaTokenNode getThrowToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ThrowStatement.THROW_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitThrowStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ThrowStatement && !other.isNull() && this.getExpression().matches(((ThrowStatement)other).getExpression(), match);
    }
}
