package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class MethodDeclaration extends EntityDeclaration
{
    public static final Role<Expression> DEFAULT_VALUE_ROLE;
    public static final TokenRole DEFAULT_KEYWORD;
    public static final TokenRole THROWS_KEYWORD;
    
    static {
        DEFAULT_VALUE_ROLE = new Role<Expression>("DefaultValue", Expression.class, Expression.NULL);
        DEFAULT_KEYWORD = new TokenRole("default", 1);
        THROWS_KEYWORD = new TokenRole("throws", 1);
    }
    
    public final AstType getPrivateImplementationType() {
        return this.getChildByRole(MethodDeclaration.PRIVATE_IMPLEMENTATION_TYPE_ROLE);
    }
    
    public final void setPrivateImplementationType(final AstType type) {
        this.setChildByRole(MethodDeclaration.PRIVATE_IMPLEMENTATION_TYPE_ROLE, type);
    }
    
    public final Expression getDefaultValue() {
        return this.getChildByRole(MethodDeclaration.DEFAULT_VALUE_ROLE);
    }
    
    public final void setDefaultValue(final Expression value) {
        this.setChildByRole(MethodDeclaration.DEFAULT_VALUE_ROLE, value);
    }
    
    public final AstNodeCollection<AstType> getThrownTypes() {
        return this.getChildrenByRole(Roles.THROWN_TYPE);
    }
    
    public final AstNodeCollection<TypeDeclaration> getDeclaredTypes() {
        return this.getChildrenByRole(Roles.LOCAL_TYPE_DECLARATION);
    }
    
    public final AstNodeCollection<TypeParameterDeclaration> getTypeParameters() {
        return this.getChildrenByRole(Roles.TYPE_PARAMETER);
    }
    
    public final AstNodeCollection<ParameterDeclaration> getParameters() {
        return this.getChildrenByRole(Roles.PARAMETER);
    }
    
    public final BlockStatement getBody() {
        return this.getChildByRole(Roles.BODY);
    }
    
    public final void setBody(final BlockStatement value) {
        this.setChildByRole(Roles.BODY, value);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.METHOD;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitMethodDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MethodDeclaration) {
            final MethodDeclaration otherDeclaration = (MethodDeclaration)other;
            return !otherDeclaration.isNull() && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.matchAnnotationsAndModifiers(otherDeclaration, match) && this.getPrivateImplementationType().matches(otherDeclaration.getPrivateImplementationType(), match) && this.getTypeParameters().matches(otherDeclaration.getTypeParameters(), match) && this.getReturnType().matches(otherDeclaration.getReturnType(), match) && this.getParameters().matches(otherDeclaration.getParameters(), match) && this.getBody().matches(otherDeclaration.getBody(), match);
        }
        return false;
    }
}
