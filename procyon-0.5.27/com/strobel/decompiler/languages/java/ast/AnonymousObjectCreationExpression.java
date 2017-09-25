package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class AnonymousObjectCreationExpression extends ObjectCreationExpression
{
    public AnonymousObjectCreationExpression(final int offset, final TypeDeclaration typeDeclaration, final AstType type) {
        super(offset, type);
        this.setTypeDeclaration(typeDeclaration);
    }
    
    public AnonymousObjectCreationExpression(final int offset, final TypeDeclaration typeDeclaration, final AstType type, final Expression... arguments) {
        super(offset, type, arguments);
        this.setTypeDeclaration(typeDeclaration);
    }
    
    public AnonymousObjectCreationExpression(final int offset, final TypeDeclaration typeDeclaration, final AstType type, final Iterable<Expression> arguments) {
        super(offset, type, arguments);
        this.setTypeDeclaration(typeDeclaration);
    }
    
    public final TypeDeclaration getTypeDeclaration() {
        return this.getChildByRole(Roles.LOCAL_TYPE_DECLARATION);
    }
    
    public final void setTypeDeclaration(final TypeDeclaration value) {
        this.setChildByRole(Roles.LOCAL_TYPE_DECLARATION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitAnonymousObjectCreationExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AnonymousObjectCreationExpression) {
            final AnonymousObjectCreationExpression otherExpression = (AnonymousObjectCreationExpression)other;
            return super.matches(other, match) && this.getTypeDeclaration().matches(otherExpression.getTypeDeclaration(), match);
        }
        return false;
    }
}
