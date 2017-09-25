package com.strobel.decompiler.languages.java.ast.transforms;

import java.util.*;
import com.strobel.decompiler.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class RemoveImplicitBoxingTransform extends ContextTrackingVisitor<Void>
{
    private static final Set<String> BOX_METHODS;
    private static final Set<String> UNBOX_METHODS;
    private final JavaResolver _resolver;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType;
    
    static {
        BOX_METHODS = new HashSet<String>();
        UNBOX_METHODS = new HashSet<String>();
        final String[] boxTypes = { "java/lang/Byte", "java/lang/Short", "java/lang/Integer", "java/lang/Long", "java/lang/Float", "java/lang/Double" };
        final String[] unboxMethods = { "byteValue:()B", "shortValue:()S", "intValue:()I", "longValue:()J", "floatValue:()F", "doubleValue:()D" };
        final String[] boxMethods = { "java/lang/Boolean.valueOf:(Z)Ljava/lang/Boolean;", "java/lang/Character.valueOf:(C)Ljava/lang/Character;", "java/lang/Byte.valueOf:(B)Ljava/lang/Byte;", "java/lang/Short.valueOf:(S)Ljava/lang/Short;", "java/lang/Integer.valueOf:(I)Ljava/lang/Integer;", "java/lang/Long.valueOf:(J)Ljava/lang/Long;", "java/lang/Float.valueOf:(F)Ljava/lang/Float;", "java/lang/Double.valueOf:(D)Ljava/lang/Double;" };
        Collections.addAll(RemoveImplicitBoxingTransform.BOX_METHODS, boxMethods);
        String[] loc_1;
        for (int loc_0 = (loc_1 = boxTypes).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final String boxType = loc_1[loc_2];
            String[] loc_4;
            for (int loc_3 = (loc_4 = unboxMethods).length, loc_5 = 0; loc_5 < loc_3; ++loc_5) {
                final String unboxMethod = loc_4[loc_5];
                RemoveImplicitBoxingTransform.UNBOX_METHODS.add(String.valueOf(boxType) + "." + unboxMethod);
            }
        }
        RemoveImplicitBoxingTransform.UNBOX_METHODS.add("java/lang/Character.charValue:()C");
        RemoveImplicitBoxingTransform.UNBOX_METHODS.add("java/lang/Boolean.booleanValue:()Z");
    }
    
    public RemoveImplicitBoxingTransform(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);
        if (node.getArguments().size() == 1 && node.getTarget() instanceof MemberReferenceExpression) {
            this.removeBoxing(node);
        }
        else {
            this.removeUnboxing(node);
        }
        return null;
    }
    
    private boolean isValidPrimitiveParent(final InvocationExpression node, final AstNode parent) {
        if (parent == null || parent.isNull()) {
            return false;
        }
        if (!(parent instanceof BinaryOperatorExpression)) {
            if (node.getRole() == Roles.ARGUMENT) {
                final MemberReference member = parent.getUserData(Keys.MEMBER_REFERENCE);
                if (member instanceof MethodReference) {
                    final MethodReference method = parent.getUserData(Keys.MEMBER_REFERENCE);
                    if (method == null || MetadataHelper.isOverloadCheckingRequired(method)) {
                        return false;
                    }
                }
            }
            return node.getRole() != Roles.TARGET_EXPRESSION && !(parent instanceof ClassOfExpression) && !(parent instanceof SynchronizedStatement) && !(parent instanceof ThrowStatement);
        }
        final BinaryOperatorExpression binary = (BinaryOperatorExpression)parent;
        if (binary.getLeft() instanceof NullReferenceExpression || binary.getRight() instanceof NullReferenceExpression) {
            return false;
        }
        final ResolveResult leftResult = this._resolver.apply((AstNode)binary.getLeft());
        final ResolveResult rightResult = this._resolver.apply((AstNode)binary.getRight());
        if (leftResult != null && rightResult != null && leftResult.getType() != null && rightResult.getType() != null) {
            if (node == binary.getLeft()) {
                if (!rightResult.getType().isPrimitive()) {
                    return false;
                }
            }
            else if (!leftResult.getType().isPrimitive()) {
                return false;
            }
            return true;
        }
        return false;
    }
    
    private void removeUnboxing(final InvocationExpression e) {
        if (e == null || e.isNull()) {
            return;
        }
        final Expression target = e.getTarget();
        if (!(target instanceof MemberReferenceExpression)) {
            return;
        }
        final MemberReference reference = e.getUserData(Keys.MEMBER_REFERENCE);
        if (!(reference instanceof MethodReference)) {
            return;
        }
        final String key = String.valueOf(reference.getFullName()) + ":" + reference.getSignature();
        if (!RemoveImplicitBoxingTransform.UNBOX_METHODS.contains(key)) {
            return;
        }
        this.performUnboxingRemoval(e, (MemberReferenceExpression)target);
    }
    
    private void removeUnboxingForCondition(final InvocationExpression e, final MemberReferenceExpression target, final ConditionalExpression parent) {
        final boolean leftSide = parent.getTrueExpression().isAncestorOf(e);
        final Expression otherSide = leftSide ? parent.getFalseExpression() : parent.getTrueExpression();
        final ResolveResult otherResult = this._resolver.apply((AstNode)otherSide);
        if (otherResult == null || otherResult.getType() == null || !otherResult.getType().isPrimitive()) {
            return;
        }
        this.performUnboxingRemoval(e, target);
    }
    
    private void performUnboxingRemoval(final InvocationExpression e, final MemberReferenceExpression target) {
        final Expression boxedValue = target.getTarget();
        final MethodReference unboxMethod = e.getUserData(Keys.MEMBER_REFERENCE);
        final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
        boxedValue.remove();
        e.replaceWith(new CastExpression(astBuilder.convertType(unboxMethod.getReturnType()), boxedValue));
    }
    
    private void removeUnboxingForArgument(final InvocationExpression e) {
        final AstNode parent = e.getParent();
        final MemberReference unboxMethod = e.getUserData(Keys.MEMBER_REFERENCE);
        final MemberReference outerBoxMethod = parent.getUserData(Keys.MEMBER_REFERENCE);
        if (!(unboxMethod instanceof MethodReference) || !(outerBoxMethod instanceof MethodReference)) {
            return;
        }
        final String unboxMethodKey = String.valueOf(unboxMethod.getFullName()) + ":" + unboxMethod.getSignature();
        final String boxMethodKey = String.valueOf(outerBoxMethod.getFullName()) + ":" + outerBoxMethod.getSignature();
        if (!RemoveImplicitBoxingTransform.UNBOX_METHODS.contains(unboxMethodKey)) {
            return;
        }
        final Expression boxedValue = ((MemberReferenceExpression)e.getTarget()).getTarget();
        if (!RemoveImplicitBoxingTransform.BOX_METHODS.contains(boxMethodKey) || !(parent instanceof InvocationExpression) || !this.isValidPrimitiveParent((InvocationExpression)parent, parent.getParent())) {
            boxedValue.remove();
            e.replaceWith(boxedValue);
            return;
        }
        final ResolveResult boxedValueResult = this._resolver.apply((AstNode)boxedValue);
        if (boxedValueResult == null || boxedValueResult.getType() == null) {
            return;
        }
        final TypeReference targetType = ((MethodReference)outerBoxMethod).getReturnType();
        final TypeReference sourceType = boxedValueResult.getType();
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[MetadataHelper.getNumericConversionType(targetType, sourceType).ordinal()]) {
            case 1:
            case 2: {}
            case 4: {
                final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                if (astBuilder == null) {
                    return;
                }
                boxedValue.remove();
                final TypeReference castType = ((MethodReference)outerBoxMethod).getParameters().get(0).getParameterType();
                final CastExpression cast = new CastExpression(astBuilder.convertType(castType), boxedValue);
                parent.replaceWith(cast);
            }
            default: {}
        }
    }
    
    private void removeUnboxingForCast(final InvocationExpression e, final MemberReferenceExpression target, final CastExpression parent) {
        final TypeReference targetType = parent.getType().toTypeReference();
        if (targetType == null || !targetType.isPrimitive()) {
            return;
        }
        final Expression boxedValue = target.getTarget();
        final ResolveResult boxedValueResult = this._resolver.apply((AstNode)boxedValue);
        if (boxedValueResult == null || boxedValueResult.getType() == null) {
            return;
        }
        final TypeReference sourceType = boxedValueResult.getType();
        final ConversionType conversionType = MetadataHelper.getNumericConversionType(targetType, sourceType);
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[conversionType.ordinal()]) {
            case 2:
            case 3:
            case 4: {
                boxedValue.remove();
                e.replaceWith(boxedValue);
            }
            default: {}
        }
    }
    
    private void removeBoxing(final InvocationExpression node) {
        if (!this.isValidPrimitiveParent(node, node.getParent())) {
            return;
        }
        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);
        if (!(reference instanceof MethodReference)) {
            return;
        }
        final String key = String.valueOf(reference.getFullName()) + ":" + reference.getSignature();
        if (!RemoveImplicitBoxingTransform.BOX_METHODS.contains(key)) {
            return;
        }
        final AstNodeCollection<Expression> arguments = node.getArguments();
        final Expression underlyingValue = arguments.firstOrNullObject();
        final ResolveResult valueResult = this._resolver.apply((AstNode)underlyingValue);
        if (valueResult == null || valueResult.getType() == null) {
            return;
        }
        final TypeReference sourceType = valueResult.getType();
        final TypeReference targetType = ((MethodReference)reference).getReturnType();
        final ConversionType conversionType = MetadataHelper.getNumericConversionType(targetType, sourceType);
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[conversionType.ordinal()]) {
            case 2: {
                underlyingValue.remove();
                node.replaceWith(underlyingValue);
                break;
            }
            case 3:
            case 4: {
                final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                if (astBuilder == null) {
                    return;
                }
                TypeReference castType;
                if (conversionType == ConversionType.EXPLICIT_TO_UNBOXED) {
                    castType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(targetType);
                }
                else {
                    castType = targetType;
                }
                underlyingValue.remove();
                node.replaceWith(new CastExpression(astBuilder.convertType(castType), underlyingValue));
                break;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType() {
        final int[] loc_0 = RemoveImplicitBoxingTransform.$SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ConversionType.values().length];
        try {
            loc_1[ConversionType.EXPLICIT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ConversionType.EXPLICIT_TO_UNBOXED.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ConversionType.IDENTITY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ConversionType.IMPLICIT.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[ConversionType.NONE.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_6) {}
        return RemoveImplicitBoxingTransform.$SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType = loc_1;
    }
}
