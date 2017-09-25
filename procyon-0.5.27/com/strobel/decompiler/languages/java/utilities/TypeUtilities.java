package com.strobel.decompiler.languages.java.utilities;

import com.strobel.annotations.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

public final class TypeUtilities
{
    private static final String OBJECT_DESCRIPTOR = "java/lang/Object";
    private static final String STRING_DESCRIPTOR = "java/lang/String";
    private static final Map<JvmType, Integer> TYPE_TO_RANK_MAP;
    private static final int BYTE_RANK = 1;
    private static final int SHORT_RANK = 2;
    private static final int CHAR_RANK = 3;
    private static final int INT_RANK = 4;
    private static final int LONG_RANK = 5;
    private static final int FLOAT_RANK = 6;
    private static final int DOUBLE_RANK = 7;
    private static final int BOOL_RANK = 10;
    private static final int STRING_RANK = 100;
    private static final int MAX_NUMERIC_RANK = 7;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    static {
        final Map<JvmType, Integer> rankMap = new EnumMap<JvmType, Integer>(JvmType.class);
        rankMap.put(JvmType.Byte, 1);
        rankMap.put(JvmType.Short, 2);
        rankMap.put(JvmType.Character, 3);
        rankMap.put(JvmType.Integer, 4);
        rankMap.put(JvmType.Long, 5);
        rankMap.put(JvmType.Float, 6);
        rankMap.put(JvmType.Double, 7);
        rankMap.put(JvmType.Boolean, 10);
        TYPE_TO_RANK_MAP = Collections.unmodifiableMap(rankMap);
    }
    
