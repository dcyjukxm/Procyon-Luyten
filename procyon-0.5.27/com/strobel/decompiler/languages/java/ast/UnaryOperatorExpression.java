package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class UnaryOperatorExpression extends Expression
{
    public static final TokenRole NOT_ROLE;
    public static final TokenRole BITWISE_NOT_ROLE;
    public static final TokenRole MINUS_ROLE;
    public static final TokenRole PLUS_ROLE;
    public static final TokenRole INCREMENT_ROLE;
    public static final TokenRole DECREMENT_ROLE;
    public static final TokenRole DEREFERENCE_ROLE;
    public static final TokenRole ADDRESS_OF_ROLE;
    private UnaryOperatorType _operator;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    
    static {
        NOT_ROLE = new TokenRole("!");
        BITWISE_NOT_ROLE = new TokenRole("~");
        MINUS_ROLE = new TokenRole("-");
        PLUS_ROLE = new TokenRole("+");
        INCREMENT_ROLE = new TokenRole("++");
        DECREMENT_ROLE = new TokenRole("--");
        DEREFERENCE_ROLE = new TokenRole("*");
        ADDRESS_OF_ROLE = new TokenRole("&");
    }
    
    public UnaryOperatorExpression(final UnaryOperatorType operator, final Expression expression) {
        super(expression.getOffset());
        this.setOperator(operator);
        this.setExpression(expression);
    }
    
    public final UnaryOperatorType getOperator() {
        return this._operator;
    }
    
    public final void setOperator(final UnaryOperatorType operator) {
        this.verifyNotFrozen();
        this._operator = operator;
    }
    
    public final JavaTokenNode getOperatorToken() {
        return this.getChildByRole((Role<JavaTokenNode>)getOperatorRole(this.getOperator()));
    }
    
    public final Expression getExpression() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setExpression(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitUnaryOperatorExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression otherOperator = (UnaryOperatorExpression)other;
            return !otherOperator.isNull() && (otherOperator._operator == this._operator || this._operator == UnaryOperatorType.ANY || otherOperator._operator == UnaryOperatorType.ANY) && this.getExpression().matches(otherOperator.getExpression(), match);
        }
        return false;
    }
    
    public static TokenRole getOperatorRole(final UnaryOperatorType operator) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[operator.ordinal()]) {
            case 2: {
                return UnaryOperatorExpression.NOT_ROLE;
            }
            case 3: {
                return UnaryOperatorExpression.BITWISE_NOT_ROLE;
            }
            case 4: {
                return UnaryOperatorExpression.MINUS_ROLE;
            }
            case 5: {
                return UnaryOperatorExpression.PLUS_ROLE;
            }
            case 6: {
                return UnaryOperatorExpression.INCREMENT_ROLE;
            }
            case 7: {
                return UnaryOperatorExpression.DECREMENT_ROLE;
            }
            case 8: {
                return UnaryOperatorExpression.INCREMENT_ROLE;
            }
            case 9: {
                return UnaryOperatorExpression.DECREMENT_ROLE;
            }
            default: {
                throw new IllegalArgumentException("Invalid value for UnaryOperatorType.");
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = UnaryOperatorExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[UnaryOperatorType.values().length];
        try {
            loc_1[UnaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[UnaryOperatorType.BITWISE_NOT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[UnaryOperatorType.DECREMENT.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[UnaryOperatorType.INCREMENT.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[UnaryOperatorType.MINUS.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[UnaryOperatorType.NOT.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[UnaryOperatorType.PLUS.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[UnaryOperatorType.POST_DECREMENT.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[UnaryOperatorType.POST_INCREMENT.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        return UnaryOperatorExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
}
