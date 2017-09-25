package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class CaseLabel extends AstNode
{
    public static final TokenRole CASE_KEYWORD_ROLE;
    public static final TokenRole DEFAULT_KEYWORD_ROLE;
    
    static {
        CASE_KEYWORD_ROLE = new TokenRole("case", 1);
        DEFAULT_KEYWORD_ROLE = new TokenRole("default", 1);
    }
    
    public CaseLabel() {
        super();
    }
    
    public CaseLabel(final Expression value) {
        super();
        this.setExpression(value);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    public final JavaTokenNode getColonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.COLON);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitCaseLabel(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof CaseLabel && !other.isNull() && this.getExpression().matches(((CaseLabel)other).getExpression(), match);
    }
}
