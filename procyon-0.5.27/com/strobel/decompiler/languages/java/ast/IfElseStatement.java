package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class IfElseStatement extends Statement
{
    public static final TokenRole IF_KEYWORD_ROLE;
    public static final TokenRole ELSE_KEYWORD_ROLE;
    public static final Role<Expression> CONDITION_ROLE;
    public static final Role<Statement> TRUE_ROLE;
    public static final Role<Statement> FALSE_ROLE;
    
    static {
        IF_KEYWORD_ROLE = new TokenRole("if", 1);
        ELSE_KEYWORD_ROLE = new TokenRole("else", 1);
        CONDITION_ROLE = Roles.CONDITION;
        TRUE_ROLE = new Role<Statement>("True", Statement.class, Statement.NULL);
        FALSE_ROLE = new Role<Statement>("False", Statement.class, Statement.NULL);
    }
    
    public IfElseStatement(final int offset, final Expression condition, final Statement trueStatement) {
        this(offset, condition, trueStatement, null);
    }
    
    public IfElseStatement(final int offset, final Expression condition, final Statement trueStatement, final Statement falseStatement) {
        super(offset);
        this.setCondition(condition);
        this.setTrueStatement(trueStatement);
        this.setFalseStatement(falseStatement);
    }
    
    public final JavaTokenNode getIfToken() {
        return this.getChildByRole((Role<JavaTokenNode>)IfElseStatement.IF_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getElseToken() {
        return this.getChildByRole((Role<JavaTokenNode>)IfElseStatement.IF_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final Expression getCondition() {
        return this.getChildByRole(IfElseStatement.CONDITION_ROLE);
    }
    
    public final void setCondition(final Expression value) {
        this.setChildByRole(IfElseStatement.CONDITION_ROLE, value);
    }
    
    public final Statement getTrueStatement() {
        return this.getChildByRole(IfElseStatement.TRUE_ROLE);
    }
    
    public final void setTrueStatement(final Statement value) {
        this.setChildByRole(IfElseStatement.TRUE_ROLE, value);
    }
    
    public final Statement getFalseStatement() {
        return this.getChildByRole(IfElseStatement.FALSE_ROLE);
    }
    
    public final void setFalseStatement(final Statement value) {
        this.setChildByRole(IfElseStatement.FALSE_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitIfElseStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IfElseStatement) {
            final IfElseStatement otherStatement = (IfElseStatement)other;
            return !other.isNull() && this.getCondition().matches(otherStatement.getCondition(), match) && this.getTrueStatement().matches(otherStatement.getTrueStatement(), match) && this.getFalseStatement().matches(otherStatement.getFalseStatement(), match);
        }
        return false;
    }
}
