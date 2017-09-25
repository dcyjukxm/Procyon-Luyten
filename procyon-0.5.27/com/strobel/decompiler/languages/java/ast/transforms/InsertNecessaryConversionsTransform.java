package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.utilities.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class InsertNecessaryConversionsTransform extends ContextTrackingVisitor<Void>
{
    private static final ConvertTypeOptions NO_IMPORT_OPTIONS;
    private static final INode TRUE_NODE;
    private static final INode FALSE_NODE;
    private final JavaResolver _resolver;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    static {
        (NO_IMPORT_OPTIONS = new ConvertTypeOptions()).setAddImports(false);
        TRUE_NODE = new PrimitiveExpression(-34, true);
        FALSE_NODE = new PrimitiveExpression(-34, false);
    }
    
    public InsertNecessaryConversionsTransform(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        super.visitCastExpression(node, data);
        final Expression operand = node.getExpression();
        final ResolveResult targetResult = this._resolver.apply((AstNode)node.getType());
        if (targetResult == null || targetResult.getType() == null) {
            return null;
        }
        final ResolveResult valueResult = this._resolver.apply((AstNode)operand);
        if (valueResult == null || valueResult.getType() == null) {
            return null;
        }
        final ConversionType conversionType = MetadataHelper.getConversionType(targetResult.getType(), valueResult.getType());
        if (conversionType == ConversionType.NONE) {
            this.addCastForAssignment(node.getType(), node.getExpression());
        }
        if (RedundantCastUtility.isCastRedundant(this._resolver, node)) {
            RedundantCastUtility.removeCast(node);
        }
        return null;
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        super.visitMemberReferenceExpression(node, data);
        MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
        if (member == null && node.getParent() != null && node.getRole() == Roles.TARGET_EXPRESSION) {
            member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
        }
        if (member == null) {
            return null;
        }
        final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
        if (astBuilder == null) {
            return null;
        }
        final ResolveResult valueResult = this._resolver.apply((AstNode)node.getTarget());
        TypeReference declaringType = member.getDeclaringType();
        if (valueResult != null && valueResult.getType() != null) {
            if (MetadataHelper.isAssignableFrom(declaringType, valueResult.getType())) {
                return null;
            }
            if (valueResult.getType().isGenericType() && (declaringType.isGenericType() || MetadataHelper.isRawType(declaringType))) {
                final TypeReference asSuper = MetadataHelper.asSuper(declaringType, valueResult.getType());
                if (asSuper != null) {
                    declaringType = asSuper;
                }
            }
        }
        this.addCastForAssignment(astBuilder.convertType(declaringType, InsertNecessaryConversionsTransform.NO_IMPORT_OPTIONS), node.getTarget());
        return null;
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);
        this.addCastForAssignment(node.getLeft(), node.getRight());
        return null;
    }
    
    @Override
    public Void visitVariableDeclaration(final VariableDeclarationStatement node, final Void data) {
        super.visitVariableDeclaration(node, data);
        for (final VariableInitializer initializer : node.getVariables()) {
            this.addCastForAssignment(node, initializer.getInitializer());
        }
        return null;
    }
    
    @Override
    public Void visitReturnStatement(final ReturnStatement node, final Void data) {
        super.visitReturnStatement(node, data);
        final AstNode function = CollectionUtilities.firstOrDefault(node.getAncestors(), Predicates.or(Predicates.instanceOf(MethodDeclaration.class), Predicates.instanceOf(LambdaExpression.class)));
        if (function == null) {
            return null;
        }
        AstType left;
        if (function instanceof MethodDeclaration) {
            left = ((MethodDeclaration)function).getReturnType();
        }
        else {
            final TypeReference expectedType = TypeUtilities.getExpectedTypeByParent(this._resolver, (Expression)function);
            if (expectedType == null) {
                return null;
            }
            final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
            if (astBuilder == null) {
                return null;
            }
            final IMethodSignature method = TypeUtilities.getLambdaSignature((LambdaExpression)function);
            if (method == null) {
                return null;
            }
            left = astBuilder.convertType(method.getReturnType(), InsertNecessaryConversionsTransform.NO_IMPORT_OPTIONS);
        }
        final Expression right = node.getExpression();
        this.addCastForAssignment(left, right);
        return null;
    }
    
    private boolean addCastForAssignment(final AstNode left, final Expression right) {
        final ResolveResult targetResult = this._resolver.apply(left);
        if (targetResult == null || targetResult.getType() == null) {
            return false;
        }
        final ResolveResult valueResult = this._resolver.apply((AstNode)right);
        if (valueResult == null || valueResult.getType() == null) {
            return false;
        }
        final TypeReference unboxedTargetType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(targetResult.getType());
        if (right instanceof PrimitiveExpression && TypeUtilities.isValidPrimitiveLiteralAssignment(unboxedTargetType, ((PrimitiveExpression)right).getValue())) {
            return false;
        }
        final ConversionType conversionType = MetadataHelper.getConversionType(targetResult.getType(), valueResult.getType());
        AstNode replacement = null;
        if (conversionType == ConversionType.EXPLICIT || conversionType == ConversionType.EXPLICIT_TO_UNBOXED) {
            final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
            if (astBuilder == null) {
                return false;
            }
            final ConvertTypeOptions convertTypeOptions = new ConvertTypeOptions();
            convertTypeOptions.setAllowWildcards(false);
            final AstType castToType = astBuilder.convertType(targetResult.getType(), convertTypeOptions);
            replacement = right.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, Expression>() {
                @Override
                public Expression apply(final AstNode e) {
                    return new CastExpression(castToType, right);
                }
            });
        }
        else if (conversionType == ConversionType.NONE) {
            if (valueResult.getType().getSimpleType() == JvmType.Boolean && targetResult.getType().getSimpleType() != JvmType.Boolean && targetResult.getType().getSimpleType().isNumeric()) {
                replacement = this.convertBooleanToNumeric(right);
                if (targetResult.getType().getSimpleType().bitWidth() < 32) {
                    final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                    if (astBuilder != null) {
                        replacement = replacement.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, AstNode>() {
                            @Override
                            public AstNode apply(final AstNode input) {
                                return new CastExpression(astBuilder.convertType(targetResult.getType()), (Expression)input);
                            }
                        });
                    }
                }
            }
            else if (targetResult.getType().getSimpleType() == JvmType.Boolean && valueResult.getType().getSimpleType() != JvmType.Boolean && valueResult.getType().getSimpleType().isNumeric()) {
                replacement = this.convertNumericToBoolean(right, valueResult.getType());
            }
            else {
                final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                if (astBuilder != null) {
                    replacement = right.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, AstNode>() {
                        @Override
                        public AstNode apply(final AstNode input) {
                            return new CastExpression(astBuilder.convertType(BuiltinTypes.Object), right);
                        }
                    });
                }
            }
        }
        if (replacement != null) {
            this.recurse(replacement);
            return true;
        }
        return false;
    }
    
    @Override
    public Void visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
        super.visitUnaryOperatorExpression(node, data);
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[node.getOperator().ordinal()]) {
            case 2: {
                final Expression operand = node.getExpression();
                final ResolveResult result = this._resolver.apply((AstNode)operand);
                if (result != null && result.getType() != null && !TypeUtilities.isBoolean(result.getType()) && MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(result.getType()).getSimpleType().isNumeric()) {
                    final TypeReference comparandType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(result.getType());
                    operand.replaceWith((Function<? super AstNode, ? extends AstNode>)new Function<AstNode, AstNode>() {
                        @Override
                        public AstNode apply(final AstNode input) {
                            return new BinaryOperatorExpression(operand, BinaryOperatorType.INEQUALITY, new PrimitiveExpression(-34, JavaPrimitiveCast.cast(comparandType.getSimpleType(), 0)));
                        }
                    });
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
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[node.getOperator().ordinal()]) {
            case 7:
            case 8:
            case 9:
            case 10:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20: {
                final Expression left = node.getLeft();
                final Expression right = node.getRight();
                final ResolveResult leftResult = this._resolver.apply((AstNode)left);
                final ResolveResult rightResult = this._resolver.apply((AstNode)right);
                if (leftResult != null && TypeUtilities.isBoolean(leftResult.getType())) {
                    this.convertBooleanToNumeric(left);
                }
                if (rightResult != null && TypeUtilities.isBoolean(rightResult.getType())) {
                    this.convertBooleanToNumeric(right);
                    break;
                }
                break;
            }
            case 2:
            case 3:
            case 4: {
                final Expression left = node.getLeft();
                final Expression right = node.getRight();
                final ResolveResult leftResult = this._resolver.apply((AstNode)left);
                final ResolveResult rightResult = this._resolver.apply((AstNode)right);
                if (leftResult != null && leftResult.getType() != null && rightResult != null && rightResult.getType() != null && (TypeUtilities.isBoolean(leftResult.getType()) ^ TypeUtilities.isBoolean(rightResult.getType()))) {
                    if (TypeUtilities.isBoolean(leftResult.getType()) && TypeUtilities.isArithmetic(rightResult.getType())) {
                        final TypeReference comparandType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(rightResult.getType());
                        if (InsertNecessaryConversionsTransform.TRUE_NODE.matches(left)) {
                            ((PrimitiveExpression)left).setValue(JavaPrimitiveCast.cast(comparandType.getSimpleType(), 1));
                            break;
                        }
                        if (InsertNecessaryConversionsTransform.FALSE_NODE.matches(left)) {
                            ((PrimitiveExpression)left).setValue(JavaPrimitiveCast.cast(comparandType.getSimpleType(), 0));
                            break;
                        }
                        this.convertBooleanToNumeric(left);
                        break;
                    }
                    else {
                        if (!TypeUtilities.isArithmetic(leftResult.getType())) {
                            break;
                        }
                        final TypeReference comparandType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(leftResult.getType());
                        if (InsertNecessaryConversionsTransform.TRUE_NODE.matches(right)) {
                            ((PrimitiveExpression)right).setValue(JavaPrimitiveCast.cast(comparandType.getSimpleType(), 1));
                            break;
                        }
                        if (InsertNecessaryConversionsTransform.FALSE_NODE.matches(right)) {
                            ((PrimitiveExpression)right).setValue(JavaPrimitiveCast.cast(comparandType.getSimpleType(), 0));
                            break;
                        }
                        this.convertBooleanToNumeric(right);
                        break;
                    }
                }
                else {
                    final TypeReference expectedType = TypeUtilities.getExpectedTypeByParent(this._resolver, node);
                    if (expectedType == null || !TypeUtilities.isBoolean(expectedType)) {
                        break;
                    }
                    final ResolveResult result = this._resolver.apply((AstNode)node);
                    if (result != null && result.getType() != null && TypeUtilities.isArithmetic(result.getType())) {
                        this.convertNumericToBoolean(node, result.getType());
                        break;
                    }
                    break;
                }
                break;
            }
        }
        return null;
    }
    
    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void data) {
        super.visitIfElseStatement(node, data);
        final Expression condition = node.getCondition();
        final ResolveResult conditionResult = this._resolver.apply((AstNode)condition);
        if (conditionResult != null && TypeUtilities.isArithmetic(conditionResult.getType())) {
            this.convertNumericToBoolean(condition, conditionResult.getType());
        }
        return null;
    }
    
    @Override
    public Void visitConditionalExpression(final ConditionalExpression node, final Void data) {
        super.visitConditionalExpression(node, data);
        final Expression condition = node.getCondition();
        final ResolveResult conditionResult = this._resolver.apply((AstNode)condition);
        if (conditionResult != null && TypeUtilities.isArithmetic(conditionResult.getType())) {
            this.convertNumericToBoolean(condition, conditionResult.getType());
        }
        return null;
    }
    
    private Expression convertNumericToBoolean(final Expression node, final TypeReference type) {
        return node.replaceWith((Function<? super AstNode, ? extends Expression>)new Function<AstNode, Expression>() {
            @Override
            public Expression apply(final AstNode input) {
                return new BinaryOperatorExpression(node, BinaryOperatorType.INEQUALITY, new PrimitiveExpression(-34, JavaPrimitiveCast.cast(MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(type).getSimpleType(), 0)));
            }
        });
    }
    
    private Expression convertBooleanToNumeric(final Expression operand) {
        Expression e = operand;
        boolean invert;
        if (e instanceof UnaryOperatorExpression && ((UnaryOperatorExpression)e).getOperator() == UnaryOperatorType.NOT) {
            final Expression inner = ((UnaryOperatorExpression)e).getExpression();
            inner.remove();
            e.replaceWith(inner);
            e = inner;
            invert = true;
        }
        else {
            invert = false;
        }
        return e.replaceWith((Function<? super AstNode, ? extends Expression>)new Function<AstNode, AstNode>() {
            @Override
            public AstNode apply(final AstNode input) {
                return new ConditionalExpression((Expression)input, new PrimitiveExpression(-34, (int)(invert ? 0 : 1)), new PrimitiveExpression(-34, (int)(invert ? 1 : 0)));
            }
        });
    }
    
    private void recurse(final AstNode replacement) {
        final AstNode parent = replacement.getParent();
        if (parent != null) {
            parent.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
        else {
            replacement.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = InsertNecessaryConversionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
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
        return InsertNecessaryConversionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = InsertNecessaryConversionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
        return InsertNecessaryConversionsTransform.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
