package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class BinaryOperatorExpression extends Expression
{
    public static final TokenRole BITWISE_AND_ROLE;
    public static final TokenRole BITWISE_OR_ROLE;
    public static final TokenRole LOGICAL_AND_ROLE;
    public static final TokenRole LOGICAL_OR_ROLE;
    public static final TokenRole EXCLUSIVE_OR_ROLE;
    public static final TokenRole GREATER_THAN_ROLE;
    public static final TokenRole GREATER_THAN_OR_EQUAL_ROLE;
    public static final TokenRole EQUALITY_ROLE;
    public static final TokenRole IN_EQUALITY_ROLE;
    public static final TokenRole LESS_THAN_ROLE;
    public static final TokenRole LESS_THAN_OR_EQUAL_ROLE;
    public static final TokenRole ADD_ROLE;
    public static final TokenRole SUBTRACT_ROLE;
    public static final TokenRole MULTIPLY_ROLE;
    public static final TokenRole DIVIDE_ROLE;
    public static final TokenRole MODULUS_ROLE;
    public static final TokenRole SHIFT_LEFT_ROLE;
    public static final TokenRole SHIFT_RIGHT_ROLE;
    public static final TokenRole UNSIGNED_SHIFT_RIGHT_ROLE;
    public static final TokenRole ANY_ROLE;
    public static final Role<Expression> LEFT_ROLE;
    public static final Role<Expression> RIGHT_ROLE;
    private BinaryOperatorType _operator;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    static {
        BITWISE_AND_ROLE = new TokenRole("&", 2);
        BITWISE_OR_ROLE = new TokenRole("|", 2);
        LOGICAL_AND_ROLE = new TokenRole("&&", 2);
        LOGICAL_OR_ROLE = new TokenRole("||", 2);
        EXCLUSIVE_OR_ROLE = new TokenRole("^", 2);
        GREATER_THAN_ROLE = new TokenRole(">", 2);
        GREATER_THAN_OR_EQUAL_ROLE = new TokenRole(">=", 2);
        EQUALITY_ROLE = new TokenRole("==", 2);
        IN_EQUALITY_ROLE = new TokenRole("!=", 2);
        LESS_THAN_ROLE = new TokenRole("<", 2);
        LESS_THAN_OR_EQUAL_ROLE = new TokenRole("<=", 2);
        ADD_ROLE = new TokenRole("+", 2);
        SUBTRACT_ROLE = new TokenRole("-", 2);
        MULTIPLY_ROLE = new TokenRole("*", 2);
        DIVIDE_ROLE = new TokenRole("/", 2);
        MODULUS_ROLE = new TokenRole("%", 2);
        SHIFT_LEFT_ROLE = new TokenRole("<<", 2);
        SHIFT_RIGHT_ROLE = new TokenRole(">>", 2);
        UNSIGNED_SHIFT_RIGHT_ROLE = new TokenRole(">>>", 2);
        ANY_ROLE = new TokenRole("(op)", 2);
        LEFT_ROLE = new Role<Expression>("Left", Expression.class, Expression.NULL);
        RIGHT_ROLE = new Role<Expression>("Right", Expression.class, Expression.NULL);
    }
    
    public BinaryOperatorExpression(final Expression left, final BinaryOperatorType operator, final Expression right) {
        super(left.getOffset());
        this.setLeft(left);
        this.setOperator(operator);
        this.setRight(right);
    }
    
    public final BinaryOperatorType getOperator() {
        return this._operator;
    }
    
    public final void setOperator(final BinaryOperatorType operator) {
        this.verifyNotFrozen();
        this._operator = operator;
    }
    
    public final JavaTokenNode getOperatorToken() {
        return this.getChildByRole((Role<JavaTokenNode>)getOperatorRole(this.getOperator()));
    }
    
    public final Expression getLeft() {
        return this.getChildByRole(BinaryOperatorExpression.LEFT_ROLE);
    }
    
    public final void setLeft(final Expression value) {
        this.setChildByRole(BinaryOperatorExpression.LEFT_ROLE, value);
    }
    
    public final Expression getRight() {
        return this.getChildByRole(BinaryOperatorExpression.RIGHT_ROLE);
    }
    
    public final void setRight(final Expression value) {
        this.setChildByRole(BinaryOperatorExpression.RIGHT_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitBinaryOperatorExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression otherExpression = (BinaryOperatorExpression)other;
            return !otherExpression.isNull() && (otherExpression._operator == this._operator || this._operator == BinaryOperatorType.ANY || otherExpression._operator == BinaryOperatorType.ANY) && this.getLeft().matches(otherExpression.getLeft(), match) && this.getRight().matches(otherExpression.getRight(), match);
        }
        return false;
    }
    
    public static TokenRole getOperatorRole(final BinaryOperatorType operator) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[operator.ordinal()]) {
            case 2: {
                return BinaryOperatorExpression.BITWISE_AND_ROLE;
            }
            case 3: {
                return BinaryOperatorExpression.BITWISE_OR_ROLE;
            }
            case 5: {
                return BinaryOperatorExpression.LOGICAL_AND_ROLE;
            }
            case 6: {
                return BinaryOperatorExpression.LOGICAL_OR_ROLE;
            }
            case 4: {
                return BinaryOperatorExpression.EXCLUSIVE_OR_ROLE;
            }
            case 7: {
                return BinaryOperatorExpression.GREATER_THAN_ROLE;
            }
            case 8: {
                return BinaryOperatorExpression.GREATER_THAN_OR_EQUAL_ROLE;
            }
            case 11: {
                return BinaryOperatorExpression.EQUALITY_ROLE;
            }
            case 12: {
                return BinaryOperatorExpression.IN_EQUALITY_ROLE;
            }
            case 9: {
                return BinaryOperatorExpression.LESS_THAN_ROLE;
            }
            case 10: {
                return BinaryOperatorExpression.LESS_THAN_OR_EQUAL_ROLE;
            }
            case 13: {
                return BinaryOperatorExpression.ADD_ROLE;
            }
            case 14: {
                return BinaryOperatorExpression.SUBTRACT_ROLE;
            }
            case 15: {
                return BinaryOperatorExpression.MULTIPLY_ROLE;
            }
            case 16: {
                return BinaryOperatorExpression.DIVIDE_ROLE;
            }
            case 17: {
                return BinaryOperatorExpression.MODULUS_ROLE;
            }
            case 18: {
                return BinaryOperatorExpression.SHIFT_LEFT_ROLE;
            }
            case 19: {
                return BinaryOperatorExpression.SHIFT_RIGHT_ROLE;
            }
            case 20: {
                return BinaryOperatorExpression.UNSIGNED_SHIFT_RIGHT_ROLE;
            }
            case 1: {
                return BinaryOperatorExpression.ANY_ROLE;
            }
            default: {
                throw new IllegalArgumentException("Invalid value for BinaryOperatorType.");
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = BinaryOperatorExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[BinaryOperatorType.values().length];
        try {
            loc_1[BinaryOperatorType.ADD.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[BinaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_AND.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_OR.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[BinaryOperatorType.DIVIDE.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[BinaryOperatorType.EQUALITY.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[BinaryOperatorType.EXCLUSIVE_OR.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN_OR_EQUAL.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[BinaryOperatorType.INEQUALITY.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN_OR_EQUAL.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_AND.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_OR.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[BinaryOperatorType.MODULUS.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[BinaryOperatorType.MULTIPLY.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_LEFT.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_RIGHT.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[BinaryOperatorType.SUBTRACT.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[BinaryOperatorType.UNSIGNED_SHIFT_RIGHT.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_21) {}
        return BinaryOperatorExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
