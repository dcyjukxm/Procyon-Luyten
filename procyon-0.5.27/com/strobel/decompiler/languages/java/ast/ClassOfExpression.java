package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public final class ClassOfExpression extends Expression
{
    public static final TokenRole ClassKeywordRole;
    
    static {
        ClassKeywordRole = new TokenRole("class", 1);
    }
    
    public ClassOfExpression(final int offset, final AstType type) {
        super(offset);
        this.addChild(type, Roles.TYPE);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final JavaTokenNode getDotToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.DOT);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitClassOfExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ClassOfExpression && this.getType().matches(((ClassOfExpression)other).getType(), match);
    }
    
    @Override
    public boolean isReference() {
        return true;
    }
}
