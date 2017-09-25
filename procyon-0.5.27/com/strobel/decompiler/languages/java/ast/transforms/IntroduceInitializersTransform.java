package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class IntroduceInitializersTransform extends ContextTrackingVisitor<Void>
{
    private final Map<String, FieldDeclaration> _fieldDeclarations;
    private final Map<String, AssignmentExpression> _initializers;
    private MethodDefinition _currentInitializerMethod;
    private MethodDefinition _currentConstructor;
    private static final INode FIELD_ASSIGNMENT;
    
    static {
        FIELD_ASSIGNMENT = new AssignmentExpression(new MemberReferenceTypeNode("target", new Choice(new INode[] { new MemberReferenceExpression(-34, new Choice(new INode[] { new TypedNode(TypeReferenceExpression.class), new TypedNode(ThisReferenceExpression.class) }).toExpression(), "$any$", new AstType[0]), new IdentifierExpression(-34, "$any$") }).toExpression(), FieldReference.class).toExpression(), AssignmentOperatorType.ASSIGN, new AnyNode("value").toExpression());
    }
    
    public IntroduceInitializersTransform(final DecompilerContext context) {
        super(context);
        this._fieldDeclarations = new HashMap<String, FieldDeclaration>();
        this._initializers = new HashMap<String, AssignmentExpression>();
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        new ContextTrackingVisitor<Void>(this.context) {
            @Override
            public Void visitFieldDeclaration(final FieldDeclaration node, final Void _) {
                final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);
                if (field != null) {
                    IntroduceInitializersTransform.access$4(IntroduceInitializersTransform.this).put(field.getFullName(), node);
                }
                return super.visitFieldDeclaration(node, _);
            }
        }.run(compilationUnit);
        super.run(compilationUnit);
        this.inlineInitializers();
        LocalClassHelper.introduceInitializerBlocks(this.context, compilationUnit);
    }
    
    private void inlineInitializers() {
        for (final String fieldName : this._initializers.keySet()) {
            final FieldDeclaration declaration = this._fieldDeclarations.get(fieldName);
            if (declaration != null && declaration.getVariables().firstOrNullObject().getInitializer().isNull()) {
                final AssignmentExpression assignment = this._initializers.get(fieldName);
                final Expression value = assignment.getRight();
                value.remove();
                declaration.getVariables().firstOrNullObject().setInitializer(value);
                final AstNode parent = assignment.getParent();
                if (parent instanceof ExpressionStatement) {
                    parent.remove();
                }
                else if (parent.getRole() == Roles.VARIABLE) {
                    final Expression left = assignment.getLeft();
                    left.remove();
                    assignment.replaceWith(left);
                }
                else {
                    final Expression left = assignment.getLeft();
                    left.remove();
                    parent.replaceWith(left);
                }
            }
        }
    }
    
    @Override
    public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void data) {
        final MethodDefinition oldInitializer = this._currentInitializerMethod;
        final MethodDefinition oldConstructor = this._currentConstructor;
        this._currentInitializerMethod = null;
        this._currentConstructor = null;
        try {
            return super.visitAnonymousObjectCreationExpression(node, data);
        }
        finally {
            this._currentInitializerMethod = oldInitializer;
            this._currentConstructor = oldConstructor;
        }
    }
    
    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
        final MethodDefinition oldInitializer = this._currentInitializerMethod;
        final MethodDefinition oldConstructor = this._currentConstructor;
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
        if (method != null && method.isTypeInitializer() && method.getDeclaringType().isInterface()) {
            this._currentConstructor = null;
            this._currentInitializerMethod = method;
        }
        else {
            this._currentConstructor = ((method != null && method.isConstructor()) ? method : null);
            this._currentInitializerMethod = null;
        }
        try {
            return super.visitMethodDeclaration(node, _);
        }
        finally {
            this._currentConstructor = oldConstructor;
            this._currentInitializerMethod = oldInitializer;
        }
    }
    
    @Override
    public Void visitAssignmentExpression(final AssignmentExpression node, final Void data) {
        super.visitAssignmentExpression(node, data);
        if ((this._currentInitializerMethod == null && this._currentConstructor == null) || this.context.getCurrentType() == null) {
            return null;
        }
        final Match match = IntroduceInitializersTransform.FIELD_ASSIGNMENT.match(node);
        if (match.success()) {
            final Expression target = CollectionUtilities.firstOrDefault(match.get("target"));
            final FieldReference reference = target.getUserData(Keys.MEMBER_REFERENCE);
            final FieldDefinition definition = reference.resolve();
            if (definition != null && definition.isFinal() && definition.getConstantValue() != null) {
                node.remove();
                return null;
            }
            if (this._currentInitializerMethod != null && StringUtilities.equals(this.context.getCurrentType().getInternalName(), reference.getDeclaringType().getInternalName())) {
                this._initializers.put(reference.getFullName(), node);
            }
        }
        return null;
    }
    
    @Override
    public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void _) {
        super.visitSuperReferenceExpression(node, _);
        final MethodDefinition method = this.context.getCurrentMethod();
        if (method != null && method.isConstructor() && (method.isSynthetic() || method.getDeclaringType().isAnonymous()) && node.getParent() instanceof InvocationExpression && node.getRole() == Roles.TARGET_EXPRESSION) {
            final Statement parentStatement = CollectionUtilities.firstOrDefault(node.getAncestors(Statement.class));
            final ConstructorDeclaration constructor = CollectionUtilities.firstOrDefault(node.getAncestors(ConstructorDeclaration.class));
            if (parentStatement == null || constructor == null || constructor.getParent() == null || parentStatement.getNextStatement() == null) {
                return null;
            }
            Statement next;
            for (Statement current = parentStatement.getNextStatement(); current instanceof ExpressionStatement; current = next) {
                next = current.getNextStatement();
                final Expression expression = ((ExpressionStatement)current).getExpression();
                final Match match = IntroduceInitializersTransform.FIELD_ASSIGNMENT.match(expression);
                if (!match.success()) {
                    break;
                }
                final Expression target = CollectionUtilities.firstOrDefault(match.get("target"));
                final MemberReference reference = target.getUserData(Keys.MEMBER_REFERENCE);
                if (StringUtilities.equals(this.context.getCurrentType().getInternalName(), reference.getDeclaringType().getInternalName())) {
                    this._initializers.put(reference.getFullName(), (AssignmentExpression)expression);
                }
            }
        }
        return null;
    }
    
    static /* synthetic */ Map access$4(final IntroduceInitializersTransform param_0) {
        return param_0._fieldDeclarations;
    }
}
