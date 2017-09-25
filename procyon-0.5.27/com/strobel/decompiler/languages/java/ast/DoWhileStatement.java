package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class DoWhileStatement extends Statement
{
    public static final TokenRole DO_KEYWORD_ROLE;
    public static final TokenRole WHILE_KEYWORD_ROLE;
    
    static {
        DO_KEYWORD_ROLE = new TokenRole("do", 1);
        WHILE_KEYWORD_ROLE = new TokenRole("while", 1);
    }
    
    public DoWhileStatement(final int offset) {
        super(offset);
    }
    
    public final Statement getEmbeddedStatement() {
        return this.getChildByRole(Roles.EMBEDDED_STATEMENT);
    }
    
    public final void setEmbeddedStatement(final Statement value) {
        this.setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }
    
    public final Expression getCondition() {
        return this.getChildByRole(Roles.CONDITION);
    }
    
    public final void setCondition(final Expression value) {
        this.setChildByRole(Roles.CONDITION, value);
    }
    
    public final JavaTokenNode getDoToken() {
        return this.getChildByRole((Role<JavaTokenNode>)DoWhileStatement.DO_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getWhileToken() {
        return this.getChildByRole((Role<JavaTokenNode>)DoWhileStatement.WHILE_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitDoWhileStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof DoWhileStatement) {
            final DoWhileStatement otherStatement = (DoWhileStatement)other;
            return !other.isNull() && this.getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match) && this.getCondition().matches(otherStatement.getCondition(), match);
        }
        return false;
    }
}
