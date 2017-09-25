package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class FieldDeclaration extends EntityDeclaration
{
    public final AstNodeCollection<VariableInitializer> getVariables() {
        return this.getChildrenByRole(Roles.VARIABLE);
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.FIELD;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitFieldDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof FieldDeclaration) {
            final FieldDeclaration otherDeclaration = (FieldDeclaration)other;
            return !otherDeclaration.isNull() && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.matchAnnotationsAndModifiers(otherDeclaration, match) && this.getReturnType().matches(otherDeclaration.getReturnType(), match);
        }
        return false;
    }
}
