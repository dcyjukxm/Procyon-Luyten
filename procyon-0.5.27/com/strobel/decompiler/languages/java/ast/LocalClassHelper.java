package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;

public final class LocalClassHelper
{
    private static final ConvertTypeOptions OUTER_TYPE_CONVERT_OPTIONS;
    
    static {
        (OUTER_TYPE_CONVERT_OPTIONS = new ConvertTypeOptions(false, false)).setIncludeTypeArguments(false);
    }
    
    public static void replaceClosureMembers(final DecompilerContext context, final AnonymousObjectCreationExpression node) {
        replaceClosureMembers(context, node.getTypeDeclaration(), Collections.singletonList(node));
    }
    
    public static void replaceClosureMembers(final DecompilerContext context, final TypeDeclaration declaration, final List<? extends ObjectCreationExpression> instantiations) {
        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(declaration, "declaration");
        VerifyArgument.notNull(instantiations, "instantiations");
        final Map<String, Expression> initializers = new HashMap<String, Expression>();
        final Map<String, Expression> replacements = new HashMap<String, Expression>();
        final List<AstNode> nodesToRemove = new ArrayList<AstNode>();
        final List<ParameterDefinition> parametersToRemove = new ArrayList<ParameterDefinition>();
        List<Expression> originalArguments;
        if (instantiations.isEmpty()) {
            originalArguments = Collections.emptyList();
        }
        else {
            originalArguments = new ArrayList<Expression>(((ObjectCreationExpression)instantiations.get(0)).getArguments());
        }
        new ClosureRewriterPhaseOneVisitor(context, originalArguments, replacements, initializers, parametersToRemove, nodesToRemove).run(declaration);
        rewriteThisReferences(context, declaration, initializers);
        new ClosureRewriterPhaseTwoVisitor(context, replacements, initializers).run(declaration);
        for (final ObjectCreationExpression instantiation : instantiations) {
            for (final ParameterDefinition p : parametersToRemove) {
                final Expression argumentToRemove = CollectionUtilities.getOrDefault(instantiation.getArguments(), p.getPosition());
                if (argumentToRemove != null) {
                    instantiation.getArguments().remove(argumentToRemove);
                }
            }
        }
        for (final AstNode n : nodesToRemove) {
            if (n instanceof Expression) {
                final int argumentIndex = originalArguments.indexOf(n);
                if (argumentIndex >= 0) {
                    for (final ObjectCreationExpression instantiation2 : instantiations) {
                        final Expression argumentToRemove2 = CollectionUtilities.getOrDefault(instantiation2.getArguments(), argumentIndex);
                        if (argumentToRemove2 != null) {
                            argumentToRemove2.remove();
                        }
                    }
                }
            }
            n.remove();
        }
    }
    
    public static void introduceInitializerBlocks(final DecompilerContext context, final AstNode node) {
        VerifyArgument.notNull(context, "context");
        VerifyArgument.notNull(node, "node");
        new IntroduceInitializersVisitor(context).run(node);
    }
    
    private static void rewriteThisReferences(final DecompilerContext context, final TypeDeclaration declaration, final Map<String, Expression> initializers) {
        final TypeDefinition innerClass = declaration.getUserData(Keys.TYPE_DEFINITION);
        if (innerClass != null) {
            final ContextTrackingVisitor<Void> thisRewriter = new ThisReferenceReplacingVisitor(context, innerClass);
            for (final Expression e : initializers.values()) {
                thisRewriter.run(e);
            }
        }
    }
    
    private static boolean isLocalOrAnonymous(final TypeDefinition type) {
        return type != null && (type.isLocalClass() || type.isAnonymous());
    }
    
    private static boolean hasSideEffects(final Expression e) {
        return !(e instanceof IdentifierExpression) && !(e instanceof PrimitiveExpression) && !(e instanceof ThisReferenceExpression) && !(e instanceof SuperReferenceExpression) && !(e instanceof NullReferenceExpression) && !(e instanceof ClassOfExpression);
    }
    
    static /* synthetic */ boolean access$0(final Expression param_0) {
        return hasSideEffects(param_0);
    }
    
