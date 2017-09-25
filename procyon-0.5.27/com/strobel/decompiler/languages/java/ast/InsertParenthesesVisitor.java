package com.strobel.decompiler.languages.java.ast;

import com.strobel.functions.*;
import com.strobel.util.*;

public final class InsertParenthesesVisitor extends DepthFirstAstVisitor<Void, Void>
{
    private static final int PRIMARY = 16;
    private static final int CAST = 15;
    private static final int UNARY = 14;
    private static final int MULTIPLICATIVE = 13;
    private static final int ADDITIVE = 12;
    private static final int SHIFT = 11;
    private static final int RELATIONAL_AND_TYPE_TESTING = 10;
    private static final int EQUALITY = 9;
    private static final int BITWISE_AND = 8;
    private static final int EXCLUSIVE_OR = 7;
    private static final int BITWISE_OR = 6;
    private static final int LOGICAL_AND = 5;
    private static final int LOGICAL_OR = 4;
    private static final int CONDITIONAL = 2;
    private static final int ASSIGNMENT = 1;
    private static final Function<AstNode, AstNode> PARENTHESIZE_FUNCTION;
    private boolean _insertParenthesesForReadability;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    static {
        PARENTHESIZE_FUNCTION = new Function<AstNode, AstNode>() {
            @Override
            public AstNode apply(final AstNode input) {
                return new ParenthesizedExpression((Expression)input);
            }
        };
    }
    
    public InsertParenthesesVisitor() {
        super();
        this._insertParenthesesForReadability = true;
    }
    
    public final boolean getInsertParenthesesForReadability() {
        return this._insertParenthesesForReadability;
    }
    
    public final void setInsertParenthesesForReadability(final boolean insertParenthesesForReadability) {
        this._insertParenthesesForReadability = insertParenthesesForReadability;
    }
    
