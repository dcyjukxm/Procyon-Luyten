package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;

public class RewriteInnerClassConstructorCalls extends ContextTrackingVisitor<Void>
{
    private final JavaResolver _resolver;
    
    public RewriteInnerClassConstructorCalls(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void data) {
        super.visitObjectCreationExpression(node, data);
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (!arguments.isEmpty()) {
            final Expression firstArgument = arguments.firstOrNullObject();
            final ResolveResult resolvedArgument = this._resolver.apply((AstNode)firstArgument);
            if (resolvedArgument != null) {
                final TypeReference createdType = node.getType().getUserData(Keys.TYPE_REFERENCE);
                final TypeReference argumentType = resolvedArgument.getType();
                if (createdType != null && argumentType != null) {
                    final TypeDefinition resolvedCreatedType = createdType.resolve();
                    if (resolvedCreatedType != null && resolvedCreatedType.isInnerClass() && !resolvedCreatedType.isStatic() && isEnclosedBy(resolvedCreatedType, argumentType)) {
                        if (this.isContextWithinTypeInstance(argumentType) && firstArgument instanceof ThisReferenceExpression) {
                            final MethodReference constructor = node.getUserData(Keys.MEMBER_REFERENCE);
                            if (constructor != null && arguments.size() == constructor.getParameters().size()) {
                                firstArgument.remove();
                            }
                        }
                        else {
                            firstArgument.remove();
                            node.setTarget(firstArgument);
                            final SimpleType type = new SimpleType(resolvedCreatedType.getSimpleName());
                            type.putUserData(Keys.TYPE_REFERENCE, resolvedCreatedType);
                            node.getType().replaceWith(type);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public Void visitSuperReferenceExpression(final SuperReferenceExpression node, final Void data) {
        super.visitSuperReferenceExpression(node, data);
        if (node.getParent() instanceof InvocationExpression) {
            final InvocationExpression parent = (InvocationExpression)node.getParent();
            if (!parent.getArguments().isEmpty()) {
                final Expression firstArgument = parent.getArguments().firstOrNullObject();
                final ResolveResult resolvedArgument = this._resolver.apply((AstNode)firstArgument);
                if (resolvedArgument != null) {
                    final TypeReference superType = node.getUserData(Keys.TYPE_REFERENCE);
                    final TypeReference argumentType = resolvedArgument.getType();
                    if (superType != null && argumentType != null) {
                        final TypeDefinition resolvedSuperType = superType.resolve();
                        if (resolvedSuperType != null && resolvedSuperType.isInnerClass() && !resolvedSuperType.isStatic() && isEnclosedBy(this.context.getCurrentType(), argumentType)) {
                            firstArgument.remove();
                            if (!(firstArgument instanceof ThisReferenceExpression)) {
                                node.setTarget(firstArgument);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean isEnclosedBy(final TypeReference innerType, final TypeReference outerType) {
        if (innerType == null) {
            return false;
        }
        for (TypeReference current = innerType.getDeclaringType(); current != null; current = current.getDeclaringType()) {
            if (MetadataResolver.areEquivalent(current, outerType)) {
                return true;
            }
        }
        final TypeDefinition resolvedInnerType = innerType.resolve();
        return resolvedInnerType != null && isEnclosedBy(resolvedInnerType.getBaseType(), outerType);
    }
    
    private boolean isContextWithinTypeInstance(final TypeReference type) {
        final MethodReference method = this.context.getCurrentMethod();
        if (method != null) {
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod != null && resolvedMethod.isStatic()) {
                return false;
            }
        }
        TypeReference current;
        for (TypeReference scope = current = this.context.getCurrentType(); current != null; current = current.getDeclaringType()) {
            if (MetadataResolver.areEquivalent(current, type)) {
                return true;
            }
            final TypeDefinition resolved = current.resolve();
            if (resolved != null && resolved.isLocalClass()) {
                final MethodReference declaringMethod = resolved.getDeclaringMethod();
                if (declaringMethod != null) {
                    final MethodDefinition resolvedDeclaringMethod = declaringMethod.resolve();
                    if (resolvedDeclaringMethod != null && resolvedDeclaringMethod.isStatic()) {
                        break;
                    }
                }
            }
        }
        return false;
    }
}
