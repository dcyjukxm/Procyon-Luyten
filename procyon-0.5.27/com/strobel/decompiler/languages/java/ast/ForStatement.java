package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ForStatement extends Statement
{
    public static final TokenRole FOR_KEYWORD_ROLE;
    public static final Role<Statement> INITIALIZER_ROLE;
    public static final Role<Statement> ITERATOR_ROLE;
    
    static {
        FOR_KEYWORD_ROLE = new TokenRole("for", 1);
        INITIALIZER_ROLE = new Role<Statement>("Initializer", Statement.class, Statement.NULL);
        ITERATOR_ROLE = new Role<Statement>("Iterator", Statement.class, Statement.NULL);
    }
    
    public ForStatement(final int offset) {
        super(offset);
    }
    
    public final JavaTokenNode getForToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ForStatement.FOR_KEYWORD_ROLE);
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
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final AstNodeCollection<Statement> getInitializers() {
        return this.getChildrenByRole(ForStatement.INITIALIZER_ROLE);
    }
    
    public final AstNodeCollection<Statement> getIterators() {
        return this.getChildrenByRole(ForStatement.ITERATOR_ROLE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitForStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ForStatement) {
            final ForStatement otherStatement = (ForStatement)other;
            return !other.isNull() && this.getInitializers().matches(otherStatement.getInitializers(), match) && this.getCondition().matches(otherStatement.getCondition(), match) && this.getIterators().matches(otherStatement.getIterators(), match) && this.getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }
        return false;
    }
}
