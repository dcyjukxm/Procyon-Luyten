package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class GotoStatement extends Statement
{
    public static final TokenRole GOTO_KEYWORD_ROLE;
    
    static {
        GOTO_KEYWORD_ROLE = new TokenRole("goto", 1);
    }
    
    public GotoStatement(final int offset, final String label) {
        super(offset);
        this.setLabel(label);
    }
    
    public final JavaTokenNode getGotoToken() {
        return this.getChildByRole((Role<JavaTokenNode>)GotoStatement.GOTO_KEYWORD_ROLE);
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
        return (R)visitor.visitGotoStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof GotoStatement && AstNode.matchString(this.getLabel(), ((GotoStatement)other).getLabel());
    }
}
