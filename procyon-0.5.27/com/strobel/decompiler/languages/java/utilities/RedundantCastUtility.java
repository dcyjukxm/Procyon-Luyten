package com.strobel.decompiler.languages.java.utilities;

import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.metadata.annotations.*;

public final class RedundantCastUtility
{
    @NotNull
    public static List<CastExpression> getRedundantCastsInside(final Function<AstNode, ResolveResult> resolver, final AstNode site) {
        VerifyArgument.notNull(resolver, "resolver");
        if (site == null) {
            return Collections.emptyList();
        }
        final CastCollector visitor = new CastCollector(resolver);
        site.acceptVisitor((IAstVisitor<? super Object, ?>)visitor, (Object)null);
        return new ArrayList<CastExpression>(CastCollector.access$4(visitor));
    }
    
    public static boolean isCastRedundant(final Function<AstNode, ResolveResult> resolver, final CastExpression cast) {
        AstNode parent = skipParenthesesUp(cast.getParent());
        if (parent == null) {
            return false;
        }
        if (parent.getRole() == Roles.ARGUMENT || parent.isReference()) {
            parent = parent.getParent();
        }
        final IsRedundantVisitor visitor = new IsRedundantVisitor(resolver, false);
        parent.acceptVisitor((IAstVisitor<? super Object, ?>)visitor, (Object)null);
        return visitor.isRedundant();
    }
    
    public static void removeCast(final CastExpression castExpression) {
        if (castExpression == null || castExpression.isNull()) {
            return;
        }
        Expression operand = castExpression.getExpression();
        if (operand instanceof ParenthesizedExpression) {
            operand = ((ParenthesizedExpression)operand).getExpression();
        }
        if (operand == null || operand.isNull()) {
            return;
        }
        AstNode toBeReplaced = castExpression;
        for (AstNode parent = castExpression.getParent(); parent instanceof ParenthesizedExpression; parent = parent.getParent()) {
            toBeReplaced = parent;
        }
        toBeReplaced.replaceWith(operand);
    }
    
    @Nullable
    private static Expression removeParentheses(final Expression e) {
        Expression result;
        for (result = e; result instanceof ParenthesizedExpression; result = ((ParenthesizedExpression)result).getExpression()) {}
        return result;
    }
    
    @Nullable
    private static AstNode skipParenthesesUp(final AstNode e) {
        AstNode result;
        for (result = e; result instanceof ParenthesizedExpression; result = result.getParent()) {}
        return result;
    }
    
    static /* synthetic */ Expression access$0(final Expression param_0) {
        return removeParentheses(param_0);
    }
    
    static /* synthetic */ AstNode access$1(final AstNode param_0) {
        return skipParenthesesUp(param_0);
    }
    
    private static class CastCollector extends IsRedundantVisitor
    {
        private final Set<CastExpression> _foundCasts;
        
        CastCollector(final Function<AstNode, ResolveResult> resolver) {
            super(resolver, true);
            this._foundCasts = new HashSet<CastExpression>();
        }
        
        private Set<CastExpression> getFoundCasts() {
            return this._foundCasts;
        }
        
