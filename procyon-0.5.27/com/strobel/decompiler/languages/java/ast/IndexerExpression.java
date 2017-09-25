package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class IndexerExpression extends Expression
{
    public IndexerExpression(final int offset, final Expression target, final Expression argument) {
        super(offset);
        this.setTarget(target);
        this.setArgument(argument);
    }
    
    public final Expression getTarget() {
        return this.getChildByRole(Roles.TARGET_EXPRESSION);
    }
    
    public final void setTarget(final Expression value) {
        this.setChildByRole(Roles.TARGET_EXPRESSION, value);
    }
    
    public final Expression getArgument() {
        return this.getChildByRole(Roles.ARGUMENT);
    }
    
    public final void setArgument(final Expression value) {
        this.setChildByRole(Roles.ARGUMENT, value);
    }
    
    public final JavaTokenNode getLeftBracketToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACKET);
    }
    
    public final JavaTokenNode getRightBracketToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACKET);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitIndexerExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof IndexerExpression) {
            final IndexerExpression otherIndexer = (IndexerExpression)other;
            return !otherIndexer.isNull() && this.getTarget().matches(otherIndexer.getTarget(), match) && this.getArgument().matches(otherIndexer.getArgument(), match);
        }
        return false;
    }
}
