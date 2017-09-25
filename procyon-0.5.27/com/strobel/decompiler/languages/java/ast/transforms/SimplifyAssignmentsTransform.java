package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.functions.*;
import com.strobel.decompiler.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.utilities.*;

public class SimplifyAssignmentsTransform extends ContextTrackingVisitor<AstNode> implements IAstTransform
{
    private static final Function<AstNode, AstNode> NEGATE_FUNCTION;
    private final JavaResolver _resolver;
    private static final PrimitiveExpression TRUE_CONSTANT;
    private static final PrimitiveExpression FALSE_CONSTANT;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    static {
        NEGATE_FUNCTION = new Function<AstNode, AstNode>() {
            @Override
            public AstNode apply(final AstNode n) {
                if (n instanceof UnaryOperatorExpression) {
                    final UnaryOperatorExpression unary = (UnaryOperatorExpression)n;
                    if (unary.getOperator() == UnaryOperatorType.NOT) {
                        final Expression operand = unary.getExpression();
                        operand.remove();
                        return operand;
                    }
                }
                return new UnaryOperatorExpression(UnaryOperatorType.NOT, (Expression)n);
            }
        };
        TRUE_CONSTANT = new PrimitiveExpression(-34, true);
        FALSE_CONSTANT = new PrimitiveExpression(-34, false);
    }
    
    public SimplifyAssignmentsTransform(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public AstNode visitConditionalExpression(final ConditionalExpression node, final Void data) {
        final Expression condition = node.getCondition();
        final Expression trueExpression = node.getTrueExpression();
        final Expression falseExpression = node.getFalseExpression();
        if (SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(trueExpression) && SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(falseExpression)) {
            condition.remove();
            trueExpression.remove();
            falseExpression.remove();
            node.replaceWith(condition);
            return condition.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
        }
        if (SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(trueExpression) && SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(falseExpression)) {
            condition.remove();
            trueExpression.remove();
            falseExpression.remove();
            final Expression negatedCondition = new UnaryOperatorExpression(UnaryOperatorType.NOT, condition);
            node.replaceWith(negatedCondition);
            return negatedCondition.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
        }
        return super.visitConditionalExpression(node, data);
    }
    
    @Override
    public AstNode visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
        final BinaryOperatorType operator = node.getOperator();
        if (operator == BinaryOperatorType.EQUALITY || operator == BinaryOperatorType.INEQUALITY) {
            final Expression left = node.getLeft();
            final Expression right = node.getRight();
            if (SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(left) || SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(left)) {
                if (SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(right) || SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(right)) {
                    return new PrimitiveExpression(node.getOffset(), SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(left) == SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(right) ^ operator == BinaryOperatorType.INEQUALITY);
                }
                final boolean negate = SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(left) ^ operator == BinaryOperatorType.INEQUALITY;
                right.remove();
                final Expression replacement = negate ? new UnaryOperatorExpression(UnaryOperatorType.NOT, right) : right;
                node.replaceWith(replacement);
                return replacement.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
            }
            else if (SimplifyAssignmentsTransform.TRUE_CONSTANT.matches(right) || SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(right)) {
                final boolean negate = SimplifyAssignmentsTransform.FALSE_CONSTANT.matches(right) ^ operator == BinaryOperatorType.INEQUALITY;
                left.remove();
                final Expression replacement = negate ? new UnaryOperatorExpression(UnaryOperatorType.NOT, left) : left;
                node.replaceWith(replacement);
                return replacement.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
            }
        }
        return super.visitBinaryOperatorExpression(node, data);
    }
    
