package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class SimplifyArithmeticExpressionsTransform extends ContextTrackingVisitor<Void>
{
    private final JavaResolver _resolver;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
    
    public SimplifyArithmeticExpressionsTransform(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public Void visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
        super.visitUnaryOperatorExpression(node, data);
        final UnaryOperatorType operator = node.getOperator();
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[operator.ordinal()]) {
            case 4:
            case 5: {
                final boolean minus = operator == UnaryOperatorType.MINUS;
                if (!(node.getExpression() instanceof PrimitiveExpression)) {
                    break;
                }
                final PrimitiveExpression operand = (PrimitiveExpression)node.getExpression();
                if (!(operand.getValue() instanceof Number)) {
                    break;
                }
                boolean isNegative;
                Number negatedValue;
                if (operand.getValue() instanceof Float || operand.getValue() instanceof Double) {
                    final double value = (double)JavaPrimitiveCast.cast(JvmType.Double, operand.getValue());
                    isNegative = (!Double.isNaN(value) && (Double.doubleToRawLongBits(value) & Long.MIN_VALUE) != 0x0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(operand.getValue(), true), -value);
                }
                else {
                    final long value2 = (long)JavaPrimitiveCast.cast(JvmType.Long, operand.getValue());
                    isNegative = (value2 < 0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(operand.getValue(), true), -value2);
                }
                if (minus != isNegative) {
                    break;
                }
                operand.remove();
                node.replaceWith(operand);
                if (isNegative) {
                    operand.setValue(negatedValue);
                    break;
                }
                break;
            }
        }
        return null;
    }
    
    @Override
    public Void visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
        super.visitBinaryOperatorExpression(node, data);
        final BinaryOperatorType operator = node.getOperator();
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[operator.ordinal()]) {
            case 13:
            case 14: {
                final ResolveResult leftResult = this._resolver.apply((AstNode)node.getLeft());
                if (leftResult == null || leftResult.getType() == null || leftResult.getType().isEquivalentTo(CommonTypeReferences.String)) {
                    return null;
                }
                if (!(node.getRight() instanceof PrimitiveExpression)) {
                    break;
                }
                final PrimitiveExpression right = (PrimitiveExpression)node.getRight();
                if (!(right.getValue() instanceof Number)) {
                    break;
                }
                boolean isNegative;
                Number negatedValue;
                if (right.getValue() instanceof Float || right.getValue() instanceof Double) {
                    final double value = (double)JavaPrimitiveCast.cast(JvmType.Double, right.getValue());
                    isNegative = (!Double.isNaN(value) && (Double.doubleToRawLongBits(value) & Long.MIN_VALUE) != 0x0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(right.getValue(), true), -value);
                }
                else {
                    final long value2 = (long)JavaPrimitiveCast.cast(JvmType.Long, right.getValue());
                    isNegative = (value2 < 0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(right.getValue(), true), -value2);
                }
                if (isNegative) {
                    right.setValue(negatedValue);
                    node.setOperator((operator == BinaryOperatorType.ADD) ? BinaryOperatorType.SUBTRACT : BinaryOperatorType.ADD);
                    break;
                }
                break;
            }
            case 4: {
                if (!(node.getRight() instanceof PrimitiveExpression)) {
                    break;
                }
                final Expression left = node.getLeft();
                final PrimitiveExpression right = (PrimitiveExpression)node.getRight();
                if (!(right.getValue() instanceof Number)) {
                    break;
                }
                final long value3 = (long)JavaPrimitiveCast.cast(JvmType.Long, right.getValue());
                if (value3 == -1L) {
                    left.remove();
                    final UnaryOperatorExpression replacement = new UnaryOperatorExpression(UnaryOperatorType.BITWISE_NOT, left);
                    node.replaceWith(replacement);
                    break;
                }
                break;
            }
        }
        return null;
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);
        final AssignmentOperatorType operator = node.getOperator();
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType()[operator.ordinal()]) {
            case 2:
            case 3: {
                final ResolveResult leftResult = this._resolver.apply((AstNode)node.getLeft());
                if (leftResult == null || leftResult.getType() == null || leftResult.getType().isEquivalentTo(CommonTypeReferences.String)) {
                    return null;
                }
                Expression rValue = node.getRight();
                boolean dropCast = false;
                if (rValue instanceof CastExpression) {
                    final CastExpression cast = (CastExpression)rValue;
                    final AstType castType = cast.getType();
                    if (castType != null && !castType.isNull()) {
                        final TypeReference typeReference = castType.getUserData(Keys.TYPE_REFERENCE);
                        if (typeReference != null) {
                            final JvmType jvmType = typeReference.getSimpleType();
                            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[jvmType.ordinal()]) {
                                case 2:
                                case 3:
                                case 4: {
                                    if (cast.getExpression() instanceof PrimitiveExpression) {
                                        rValue = cast.getExpression();
                                        dropCast = true;
                                        break;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
                if (!(rValue instanceof PrimitiveExpression)) {
                    break;
                }
                final PrimitiveExpression right = (PrimitiveExpression)rValue;
                if (!(right.getValue() instanceof Number)) {
                    break;
                }
                boolean isNegative;
                Number negatedValue;
                if (right.getValue() instanceof Float || right.getValue() instanceof Double) {
                    final double value = (double)JavaPrimitiveCast.cast(JvmType.Double, right.getValue());
                    isNegative = (!Double.isNaN(value) && (Double.doubleToRawLongBits(value) & Long.MIN_VALUE) != 0x0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(right.getValue(), true), -value);
                }
                else {
                    final long value2 = (long)JavaPrimitiveCast.cast(JvmType.Long, right.getValue());
                    isNegative = (value2 < 0L);
                    negatedValue = (Number)JavaPrimitiveCast.cast(JvmType.forValue(right.getValue(), true), -value2);
                }
                if (isNegative) {
                    right.setValue(negatedValue);
                    node.setOperator((operator == AssignmentOperatorType.ADD) ? AssignmentOperatorType.SUBTRACT : AssignmentOperatorType.ADD);
                }
                if (dropCast) {
                    rValue.remove();
                    node.setRight(rValue);
                    break;
                }
                break;
            }
        }
        return null;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
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
        return SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JvmType.values().length];
        try {
            loc_1[JvmType.Array.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JvmType.Boolean.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JvmType.Byte.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JvmType.Character.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[JvmType.Double.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[JvmType.Float.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[JvmType.Integer.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[JvmType.Long.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[JvmType.Object.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[JvmType.Short.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[JvmType.TypeVariable.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[JvmType.Void.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[JvmType.Wildcard.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_14) {}
        return SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType() {
        final int[] loc_0 = SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
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
        return SimplifyArithmeticExpressionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType = loc_1;
    }
}
