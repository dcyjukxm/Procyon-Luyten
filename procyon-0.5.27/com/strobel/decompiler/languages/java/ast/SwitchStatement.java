package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class SwitchStatement extends Statement
{
    public static final TokenRole SWITCH_KEYWORD_ROLE;
    public static final Role<SwitchSection> SWITCH_SECTION_ROLE;
    
    static {
        SWITCH_KEYWORD_ROLE = new TokenRole("switch", 1);
        SWITCH_SECTION_ROLE = new Role<SwitchSection>("SwitchSection", SwitchSection.class);
    }
    
    public SwitchStatement(final Expression testExpression) {
        super(testExpression.getOffset());
        this.setExpression(testExpression);
    }
    
    public final JavaTokenNode getReturnToken() {
        return this.getChildByRole((Role<JavaTokenNode>)SwitchStatement.SWITCH_KEYWORD_ROLE);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final JavaTokenNode getLeftBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACE);
    }
    
    public final AstNodeCollection<SwitchSection> getSwitchSections() {
        return this.getChildrenByRole(SwitchStatement.SWITCH_SECTION_ROLE);
    }
    
    public final JavaTokenNode getRightBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitSwitchStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof SwitchStatement) {
            final SwitchStatement otherStatement = (SwitchStatement)other;
            return !otherStatement.isNull() && this.getExpression().matches(otherStatement.getExpression(), match) && this.getSwitchSections().matches(otherStatement.getSwitchSections(), match);
        }
        return false;
    }
}
