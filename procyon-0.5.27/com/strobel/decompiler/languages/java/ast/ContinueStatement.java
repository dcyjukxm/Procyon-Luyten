package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class ContinueStatement extends Statement
{
    public static final TokenRole CONTINUE_KEYWORD_ROLE;
    
    static {
        CONTINUE_KEYWORD_ROLE = new TokenRole("continue", 1);
    }
    
    public ContinueStatement(final int offset) {
        super(offset);
    }
    
    public ContinueStatement(final int offset, final String label) {
        super(offset);
        this.setLabel(label);
    }
    
    public final JavaTokenNode getContinueToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ContinueStatement.CONTINUE_KEYWORD_ROLE);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    public final String getLabel() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setLabel(final String value) {
        if (StringUtilities.isNullOrEmpty(value)) {
            this.setChildByRole(Roles.IDENTIFIER, Identifier.create(null));
        }
        else {
            this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
        }
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitContinueStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ContinueStatement && AstNode.matchString(this.getLabel(), ((ContinueStatement)other).getLabel());
    }
}
