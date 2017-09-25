package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class AssignmentExpression extends Expression
{
    public static final Role<Expression> LEFT_ROLE;
    public static final Role<Expression> RIGHT_ROLE;
    public static final TokenRole ASSIGN_ROLE;
    public static final TokenRole ADD_ROLE;
    public static final TokenRole SUBTRACT_ROLE;
    public static final TokenRole MULTIPLY_ROLE;
    public static final TokenRole DIVIDE_ROLE;
    public static final TokenRole MODULUS_ROLE;
    public static final TokenRole SHIFT_LEFT_ROLE;
    public static final TokenRole SHIFT_RIGHT_ROLE;
    public static final TokenRole UNSIGNED_SHIFT_RIGHT_ROLE;
    public static final TokenRole BITWISE_AND_ROLE;
    public static final TokenRole BITWISE_OR_ROLE;
    public static final TokenRole EXCLUSIVE_OR_ROLE;
    public static final TokenRole ANY_ROLE;
    private AssignmentOperatorType _operator;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    static {
        LEFT_ROLE = BinaryOperatorExpression.LEFT_ROLE;
        RIGHT_ROLE = BinaryOperatorExpression.RIGHT_ROLE;
        ASSIGN_ROLE = new TokenRole("=", 2);
        ADD_ROLE = new TokenRole("+=", 2);
        SUBTRACT_ROLE = new TokenRole("-=", 2);
        MULTIPLY_ROLE = new TokenRole("*=", 2);
        DIVIDE_ROLE = new TokenRole("/=", 2);
        MODULUS_ROLE = new TokenRole("%=", 2);
        SHIFT_LEFT_ROLE = new TokenRole("<<=", 2);
        SHIFT_RIGHT_ROLE = new TokenRole(">>=", 2);
        UNSIGNED_SHIFT_RIGHT_ROLE = new TokenRole(">>>=", 2);
        BITWISE_AND_ROLE = new TokenRole("&=", 2);
        BITWISE_OR_ROLE = new TokenRole("|=", 2);
        EXCLUSIVE_OR_ROLE = new TokenRole("^=", 2);
        ANY_ROLE = new TokenRole("(assign)", 2);
    }
    
    public AssignmentExpression(final Expression left, final Expression right) {
        super(left.getOffset());
        this.setLeft(left);
        this.setOperator(AssignmentOperatorType.ASSIGN);
        this.setRight(right);
    }
    
    public AssignmentExpression(final Expression left, final AssignmentOperatorType operator, final Expression right) {
        super(left.getOffset());
        this.setLeft(left);
        this.setOperator(operator);
        this.setRight(right);
    }
    
    public final AssignmentOperatorType getOperator() {
        return this._operator;
    }
    
    public final void setOperator(final AssignmentOperatorType operator) {
        this.verifyNotFrozen();
        this._operator = operator;
    }
    
    public final JavaTokenNode getOperatorToken() {
        return this.getChildByRole((Role<JavaTokenNode>)getOperatorRole(this.getOperator()));
    }
    
    public final Expression getLeft() {
        return this.getChildByRole(AssignmentExpression.LEFT_ROLE);
    }
    
    public final void setLeft(final Expression value) {
        this.setChildByRole(AssignmentExpression.LEFT_ROLE, value);
    }
    
    public final Expression getRight() {
        return this.getChildByRole(AssignmentExpression.RIGHT_ROLE);
    }
    
    public final void setRight(final Expression value) {
        this.setChildByRole(AssignmentExpression.RIGHT_ROLE, value);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitAssignmentExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof AssignmentExpression) {
            final AssignmentExpression otherExpression = (AssignmentExpression)other;
            return !otherExpression.isNull() && (otherExpression._operator == this._operator || this._operator == AssignmentOperatorType.ANY || otherExpression._operator == AssignmentOperatorType.ANY) && this.getLeft().matches(otherExpression.getLeft(), match) && this.getRight().matches(otherExpression.getRight(), match);
        }
        return false;
    }
    
    public static TokenRole getOperatorRole(final AssignmentOperatorType operator) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType()[operator.ordinal()]) {
            case 1: {
                return AssignmentExpression.ASSIGN_ROLE;
            }
            case 2: {
                return AssignmentExpression.ADD_ROLE;
            }
            case 3: {
                return AssignmentExpression.SUBTRACT_ROLE;
            }
            case 4: {
                return AssignmentExpression.MULTIPLY_ROLE;
            }
            case 5: {
                return AssignmentExpression.DIVIDE_ROLE;
            }
            case 6: {
                return AssignmentExpression.MODULUS_ROLE;
            }
            case 7: {
                return AssignmentExpression.SHIFT_LEFT_ROLE;
            }
            case 8: {
                return AssignmentExpression.SHIFT_RIGHT_ROLE;
            }
            case 9: {
                return AssignmentExpression.UNSIGNED_SHIFT_RIGHT_ROLE;
            }
            case 10: {
                return AssignmentExpression.BITWISE_AND_ROLE;
            }
            case 11: {
                return AssignmentExpression.BITWISE_OR_ROLE;
            }
            case 12: {
                return AssignmentExpression.EXCLUSIVE_OR_ROLE;
            }
            case 13: {
                return AssignmentExpression.ANY_ROLE;
            }
            default: {
                throw new IllegalArgumentException("Invalid value for AssignmentOperatorType");
            }
        }
    }
    
    public static BinaryOperatorType getCorrespondingBinaryOperator(final AssignmentOperatorType operator) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType()[operator.ordinal()]) {
            case 1: {
                return null;
            }
            case 2: {
                return BinaryOperatorType.ADD;
            }
            case 3: {
                return BinaryOperatorType.SUBTRACT;
            }
            case 4: {
                return BinaryOperatorType.MULTIPLY;
            }
            case 5: {
                return BinaryOperatorType.DIVIDE;
            }
            case 6: {
                return BinaryOperatorType.MODULUS;
            }
            case 7: {
                return BinaryOperatorType.SHIFT_LEFT;
            }
            case 8: {
                return BinaryOperatorType.SHIFT_RIGHT;
            }
            case 9: {
                return BinaryOperatorType.UNSIGNED_SHIFT_RIGHT;
            }
            case 10: {
                return BinaryOperatorType.BITWISE_AND;
            }
            case 11: {
                return BinaryOperatorType.BITWISE_OR;
            }
            case 12: {
                return BinaryOperatorType.EXCLUSIVE_OR;
            }
            case 13: {
                return BinaryOperatorType.ANY;
            }
            default: {
                return null;
            }
        }
    }
    
    public static AssignmentOperatorType getCorrespondingAssignmentOperator(final BinaryOperatorType operator) {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[operator.ordinal()]) {
            case 13: {
                return AssignmentOperatorType.ADD;
            }
            case 14: {
                return AssignmentOperatorType.SUBTRACT;
            }
            case 15: {
                return AssignmentOperatorType.MULTIPLY;
            }
            case 16: {
                return AssignmentOperatorType.DIVIDE;
            }
            case 17: {
                return AssignmentOperatorType.MODULUS;
            }
            case 18: {
                return AssignmentOperatorType.SHIFT_LEFT;
            }
            case 19: {
                return AssignmentOperatorType.SHIFT_RIGHT;
            }
            case 20: {
                return AssignmentOperatorType.UNSIGNED_SHIFT_RIGHT;
            }
            case 2: {
                return AssignmentOperatorType.BITWISE_AND;
            }
            case 3: {
                return AssignmentOperatorType.BITWISE_OR;
            }
            case 4: {
                return AssignmentOperatorType.EXCLUSIVE_OR;
            }
            case 1: {
                return AssignmentOperatorType.ANY;
            }
            default: {
                return null;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType() {
        final int[] loc_0 = AssignmentExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[AssignmentOperatorType.values().length];
        try {
            loc_1[AssignmentOperatorType.ADD.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AssignmentOperatorType.ANY.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AssignmentOperatorType.ASSIGN.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AssignmentOperatorType.BITWISE_AND.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AssignmentOperatorType.BITWISE_OR.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[AssignmentOperatorType.DIVIDE.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[AssignmentOperatorType.EXCLUSIVE_OR.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[AssignmentOperatorType.MODULUS.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[AssignmentOperatorType.MULTIPLY.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[AssignmentOperatorType.SHIFT_LEFT.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[AssignmentOperatorType.SHIFT_RIGHT.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[AssignmentOperatorType.SUBTRACT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[AssignmentOperatorType.UNSIGNED_SHIFT_RIGHT.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_14) {}
        return AssignmentExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = AssignmentExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return AssignmentExpression.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
