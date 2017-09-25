package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class TypeReferenceExpression extends Expression
{
    public TypeReferenceExpression(final int offset, final AstType type) {
        super(offset);
        this.addChild((AstType)VerifyArgument.notNull((T)type, "type"), Roles.TYPE);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitTypeReference(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof TypeReferenceExpression && !other.isNull() && this.getType().matches(((TypeReferenceExpression)other).getType(), match);
    }
    
    @Override
    public boolean isReference() {
        return true;
    }
}
