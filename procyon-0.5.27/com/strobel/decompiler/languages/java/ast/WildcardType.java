package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class WildcardType extends AstType
{
    public static final TokenRole WILDCARD_TOKEN_ROLE;
    public static final TokenRole EXTENDS_KEYWORD_ROLE;
    public static final TokenRole SUPER_KEYWORD_ROLE;
    
    static {
        WILDCARD_TOKEN_ROLE = new TokenRole("?");
        EXTENDS_KEYWORD_ROLE = Roles.EXTENDS_KEYWORD;
        SUPER_KEYWORD_ROLE = new TokenRole("super", 1);
    }
    
    public final JavaTokenNode getWildcardToken() {
        return this.getChildByRole((Role<JavaTokenNode>)WildcardType.WILDCARD_TOKEN_ROLE);
    }
    
    public final AstNodeCollection<AstType> getExtendsBounds() {
        return this.getChildrenByRole(Roles.EXTENDS_BOUND);
    }
    
    public final AstNodeCollection<AstType> getSuperBounds() {
        return this.getChildrenByRole(Roles.SUPER_BOUND);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitWildcardType(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof WildcardType) {
            final WildcardType otherWildcard = (WildcardType)other;
            return this.getExtendsBounds().matches(otherWildcard.getExtendsBounds(), match) && this.getSuperBounds().matches(otherWildcard.getSuperBounds(), match);
        }
        return false;
    }
}
