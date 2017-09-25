package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.patterns.*;

public class ObjectCreationExpression extends Expression
{
    public static final TokenRole NEW_KEYWORD_ROLE;
    
    static {
        NEW_KEYWORD_ROLE = new TokenRole("new", 1);
    }
    
    public ObjectCreationExpression(final int offset, final AstType type) {
        super(offset);
        this.setType(type);
    }
    
    public ObjectCreationExpression(final int offset, final AstType type, final Iterable<Expression> arguments) {
        super(offset);
        this.setType(type);
        if (arguments != null) {
            for (final Expression argument : arguments) {
                this.addChild(argument, Roles.ARGUMENT);
            }
        }
    }
    
    public ObjectCreationExpression(final int offset, final AstType type, final Expression... arguments) {
        super(offset);
        this.setType(type);
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
    
    public final JavaTokenNode getNewToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ObjectCreationExpression.NEW_KEYWORD_ROLE);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final JavaTokenNode getLeftParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_PARENTHESIS);
    }
    
    public final JavaTokenNode getRightParenthesisToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_PARENTHESIS);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitObjectCreationExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ObjectCreationExpression) {
            final ObjectCreationExpression otherExpression = (ObjectCreationExpression)other;
            return !otherExpression.isNull() && this.getTarget().matches(otherExpression.getTarget(), match) && this.getType().matches(otherExpression.getType(), match) && this.getArguments().matches(otherExpression.getArguments(), match);
        }
        return false;
    }
}
