package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class LocalTypeDeclarationStatement extends Statement
{
    public LocalTypeDeclarationStatement(final int offset, final TypeDeclaration type) {
        super(offset);
        this.setChildByRole(Roles.LOCAL_TYPE_DECLARATION, type);
    }
    
    public final TypeDeclaration getTypeDeclaration() {
        return this.getChildByRole(Roles.LOCAL_TYPE_DECLARATION);
    }
    
    public final void setTypeDeclaration(final TypeDeclaration type) {
        this.setChildByRole(Roles.LOCAL_TYPE_DECLARATION, type);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitLocalTypeDeclarationStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return false;
    }
}
