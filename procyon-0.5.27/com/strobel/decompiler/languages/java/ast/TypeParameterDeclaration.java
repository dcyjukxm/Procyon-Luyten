package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class TypeParameterDeclaration extends AstNode
{
    public static final Role<Annotation> ANNOTATION_ROLE;
    
    static {
        ANNOTATION_ROLE = EntityDeclaration.ANNOTATION_ROLE;
    }
    
    public TypeParameterDeclaration() {
        super();
    }
    
    public TypeParameterDeclaration(final String name) {
        super();
        this.setName(name);
    }
    
    public final AstNodeCollection<Annotation> getAnnotations() {
        return this.getChildrenByRole(TypeParameterDeclaration.ANNOTATION_ROLE);
    }
    
    public final String getName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setName(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final AstType getExtendsBound() {
        return this.getChildByRole(Roles.EXTENDS_BOUND);
    }
    
    public final void setExtendsBound(final AstType value) {
        this.setChildByRole(Roles.EXTENDS_BOUND, value);
    }
    
    public final Identifier getNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setNameToken(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitTypeParameterDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TypeParameterDeclaration) {
            final TypeParameterDeclaration otherDeclaration = (TypeParameterDeclaration)other;
            return !otherDeclaration.isNull() && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.getExtendsBound().matches(otherDeclaration.getExtendsBound(), match) && this.getAnnotations().matches(otherDeclaration.getAnnotations(), match);
        }
        return false;
    }
}
