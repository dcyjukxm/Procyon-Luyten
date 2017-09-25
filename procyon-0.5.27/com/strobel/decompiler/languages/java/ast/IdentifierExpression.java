package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class IdentifierExpression extends Expression
{
    public IdentifierExpression(final int offset, final String identifier) {
        super(offset);
        this.setIdentifier(identifier);
    }
    
    public IdentifierExpression(final int offset, final Identifier identifier) {
        super(offset);
        this.setIdentifierToken(identifier);
    }
    
    public final String getIdentifier() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setIdentifier(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final Identifier getIdentifierToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setIdentifierToken(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    public final AstNodeCollection<AstType> getTypeArguments() {
        return this.getChildrenByRole(Roles.TYPE_ARGUMENT);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitIdentifierExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IdentifierExpression) {
            final IdentifierExpression otherIdentifier = (IdentifierExpression)other;
            return !otherIdentifier.isNull() && AstNode.matchString(this.getIdentifier(), otherIdentifier.getIdentifier()) && this.getTypeArguments().matches(otherIdentifier.getTypeArguments(), match);
        }
        return false;
    }
}
