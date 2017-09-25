package com.strobel.decompiler.languages.java.ast.transforms;

import javax.lang.model.element.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class EliminateSyntheticAccessorsTransform extends ContextTrackingVisitor<Void>
{
    private final List<AstNode> _nodesToRemove;
    private final Map<String, MethodDeclaration> _accessMethodDeclarations;
    private final Set<String> _visitedTypes;
    private static final MethodDeclaration SYNTHETIC_GET_ACCESSOR;
    private static final MethodDeclaration SYNTHETIC_SET_ACCESSOR;
    private static final MethodDeclaration SYNTHETIC_SET_ACCESSOR_ALT;
    private static final MethodDeclaration SYNTHETIC_STATIC_GET_ACCESSOR;
    private static final MethodDeclaration SYNTHETIC_STATIC_SET_ACCESSOR;
    private static final MethodDeclaration SYNTHETIC_STATIC_SET_ACCESSOR_ALT;
    
    static {
        final MethodDeclaration getAccessor = new MethodDeclaration();
        final MethodDeclaration setAccessor = new MethodDeclaration();
        getAccessor.setName("$any$");
        getAccessor.getModifiers().add(new JavaModifierToken(Modifier.STATIC));
        getAccessor.setReturnType(new AnyNode("returnType").toType());
        setAccessor.setName("$any$");
        setAccessor.getModifiers().add(new JavaModifierToken(Modifier.STATIC));
        setAccessor.setReturnType(new AnyNode("returnType").toType());
        final ParameterDeclaration getParameter = new ParameterDeclaration("$any$", new AnyNode("targetType").toType());
        getParameter.setAnyModifiers(true);
        getAccessor.getParameters().add(getParameter);
        final ParameterDeclaration setParameter1 = new ParameterDeclaration("$any$", new AnyNode("targetType").toType());
        final ParameterDeclaration setParameter2 = new ParameterDeclaration("$any$", new BackReference("returnType").toType());
        setParameter1.setAnyModifiers(true);
        setParameter2.setAnyModifiers(true);
        setAccessor.getParameters().add(setParameter1);
        setAccessor.getParameters().add(new OptionalNode(setParameter2).toParameterDeclaration());
        getAccessor.setBody(new BlockStatement(new Statement[] { new ReturnStatement(-34, new SubtreeMatch(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new ParameterReferenceNode(0).toExpression(), "$any$", new AstType[0]), FieldReference.class)).toExpression()) }));
        final MethodDeclaration altSetAccessor = (MethodDeclaration)setAccessor.clone();
        setAccessor.setBody(new Choice(new INode[] { new BlockStatement(new Statement[] { new ExpressionStatement(new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new ParameterReferenceNode(0).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new ParameterReferenceNode(1, "value").toExpression())), new ReturnStatement(-34, new BackReference("value").toExpression()) }), new BlockStatement(new Statement[] { new ReturnStatement(-34, new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new ParameterReferenceNode(0).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new ParameterReferenceNode(1, "value").toExpression())) }) }).toBlockStatement());
        final VariableDeclarationStatement tempVariable = new VariableDeclarationStatement(new AnyNode().toType(), "$any$", new AnyNode("value").toExpression());
        tempVariable.addModifier(Modifier.FINAL);
        altSetAccessor.setBody(new BlockStatement(new Statement[] { new NamedNode("tempVariable", tempVariable).toStatement(), new ExpressionStatement(new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new ParameterReferenceNode(0).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new SubtreeMatch(new DeclaredVariableBackReference("tempVariable")).toExpression())), new ReturnStatement(-34, new DeclaredVariableBackReference("tempVariable").toExpression()) }));
        SYNTHETIC_GET_ACCESSOR = getAccessor;
        SYNTHETIC_SET_ACCESSOR = setAccessor;
        SYNTHETIC_SET_ACCESSOR_ALT = altSetAccessor;
        final MethodDeclaration staticGetAccessor = (MethodDeclaration)getAccessor.clone();
        final MethodDeclaration staticSetAccessor = (MethodDeclaration)setAccessor.clone();
        final MethodDeclaration altStaticSetAccessor = (MethodDeclaration)altSetAccessor.clone();
        staticGetAccessor.getParameters().clear();
        staticGetAccessor.setBody(new BlockStatement(new Statement[] { new ReturnStatement(-34, new SubtreeMatch(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new TypedNode(TypeReferenceExpression.class).toExpression(), "$any$", new AstType[0]), FieldReference.class)).toExpression()) }));
        staticSetAccessor.getParameters().firstOrNullObject().remove();
        staticSetAccessor.setBody(new Choice(new INode[] { new BlockStatement(new Statement[] { new ExpressionStatement(new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new TypedNode(TypeReferenceExpression.class).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new NamedNode("value", new SubtreeMatch(new ParameterReferenceNode(0))).toExpression())), new ReturnStatement(-34, new BackReference("value").toExpression()) }), new BlockStatement(new Statement[] { new ReturnStatement(-34, new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new TypedNode(TypeReferenceExpression.class).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new NamedNode("value", new SubtreeMatch(new ParameterReferenceNode(0))).toExpression())) }) }).toBlockStatement());
        altStaticSetAccessor.getParameters().firstOrNullObject().remove();
        altStaticSetAccessor.setBody(new BlockStatement(new Statement[] { new NamedNode("tempVariable", tempVariable).toStatement(), new ExpressionStatement(new AssignmentExpression(new MemberReferenceTypeNode(new MemberReferenceExpression(-34, new TypedNode(TypeReferenceExpression.class).toExpression(), "$any$", new AstType[0]), FieldReference.class).toExpression(), AssignmentOperatorType.ANY, new SubtreeMatch(new DeclaredVariableBackReference("tempVariable")).toExpression())), new ReturnStatement(-34, new DeclaredVariableBackReference("tempVariable").toExpression()) }));
        SYNTHETIC_STATIC_GET_ACCESSOR = staticGetAccessor;
        SYNTHETIC_STATIC_SET_ACCESSOR = staticSetAccessor;
        SYNTHETIC_STATIC_SET_ACCESSOR_ALT = altStaticSetAccessor;
    }
    
    public EliminateSyntheticAccessorsTransform(final DecompilerContext context) {
        super(context);
        this._nodesToRemove = new ArrayList<AstNode>();
        this._accessMethodDeclarations = new HashMap<String, MethodDeclaration>();
        this._visitedTypes = new HashSet<String>();
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        new PhaseOneVisitor((PhaseOneVisitor)null).run(compilationUnit);
        for (final AstNode node : this._nodesToRemove) {
            node.remove();
        }
    }
    
    private static String makeMethodKey(final MethodReference method) {
        return String.valueOf(method.getFullName()) + ":" + method.getErasedSignature();
    }
    
    @Override
    public Void visitInvocationExpression(final InvocationExpression node, final Void data) {
        super.visitInvocationExpression(node, data);
        final Expression target = node.getTarget();
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (target instanceof MemberReferenceExpression) {
            final MemberReferenceExpression memberReference = (MemberReferenceExpression)target;
            MemberReference reference = memberReference.getUserData(Keys.MEMBER_REFERENCE);
            if (reference == null) {
                reference = node.getUserData(Keys.MEMBER_REFERENCE);
            }
            if (reference instanceof MethodReference) {
                final MethodReference method = (MethodReference)reference;
                final TypeReference declaringType = method.getDeclaringType();
                if (!MetadataResolver.areEquivalent(this.context.getCurrentType(), declaringType) && !MetadataHelper.isEnclosedBy(this.context.getCurrentType(), declaringType) && !this._visitedTypes.contains(declaringType.getInternalName())) {
                    final MethodDefinition resolvedMethod = method.resolve();
                    if (resolvedMethod != null && resolvedMethod.isSynthetic()) {
                        final AstBuilder astBuilder = this.context.getUserData(Keys.AST_BUILDER);
                        if (astBuilder != null) {
                            final TypeDeclaration ownerTypeDeclaration = astBuilder.createType(resolvedMethod.getDeclaringType());
                            ownerTypeDeclaration.acceptVisitor((IAstVisitor<? super Void, ?>)new PhaseOneVisitor((PhaseOneVisitor)null), data);
                        }
                    }
                }
                final String key = makeMethodKey(method);
                final MethodDeclaration declaration = this._accessMethodDeclarations.get(key);
                if (declaration != null) {
                    final MethodDefinition definition = declaration.getUserData(Keys.METHOD_DEFINITION);
                    final List<ParameterDefinition> parameters = (definition != null) ? definition.getParameters() : null;
                    if (definition != null && parameters.size() == arguments.size()) {
                        final Map<ParameterDefinition, AstNode> parameterMap = new IdentityHashMap<ParameterDefinition, AstNode>();
                        int i = 0;
                        for (final Expression argument : arguments) {
                            parameterMap.put(parameters.get(i++), argument);
                        }
                        final AstNode inlinedBody = InliningHelper.inlineMethod(declaration, parameterMap);
                        if (inlinedBody instanceof Expression) {
                            node.replaceWith(inlinedBody);
                        }
                        else if (inlinedBody instanceof BlockStatement) {
                            final BlockStatement block = (BlockStatement)inlinedBody;
                            if (block.getStatements().size() == 2) {
                                final Statement setStatement = block.getStatements().firstOrNullObject();
                                if (setStatement instanceof ExpressionStatement) {
                                    final Expression expression = ((ExpressionStatement)setStatement).getExpression();
                                    if (expression instanceof AssignmentExpression) {
                                        expression.remove();
                                        node.replaceWith(expression);
                                    }
                                }
                            }
                            else if (block.getStatements().size() == 3) {
                                final Statement tempAssignment = block.getStatements().firstOrNullObject();
                                final Statement setStatement2 = CollectionUtilities.getOrDefault(block.getStatements(), 1);
                                if (tempAssignment instanceof VariableDeclarationStatement && setStatement2 instanceof ExpressionStatement) {
                                    final Expression expression2 = ((ExpressionStatement)setStatement2).getExpression();
                                    if (expression2 instanceof AssignmentExpression) {
                                        final VariableDeclarationStatement tempVariable = (VariableDeclarationStatement)tempAssignment;
                                        final Expression initializer = tempVariable.getVariables().firstOrNullObject().getInitializer();
                                        final AssignmentExpression assignment = (AssignmentExpression)expression2;
                                        initializer.remove();
                                        assignment.setRight(initializer);
                                        expression2.remove();
                                        node.replaceWith(expression2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    static /* synthetic */ DecompilerContext access$1(final EliminateSyntheticAccessorsTransform param_0) {
        return param_0.context;
    }
    
    static /* synthetic */ Set access$2(final EliminateSyntheticAccessorsTransform param_0) {
        return param_0._visitedTypes;
    }
    
    static /* synthetic */ Map access$3(final EliminateSyntheticAccessorsTransform param_0) {
        return param_0._accessMethodDeclarations;
    }
    
    static /* synthetic */ String access$4(final MethodReference param_0) {
        return makeMethodKey(param_0);
    }
    
    static /* synthetic */ MethodDeclaration access$5() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_GET_ACCESSOR;
    }
    
    static /* synthetic */ MethodDeclaration access$6() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_SET_ACCESSOR;
    }
    
    static /* synthetic */ MethodDeclaration access$7() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_SET_ACCESSOR_ALT;
    }
    
    static /* synthetic */ MethodDeclaration access$8() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_STATIC_GET_ACCESSOR;
    }
    
    static /* synthetic */ MethodDeclaration access$9() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_STATIC_SET_ACCESSOR;
    }
    
    static /* synthetic */ MethodDeclaration access$10() {
        return EliminateSyntheticAccessorsTransform.SYNTHETIC_STATIC_SET_ACCESSOR_ALT;
    }
    
    private class PhaseOneVisitor extends ContextTrackingVisitor<Void>
    {
        private PhaseOneVisitor() {
            super(EliminateSyntheticAccessorsTransform.access$1(EliminateSyntheticAccessorsTransform.this));
        }
        
        @Override
        public Void visitTypeDeclaration(final TypeDeclaration node, final Void _) {
            final TypeDefinition type = node.getUserData(Keys.TYPE_DEFINITION);
            if (type != null && !EliminateSyntheticAccessorsTransform.access$2(EliminateSyntheticAccessorsTransform.this).add(type.getInternalName())) {
                return null;
            }
            return super.visitTypeDeclaration(node, _);
        }
        
        @Override
        public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
            final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
            if (method != null && method.isSynthetic() && method.isStatic() && (this.tryMatchAccessor(node) || this.tryMatchCallWrapper(node))) {
                EliminateSyntheticAccessorsTransform.access$3(EliminateSyntheticAccessorsTransform.this).put(EliminateSyntheticAccessorsTransform.access$4(method), node);
            }
            return super.visitMethodDeclaration(node, _);
        }
        
        private boolean tryMatchAccessor(final MethodDeclaration node) {
            return EliminateSyntheticAccessorsTransform.access$5().matches(node) || EliminateSyntheticAccessorsTransform.access$6().matches(node) || EliminateSyntheticAccessorsTransform.access$7().matches(node) || EliminateSyntheticAccessorsTransform.access$8().matches(node) || EliminateSyntheticAccessorsTransform.access$9().matches(node) || EliminateSyntheticAccessorsTransform.access$10().matches(node);
        }
        
        private boolean tryMatchCallWrapper(final MethodDeclaration node) {
            final AstNodeCollection<Statement> statements = node.getBody().getStatements();
            if (!statements.hasSingleElement()) {
                return false;
            }
            final Statement s = statements.firstOrNullObject();
            InvocationExpression invocation;
            if (s instanceof ExpressionStatement) {
                final ExpressionStatement e = (ExpressionStatement)s;
                invocation = ((e.getExpression() instanceof InvocationExpression) ? ((InvocationExpression)e.getExpression()) : null);
            }
            else if (s instanceof ReturnStatement) {
                final ReturnStatement r = (ReturnStatement)s;
                invocation = ((r.getExpression() instanceof InvocationExpression) ? ((InvocationExpression)r.getExpression()) : null);
            }
            else {
                invocation = null;
            }
            if (invocation == null) {
                return false;
            }
            final MethodReference targetMethod = invocation.getUserData(Keys.MEMBER_REFERENCE);
            final MethodDefinition resolvedTarget = (targetMethod != null) ? targetMethod.resolve() : null;
            if (resolvedTarget == null) {
                return false;
            }
            final int parametersStart = resolvedTarget.isStatic() ? 0 : 1;
            final List<ParameterDeclaration> parameterList = CollectionUtilities.toList(node.getParameters());
            final List<Expression> argumentList = CollectionUtilities.toList(invocation.getArguments());
            if (argumentList.size() != parameterList.size() - parametersStart) {
                return false;
            }
            if (!resolvedTarget.isStatic()) {
                if (!(invocation.getTarget() instanceof MemberReferenceExpression)) {
                    return false;
                }
                final MemberReferenceExpression m = (MemberReferenceExpression)invocation.getTarget();
                final Expression target = m.getTarget();
                if (!target.matches(new IdentifierExpression(-34, parameterList.get(0).getName()))) {
                    return false;
                }
            }
            int i;
            int j;
            for (i = parametersStart, j = 0; i < parameterList.size() && j < argumentList.size(); ++i, ++j) {
                final Expression pattern = new Choice(new INode[] { new CastExpression(new AnyNode().toType(), new IdentifierExpression(-34, parameterList.get(i).getName())), new IdentifierExpression(-34, parameterList.get(i).getName()) }).toExpression();
                if (!pattern.matches(argumentList.get(j))) {
                    return false;
                }
            }
            return i == j + parametersStart;
        }
    }
}