        @Override
        public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void data) {
            for (final Expression argument : node.getArguments()) {
                argument.acceptVisitor((IAstVisitor<? super Void, ?>)this, data);
            }
            return null;
        }
        
        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void _) {
            return null;
        }
        
        @Override
        public Void visitLocalTypeDeclarationStatement(final LocalTypeDeclarationStatement node, final Void data) {
            return null;
        }
        
        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
            return null;
        }
        
        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
            return null;
        }
        
        @Override
        protected void addToResults(@NotNull final CastExpression cast, final boolean force) {
            if (force || !this.isTypeCastSemantic(cast)) {
                this._foundCasts.add(cast);
            }
        }
        
        static /* synthetic */ Set access$4(final CastCollector param_0) {
            return param_0.getFoundCasts();
        }
    }
    
    private static class IsRedundantVisitor extends DepthFirstAstVisitor<Void, Void>
    {
        private final boolean _isRecursive;
        private final Function<AstNode, ResolveResult> _resolver;
        private boolean _isRedundant;
        
        IsRedundantVisitor(final Function<AstNode, ResolveResult> resolver, final boolean recursive) {
            super();
            this._isRecursive = recursive;
            this._resolver = resolver;
        }
        
        public final boolean isRedundant() {
            return this._isRedundant;
        }
        
        @Override
        protected Void visitChildren(final AstNode node, final Void data) {
            if (this._isRecursive) {
                return super.visitChildren(node, data);
            }
            return null;
        }
        
        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
            this.processPossibleTypeCast(node.getRight(), this.getType(node.getLeft()));
            return super.visitAssignmentExpression(node, data);
        }
        
        @Override
        public Void visitVariableDeclaration(final VariableDeclarationStatement node, final Void data) {
            final TypeReference leftType = this.getType(node.getType());
            if (leftType != null) {
                for (final VariableInitializer initializer : node.getVariables()) {
                    this.processPossibleTypeCast(initializer.getInitializer(), leftType);
                }
            }
            return super.visitVariableDeclaration(node, data);
        }
        
        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            final TypeReference leftType = this.getType(node.getReturnType());
            if (leftType != null) {
                for (final VariableInitializer initializer : node.getVariables()) {
                    this.processPossibleTypeCast(initializer.getInitializer(), leftType);
                }
            }
            return super.visitFieldDeclaration(node, data);
        }
        
        @Override
        public Void visitReturnStatement(final ReturnStatement node, final Void data) {
            final MethodDeclaration methodDeclaration = CollectionUtilities.firstOrDefault(node.getAncestors(MethodDeclaration.class));
            if (methodDeclaration != null && !methodDeclaration.isNull()) {
                final TypeReference returnType = this.getType(methodDeclaration.getReturnType());
                final Expression returnValue = node.getExpression();
                if (returnType != null && returnValue != null && !returnValue.isNull()) {
                    this.processPossibleTypeCast(returnValue, returnType);
                }
            }
            return super.visitReturnStatement(node, data);
        }
        
        @Override
        public Void visitBinaryOperatorExpression(final BinaryOperatorExpression node, final Void data) {
            final TypeReference leftType = this.getType(node.getLeft());
            final TypeReference rightType = this.getType(node.getRight());
            this.processBinaryExpressionOperand(node.getLeft(), rightType, node.getOperator());
            this.processBinaryExpressionOperand(node.getRight(), leftType, node.getOperator());
            return super.visitBinaryOperatorExpression(node, data);
        }
        
        @Override
        public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
            super.visitInvocationExpression(node, data);
            this.processCall(node);
            return null;
        }
        
        @Override
        public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void data) {
            for (final Expression argument : node.getArguments()) {
                argument.acceptVisitor((IAstVisitor<? super Void, ?>)this, data);
            }
            this.processCall(node);
            return null;
        }
        
        @Override
        public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void data) {
            for (final Expression argument : node.getArguments()) {
                argument.acceptVisitor((IAstVisitor<? super Void, ?>)this, data);
            }
            this.processCall(node);
            return null;
        }
        
        @Override
        public Void visitCastExpression(final CastExpression node, final Void data) {
            final Expression operand = node.getExpression();
            if (operand == null || operand.isNull()) {
                return null;
            }
            final TypeReference topCastType = this.getType(node);
            if (topCastType == null) {
                return null;
            }
            final Expression e = RedundantCastUtility.access$0(operand);
            if (e instanceof CastExpression) {
                final CastExpression innerCast = (CastExpression)e;
                final TypeReference innerCastType = this.getType(innerCast.getType());
                if (innerCastType == null) {
                    return null;
                }
                final Expression innerOperand = innerCast.getExpression();
                final TypeReference innerOperandType = this.getType(innerOperand);
                if (!innerCastType.isPrimitive()) {
                    if (innerOperandType != null && MetadataHelper.getConversionType(topCastType, innerOperandType) != ConversionType.NONE) {
                        this.addToResults(innerCast, false);
                    }
                }
                else {
                    final ConversionType valueToInner = MetadataHelper.getNumericConversionType(innerCastType, innerOperandType);
                    final ConversionType outerToInner = MetadataHelper.getNumericConversionType(innerCastType, topCastType);
                    if (outerToInner == ConversionType.IDENTITY) {
                        if (valueToInner == ConversionType.IDENTITY) {
                            this.addToResults(node, false);
                            this.addToResults(innerCast, true);
                        }
                        else {
                            this.addToResults(innerCast, true);
                        }
                    }
                    else if (outerToInner == ConversionType.IMPLICIT) {
                        final ConversionType valueToOuter = MetadataHelper.getNumericConversionType(topCastType, innerOperandType);
                        if (valueToOuter != ConversionType.NONE) {
                            this.addToResults(innerCast, true);
                        }
                    }
                    else if (valueToInner == ConversionType.IMPLICIT && MetadataHelper.getNumericConversionType(topCastType, innerOperandType) == ConversionType.IMPLICIT) {
                        this.addToResults(innerCast, true);
                    }
                }
            }
            else {
                final AstNode parent = node.getParent();
                if (parent instanceof ConditionalExpression) {
                    final TypeReference operandType = this.getType(operand);
                    final TypeReference conditionalType = this.getType(parent);
                    if (!MetadataHelper.isSameType(operandType, conditionalType, true)) {
                        if (!this.checkResolveAfterRemoveCast(parent)) {
                            return null;
                        }
                        final Expression thenExpression = ((ConditionalExpression)parent).getTrueExpression();
                        final Expression elseExpression = ((ConditionalExpression)parent).getFalseExpression();
                        final Expression opposite = (thenExpression == node) ? elseExpression : thenExpression;
                        final TypeReference oppositeType = this.getType(opposite);
                        if (oppositeType == null || !MetadataHelper.isSameType(conditionalType, oppositeType, true)) {
                            return null;
                        }
                    }
                    else if (topCastType.isPrimitive() && !operandType.isPrimitive()) {
                        return null;
                    }
                }
                else {
                    if (parent instanceof SynchronizedStatement && this.getType(e) instanceof PrimitiveType) {
                        return null;
                    }
                    if (e instanceof LambdaExpression || e instanceof MethodGroupExpression) {
                        if (parent instanceof ParenthesizedExpression && parent.getParent() != null && parent.getParent().isReference()) {
                            return null;
                        }
                        final ResolveResult lambdaResult = this._resolver.apply(e);
                        TypeReference functionalInterfaceType;
                        if (lambdaResult != null && lambdaResult.getType() != null) {
                            final TypeReference asSubType = MetadataHelper.asSubType(lambdaResult.getType(), topCastType);
                            functionalInterfaceType = ((asSubType != null) ? asSubType : lambdaResult.getType());
                        }
                        else {
                            final DynamicCallSite callSite = e.getUserData(Keys.DYNAMIC_CALL_SITE);
                            if (callSite == null) {
                                return null;
                            }
                            functionalInterfaceType = callSite.getMethodType().getReturnType();
                        }
                        if (!MetadataHelper.isAssignableFrom(topCastType, functionalInterfaceType, false)) {
                            return null;
                        }
                    }
                }
                this.processAlreadyHasTypeCast(node);
            }
            return super.visitCastExpression(node, data);
        }
        
        protected TypeReference getType(final AstNode node) {
            final ResolveResult result = this._resolver.apply(node);
            return (result != null) ? result.getType() : null;
        }
        
        @NotNull
        protected List<TypeReference> getTypes(final AstNodeCollection<? extends AstNode> nodes) {
            if (nodes == null || nodes.isEmpty()) {
                return Collections.emptyList();
            }
            final List<TypeReference> types = new ArrayList<TypeReference>();
            for (final AstNode node : nodes) {
                final TypeReference nodeType = this.getType(node);
                if (nodeType == null) {
                    return Collections.emptyList();
                }
                types.add(nodeType);
            }
            return types;
        }
        
        protected void processPossibleTypeCast(final Expression rightExpression, @Nullable final TypeReference leftType) {
            if (leftType == null) {
                return;
            }
            final Expression r = RedundantCastUtility.access$0(rightExpression);
            if (r instanceof CastExpression) {
                final AstType castAstType = ((CastExpression)r).getType();
                final TypeReference castType = (castAstType != null) ? castAstType.toTypeReference() : null;
                final Expression castOperand = ((CastExpression)r).getExpression();
                if (castOperand != null && !castOperand.isNull() && castType != null) {
                    final TypeReference operandType = this.getType(castOperand);
                    if (operandType != null) {
                        if (MetadataHelper.isAssignableFrom(leftType, operandType, false)) {
                            this.addToResults((CastExpression)r, false);
                        }
                        else {
                            final TypeReference unboxedCastType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(castType);
                            final TypeReference unboxedLeftType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(leftType);
                            if (castOperand instanceof PrimitiveExpression && TypeUtilities.isValidPrimitiveLiteralAssignment(unboxedCastType, ((PrimitiveExpression)castOperand).getValue()) && TypeUtilities.isValidPrimitiveLiteralAssignment(unboxedLeftType, ((PrimitiveExpression)castOperand).getValue())) {
                                this.addToResults((CastExpression)r, true);
                            }
                        }
                    }
                }
            }
        }
        
        protected void addToResults(@NotNull final CastExpression cast, final boolean force) {
            if (force || !this.isTypeCastSemantic(cast)) {
                this._isRedundant = true;
            }
        }
        
        protected void processBinaryExpressionOperand(final Expression operand, final TypeReference otherType, final BinaryOperatorType op) {
            if (operand instanceof CastExpression) {
                final CastExpression cast = (CastExpression)operand;
                final Expression toCast = cast.getExpression();
                final TypeReference castType = this.getType(cast);
                final TypeReference innerType = this.getType(toCast);
                if (castType != null && innerType != null && TypeUtilities.isBinaryOperatorApplicable(op, innerType, otherType, false)) {
                    this.addToResults(cast, false);
                }
            }
        }
        
        protected void processCall(@NotNull final Expression e) {
            final AstNodeCollection<Expression> arguments = e.getChildrenByRole(Roles.ARGUMENT);
            if (arguments.isEmpty()) {
                return;
            }
            MemberReference reference = e.getUserData(Keys.MEMBER_REFERENCE);
            if (reference == null && e.getParent() instanceof MemberReferenceExpression) {
                reference = e.getParent().getUserData(Keys.MEMBER_REFERENCE);
            }
            if (reference instanceof MethodReference) {
                final MethodReference method = (MethodReference)reference;
                Expression target = e.getChildByRole(Roles.TARGET_EXPRESSION);
                if (target instanceof MemberReferenceExpression) {
                    target = target.getChildByRole(Roles.TARGET_EXPRESSION);
                }
                TypeReference targetType = this.getType(target);
                if (targetType == null) {
                    targetType = method.getDeclaringType();
                }
                else if (!(targetType instanceof RawType) && MetadataHelper.isRawType(targetType)) {
                    targetType = MetadataHelper.eraseRecursive(targetType);
                }
                else {
                    final TypeReference asSuper = MetadataHelper.asSuper(method.getDeclaringType(), targetType);
                    final TypeReference asSubType = (asSuper != null) ? MetadataHelper.asSubType(method.getDeclaringType(), asSuper) : null;
                    targetType = ((asSubType != null) ? asSubType : targetType);
                }
                final List<MethodReference> candidates = MetadataHelper.findMethods(targetType, MetadataFilters.matchName(method.getName()));
                final MethodDefinition resolvedMethod = method.resolve();
                final List<TypeReference> originalTypes = new ArrayList<TypeReference>();
                final List<ParameterDefinition> parameters = method.getParameters();
                final Expression lastArgument = arguments.lastOrNullObject();
                List<TypeReference> newTypes = null;
                int syntheticLeadingCount = 0;
                int syntheticTrailingCount = 0;
                for (final ParameterDefinition parameter : parameters) {
                    if (!parameter.isSynthetic()) {
                        break;
                    }
                    ++syntheticLeadingCount;
                    originalTypes.add(parameter.getParameterType());
                }
                for (int i = parameters.size() - 1; i >= 0 && parameters.get(i).isSynthetic(); --i, ++syntheticTrailingCount) {}
                for (final Expression argument : arguments) {
                    final TypeReference argumentType = this.getType(argument);
                    if (argumentType == null) {
                        return;
                    }
                    originalTypes.add(argumentType);
                }
                int j;
                int realParametersEnd;
                for (realParametersEnd = (j = parameters.size() - syntheticTrailingCount); j < parameters.size(); ++j) {
                    originalTypes.add(parameters.get(j).getParameterType());
                }
                j = syntheticLeadingCount;
                for (Expression a = arguments.firstOrNullObject(); j < realParametersEnd && a != null && !a.isNull(); a = a.getNextSibling(Roles.ARGUMENT), ++j) {
                    final Expression arg = RedundantCastUtility.access$0(a);
                    if (arg instanceof CastExpression) {
                        if (a != lastArgument || j != parameters.size() - 1 || resolvedMethod == null || !resolvedMethod.isVarArgs()) {
                            final CastExpression cast = (CastExpression)arg;
                            final Expression castOperand = cast.getExpression();
                            final TypeReference castType = this.getType(cast);
                            final TypeReference operandType = this.getType(castOperand);
                            if (castType != null) {
                                if (operandType != null) {
                                    if (castType.isPrimitive() && !operandType.isPrimitive()) {
                                        final ParameterDefinition p = parameters.get(j);
                                        final TypeReference parameterType = p.getParameterType();
                                        if (!parameterType.isPrimitive()) {
                                            continue;
                                        }
                                    }
                                    if (newTypes == null) {
                                        newTypes = new ArrayList<TypeReference>(originalTypes);
                                    }
                                    else {
                                        newTypes.clear();
                                        newTypes.addAll(originalTypes);
                                    }
                                    newTypes.set(j, operandType);
                                    final MethodBinder.BindResult result = MethodBinder.selectMethod(candidates, newTypes);
                                    if (!result.isFailure()) {
                                        if (!result.isAmbiguous()) {
                                            final boolean sameMethod = StringUtilities.equals(method.getErasedSignature(), result.getMethod().getErasedSignature());
                                            if (sameMethod) {
                                                final ParameterDefinition newParameter = result.getMethod().getParameters().get(j);
                                                if (castType.isPrimitive()) {
                                                    final boolean castNeeded = !MetadataHelper.isSameType(castType, MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(newParameter.getParameterType()));
                                                    if (castNeeded) {
                                                        continue;
                                                    }
                                                }
                                                if (MetadataHelper.isAssignableFrom(newParameter.getParameterType(), castType)) {
                                                    this.addToResults(cast, false);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        protected void processAlreadyHasTypeCast(final CastExpression cast) {
            AstNode parent;
            for (parent = cast.getParent(); parent instanceof ParenthesizedExpression; parent = parent.getParent()) {}
            if (parent == null || (cast.getRole() == Roles.ARGUMENT && !(parent instanceof IndexerExpression)) || parent instanceof AssignmentExpression || parent instanceof ReturnStatement || parent instanceof CastExpression || parent instanceof BinaryOperatorExpression) {
                return;
            }
            if (this.isTypeCastSemantic(cast)) {
                return;
            }
            final TypeReference castTo = this.getType(cast.getType());
            final Expression operand = cast.getExpression();
            TypeReference operandType = this.getType(operand);
            if (castTo == null || operandType == null) {
                return;
            }
            final TypeReference expectedType = TypeUtilities.getExpectedTypeByParent(this._resolver, cast);
            final boolean isCharConversion = operandType == BuiltinTypes.Character ^ castTo == BuiltinTypes.Character;
            if (expectedType != null) {
                if (isCharConversion && !expectedType.isPrimitive()) {
                    return;
                }
                operandType = expectedType;
            }
            else if (isCharConversion) {
                return;
            }
            if (operandType == BuiltinTypes.Null && castTo.isPrimitive()) {
                return;
            }
            if (parent.isReference()) {
                if (operandType.isPrimitive() && !castTo.isPrimitive()) {
                    return;
                }
                final TypeReference referenceType = this.getType(parent);
                if (!operandType.isPrimitive() && referenceType != null && !this.isCastRedundantInReferenceExpression(referenceType, operand)) {
                    return;
                }
            }
            if (this.arrayAccessAtTheLeftSideOfAssignment(parent)) {
                if (MetadataHelper.isAssignableFrom(operandType, castTo, false) && MetadataHelper.getArrayRank(operandType) == MetadataHelper.getArrayRank(castTo)) {
                    this.addToResults(cast, false);
                }
            }
            else if (MetadataHelper.isAssignableFrom(castTo, operandType, false)) {
                this.addToResults(cast, false);
            }
        }
        
        protected boolean arrayAccessAtTheLeftSideOfAssignment(final AstNode node) {
            final AssignmentExpression assignment = CollectionUtilities.firstOrDefault(node.getAncestors(AssignmentExpression.class));
            if (assignment == null) {
                return false;
            }
            final Expression left = assignment.getLeft();
            return left.isAncestorOf(node) && left instanceof IndexerExpression;
        }
        
        protected boolean isCastRedundantInReferenceExpression(final TypeReference type, final Expression operand) {
            return false;
        }
        
        protected boolean checkResolveAfterRemoveCast(final AstNode parent) {
            final AstNode grandParent = parent.getParent();
            if (grandParent == null || parent.getRole() != Roles.ARGUMENT) {
                return true;
            }
            TypeReference targetType;
            if (grandParent instanceof InvocationExpression) {
                targetType = this.getType(((InvocationExpression)grandParent).getTarget());
            }
            else {
                targetType = this.getType(grandParent);
            }
            if (targetType == null) {
                return false;
            }
            final Expression expression = (Expression)grandParent.clone();
            final AstNodeCollection<Expression> arguments = expression.getChildrenByRole(Roles.ARGUMENT);
            final List<TypeReference> argumentTypes = this.getTypes(arguments);
            if (argumentTypes.isEmpty()) {
                return arguments.isEmpty();
            }
            MemberReference memberReference = grandParent.getUserData(Keys.MEMBER_REFERENCE);
            if (!(memberReference instanceof MethodReference) && grandParent.getParent() != null) {
                memberReference = grandParent.getParent().getUserData(Keys.MEMBER_REFERENCE);
            }
            if (!(memberReference instanceof MethodReference)) {
                return false;
            }
            final MethodReference method = (MethodReference)memberReference;
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod == null) {
                return false;
            }
            final int argumentIndex = CollectionUtilities.indexOf(grandParent.getChildrenByRole(Roles.ARGUMENT), (Expression)parent);
            final Expression toReplace = CollectionUtilities.get(arguments, argumentIndex);
            if (toReplace instanceof ConditionalExpression) {
                final Expression trueExpression = ((ConditionalExpression)toReplace).getTrueExpression();
                final Expression falseExpression = ((ConditionalExpression)toReplace).getFalseExpression();
                if (trueExpression instanceof CastExpression) {
                    final Expression trueOperand = ((CastExpression)trueExpression).getExpression();
                    final TypeReference operandType = this.getType(trueOperand);
                    if (operandType != null) {
                        trueExpression.replaceWith(trueOperand);
                    }
                }
                else if (falseExpression instanceof CastExpression) {
                    final Expression falseOperand = ((CastExpression)falseExpression).getExpression();
                    final TypeReference operandType = this.getType(falseOperand);
                    if (operandType != null) {
                        falseExpression.replaceWith(falseOperand);
                    }
                }
                final TypeReference newArgumentType = this.getType(toReplace);
                if (newArgumentType == null) {
                    return false;
                }
                argumentTypes.set(argumentIndex, newArgumentType);
            }
            final List<MethodReference> candidates = MetadataHelper.findMethods(targetType, MetadataFilters.matchName(resolvedMethod.getName()));
            final MethodBinder.BindResult result = MethodBinder.selectMethod(candidates, argumentTypes);
            return !result.isFailure() && !result.isAmbiguous() && StringUtilities.equals(resolvedMethod.getErasedSignature(), result.getMethod().getErasedSignature());
        }
        
        public boolean isTypeCastSemantic(final CastExpression cast) {
            final Expression operand = cast.getExpression();
            if (operand.isNull()) {
                return false;
            }
            if (this.isInPolymorphicCall(cast)) {
                return true;
            }
            final TypeReference opType = this.getType(operand);
            final TypeReference castType = this.getType(cast.getType());
            if (opType == null || castType == null) {
                return false;
            }
            if (castType instanceof PrimitiveType) {
                if (opType instanceof PrimitiveType) {
                    if (operand instanceof PrimitiveExpression) {
                        final TypeReference unboxedCastType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(castType);
                        final TypeReference unboxedOpType = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(opType);
                        if (TypeUtilities.isValidPrimitiveLiteralAssignment(unboxedCastType, ((PrimitiveExpression)operand).getValue()) && TypeUtilities.isValidPrimitiveLiteralAssignment(unboxedOpType, ((PrimitiveExpression)operand).getValue())) {
                            return false;
                        }
                    }
                    final ConversionType conversionType = MetadataHelper.getNumericConversionType(castType, opType);
                    if (conversionType != ConversionType.IDENTITY && conversionType != ConversionType.IMPLICIT) {
                        return true;
                    }
                }
                final TypeReference unboxedOpType2 = MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(opType);
                if (unboxedOpType2.isPrimitive()) {
                    final ConversionType conversionType2 = MetadataHelper.getNumericConversionType(castType, unboxedOpType2);
                    if (conversionType2 != ConversionType.IDENTITY && conversionType2 != ConversionType.IMPLICIT) {
                        return true;
                    }
                }
            }
            else if (castType instanceof IGenericInstance) {
                if (MetadataHelper.isRawType(opType) && !MetadataHelper.isAssignableFrom(castType, opType)) {
                    return true;
                }
            }
            else if (MetadataHelper.isRawType(castType) && opType instanceof IGenericInstance && !MetadataHelper.isAssignableFrom(castType, opType)) {
                return true;
            }
            if (operand instanceof LambdaExpression || operand instanceof MethodGroupExpression) {
                final MetadataParser parser = new MetadataParser(IMetadataResolver.EMPTY);
                final TypeReference serializable = parser.parseTypeDescriptor("java/lang/Serializable");
                if (!castType.isPrimitive() && MetadataHelper.isSubType(castType, serializable)) {
                    return true;
                }
                if (castType instanceof CompoundTypeReference) {
                    boolean redundant = false;
                    final CompoundTypeReference compoundType = (CompoundTypeReference)castType;
                    final List<TypeReference> interfaces = compoundType.getInterfaces();
                    int start = 0;
                    TypeReference baseType = compoundType.getBaseType();
                    if (baseType == null) {
                        baseType = CollectionUtilities.first(interfaces);
                        start = 1;
                    }
                    for (int i = start; i < interfaces.size(); ++i) {
                        final TypeReference conjunct = interfaces.get(i);
                        if (MetadataHelper.isAssignableFrom(baseType, conjunct)) {
                            redundant = true;
                            break;
                        }
                    }
                    if (!redundant) {
                        return true;
                    }
                }
            }
            AstNode parent;
            for (parent = cast.getParent(); parent instanceof ParenthesizedExpression; parent = parent.getParent()) {}
            if (parent instanceof BinaryOperatorExpression) {
                final BinaryOperatorExpression expression = (BinaryOperatorExpression)parent;
                Expression firstOperand = expression.getLeft();
                Expression otherOperand = expression.getRight();
                if (otherOperand.isAncestorOf(cast)) {
                    final Expression temp = otherOperand;
                    otherOperand = firstOperand;
                    firstOperand = temp;
                }
                if (firstOperand != null && otherOperand != null && this.castChangesComparisonSemantics(firstOperand, otherOperand, operand, expression.getOperator())) {
                    return true;
                }
            }
            else if (parent instanceof ConditionalExpression && opType.isPrimitive() && !(this.getType(parent) instanceof PrimitiveType)) {
                final TypeReference expectedType = TypeUtilities.getExpectedTypeByParent(this._resolver, (Expression)parent);
                if (expectedType != null && MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(expectedType).isPrimitive()) {
                    return true;
                }
            }
            return false;
        }
        
        public boolean isInPolymorphicCall(final CastExpression cast) {
            final Expression operand = cast.getExpression();
            return ((operand instanceof InvocationExpression || (operand instanceof MemberReferenceExpression && operand.getParent() instanceof InvocationExpression) || operand instanceof ObjectCreationExpression) && isPolymorphicMethod(operand)) || (cast.getRole() == Roles.ARGUMENT && isPolymorphicMethod(RedundantCastUtility.access$1(cast.getParent())));
        }
        
        private static boolean isPolymorphicMethod(final AstNode expression) {
            if (expression == null) {
                return false;
            }
            MemberReference memberReference = expression.getUserData(Keys.MEMBER_REFERENCE);
            if (memberReference == null && expression.getParent() instanceof MemberReferenceExpression) {
                memberReference = expression.getParent().getUserData(Keys.MEMBER_REFERENCE);
            }
            if (memberReference != null) {
                final List<CustomAnnotation> annotations = memberReference.getAnnotations();
                for (final CustomAnnotation annotation : annotations) {
                    final String typeName = annotation.getAnnotationType().getInternalName();
                    if (StringUtilities.equals(typeName, "java.lang.invoke.MethodHandle.PolymorphicSignature")) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        private boolean castChangesComparisonSemantics(final Expression operand, final Expression otherOperand, final Expression toCast, final BinaryOperatorType operator) {
            final TypeReference operandType = this.getType(operand);
            final TypeReference otherType = this.getType(otherOperand);
            final TypeReference castType = this.getType(toCast);
            boolean isPrimitiveComparisonWithCast;
            boolean isPrimitiveComparisonWithoutCast;
            if (operator == BinaryOperatorType.EQUALITY || operator == BinaryOperatorType.INEQUALITY) {
                if (TypeUtilities.isPrimitive(otherType)) {
                    isPrimitiveComparisonWithCast = TypeUtilities.isPrimitiveOrWrapper(operandType);
                    isPrimitiveComparisonWithoutCast = TypeUtilities.isPrimitiveOrWrapper(castType);
                }
                else {
                    isPrimitiveComparisonWithCast = TypeUtilities.isPrimitive(operandType);
                    isPrimitiveComparisonWithoutCast = TypeUtilities.isPrimitive(castType);
                }
            }
            else {
                isPrimitiveComparisonWithCast = ((operandType != null && operandType.isPrimitive()) || (otherType != null && otherType.isPrimitive()));
                isPrimitiveComparisonWithoutCast = ((castType != null && castType.isPrimitive()) || (operandType != null && operandType.isPrimitive()));
            }
            return isPrimitiveComparisonWithCast ^ isPrimitiveComparisonWithoutCast;
        }
    }
}
