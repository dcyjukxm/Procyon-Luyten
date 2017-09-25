package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class LabelStatement extends Statement
{
    public LabelStatement(final int offset, final String name) {
        super(offset);
        this.setLabel(name);
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
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitLabelStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof LabelStatement && AstNode.matchString(this.getLabel(), ((LabelStatement)other).getLabel());
    }
}
