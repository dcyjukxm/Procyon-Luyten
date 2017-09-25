package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class MethodGroupExpression extends Expression
{
    public static final Role<Expression> CLOSURE_ARGUMENT_RULE;
    public static final TokenRole DOUBLE_COLON_ROLE;
    
    static {
        CLOSURE_ARGUMENT_RULE = new Role<Expression>("ClosureArgument", Expression.class, Expression.NULL);
        DOUBLE_COLON_ROLE = new TokenRole("::", 2);
    }
    
    public MethodGroupExpression(final int offset, final Expression target, final String methodName) {
        super(offset);
        this.setTarget(target);
        this.setMethodName(methodName);
    }
    
    public final AstNodeCollection<Expression> getClosureArguments() {
        return this.getChildrenByRole(MethodGroupExpression.CLOSURE_ARGUMENT_RULE);
    }
    
    public final JavaTokenNode getDoubleColonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)MethodGroupExpression.DOUBLE_COLON_ROLE);
    }
    
    public final String getMethodName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setMethodName(final String name) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(name));
    }
    
    public final Identifier getMethodNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setMethodNameToken(final Identifier token) {
        this.setChildByRole(Roles.IDENTIFIER, token);
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitMethodGroupExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return false;
    }
    
    @Override
    public boolean isReference() {
        return true;
    }
}
