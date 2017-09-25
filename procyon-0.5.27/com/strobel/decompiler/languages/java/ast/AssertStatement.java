package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class AssertStatement extends Statement
{
    public static final TokenRole ASSERT_KEYWORD_ROLE;
    
    static {
        ASSERT_KEYWORD_ROLE = new TokenRole("assert", 1);
    }
    
    public AssertStatement(final int offset) {
        super(offset);
    }
    
    public final JavaTokenNode getColon() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.COLON);
    }
    
    public final Expression getCondition() {
        return this.getChildByRole(Roles.CONDITION);
    }
    
    public final void setCondition(final Expression value) {
        this.setChildByRole(Roles.CONDITION, value);
    }
    
    public final Expression getMessage() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setMessage(final Expression message) {
        this.setChildByRole(Roles.EXPRESSION, message);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitAssertStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AssertStatement) {
            final AssertStatement otherAssert = (AssertStatement)other;
            return this.getCondition().matches(otherAssert.getCondition(), match) && this.getMessage().matches(otherAssert.getMessage());
        }
        return false;
    }
}
