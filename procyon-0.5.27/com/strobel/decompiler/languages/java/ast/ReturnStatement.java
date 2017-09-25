package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ReturnStatement extends Statement
{
    public static final TokenRole RETURN_KEYWORD_ROLE;
    
    static {
        RETURN_KEYWORD_ROLE = new TokenRole("return", 1);
    }
    
    public ReturnStatement(final int offset) {
        super(offset);
    }
    
    public ReturnStatement(final int offset, final Expression returnValue) {
        super(offset);
        this.setExpression(returnValue);
    }
    
    public final JavaTokenNode getReturnToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ReturnStatement.RETURN_KEYWORD_ROLE);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitReturnStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ReturnStatement && !other.isNull() && this.getExpression().matches(((ReturnStatement)other).getExpression(), match);
    }
}