    static /* synthetic */ boolean access$1(final TypeDefinition param_0) {
        return isLocalOrAnonymous(param_0);
    }
    
    static /* synthetic */ ConvertTypeOptions access$2() {
        return LocalClassHelper.OUTER_TYPE_CONVERT_OPTIONS;
    }
    
    private static final class ClosureRewriterPhaseOneVisitor extends ContextTrackingVisitor<Void>
    {
        private final Map<String, Expression> _replacements;
        private final List<Expression> _originalArguments;
        private final List<ParameterDefinition> _parametersToRemove;
        private final Map<String, Expression> _initializers;
        private final List<AstNode> _nodesToRemove;
        private boolean _baseConstructorCalled;
        
        public ClosureRewriterPhaseOneVisitor(final DecompilerContext context, final List<Expression> originalArguments, final Map<String, Expression> replacements, final Map<String, Expression> initializers, final List<ParameterDefinition> parametersToRemove, final List<AstNode> nodesToRemove) {
            super(context);
            this._originalArguments = VerifyArgument.notNull(originalArguments, "originalArguments");
            this._replacements = VerifyArgument.notNull(replacements, "replacements");
            this._initializers = VerifyArgument.notNull(initializers, "initializers");
            this._parametersToRemove = VerifyArgument.notNull(parametersToRemove, "parametersToRemove");
            this._nodesToRemove = VerifyArgument.notNull(nodesToRemove, "nodesToRemove");
        }
        
        @Override
        public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
            final boolean wasDone = this._baseConstructorCalled;
            this._baseConstructorCalled = false;
            try {
                return super.visitConstructorDeclaration(node, _);
            }
            finally {
                this._baseConstructorCalled = wasDone;
            }
        }
        
        @Override
        protected Void visitChildren(final AstNode node, final Void _) {
            final MethodDefinition currentMethod = this.context.getCurrentMethod();
            if (currentMethod != null && !currentMethod.isConstructor()) {
                return null;
            }
            return super.visitChildren(node, _);
        }
        
