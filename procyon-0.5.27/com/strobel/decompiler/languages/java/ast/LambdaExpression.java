package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class LambdaExpression extends Expression
{
    public static final TokenRole ARROW_ROLE;
    public static final Role<AstNode> BODY_ROLE;
    
    static {
        ARROW_ROLE = new TokenRole("->", 2);
        BODY_ROLE = new Role<AstNode>("Body", AstNode.class, AstNode.NULL);
    }
    
    public LambdaExpression(final int offset) {
        super(offset);
    }
    
    public final AstNodeCollection<ParameterDeclaration> getParameters() {
        return this.getChildrenByRole(Roles.PARAMETER);
    }
    
    public final JavaTokenNode getArrowToken() {
        return this.getChildByRole((Role<JavaTokenNode>)LambdaExpression.ARROW_ROLE);
    }
    
    public final AstNode getBody() {
        return this.getChildByRole(LambdaExpression.BODY_ROLE);
    }
    
    public final void setBody(final AstNode value) {
        this.setChildByRole(LambdaExpression.BODY_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitLambdaExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof LambdaExpression) {
            final LambdaExpression otherLambda = (LambdaExpression)other;
            return this.getParameters().matches(otherLambda.getParameters(), match) && this.getBody().matches(otherLambda.getBody(), match);
        }
        return false;
    }
}
