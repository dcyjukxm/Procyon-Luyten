package com.strobel.decompiler.languages.java.ast;

import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.*;
import com.strobel.decompiler.ast.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public class JavaResolver implements Function<AstNode, ResolveResult>
{
    private final DecompilerContext _context;
    
    public JavaResolver(final DecompilerContext context) {
        super();
        this._context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public ResolveResult apply(final AstNode input) {
        return input.acceptVisitor((IAstVisitor<? super Object, ? extends ResolveResult>)new ResolveVisitor(this._context), (Object)null);
    }
    
    private static ResolveResult resolveTypeFromVariable(final Variable variable) {
        if (variable == null) {
            return null;
        }
        TypeReference type = variable.getType();
        if (type == null) {
            type = (variable.isParameter() ? variable.getOriginalParameter().getParameterType() : variable.getOriginalVariable().getVariableType());
        }
        if (type != null) {
            return new ResolveResult(type);
        }
        return null;
    }
    
    private static ResolveResult resolveType(final AstType type) {
        if (type == null || type.isNull()) {
            return null;
        }
        return resolveType(type.toTypeReference());
    }
    
    private static ResolveResult resolveType(final TypeReference type) {
        return (type == null) ? null : new ResolveResult(type);
    }
    
    private static ResolveResult resolveTypeFromMember(final MemberReference member) {
        if (member == null) {
            return null;
        }
        if (member instanceof FieldReference) {
            return new ResolveResult(((FieldReference)member).getFieldType());
        }
        if (!(member instanceof MethodReference)) {
            return null;
        }
        final MethodReference method = (MethodReference)member;
        if (method.isConstructor()) {
            return new ResolveResult(method.getDeclaringType());
        }
        return new ResolveResult(method.getReturnType());
    }
    
    static /* synthetic */ ResolveResult access$1(final AstType param_0) {
        return resolveType(param_0);
    }
    
    static /* synthetic */ ResolveResult access$2(final MemberReference param_0) {
        return resolveTypeFromMember(param_0);
    }
    
    static /* synthetic */ ResolveResult access$3(final TypeReference param_0) {
        return resolveType(param_0);
    }
    
    static /* synthetic */ ResolveResult access$4(final Variable param_0) {
        return resolveTypeFromVariable(param_0);
    }
    
    private static final class ResolveVisitor extends ContextTrackingVisitor<ResolveResult>
    {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
        
        protected ResolveVisitor(final DecompilerContext context) {
            super(context);
        }
        
        @Override
        public ResolveResult visitVariableDeclaration(final VariableDeclarationStatement node, final Void data) {
            return JavaResolver.access$1(node.getType());
        }
        
        @Override
        public ResolveResult visitVariableInitializer(final VariableInitializer node, final Void data) {
            return node.getInitializer().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
        }
        
        @Override
        public ResolveResult visitObjectCreationExpression(final ObjectCreationExpression node, final Void _) {
            return node.getType().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, _);
        }
        
        @Override
        public ResolveResult visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void _) {
            final ResolveResult result = JavaResolver.access$2(node.getUserData(Keys.MEMBER_REFERENCE));
            if (result != null) {
                return result;
            }
            return node.getType().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, _);
        }
        
        @Override
        public ResolveResult visitComposedType(final ComposedType node, final Void _) {
            return JavaResolver.access$3(node.toTypeReference());
        }
        
        @Override
        public ResolveResult visitSimpleType(final SimpleType node, final Void _) {
            return JavaResolver.access$3(node.toTypeReference());
        }
        
        @Override
        public ResolveResult visitThisReferenceExpression(final ThisReferenceExpression node, final Void data) {
            if (node.getTarget().isNull()) {
                return JavaResolver.access$3(node.getUserData(Keys.TYPE_REFERENCE));
            }
            return node.getTarget().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
        }
        
        @Override
        public ResolveResult visitSuperReferenceExpression(final SuperReferenceExpression node, final Void data) {
            if (node.getTarget().isNull()) {
                return JavaResolver.access$3(node.getUserData(Keys.TYPE_REFERENCE));
            }
            return node.getTarget().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
        }
        
        @Override
        public ResolveResult visitTypeReference(final TypeReferenceExpression node, final Void _) {
            return JavaResolver.access$3(node.getType().getUserData(Keys.TYPE_REFERENCE));
        }
        
        @Override
        public ResolveResult visitWildcardType(final WildcardType node, final Void _) {
            return JavaResolver.access$3(node.toTypeReference());
        }
        
        @Override
        public ResolveResult visitIdentifier(final Identifier node, final Void _) {
            final ResolveResult result = JavaResolver.access$2(node.getUserData(Keys.MEMBER_REFERENCE));
            if (result != null) {
                return result;
            }
            return JavaResolver.access$4(node.getUserData(Keys.VARIABLE));
        }
        
        @Override
        public ResolveResult visitIdentifierExpression(final IdentifierExpression node, final Void data) {
            ResolveResult result = JavaResolver.access$2(node.getUserData(Keys.MEMBER_REFERENCE));
            if (result != null) {
                return result;
            }
            final Variable variable = node.getUserData(Keys.VARIABLE);
            if (variable == null) {
                return null;
            }
            result = JavaResolver.access$4(variable);
            if (result != null) {
                return result;
            }
            return super.visitIdentifierExpression(node, data);
        }
        
        protected ResolveResult resolveLambda(final AstNode node) {
            final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);
            if (lambdaType != null) {
                return JavaResolver.access$3(lambdaType);
            }
            final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);
            if (callSite != null) {
                return JavaResolver.access$3(callSite.getMethodType().getReturnType());
            }
            return null;
        }
        
        @Override
        public ResolveResult visitMethodGroupExpression(final MethodGroupExpression node, final Void data) {
            return this.resolveLambda(node);
        }
        
        @Override
        public ResolveResult visitLambdaExpression(final LambdaExpression node, final Void data) {
            return this.resolveLambda(node);
        }
        
        @Override
        public ResolveResult visitMemberReferenceExpression(final MemberReferenceExpression node, final Void _) {
            final ResolveResult targetResult = node.getTarget().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, _);
            MemberReference memberReference = node.getUserData(Keys.MEMBER_REFERENCE);
            if (memberReference == null) {
                if (StringUtilities.equals(node.getMemberName(), "length") && targetResult != null && targetResult.getType() != null && targetResult.getType().isArray()) {
                    return new ResolveResult(BuiltinTypes.Integer);
                }
                if (node.getParent() instanceof InvocationExpression) {
                    memberReference = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
                }
            }
            else if (targetResult != null && targetResult.getType() != null) {
                if (memberReference instanceof FieldReference) {
                    final FieldDefinition resolvedField = ((FieldReference)memberReference).resolve();
                    memberReference = MetadataHelper.asMemberOf((resolvedField != null) ? resolvedField : ((FieldReference)memberReference), targetResult.getType());
                }
                else {
                    final MethodDefinition resolvedMethod = ((MethodReference)memberReference).resolve();
                    memberReference = MetadataHelper.asMemberOf((resolvedMethod != null) ? resolvedMethod : ((MethodReference)memberReference), targetResult.getType());
                }
            }
            return JavaResolver.access$2(memberReference);
        }
        
        @Override
        public ResolveResult visitInvocationExpression(final InvocationExpression node, final Void _) {
            final ResolveResult result = JavaResolver.access$2(node.getUserData(Keys.MEMBER_REFERENCE));
            if (result != null) {
                return result;
            }
            return node.getTarget().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, _);
        }
        
        @Override
        protected ResolveResult visitChildren(final AstNode node, final Void _) {
            ResolveResult result = null;
            AstNode next;
            for (AstNode child = node.getFirstChild(); child != null; child = next) {
                next = child.getNextSibling();
                if (!(child instanceof JavaTokenNode)) {
                    final ResolveResult childResult = child.acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, _);
                    if (childResult == null) {
                        return null;
                    }
                    if (result == null) {
                        result = childResult;
                    }
                    else if (!result.isCompileTimeConstant() || !childResult.isCompileTimeConstant() || !Comparer.equals(result.getConstantValue(), childResult.getConstantValue())) {
                        final TypeReference commonSuperType = this.doBinaryPromotion(result, childResult);
                        if (commonSuperType == null) {
                            return null;
                        }
                        result = new ResolveResult(commonSuperType);
                    }
                }
            }
            return null;
        }
        
        private TypeReference doBinaryPromotion(final ResolveResult left, final ResolveResult right) {
            final TypeReference leftType = left.getType();
            final TypeReference rightType = right.getType();
            if (leftType == null) {
                return rightType;
            }
            if (rightType == null) {
                return leftType;
            }
            if (StringUtilities.equals(leftType.getInternalName(), "java/lang/String")) {
                return leftType;
            }
            if (StringUtilities.equals(rightType.getInternalName(), "java/lang/String")) {
                return rightType;
            }
            return MetadataHelper.findCommonSuperType(leftType, rightType);
        }
        
        private TypeReference doBinaryPromotionStrict(final ResolveResult left, final ResolveResult right) {
            if (left == null || right == null) {
                return null;
            }
            TypeReference leftType = left.getType();
            TypeReference rightType = right.getType();
            if (leftType == null || rightType == null) {
                return null;
            }
            leftType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(leftType);
            rightType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(rightType);
            if (StringUtilities.equals(leftType.getInternalName(), "java/lang/String")) {
                return leftType;
            }
            if (StringUtilities.equals(rightType.getInternalName(), "java/lang/String")) {
                return rightType;
            }
            return MetadataHelper.findCommonSuperType(leftType, rightType);
        }
        
        @Override
        public ResolveResult visitPrimitiveExpression(final PrimitiveExpression node, final Void _) {
            final String literalValue = node.getLiteralValue();
            final Object value = node.getValue();
            TypeReference primitiveType;
            if (value instanceof String || (value == null && literalValue != null)) {
                final TypeDefinition currentType = this.context.getCurrentType();
                final IMetadataResolver resolver = (currentType != null) ? currentType.getResolver() : MetadataSystem.instance();
                primitiveType = resolver.lookupType("java/lang/String");
            }
            else if (value instanceof Number) {
                if (value instanceof Byte) {
                    primitiveType = BuiltinTypes.Byte;
                }
                else if (value instanceof Short) {
                    primitiveType = BuiltinTypes.Short;
                }
                else if (value instanceof Integer) {
                    primitiveType = BuiltinTypes.Integer;
                }
                else if (value instanceof Long) {
                    primitiveType = BuiltinTypes.Long;
                }
                else if (value instanceof Float) {
                    primitiveType = BuiltinTypes.Float;
                }
                else if (value instanceof Double) {
                    primitiveType = BuiltinTypes.Double;
                }
                else {
                    primitiveType = null;
                }
            }
            else if (value instanceof Character) {
                primitiveType = BuiltinTypes.Character;
            }
            else if (value instanceof Boolean) {
                primitiveType = BuiltinTypes.Boolean;
            }
            else {
                primitiveType = null;
            }
            if (primitiveType == null) {
                return null;
            }
            return new PrimitiveResolveResult(primitiveType, (value != null) ? value : literalValue, null);
        }
        
        @Override
        public ResolveResult visitClassOfExpression(final ClassOfExpression node, final Void data) {
            final TypeReference type = node.getType().getUserData(Keys.TYPE_REFERENCE);
            if (type == null) {
                return null;
            }
            if (BuiltinTypes.Class.isGenericType()) {
                return new ResolveResult(BuiltinTypes.Class.makeGenericType(type));
            }
            return new ResolveResult(BuiltinTypes.Class);
        }
        
        @Override
        public ResolveResult visitCastExpression(final CastExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            final ResolveResult typeResult = JavaResolver.access$1(node.getType());
            if (typeResult == null) {
                return childResult;
            }
            final TypeReference resolvedType = typeResult.getType();
            if (resolvedType == null) {
                return typeResult;
            }
            if (resolvedType.isPrimitive() && childResult != null && childResult.isCompileTimeConstant()) {
                return new PrimitiveResolveResult(resolvedType, JavaPrimitiveCast.cast(resolvedType.getSimpleType(), childResult.getConstantValue()), null);
            }
            return new ResolveResult(resolvedType);
        }
        
        @Override
        public ResolveResult visitNullReferenceExpression(final NullReferenceExpression node, final Void data) {
            return new ResolveResult(BuiltinTypes.Null);
        }
        
        @Override
        public ResolveResult visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
            final ResolveResult leftResult = node.getLeft().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            final ResolveResult rightResult = node.getRight().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (leftResult == null || rightResult == null) {
                return null;
            }
            final TypeReference leftType = leftResult.getType();
            final TypeReference rightType = rightResult.getType();
            if (leftType == null || rightType == null) {
                return null;
            }
            final TypeReference operandType = this.doBinaryPromotionStrict(leftResult, rightResult);
            if (operandType == null) {
                return null;
            }
            TypeReference resultType = null;
            Label_0196: {
                switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[node.getOperator().ordinal()]) {
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                    case 12: {
                        resultType = BuiltinTypes.Boolean;
                        break;
                    }
                    default: {
                        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[operandType.getSimpleType().ordinal()]) {
                            case 2:
                            case 3:
                            case 4: {
                                resultType = BuiltinTypes.Integer;
                                break Label_0196;
                            }
                            default: {
                                resultType = operandType;
                                break Label_0196;
                            }
                        }
                        break;
                    }
                }
            }
            if (leftResult.isCompileTimeConstant() && rightResult.isCompileTimeConstant() && operandType.isPrimitive()) {
                final Object result = BinaryOperations.doBinary(node.getOperator(), operandType.getSimpleType(), leftResult.getConstantValue(), rightResult.getConstantValue());
                if (result != null) {
                    return new PrimitiveResolveResult(resultType, result, null);
                }
            }
            return new ResolveResult(resultType);
        }
        
        @Override
        public ResolveResult visitInstanceOfExpression(final InstanceOfExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (childResult == null) {
                return new ResolveResult(BuiltinTypes.Boolean);
            }
            final TypeReference childType = childResult.getType();
            final ResolveResult typeResult = JavaResolver.access$1(node.getType());
            if (childType == null || typeResult == null || typeResult.getType() == null) {
                return new ResolveResult(BuiltinTypes.Boolean);
            }
            return new PrimitiveResolveResult(BuiltinTypes.Boolean, MetadataHelper.isSubType(typeResult.getType(), childType), null);
        }
        
        @Override
        public ResolveResult visitIndexerExpression(final IndexerExpression node, final Void data) {
            final ResolveResult childResult = node.getTarget().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (childResult == null || childResult.getType() == null || !childResult.getType().isArray()) {
                return null;
            }
            final TypeReference elementType = childResult.getType().getElementType();
            if (elementType == null) {
                return null;
            }
            return new ResolveResult(elementType);
        }
        
        @Override
        public ResolveResult visitUnaryOperatorExpression(final UnaryOperatorExpression node, final Void data) {
            final ResolveResult childResult = node.getExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (childResult == null || childResult.getType() == null) {
                return null;
            }
            TypeReference resultType = null;
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[childResult.getType().getSimpleType().ordinal()]) {
                case 2:
                case 3:
                case 4:
                case 5: {
                    resultType = BuiltinTypes.Integer;
                    break;
                }
                default: {
                    resultType = childResult.getType();
                    break;
                }
            }
            if (childResult.isCompileTimeConstant()) {
                final Object resultValue = UnaryOperations.doUnary(node.getOperator(), childResult.getConstantValue());
                if (resultValue != null) {
                    return new PrimitiveResolveResult(resultType, resultValue, null);
                }
            }
            return new ResolveResult(resultType);
        }
        
        @Override
        public ResolveResult visitConditionalExpression(final ConditionalExpression node, final Void data) {
            final ResolveResult conditionResult = node.getCondition().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (conditionResult != null && conditionResult.isCompileTimeConstant()) {
                if (Boolean.TRUE.equals(conditionResult.getConstantValue())) {
                    return node.getTrueExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
                }
                if (Boolean.FALSE.equals(conditionResult.getConstantValue())) {
                    return node.getFalseExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
                }
            }
            final ResolveResult leftResult = node.getTrueExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (leftResult == null || leftResult.getType() == null) {
                return null;
            }
            final ResolveResult rightResult = node.getFalseExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (rightResult == null || rightResult.getType() == null) {
                return null;
            }
            final TypeReference resultType = MetadataHelper.findCommonSuperType(leftResult.getType(), rightResult.getType());
            if (resultType == null) {
                return null;
            }
            if (leftResult.getType().isPrimitive() || rightResult.getType().isPrimitive()) {
                return new ResolveResult(MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(resultType));
            }
            return new ResolveResult(resultType);
        }
        
        @Override
        public ResolveResult visitArrayCreationExpression(final ArrayCreationExpression node, final Void data) {
            final TypeReference elementType = node.getType().toTypeReference();
            if (elementType == null) {
                return null;
            }
            final int rank = node.getDimensions().size() + node.getAdditionalArraySpecifiers().size();
            TypeReference arrayType = elementType;
            for (int i = 0; i < rank; ++i) {
                arrayType = arrayType.makeArrayType();
            }
            return new ResolveResult(arrayType);
        }
        
        @Override
        public ResolveResult visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            final ResolveResult leftResult = node.getLeft().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
            if (leftResult != null && leftResult.getType() != null) {
                return new ResolveResult(leftResult.getType());
            }
            return null;
        }
        
        @Override
        public ResolveResult visitParenthesizedExpression(final ParenthesizedExpression node, final Void data) {
            return node.getExpression().acceptVisitor((IAstVisitor<? super Void, ? extends ResolveResult>)this, data);
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
            final int[] loc_0 = ResolveVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
            return ResolveVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
            final int[] loc_0 = ResolveVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
            return ResolveVisitor.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
        }
    }
    
    private static final class PrimitiveResolveResult extends ResolveResult
    {
        private final Object _value;
        
        private PrimitiveResolveResult(final TypeReference type, final Object value) {
            super(type);
            this._value = value;
        }
        
        @Override
        public boolean isCompileTimeConstant() {
            return true;
        }
        
        @Override
        public Object getConstantValue() {
            return this._value;
        }
    }
    
    private static final class BinaryOperations
    {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        
        static Object doBinary(final BinaryOperatorType operator, final JvmType type, final Object left, final Object right) {
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[operator.ordinal()]) {
                case 2: {
                    return and(type, left, right);
                }
                case 3: {
                    return or(type, left, right);
                }
                case 4: {
                    return xor(type, left, right);
                }
                case 5: {
                    return andAlso(left, right);
                }
                case 6: {
                    return orElse(left, right);
                }
                case 7: {
                    return greaterThan(type, left, right);
                }
                case 8: {
                    return greaterThanOrEqual(type, left, right);
                }
                case 11: {
                    return equal(type, left, right);
                }
                case 12: {
                    return notEqual(type, left, right);
                }
                case 9: {
                    return lessThan(type, left, right);
                }
                case 10: {
                    return lessThanOrEqual(type, left, right);
                }
                case 13: {
                    return add(type, left, right);
                }
                case 14: {
                    return subtract(type, left, right);
                }
                case 15: {
                    return multiply(type, left, right);
                }
                case 16: {
                    return divide(type, left, right);
                }
                case 17: {
                    return remainder(type, left, right);
                }
                case 18: {
                    return leftShift(type, left, right);
                }
                case 19: {
                    return rightShift(type, left, right);
                }
                case 20: {
                    return unsignedRightShift(type, left, right);
                }
                default: {
                    return null;
                }
            }
        }
        
        private static Object add(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() + ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() + ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() + ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() + ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() + ((Number)right).longValue();
                    }
                    case 7: {
                        return ((Number)left).floatValue() + ((Number)right).floatValue();
                    }
                    case 8: {
                        return ((Number)left).doubleValue() + ((Number)right).doubleValue();
                    }
                }
            }
            return null;
        }
        
        private static Object subtract(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() - ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() - ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() - ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() - ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() - ((Number)right).longValue();
                    }
                    case 7: {
                        return ((Number)left).floatValue() - ((Number)right).floatValue();
                    }
                    case 8: {
                        return ((Number)left).doubleValue() - ((Number)right).doubleValue();
                    }
                }
            }
            return null;
        }
        
        private static Object multiply(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() * ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() * ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() * ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() * ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() * ((Number)right).longValue();
                    }
                    case 7: {
                        return ((Number)left).floatValue() * ((Number)right).floatValue();
                    }
                    case 8: {
                        return ((Number)left).doubleValue() * ((Number)right).doubleValue();
                    }
                }
            }
            return null;
        }
        
        private static Object divide(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                if (type.isIntegral() && ((Number)right).longValue() == 0L) {
                    return null;
                }
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() / ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() / ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() / ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() / ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() / ((Number)right).longValue();
                    }
                    case 7: {
                        return ((Number)left).floatValue() / ((Number)right).floatValue();
                    }
                    case 8: {
                        return ((Number)left).doubleValue() / ((Number)right).doubleValue();
                    }
                }
            }
            return null;
        }
        
        private static Object remainder(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() % ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() % ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() % ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() % ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() % ((Number)right).longValue();
                    }
                    case 7: {
                        return ((Number)left).floatValue() % ((Number)right).floatValue();
                    }
                    case 8: {
                        return ((Number)left).doubleValue() % ((Number)right).doubleValue();
                    }
                }
            }
            return null;
        }
        
        private static Object and(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() & ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() & ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() & ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() & ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() & ((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object or(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() | ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() | ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() | ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() | ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() | ((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object xor(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() ^ ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() ^ ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() ^ ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() ^ ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() ^ ((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object leftShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() << ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() << ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() << ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() << ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() << (int)((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object rightShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() >> ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() >> ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() >> ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() >> ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() >> (int)((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object unsignedRightShift(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2: {
                        return (byte)(((Number)left).intValue() >>> ((Number)right).intValue());
                    }
                    case 3: {
                        return (char)((Number)left).intValue() >>> ((Number)right).intValue();
                    }
                    case 4: {
                        return (short)((Number)left).intValue() >>> ((Number)right).intValue();
                    }
                    case 5: {
                        return ((Number)left).intValue() >>> ((Number)right).intValue();
                    }
                    case 6: {
                        return ((Number)left).longValue() >>> (int)((Number)right).longValue();
                    }
                }
            }
            return null;
        }
        
        private static Object andAlso(final Object left, final Object right) {
            if (Boolean.TRUE.equals(asBoolean(left)) && Boolean.TRUE.equals(asBoolean(right))) {
                return true;
            }
            return false;
        }
        
        private static Object orElse(final Object left, final Object right) {
            if (!Boolean.TRUE.equals(asBoolean(left)) && !Boolean.TRUE.equals(asBoolean(right))) {
                return false;
            }
            return true;
        }
        
        private static Boolean asBoolean(final Object o) {
            if (o instanceof Boolean) {
                return (Boolean)o;
            }
            if (!(o instanceof Number)) {
                return null;
            }
            final Number n = (Number)o;
            if (o instanceof Float) {
                if (n.floatValue() != 0.0f) {
                    return true;
                }
                return false;
            }
            else if (o instanceof Double) {
                if (n.doubleValue() != 0.0) {
                    return true;
                }
                return false;
            }
            else {
                if (n.longValue() != 0L) {
                    return true;
                }
                return false;
            }
        }
        
        private static Boolean lessThan(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() < ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() < ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() < ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        private static Boolean lessThanOrEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() <= ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() <= ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() <= ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        private static Boolean greaterThan(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() > ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() > ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() > ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        private static Boolean greaterThanOrEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() >= ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() >= ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() >= ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        private static Boolean equal(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() == ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() == ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() == ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        private static Boolean notEqual(final JvmType type, final Object left, final Object right) {
            if (left instanceof Number && right instanceof Number) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.ordinal()]) {
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6: {
                        if (((Number)left).longValue() != ((Number)right).longValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 7: {
                        if (((Number)left).floatValue() != ((Number)right).floatValue()) {
                            return true;
                        }
                        return false;
                    }
                    case 8: {
                        if (((Number)left).doubleValue() != ((Number)right).doubleValue()) {
                            return true;
                        }
                        return false;
                    }
                }
            }
            return null;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
            final int[] loc_0 = BinaryOperations.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
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
            return BinaryOperations.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
            final int[] loc_0 = BinaryOperations.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
            return BinaryOperations.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
        }
    }
    
    private static final class UnaryOperations
    {
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        
        static Object doUnary(final UnaryOperatorType operator, final Object operand) {
            switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[operator.ordinal()]) {
                case 2: {
                    return isFalse(operand);
                }
                case 3: {
                    return not(operand);
                }
                case 4: {
                    return minus(operand);
                }
                case 5: {
                    return plus(operand);
                }
                case 6: {
                    return preIncrement(operand);
                }
                case 7: {
                    return preDecrement(operand);
                }
                case 8: {
                    return postIncrement(operand);
                }
                case 9: {
                    return postDecrement(operand);
                }
                default: {
                    return null;
                }
            }
        }
        
        private static Object isFalse(final Object operand) {
            if (Boolean.TRUE.equals(operand)) {
                return Boolean.FALSE;
            }
            if (Boolean.FALSE.equals(operand)) {
                return Boolean.TRUE;
            }
            if (!(operand instanceof Number)) {
                return null;
            }
            final Number n = (Number)operand;
            if (n instanceof Float) {
                if (n.floatValue() != 0.0f) {
                    return true;
                }
                return false;
            }
            else if (n instanceof Double) {
                if (n.doubleValue() != 0.0) {
                    return true;
                }
                return false;
            }
            else {
                if (n.longValue() != 0L) {
                    return true;
                }
                return false;
            }
        }
        
        private static Object not(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number)operand;
                if (n instanceof Byte) {
                    return ~n.byteValue();
                }
                if (n instanceof Short) {
                    return ~n.shortValue();
                }
                if (n instanceof Integer) {
                    return ~n.intValue();
                }
                if (n instanceof Long) {
                    return ~n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return ~(char)operand;
            }
            return null;
        }
        
        private static Object minus(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number)operand;
                if (n instanceof Byte) {
                    return -n.byteValue();
                }
                if (n instanceof Short) {
                    return -n.shortValue();
                }
                if (n instanceof Integer) {
                    return -n.intValue();
                }
                if (n instanceof Long) {
                    return -n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return -(char)operand;
            }
            return null;
        }
        
        private static Object plus(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number)operand;
                if (n instanceof Byte) {
                    return n.byteValue();
                }
                if (n instanceof Short) {
                    return n.shortValue();
                }
                if (n instanceof Integer) {
                    return n.intValue();
                }
                if (n instanceof Long) {
                    return n.longValue();
                }
            }
            else if (operand instanceof Character) {
                return operand;
            }
            return null;
        }
        
        private static Object preIncrement(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number)operand;
                if (n instanceof Byte) {
                    byte b = n.byteValue();
                    return ++b;
                }
                if (n instanceof Short) {
                    short s = n.shortValue();
                    return ++s;
                }
                if (n instanceof Integer) {
                    int i = n.intValue();
                    return ++i;
                }
                if (n instanceof Long) {
                    long l = n.longValue();
                    return ++l;
                }
            }
            else if (operand instanceof Character) {
                char c = (char)operand;
                return ++c;
            }
            return null;
        }
        
        private static Object preDecrement(final Object operand) {
            if (operand instanceof Number) {
                final Number n = (Number)operand;
                if (n instanceof Byte) {
                    byte b = n.byteValue();
                    return --b;
                }
                if (n instanceof Short) {
                    short s = n.shortValue();
                    return --s;
                }
                if (n instanceof Integer) {
                    int i = n.intValue();
                    return --i;
                }
                if (n instanceof Long) {
                    long l = n.longValue();
                    return --l;
                }
            }
            else if (operand instanceof Character) {
                char c = (char)operand;
                return --c;
            }
            return null;
        }
        
        private static Object postIncrement(final Object operand) {
            if (operand instanceof Number) {
                return operand;
            }
            if (operand instanceof Character) {
                return operand;
            }
            return null;
        }
        
        private static Object postDecrement(final Object operand) {
            if (operand instanceof Number) {
                return operand;
            }
            if (operand instanceof Character) {
                return operand;
            }
            return null;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
            final int[] loc_0 = UnaryOperations.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
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
            return UnaryOperations.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
        }
    }
}
