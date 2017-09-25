package com.strobel.decompiler.languages.java.ast;

import javax.lang.model.element.*;
import java.util.*;
import com.strobel.decompiler.patterns.*;

public class ForEachStatement extends Statement
{
    public static final TokenRole FOR_KEYWORD_ROLE;
    public static final TokenRole COLON_ROLE;
    
    static {
        FOR_KEYWORD_ROLE = ForStatement.FOR_KEYWORD_ROLE;
        COLON_ROLE = new TokenRole(":", 2);
    }
    
    public ForEachStatement(final int offset) {
        super(offset);
    }
    
    public final JavaTokenNode getForToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ForEachStatement.FOR_KEYWORD_ROLE);
    }
    
    public final Statement getEmbeddedStatement() {
        return this.getChildByRole(Roles.EMBEDDED_STATEMENT);
    }
    
    public final void setEmbeddedStatement(final Statement value) {
        this.setChildByRole(Roles.EMBEDDED_STATEMENT, value);
    }
    
    public final AstType getVariableType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setVariableType(final AstType value) {
        this.setChildByRole(Roles.TYPE, value);
    }
    
    public final String getVariableName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setVariableName(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final Identifier getVariableNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setVariableNameToken(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    public final List<Modifier> getVariableModifiers() {
        return EntityDeclaration.getModifiers(this);
    }
    
    public final void addVariableModifier(final Modifier modifier) {
        EntityDeclaration.addModifier(this, modifier);
    }
    
    public final void removeVariableModifier(final Modifier modifier) {
        EntityDeclaration.removeModifier(this, modifier);
    }
    
    public final void setVariableModifiers(final List<Modifier> modifiers) {
        EntityDeclaration.setModifiers(this, modifiers);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    public final Expression getInExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setInExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitForEachStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ForEachStatement) {
            final ForEachStatement otherStatement = (ForEachStatement)other;
            return !other.isNull() && this.getVariableType().matches(otherStatement.getVariableType(), match) && AstNode.matchString(this.getVariableName(), otherStatement.getVariableName()) && this.getInExpression().matches(otherStatement.getInExpression(), match) && this.getEmbeddedStatement().matches(otherStatement.getEmbeddedStatement(), match);
        }
        return false;
    }
}
