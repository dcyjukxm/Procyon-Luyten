package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class InstanceOfExpression extends Expression
{
    public static final TokenRole INSTANCE_OF_KEYWORD_ROLE;
    
    static {
        INSTANCE_OF_KEYWORD_ROLE = new TokenRole("instanceof", 3);
    }
    
    public InstanceOfExpression(final int offset, final Expression expression, final AstType type) {
        super(offset);
        this.setExpression(expression);
        this.setType(type);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final JavaTokenNode getInstanceOfToken() {
        return this.getChildByRole((Role<JavaTokenNode>)InstanceOfExpression.INSTANCE_OF_KEYWORD_ROLE);
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitInstanceOfExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof InstanceOfExpression) {
            final InstanceOfExpression otherExpression = (InstanceOfExpression)other;
            return !otherExpression.isNull() && this.getExpression().matches(otherExpression.getExpression(), match) && this.getType().matches(otherExpression.getType(), match);
        }
        return false;
    }
}
