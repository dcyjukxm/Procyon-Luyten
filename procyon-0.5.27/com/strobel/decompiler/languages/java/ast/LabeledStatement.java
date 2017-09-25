package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class LabeledStatement extends Statement
{
    public LabeledStatement(final int offset) {
        super(offset);
    }
    
    public LabeledStatement(final int offset, final String name) {
        super(offset);
        this.setLabel(name);
    }
    
    public LabeledStatement(final String name, final Statement statement) {
        this(statement.getOffset());
        this.setLabel(name);
        this.setStatement(statement);
    }
    
    public final String getLabel() {
        return this.getChildByRole(Roles.LABEL).getName();
    }
    
    public final void setLabel(final String value) {
        this.setChildByRole(Roles.LABEL, Identifier.create(value));
    }
    
    public final Identifier getLabelToken() {
        return this.getChildByRole(Roles.LABEL);
    }
    
    public final void setLabelToken(final Identifier value) {
        this.setChildByRole(Roles.LABEL, value);
    }
    
    public final JavaTokenNode getColonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.COLON);
    }
    
    public final Statement getStatement() {
        return this.getChildByRole(Roles.EMBEDDED_STATEMENT);
    }
    
    public final void setStatement(final Statement value) {
        this.setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitLabeledStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof LabeledStatement) {
            final LabeledStatement otherStatement = (LabeledStatement)other;
            return AstNode.matchString(this.getLabel(), otherStatement.getLabel()) && this.getStatement().matches(otherStatement.getStatement(), match);
        }
        return false;
    }
}
