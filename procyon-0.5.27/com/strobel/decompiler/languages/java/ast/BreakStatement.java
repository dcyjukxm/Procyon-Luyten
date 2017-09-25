package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class BreakStatement extends Statement
{
    public static final TokenRole BREAK_KEYWORD_ROLE;
    
    static {
        BREAK_KEYWORD_ROLE = new TokenRole("break", 1);
    }
    
    public BreakStatement(final int offset) {
        super(offset);
    }
    
    public BreakStatement(final int offset, final String label) {
        super(offset);
        this.setLabel(label);
    }
    
    public final JavaTokenNode getBreakToken() {
        return this.getChildByRole((Role<JavaTokenNode>)BreakStatement.BREAK_KEYWORD_ROLE);
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
        return (R)visitor.visitBreakStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof BreakStatement && AstNode.matchString(this.getLabel(), ((BreakStatement)other).getLabel());
    }
}
