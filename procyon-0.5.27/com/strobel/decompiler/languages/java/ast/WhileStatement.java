package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class WhileStatement extends Statement
{
    public static final TokenRole WHILE_KEYWORD_ROLE;
    
    static {
        WHILE_KEYWORD_ROLE = new TokenRole("while", 1);
    }
    
    public WhileStatement(final int offset) {
        super(offset);
    }
    
    public WhileStatement(final Expression condition) {
        super(condition.getOffset());
        this.setCondition(condition);
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
    
    public final JavaTokenNode getWhileToken() {
        return this.getChildByRole((Role<JavaTokenNode>)WhileStatement.WHILE_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitWhileStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof WhileStatement) {
            final WhileStatement otherStatement = (WhileStatement)other;
            return !other.isNull() && this.getCondition().matches(otherStatement.getCondition(), match) && this.getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }
        return false;
    }
}
