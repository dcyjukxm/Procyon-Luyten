package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ConditionalExpression extends Expression
{
    public static final Role<Expression> CONDITION_ROLE;
    public static final TokenRole QUESTION_MARK_ROLE;
    public static final Role<Expression> TRUE_ROLE;
    public static final TokenRole COLON_ROLE;
    public static final Role<Expression> FALSE_ROLE;
    
    static {
        CONDITION_ROLE = Roles.CONDITION;
        QUESTION_MARK_ROLE = new TokenRole("?", 2);
        TRUE_ROLE = new Role<Expression>("True", Expression.class, Expression.NULL);
        COLON_ROLE = new TokenRole(":", 2);
        FALSE_ROLE = new Role<Expression>("False", Expression.class, Expression.NULL);
    }
    
    public ConditionalExpression(final Expression condition, final Expression trueExpression, final Expression falseExpression) {
        super(condition.getOffset());
        this.addChild(condition, ConditionalExpression.CONDITION_ROLE);
        this.addChild(trueExpression, ConditionalExpression.TRUE_ROLE);
        this.addChild(falseExpression, ConditionalExpression.FALSE_ROLE);
    }
    
    public final JavaTokenNode getQuestionMark() {
        return this.getChildByRole((Role<JavaTokenNode>)ConditionalExpression.QUESTION_MARK_ROLE);
    }
    
    public final JavaTokenNode getColonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ConditionalExpression.COLON_ROLE);
    }
    
    public final Expression getCondition() {
        return this.getChildByRole(ConditionalExpression.CONDITION_ROLE);
    }
    
    public final void setCondition(final Expression value) {
        this.setChildByRole(ConditionalExpression.CONDITION_ROLE, value);
    }
    
    public final Expression getTrueExpression() {
        return this.getChildByRole(ConditionalExpression.TRUE_ROLE);
    }
    
    public final void setTrueExpression(final Expression value) {
        this.setChildByRole(ConditionalExpression.TRUE_ROLE, value);
    }
    
    public final Expression getFalseExpression() {
        return this.getChildByRole(ConditionalExpression.FALSE_ROLE);
    }
    
    public final void setFalseExpression(final Expression value) {
        this.setChildByRole(ConditionalExpression.FALSE_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitConditionalExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ConditionalExpression) {
            final ConditionalExpression otherCondition = (ConditionalExpression)other;
            return !other.isNull() && this.getCondition().matches(otherCondition.getCondition(), match) && this.getTrueExpression().matches(otherCondition.getTrueExpression(), match) && this.getFalseExpression().matches(otherCondition.getFalseExpression(), match);
        }
        return false;
    }
}