    private static int getTypeRank(@NotNull final TypeReference type) {
        final TypeReference unboxedType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type);
        final Integer rank = TypeUtilities.TYPE_TO_RANK_MAP.get(unboxedType.getSimpleType());
        if (rank != null) {
            return rank;
        }
        if (StringUtilities.equals(type.getInternalName(), "java/lang/String")) {
            return 100;
        }
        return Integer.MAX_VALUE;
    }
    
    public static boolean isPrimitive(@Nullable final TypeReference type) {
        return type != null && type.isPrimitive();
    }
    
    public static boolean isPrimitiveOrWrapper(@Nullable final TypeReference type) {
        return type != null && MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).isPrimitive();
    }
    
    public static boolean isBoolean(@Nullable final TypeReference type) {
        return type != null && MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).getSimpleType() == JvmType.Boolean;
    }
    
    public static boolean isArithmetic(@Nullable final TypeReference type) {
        if (type == null) {
            return false;
        }
        final JvmType jvmType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).getSimpleType();
        return jvmType.isNumeric() && jvmType != JvmType.Boolean;
    }
    
    public static boolean isBinaryOperatorApplicable(@NotNull final BinaryOperatorType op, @NotNull final AstType lType, @NotNull final AstType rType, final boolean strict) {
        return isBinaryOperatorApplicable(op, VerifyArgument.notNull(lType, "lType").toTypeReference(), VerifyArgument.notNull(rType, "rType").toTypeReference(), strict);
    }
    
    public static boolean isBinaryOperatorApplicable(@NotNull final BinaryOperatorType op, @Nullable final TypeReference lType, @Nullable final TypeReference rType, final boolean strict) {
        if (lType == null || rType == null) {
            return true;
        }
        VerifyArgument.notNull(op, "op");
        final int lRank = getTypeRank(lType);
        final int rRank = getTypeRank(rType);
        final TypeReference lUnboxed = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(lType);
        final TypeReference rUnboxed = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(rType);
        int resultRank = 10;
        boolean isApplicable = false;
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[op.ordinal()]) {
            case 2:
            case 3:
            case 4: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = ((lRank <= 5 && rRank <= 5) || isBoolean(lUnboxed) || isBoolean(rUnboxed));
                    resultRank = ((lRank <= 5) ? 4 : 10);
                    break;
                }
                break;
            }
            case 5:
            case 6: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = (isBoolean(lType) && isBoolean(rType));
                    break;
                }
                break;
            }
            case 7:
            case 8:
            case 9:
            case 10: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = (lRank <= 7 && rRank <= 7);
                    resultRank = 4;
                    break;
                }
                break;
            }
            case 11:
            case 12: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive() && (lType.isPrimitive() || rType.isPrimitive())) {
                    isApplicable = ((lRank <= 7 && rRank <= 7) || (lRank == 10 && rRank == 10));
                    break;
                }
                if (lType.isPrimitive()) {
                    return MetadataHelper.isConvertible(lType, rType);
                }
                if (rType.isPrimitive()) {
                    return MetadataHelper.isConvertible(rType, lType);
                }
                isApplicable = (MetadataHelper.isConvertible(lType, rType) || MetadataHelper.isConvertible(rType, lType));
                break;
            }
            case 13: {
                if (StringUtilities.equals(lType.getInternalName(), "java/lang/String")) {
                    isApplicable = !rType.isVoid();
                    resultRank = 100;
                    break;
                }
                if (StringUtilities.equals(rType.getInternalName(), "java/lang/String")) {
                    isApplicable = !lType.isVoid();
                    resultRank = 100;
                    break;
                }
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    resultRank = Math.max(lRank, rRank);
                    isApplicable = (lRank <= 7 && rRank <= 7);
                    break;
                }
                break;
            }
            case 14:
            case 15:
            case 16:
            case 17: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    resultRank = Math.max(lRank, rRank);
                    isApplicable = (lRank <= 7 && rRank <= 7);
                    break;
                }
                break;
            }
            case 18:
            case 19:
            case 20: {
                if (lUnboxed.isPrimitive() && rUnboxed.isPrimitive()) {
                    isApplicable = (lRank <= 5 && rRank <= 5);
                    resultRank = 4;
                    break;
                }
                break;
            }
        }
        if (isApplicable && strict) {
            if (resultRank > 7) {
                isApplicable = (lRank == resultRank || StringUtilities.equals(lType.getInternalName(), "java/lang/Object"));
            }
            else {
                isApplicable = (lRank <= 7);
            }
        }
        return isApplicable;
    }
    
    @Nullable
    public static AstNode skipParenthesesUp(final AstNode e) {
        AstNode result;
        for (result = e; result instanceof ParenthesizedExpression; result = result.getParent()) {}
        return result;
    }
    
    @Nullable
    public static AstNode skipParenthesesDown(final AstNode e) {
        AstNode result;
        for (result = e; result instanceof ParenthesizedExpression; result = ((ParenthesizedExpression)result).getExpression()) {}
        return result;
    }
    
    @Nullable
    public static Expression skipParenthesesDown(final Expression e) {
        Expression result;
        for (result = e; result instanceof ParenthesizedExpression; result = ((ParenthesizedExpression)result).getExpression()) {}
        return result;
    }
    
    private static boolean checkSameExpression(final Expression template, final Expression expression) {
        return Comparer.equals(template, skipParenthesesDown(expression));
    }
    
    private static TypeReference getType(@NotNull final Function<AstNode, ResolveResult> resolver, @NotNull final AstNode node) {
        final ResolveResult result = resolver.apply(node);
        return (result != null) ? result.getType() : null;
    }
    
    @Nullable
    public static TypeReference getExpectedTypeByParent(final Function<AstNode, ResolveResult> resolver, final Expression expression) {
        VerifyArgument.notNull(resolver, "resolver");
        VerifyArgument.notNull(expression, "expression");
        final AstNode parent = skipParenthesesUp(expression.getParent());
        if (expression.getRole() == Roles.CONDITION) {
            return CommonTypeReferences.Boolean;
        }
        if (parent instanceof VariableInitializer) {
            if (checkSameExpression(expression, ((VariableInitializer)parent).getInitializer()) && parent.getParent() instanceof VariableDeclarationStatement) {
                return getType(resolver, parent.getParent());
            }
        }
        else if (parent instanceof AssignmentExpression) {
            if (checkSameExpression(expression, ((AssignmentExpression)parent).getRight())) {
                return getType(resolver, ((AssignmentExpression)parent).getLeft());
            }
        }
        else if (parent instanceof ReturnStatement) {
            final LambdaExpression lambdaExpression = CollectionUtilities.firstOrDefault(parent.getAncestors(LambdaExpression.class));
            if (lambdaExpression != null) {
                final DynamicCallSite callSite = lambdaExpression.getUserData(Keys.DYNAMIC_CALL_SITE);
                if (callSite == null) {
                    return null;
                }
                final MethodReference method = callSite.getBootstrapArguments().get(0);
                return method.getDeclaringType();
            }
            else {
                final MethodDeclaration method2 = CollectionUtilities.firstOrDefault(parent.getAncestors(MethodDeclaration.class));
                if (method2 != null) {
                    return getType(resolver, method2.getReturnType());
                }
            }
        }
        else if (parent instanceof ConditionalExpression) {
            if (checkSameExpression(expression, ((ConditionalExpression)parent).getTrueExpression())) {
                return getType(resolver, ((ConditionalExpression)parent).getFalseExpression());
            }
            if (checkSameExpression(expression, ((ConditionalExpression)parent).getFalseExpression())) {
                return getType(resolver, ((ConditionalExpression)parent).getTrueExpression());
            }
        }
        return null;
    }
    
    public static IMethodSignature getLambdaSignature(final MethodGroupExpression node) {
        return getLambdaSignatureCore(node);
    }
    
    public static IMethodSignature getLambdaSignature(final LambdaExpression node) {
        return getLambdaSignatureCore(node);
    }
    
    public static boolean isValidPrimitiveLiteralAssignment(final TypeReference targetType, final Object value) {
        VerifyArgument.notNull(targetType, "targetType");
        if (targetType.getSimpleType() == JvmType.Boolean) {
            return value instanceof Boolean;
        }
        if (!targetType.isPrimitive() || (!(value instanceof Number) && !(value instanceof Character))) {
            return false;
        }
        final Number n = (value instanceof Character) ? ((int)(char)value) : value;
        if (n instanceof Float || n instanceof Double) {
            if (targetType.getSimpleType() == JvmType.Float) {
                return n.doubleValue() >= 1.401298464324817E-45 && n.doubleValue() <= 3.4028234663852886E38;
            }
            return targetType.getSimpleType() == JvmType.Double;
        }
        else if (n instanceof Long) {
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[targetType.getSimpleType().ordinal()]) {
                case 6:
                case 7:
                case 8: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
        else {
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[targetType.getSimpleType().ordinal()]) {
                case 2: {
                    return n.intValue() >= -128 && n.intValue() <= 127;
                }
                case 3: {
                    return n.intValue() >= 0 && n.intValue() <= 65535;
                }
                case 4: {
                    return n.intValue() >= -32768 && n.intValue() <= 32767;
                }
                case 5: {
                    return n.longValue() >= -2147483648L && n.longValue() <= 2147483647L;
                }
                case 6:
                case 7:
                case 8: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
    }
    
    private static IMethodSignature getLambdaSignatureCore(final Expression node) {
        VerifyArgument.notNull(node, "node");
        final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);
        final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);
        if (lambdaType == null) {
            if (callSite == null) {
                return null;
            }
            return callSite.getBootstrapArguments().get(2);
        }
        else {
            final TypeDefinition resolvedType = lambdaType.resolve();
            if (resolvedType != null) {
                MethodReference functionMethod = null;
                final List<MethodReference> methods = MetadataHelper.findMethods(resolvedType, (callSite != null) ? MetadataFilters.matchName(callSite.getMethodName()) : Predicates.alwaysTrue());
                for (final MethodReference m : methods) {
                    final MethodDefinition r = m.resolve();
                    if (r != null && r.isAbstract() && !r.isStatic() && !r.isDefault()) {
                        functionMethod = r;
                        break;
                    }
                }
                if (functionMethod != null) {
                    final TypeReference asMemberOf = MetadataHelper.asSuper(functionMethod.getDeclaringType(), lambdaType);
                    final TypeReference effectiveType = (asMemberOf != null) ? asMemberOf : lambdaType;
                    if (MetadataHelper.isRawType(effectiveType)) {
                        return MetadataHelper.erase(functionMethod);
                    }
                    functionMethod = MetadataHelper.asMemberOf(functionMethod, effectiveType);
                }
                return functionMethod;
            }
            if (callSite == null) {
                return null;
            }
            return callSite.getBootstrapArguments().get(2);
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = TypeUtilities.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return TypeUtilities.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = TypeUtilities.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return TypeUtilities.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
}