        @Override
        public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void _) {
            super.visitSuperReferenceExpression(node, _);
            if (this.context.getCurrentMethod() != null && this.context.getCurrentMethod().isConstructor() && node.getParent() instanceof InvocationExpression) {
                this._baseConstructorCalled = true;
            }
            return null;
        }
        
        @Override
        public Void visitAssignmentExpression(final AssignmentExpression node, final Void _) {
            super.visitAssignmentExpression(node, _);
            if (this.context.getCurrentMethod() == null || !this.context.getCurrentMethod().isConstructor()) {
                return null;
            }
            final Expression left = node.getLeft();
            final Expression right = node.getRight();
            if (left instanceof MemberReferenceExpression) {
                if (right instanceof IdentifierExpression) {
                    final Variable variable = right.getUserData(Keys.VARIABLE);
                    if (variable == null || !variable.isParameter()) {
                        return null;
                    }
                    final MemberReferenceExpression memberReference = (MemberReferenceExpression)left;
                    final MemberReference member = memberReference.getUserData(Keys.MEMBER_REFERENCE);
                    if (member instanceof FieldReference && memberReference.getTarget() instanceof ThisReferenceExpression) {
                        final FieldDefinition resolvedField = ((FieldReference)member).resolve();
                        if (resolvedField != null && resolvedField.isSynthetic()) {
                            final ParameterDefinition parameter = variable.getOriginalParameter();
                            int parameterIndex = parameter.getPosition();
                            if (parameter.getMethod().getParameters().size() > this._originalArguments.size()) {
                                parameterIndex -= parameter.getMethod().getParameters().size() - this._originalArguments.size();
                            }
                            if (parameterIndex >= 0 && parameterIndex < this._originalArguments.size()) {
                                final Expression argument = this._originalArguments.get(parameterIndex);
                                if (argument == null) {
                                    return null;
                                }
                                this._nodesToRemove.add(argument);
                                if (argument instanceof ThisReferenceExpression) {
                                    this.markConstructorParameterForRemoval(node, parameter);
                                    return null;
                                }
                                this._parametersToRemove.add(parameter);
                                final String fullName = member.getFullName();
                                if (!LocalClassHelper.access$0(argument)) {
                                    this._replacements.put(fullName, argument);
                                }
                                else {
                                    this.context.getForcedVisibleMembers().add(resolvedField);
                                    this._initializers.put(fullName, argument);
                                }
                                if (node.getParent() instanceof ExpressionStatement) {
                                    this._nodesToRemove.add(node.getParent());
                                }
                                this.markConstructorParameterForRemoval(node, parameter);
                            }
                        }
                        else if (this._baseConstructorCalled && resolvedField != null && this.context.getCurrentMethod().isConstructor() && (!this.context.getCurrentMethod().isSynthetic() || this.context.getSettings().getShowSyntheticMembers())) {
                            final MemberReferenceExpression leftMemberReference = (MemberReferenceExpression)left;
                            final MemberReference leftMember = leftMemberReference.getUserData(Keys.MEMBER_REFERENCE);
                            final Variable rightVariable = right.getUserData(Keys.VARIABLE);
                            if (rightVariable.isParameter()) {
                                final ParameterDefinition parameter2 = variable.getOriginalParameter();
                                final int parameterIndex2 = parameter2.getPosition();
                                if (parameterIndex2 >= 0 && parameterIndex2 < this._originalArguments.size()) {
                                    final Expression argument2 = this._originalArguments.get(parameterIndex2);
                                    if (parameterIndex2 == 0 && argument2 instanceof ThisReferenceExpression && LocalClassHelper.access$1(this.context.getCurrentType())) {
                                        return null;
                                    }
                                    final FieldDefinition resolvedTargetField = ((FieldReference)leftMember).resolve();
                                    if (resolvedTargetField != null && !resolvedTargetField.isSynthetic()) {
                                        this._parametersToRemove.add(parameter2);
                                        this._initializers.put(resolvedTargetField.getFullName(), argument2);
                                        if (node.getParent() instanceof ExpressionStatement) {
                                            this._nodesToRemove.add(node.getParent());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else if (this._baseConstructorCalled && right instanceof MemberReferenceExpression) {
                    final MemberReferenceExpression leftMemberReference2 = (MemberReferenceExpression)left;
                    final MemberReference leftMember2 = leftMemberReference2.getUserData(Keys.MEMBER_REFERENCE);
                    final MemberReferenceExpression rightMemberReference = (MemberReferenceExpression)right;
                    final MemberReference rightMember = right.getUserData(Keys.MEMBER_REFERENCE);
                    if (rightMember instanceof FieldReference && rightMemberReference.getTarget() instanceof ThisReferenceExpression) {
                        final FieldDefinition resolvedTargetField2 = ((FieldReference)leftMember2).resolve();
                        final FieldDefinition resolvedSourceField = ((FieldReference)rightMember).resolve();
                        if (resolvedSourceField != null && resolvedTargetField2 != null && resolvedSourceField.isSynthetic() && !resolvedTargetField2.isSynthetic()) {
                            final Expression initializer = this._replacements.get(rightMember.getFullName());
                            if (initializer != null) {
                                this._initializers.put(resolvedTargetField2.getFullName(), initializer);
                                if (node.getParent() instanceof ExpressionStatement) {
                                    this._nodesToRemove.add(node.getParent());
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        private void markConstructorParameterForRemoval(final AssignmentExpression node, final ParameterDefinition parameter) {
            final ConstructorDeclaration constructorDeclaration = node.getParent(ConstructorDeclaration.class);
            if (constructorDeclaration != null) {
                final AstNodeCollection<ParameterDeclaration> parameters = constructorDeclaration.getParameters();
                for (final ParameterDeclaration p : parameters) {
                    if (p.getUserData(Keys.PARAMETER_DEFINITION) == parameter) {
                        this._nodesToRemove.add(p);
                        break;
                    }
                }
            }
        }
    }
    
    private static final class ClosureRewriterPhaseTwoVisitor extends ContextTrackingVisitor<Void>
    {
        private final Map<String, Expression> _replacements;
        private final Map<String, Expression> _initializers;
        
        protected ClosureRewriterPhaseTwoVisitor(final DecompilerContext context, final Map<String, Expression> replacements, final Map<String, Expression> initializers) {
            super(context);
            this._replacements = VerifyArgument.notNull(replacements, "replacements");
            this._initializers = VerifyArgument.notNull(initializers, "initializers");
        }
        
        @Override
        public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
            super.visitFieldDeclaration(node, data);
            final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);
            if (field != null && !this._initializers.isEmpty() && node.getVariables().size() == 1 && node.getVariables().firstOrNullObject().getInitializer().isNull()) {
                final Expression initializer = this._initializers.get(field.getFullName());
                if (initializer != null) {
                    node.getVariables().firstOrNullObject().setInitializer(initializer.clone());
                }
            }
            return null;
        }
        
        @Override
        public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void _) {
            super.visitMemberReferenceExpression(node, _);
            if (node.getParent() instanceof AssignmentExpression && node.getRole() == AssignmentExpression.LEFT_ROLE) {
                return null;
            }
            final MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
            if (member instanceof FieldReference) {
                final Expression replacement = this._replacements.get(member.getFullName());
                if (replacement != null) {
                    node.replaceWith(replacement.clone());
                }
            }
            return null;
        }
    }
    
    private static final class IntroduceInitializersVisitor extends ContextTrackingVisitor<Void>
    {
        public IntroduceInitializersVisitor(final DecompilerContext context) {
            super(context);
        }
        
        @Override
        public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void _) {
            super.visitSuperReferenceExpression(node, _);
            if (this.context.getCurrentMethod() != null && this.context.getCurrentMethod().isConstructor() && this.context.getCurrentMethod().getDeclaringType().isAnonymous() && node.getParent() instanceof InvocationExpression && node.getRole() == Roles.TARGET_EXPRESSION) {
                final Statement parentStatement = CollectionUtilities.firstOrDefault(node.getAncestors(Statement.class));
                final ConstructorDeclaration constructor = CollectionUtilities.firstOrDefault(node.getAncestors(ConstructorDeclaration.class));
                if (parentStatement == null || constructor == null || constructor.getParent() == null || parentStatement.getNextStatement() == null) {
                    return null;
                }
                final InstanceInitializer initializer = new InstanceInitializer();
                final BlockStatement initializerBody = new BlockStatement();
                Statement next;
                for (Statement current = parentStatement.getNextStatement(); current != null; current = next) {
                    next = current.getNextStatement();
                    current.remove();
                    initializerBody.addChild(current, current.getRole());
                }
                initializer.setBody(initializerBody);
                constructor.getParent().insertChildAfter(constructor, initializer, Roles.TYPE_MEMBER);
            }
            return null;
        }
    }
    
    private static class ThisReferenceReplacingVisitor extends ContextTrackingVisitor<Void>
    {
        private final TypeDefinition _innerClass;
        
        public ThisReferenceReplacingVisitor(final DecompilerContext context, final TypeDefinition innerClass) {
            super(context);
            this._innerClass = innerClass;
        }
        
        @Override
        public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
            super.visitMemberReferenceExpression(node, data);
            if (node.getTarget() instanceof ThisReferenceExpression) {
                final ThisReferenceExpression thisReference = (ThisReferenceExpression)node.getTarget();
                final Expression target = thisReference.getTarget();
                if (target == null || target.isNull()) {
                    MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
                    if (member == null && node.getParent() instanceof InvocationExpression) {
                        member = node.getParent().getUserData(Keys.MEMBER_REFERENCE);
                    }
                    if (member != null && MetadataHelper.isEnclosedBy(this._innerClass, member.getDeclaringType())) {
                        final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                        if (astBuilder != null) {
                            thisReference.setTarget(new TypeReferenceExpression(thisReference.getOffset(), astBuilder.convertType(member.getDeclaringType(), LocalClassHelper.access$2())));
                        }
                    }
                }
            }
            return null;
        }
    }
}
