package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class Annotation extends Expression
{
    private boolean _hasArgumentList;
    
    public Annotation() {
        super(-34);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final boolean hasArgumentList() {
        return this._hasArgumentList;
    }
    
    public final void setHasArgumentList(final boolean hasArgumentList) {
        this.verifyNotFrozen();
        this._hasArgumentList = hasArgumentList;
    }
    
    public final AstNodeCollection<Expression> getArguments() {
        return this.getChildrenByRole(Roles.ARGUMENT);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitAnnotation(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof Annotation) {
            final Annotation otherAnnotation = (Annotation)other;
            return !otherAnnotation.isNull() && this.getType().matches(otherAnnotation.getType(), match) && this.getArguments().matches(otherAnnotation.getArguments(), match);
        }
        return false;
    }
    
    @Override
    public String toString() {
        return this.isNull() ? "Null" : this.getText();
    }
}
