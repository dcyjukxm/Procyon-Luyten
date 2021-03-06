package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class EnumValueDeclaration extends EntityDeclaration
{
    public final AstNodeCollection<Expression> getArguments() {
        return this.getChildrenByRole(Roles.ARGUMENT);
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.ENUM_VALUE;
    }
    
    public final JavaTokenNode getLeftBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACE);
    }
    
    public final AstNodeCollection<EntityDeclaration> getMembers() {
        return this.getChildrenByRole(Roles.TYPE_MEMBER);
    }
    
    public final JavaTokenNode getRightBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitEnumValueDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof FieldDeclaration) {
            final FieldDeclaration otherDeclaration = (FieldDeclaration)other;
            return !otherDeclaration.isNull() && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.matchAnnotationsAndModifiers(otherDeclaration, match) && this.getReturnType().matches(otherDeclaration.getReturnType(), match) && this.getMembers().matches(this.getMembers(), match);
        }
        return false;
    }
}
