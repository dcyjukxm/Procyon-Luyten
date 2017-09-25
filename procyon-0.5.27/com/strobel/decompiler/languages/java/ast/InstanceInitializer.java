package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class InstanceInitializer extends EntityDeclaration
{
    public final AstNodeCollection<AstType> getThrownTypes() {
        return this.getChildrenByRole(Roles.THROWN_TYPE);
    }
    
    public final AstNodeCollection<TypeDeclaration> getDeclaredTypes() {
        return this.getChildrenByRole(Roles.LOCAL_TYPE_DECLARATION);
    }
    
    public final BlockStatement getBody() {
        return this.getChildByRole(Roles.BODY);
    }
    
    public final void setBody(final BlockStatement value) {
        this.setChildByRole(Roles.BODY, value);
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.METHOD;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitInitializerBlock(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof InstanceInitializer && !other.isNull() && this.getBody().matches(((InstanceInitializer)other).getBody(), match);
    }
}