    private static int getPrecedence(final Expression e) {
        if (e instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression unary = (UnaryOperatorExpression)e;
            if (unary.getOperator() == UnaryOperatorType.POST_DECREMENT || unary.getOperator() == UnaryOperatorType.POST_INCREMENT) {
                return 16;
            }
            return 14;
        }
        else {
            if (e instanceof CastExpression) {
                return 15;
            }
            if (e instanceof BinaryOperatorExpression) {
                final BinaryOperatorExpression binary = (BinaryOperatorExpression)e;
                switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[binary.getOperator().ordinal()]) {
                    case 15:
                    case 16:
                    case 17: {
                        return 13;
                    }
                    case 13:
                    case 14: {
                        return 12;
                    }
                    case 18:
                    case 19:
                    case 20: {
                        return 11;
                    }
                    case 7:
                    case 8:
                    case 9:
                    case 10: {
                        return 10;
                    }
                    case 11:
                    case 12: {
                        return 9;
                    }
                    case 2: {
                        return 8;
                    }
                    case 4: {
                        return 7;
                    }
                    case 3: {
                        return 6;
                    }
                    case 5: {
                        return 5;
                    }
                    case 6: {
                        return 4;
                    }
                    default: {
                        throw ContractUtils.unsupported();
                    }
                }
            }
            else {
                if (e instanceof InstanceOfExpression) {
                    return 10;
                }
                if (e instanceof ConditionalExpression) {
                    return 2;
                }
                if (e instanceof AssignmentExpression || e instanceof LambdaExpression) {
                    return 1;
                }
                return 16;
            }
        }
    }
    
    private static BinaryOperatorType getBinaryOperatorType(final Expression e) {
        if (e instanceof BinaryOperatorExpression) {
            return ((BinaryOperatorExpression)e).getOperator();
        }
        return null;
    }
    
    private static void parenthesizeIfRequired(final Expression expression, final int minimumPrecedence) {
        if (getPrecedence(expression) < minimumPrecedence) {
            parenthesize(expression);
        }
    }
    
    private static void parenthesize(final Expression expression) {
        expression.replaceWith(InsertParenthesesVisitor.PARENTHESIZE_FUNCTION);
    }
    
    private static boolean canTypeBeMisinterpretedAsExpression(final AstType type) {
        return type instanceof SimpleType;
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), 16);
        return super.visitMemberReferenceExpression(node, data);
    }
    
    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), 16);
        return super.visitInvocationExpression(node, data);
    }
    
    @Override
    public Void visitIndexerExpression(final IndexerExpression node, final Void data) {
        parenthesizeIfRequired(node.getTarget(), 16);
        if (node.getTarget() instanceof ArrayCreationExpression) {
            final ArrayCreationExpression arrayCreation = (ArrayCreationExpression)node.getTarget();
            if (this._insertParenthesesForReadability || arrayCreation.getInitializer().isNull()) {
                parenthesize(arrayCreation);
            }
        }
        return super.visitIndexerExpression(node, data);
    }
    
    @Override
    public Void visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
        final Expression child = node.getExpression();
        parenthesizeIfRequired(child, getPrecedence(node));
        if (this._insertParenthesesForReadability && child instanceof UnaryOperatorExpression) {
            parenthesize(child);
        }
        return super.visitUnaryOperatorExpression(node, data);
    }
    
    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        final Expression child = node.getExpression();
        parenthesizeIfRequired(child, 14);
        if (child instanceof UnaryOperatorExpression) {
            final UnaryOperatorExpression childUnary = (UnaryOperatorExpression)child;
            if (childUnary.getOperator() != UnaryOperatorType.BITWISE_NOT && childUnary.getOperator() != UnaryOperatorType.NOT && canTypeBeMisinterpretedAsExpression(node.getType())) {
                parenthesize(child);
            }
        }
        if (child instanceof PrimitiveExpression) {
            final PrimitiveExpression primitive = (PrimitiveExpression)child;
            final Object primitiveValue = primitive.getValue();
            if (primitiveValue instanceof Number) {
                final Number number = (Number)primitiveValue;
                if (primitiveValue instanceof Float || primitiveValue instanceof Double) {
                    if (number.doubleValue() < 0.0) {
                        parenthesize(child);
                    }
                }
                else if (number.longValue() < 0L) {
                    parenthesize(child);
                }
            }
        }
        return super.visitCastExpression(node, data);
    }
    
    @Override
    public Void visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
        final int precedence = getPrecedence(node);
        if (this._insertParenthesesForReadability && precedence < 9) {
            if (getBinaryOperatorType(node.getLeft()) == node.getOperator()) {
                parenthesizeIfRequired(node.getLeft(), precedence);
            }
            else {
                parenthesizeIfRequired(node.getLeft(), 9);
            }
            parenthesizeIfRequired(node.getRight(), 9);
        }
        else {
            parenthesizeIfRequired(node.getLeft(), precedence);
            parenthesizeIfRequired(node.getRight(), precedence + 1);
        }
        return super.visitBinaryOperatorExpression(node, data);
    }
    
    @Override
    public Void visitInstanceOfExpression(final InstanceOfExpression node, final Void data) {
        if (this._insertParenthesesForReadability) {
            parenthesizeIfRequired(node.getExpression(), 16);
        }
        else {
            parenthesizeIfRequired(node.getExpression(), 10);
        }
        return super.visitInstanceOfExpression(node, data);
    }
    
    @Override
    public Void visitConditionalExpression(final ConditionalExpression node, final Void data) {
        if (this._insertParenthesesForReadability) {
            parenthesizeIfRequired(node.getCondition(), 16);
            parenthesizeIfRequired(node.getTrueExpression(), 16);
            parenthesizeIfRequired(node.getFalseExpression(), 16);
        }
        else {
            parenthesizeIfRequired(node.getCondition(), 3);
            parenthesizeIfRequired(node.getTrueExpression(), 2);
            parenthesizeIfRequired(node.getFalseExpression(), 2);
        }
        return super.visitConditionalExpression(node, data);
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        parenthesizeIfRequired(node.getLeft(), 2);
        if (this._insertParenthesesForReadability) {
            parenthesizeIfRequired(node.getRight(), 11);
        }
        else {
            parenthesizeIfRequired(node.getRight(), 1);
        }
        return super.visitAssignmentExpression(node, data);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = InsertParenthesesVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return InsertParenthesesVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
