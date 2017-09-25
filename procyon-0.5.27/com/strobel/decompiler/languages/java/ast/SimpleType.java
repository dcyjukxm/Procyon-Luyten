package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import java.util.*;
import com.strobel.decompiler.patterns.*;

public class SimpleType extends AstType
{
    public SimpleType(final String identifier) {
        this(identifier, SimpleType.EMPTY_TYPES);
    }
    
    public SimpleType(final Identifier identifier) {
        super();
        this.setIdentifierToken(identifier);
    }
    
    public SimpleType(final String identifier, final TextLocation location) {
        super();
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(identifier, location));
    }
    
    public SimpleType(final String identifier, final Iterable<AstType> typeArguments) {
        super();
        this.setIdentifier(identifier);
        if (typeArguments != null) {
            for (final AstType typeArgument : typeArguments) {
                this.addChild(typeArgument, Roles.TYPE_ARGUMENT);
            }
        }
    }
    
    public SimpleType(final String identifier, final AstType... typeArguments) {
        super();
        this.setIdentifier(identifier);
        if (typeArguments != null) {
            for (final AstType typeArgument : typeArguments) {
                this.addChild(typeArgument, Roles.TYPE_ARGUMENT);
            }
        }
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
        return (R)visitor.visitSimpleType(this, (Object)data);
    }
    
    @Override
    public String toString() {
        final AstNodeCollection<AstType> typeArguments = this.getTypeArguments();
        if (typeArguments.isEmpty()) {
            return this.getIdentifier();
        }
        final StringBuilder sb = new StringBuilder(this.getIdentifier()).append('<');
        boolean first = true;
        for (final AstType typeArgument : typeArguments) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(typeArgument);
        }
        return sb.append('>').toString();
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SimpleType) {
            final SimpleType otherType = (SimpleType)other;
            return !other.isNull() && AstNode.matchString(this.getIdentifier(), otherType.getIdentifier()) && this.getTypeArguments().matches(otherType.getTypeArguments(), match);
        }
        return false;
    }
}
