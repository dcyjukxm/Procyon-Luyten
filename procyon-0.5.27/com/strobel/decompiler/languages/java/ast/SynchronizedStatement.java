package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class SynchronizedStatement extends Statement
{
    public static final TokenRole SYNCHRONIZED_KEYWORD_ROLE;
    
    static {
        SYNCHRONIZED_KEYWORD_ROLE = new TokenRole("synchronized", 1);
    }
    
    public SynchronizedStatement(final int offset) {
        super(offset);
    }
    
    public final Statement getEmbeddedStatement() {
        return this.getChildByRole(Roles.EMBEDDED_STATEMENT);
    }
    
    public final void setEmbeddedStatement(final Statement value) {
        this.setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    public final JavaTokenNode getSynchronizedToken() {
        return this.getChildByRole((Role<JavaTokenNode>)SynchronizedStatement.SYNCHRONIZED_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitSynchronizedStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SynchronizedStatement) {
            final SynchronizedStatement otherStatement = (SynchronizedStatement)other;
            return !otherStatement.isNull() && this.getExpression().matches(otherStatement.getExpression(), match) && this.getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }
        return false;
    }
}
