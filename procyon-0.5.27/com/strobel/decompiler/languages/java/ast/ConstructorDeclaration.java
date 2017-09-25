package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class ConstructorDeclaration extends EntityDeclaration
{
    public static final TokenRole THROWS_KEYWORD;
    
    static {
        THROWS_KEYWORD = MethodDeclaration.THROWS_KEYWORD;
    }
    
    public final AstNodeCollection<ParameterDeclaration> getParameters() {
        return this.getChildrenByRole(Roles.PARAMETER);
    }
    
    public final AstNodeCollection<AstType> getThrownTypes() {
        return this.getChildrenByRole(Roles.THROWN_TYPE);
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
        return EntityType.CONSTRUCTOR;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitConstructorDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof MethodDeclaration) {
            final MethodDeclaration otherDeclaration = (MethodDeclaration)other;
            return !otherDeclaration.isNull() && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.matchAnnotationsAndModifiers(otherDeclaration, match) && this.getParameters().matches(otherDeclaration.getParameters(), match) && this.getBody().matches(otherDeclaration.getBody(), match);
        }
        return false;
    }
}
