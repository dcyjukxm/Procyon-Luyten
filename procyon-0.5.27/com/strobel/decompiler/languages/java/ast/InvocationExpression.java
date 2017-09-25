package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.patterns.*;

public class InvocationExpression extends Expression
{
    public InvocationExpression(final int offset, final Expression target, final Iterable<Expression> arguments) {
        super(offset);
        this.addChild(target, Roles.TARGET_EXPRESSION);
        if (arguments != null) {
            for (final Expression argument : arguments) {
                this.addChild(argument, Roles.ARGUMENT);
            }
        }
    }
    
    public InvocationExpression(final int offset, final Expression target, final Expression... arguments) {
        super(offset);
        this.addChild(target, Roles.TARGET_EXPRESSION);
        if (arguments != null) {
            for (final Expression argument : arguments) {
                this.addChild(argument, Roles.ARGUMENT);
            }
        }
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    public final AstNodeCollection<Expression> getArguments() {
        return this.getChildrenByRole(Roles.ARGUMENT);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitInvocationExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof InvocationExpression) {
            final InvocationExpression otherExpression = (InvocationExpression)other;
            return this.getTarget().matches(otherExpression.getTarget(), match) && this.getArguments().matches(otherExpression.getArguments(), match);
        }
        return false;
    }
}