    @Override
    public AstNode visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void _) {
        if (node.getOperator() == UnaryOperatorType.NOT && node.getExpression() instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression)node.getExpression();
            boolean successful = true;
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[binary.getOperator().ordinal()]) {
                case 11: {
                    binary.setOperator(BinaryOperatorType.INEQUALITY);
                    break;
                }
                case 12: {
                    binary.setOperator(BinaryOperatorType.EQUALITY);
                    break;
                }
                case 7: {
                    binary.setOperator(BinaryOperatorType.LESS_THAN_OR_EQUAL);
                    break;
                }
                case 8: {
                    binary.setOperator(BinaryOperatorType.LESS_THAN);
                    break;
                }
                case 9: {
                    binary.setOperator(BinaryOperatorType.GREATER_THAN_OR_EQUAL);
                    break;
                }
                case 10: {
                    binary.setOperator(BinaryOperatorType.GREATER_THAN);
                    break;
                }
                default: {
                    successful = false;
                    break;
                }
            }
            if (successful) {
                node.replaceWith(binary);
                return binary.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, _);
            }
            successful = true;
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[binary.getOperator().ordinal()]) {
                case 5: {
                    binary.setOperator(BinaryOperatorType.LOGICAL_OR);
                    break;
                }
                case 6: {
                    binary.setOperator(BinaryOperatorType.LOGICAL_AND);
                    break;
                }
                default: {
                    successful = false;
                    break;
                }
            }
            if (successful) {
                binary.getLeft().replaceWith(SimplifyAssignmentsTransform.NEGATE_FUNCTION);
                binary.getRight().replaceWith(SimplifyAssignmentsTransform.NEGATE_FUNCTION);
                node.replaceWith(binary);
                return binary.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, _);
            }
        }
        return super.visitUnaryOperatorExpression(node, _);
    }
    
    @Override
    public AstNode visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        final Expression left = node.getLeft();
        final Expression right = node.getRight();
        if (node.getOperator() == AssignmentOperatorType.ASSIGN) {
            if (right instanceof CastExpression) {
                final CastExpression castExpression = (CastExpression)right;
                final TypeReference castType = castExpression.getType().toTypeReference();
                final Expression castedValue = castExpression.getExpression();
                if (castType != null && castType.isPrimitive() && castedValue instanceof BinaryOperatorExpression) {
                    final ResolveResult leftResult = this._resolver.apply((AstNode)left);
                    if (leftResult != null && MetadataResolver.areEquivalent(castType, leftResult.getType()) && this.tryRewriteBinaryAsAssignment(node, left, castedValue)) {
                        final Expression newValue = castExpression.getExpression();
                        newValue.remove();
                        right.replaceWith(newValue);
                        return newValue.acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
                    }
                }
            }
            if (this.tryRewriteBinaryAsAssignment(node, left, right)) {
                return left.getParent().acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
            }
        }
        else if (this.tryRewriteBinaryAsUnary(node, left, right)) {
            return left.getParent().acceptVisitor((IAstVisitor<? super Void, ? extends AstNode>)this, data);
        }
        return super.visitAssignmentExpression(node, data);
    }
    
    private boolean tryRewriteBinaryAsAssignment(final AssignmentExpression node, final Expression left, final Expression right) {
        if (right instanceof BinaryOperatorExpression) {
            final BinaryOperatorExpression binary = (BinaryOperatorExpression)right;
            final Expression innerLeft = binary.getLeft();
            final Expression innerRight = binary.getRight();
            final BinaryOperatorType binaryOp = binary.getOperator();
            if (innerLeft.matches(left)) {
                final AssignmentOperatorType assignOp = AssignmentExpression.getCorrespondingAssignmentOperator(binaryOp);
                if (assignOp != null) {
                    innerRight.remove();
                    right.replaceWith(innerRight);
                    node.setOperator(assignOp);
                    this.tryRewriteBinaryAsUnary(node, node.getLeft(), node.getRight());
                    return true;
                }
            }
            else if (binaryOp.isCommutative() && innerRight.matches(left)) {
                final ResolveResult leftResult = this._resolver.apply((AstNode)left);
                final ResolveResult innerLeftResult = this._resolver.apply((AstNode)innerLeft);
                if (leftResult == null || leftResult.getType() == null || innerLeftResult == null || innerLeftResult.getType() == null) {
                    return false;
                }
                if (CommonTypeReferences.String.isEquivalentTo(leftResult.getType()) || CommonTypeReferences.String.isEquivalentTo(innerLeftResult.getType())) {
                    return false;
                }
                final AssignmentOperatorType assignOp2 = AssignmentExpression.getCorrespondingAssignmentOperator(binaryOp);
                innerLeft.remove();
                right.replaceWith(innerLeft);
                node.setOperator(assignOp2);
                return true;
            }
        }
        return false;
    }
    
    private boolean tryRewriteBinaryAsUnary(final AssignmentExpression node, final Expression left, final Expression right) {
        final AssignmentOperatorType op = node.getOperator();
        if (op == AssignmentOperatorType.ADD || op == AssignmentOperatorType.SUBTRACT) {
            Expression innerRight;
            for (innerRight = right; innerRight instanceof CastExpression && RedundantCastUtility.isCastRedundant(this._resolver, (CastExpression)innerRight); innerRight = ((CastExpression)innerRight).getExpression()) {}
            if (!(innerRight instanceof PrimitiveExpression)) {
                return false;
            }
            final Object value = ((PrimitiveExpression)innerRight).getValue();
            long delta = 0L;
            if (value instanceof Number) {
                final Number n = (Number)value;
                if (value instanceof Float || value instanceof Double) {
                    final double d = n.doubleValue();
                    if (Math.abs(d) == 1.0) {
                        delta = (long)d;
                    }
                }
                else {
                    delta = n.longValue();
                }
            }
            else if (value instanceof Character) {
                delta = (char)value;
            }
            if (Math.abs(delta) == 1L) {
                final boolean increment = delta == 1L ^ op == AssignmentOperatorType.SUBTRACT;
                final UnaryOperatorType unaryOp = increment ? UnaryOperatorType.INCREMENT : UnaryOperatorType.DECREMENT;
                left.remove();
                node.replaceWith(new UnaryOperatorExpression(unaryOp, left));
                return true;
            }
        }
        return false;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = SimplifyAssignmentsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return SimplifyAssignmentsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
