package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ArraySpecifier extends AstNode
{
    public final JavaTokenNode getLeftBracketToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACKET);
    }
    
    public final JavaTokenNode getRightBracketToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACKET);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitArraySpecifier(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ArraySpecifier;
    }
    
    @Override
    public String toString() {
        return "[]";
    }
}
