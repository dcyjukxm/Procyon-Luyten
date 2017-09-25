package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;
import java.util.*;

public class LambdaTransform extends ContextTrackingVisitor<Void>
{
    private final JavaResolver _resolver;
    private final Map<String, MethodDeclaration> _methodDeclarations;
    
    public LambdaTransform(final DecompilerContext context) {
        super(context);
        this._methodDeclarations = new HashMap<String, MethodDeclaration>();
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)new ContextTrackingVisitor<Void>(this.context) {
            @Override
            public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
                final MemberReference methodReference = node.getUserData(Keys.MEMBER_REFERENCE);
                if (methodReference instanceof MethodReference) {
                    LambdaTransform.access$1(LambdaTransform.this).put(LambdaTransform.access$2((MethodReference)methodReference), node);
                }
                return super.visitMethodDeclaration(node, _);
            }
        }, (Object)null);
        super.run(compilationUnit);
    }
    
    @Override
    public Void visitMethodGroupExpression(final MethodGroupExpression node, final Void data) {
        final MemberReference reference = node.getUserData(Keys.MEMBER_REFERENCE);
        if (reference instanceof MethodReference) {
            final MethodReference method = (MethodReference)reference;
            final MethodDefinition resolvedMethod = method.resolve();
            final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);
            if (resolvedMethod != null && resolvedMethod.isSynthetic() && callSite != null) {
                this.inlineLambda(node, resolvedMethod);
                return null;
            }
        }
        return super.visitMethodGroupExpression(node, data);
    }
    
    private void inlineLambda(final MethodGroupExpression methodGroup, final MethodDefinition method) {
        final MethodDeclaration declaration = this._methodDeclarations.get(makeMethodKey(method));
        if (declaration == null) {
            return;
        }
        final BlockStatement body = (BlockStatement)declaration.getBody().clone();
        final AstNodeCollection<ParameterDeclaration> parameters = declaration.getParameters();
        final Map<String, IdentifierExpression> renamedVariables = new HashMap<String, IdentifierExpression>();
        final AstNodeCollection<Expression> closureArguments = methodGroup.getClosureArguments();
        final Statement firstStatement = body.getStatements().firstOrNullObject();
        int offset;
        if (firstStatement != null && !firstStatement.isNull()) {
            offset = firstStatement.getOffset();
        }
        else {
            offset = -34;
        }
        Expression a = closureArguments.firstOrNullObject();
        for (ParameterDeclaration p = parameters.firstOrNullObject(); p != null && !p.isNull() && a != null && !a.isNull(); p = p.getNextSibling(p.getRole()), a = a.getNextSibling(a.getRole())) {
            if (a instanceof IdentifierExpression) {
                renamedVariables.put(p.getName(), (IdentifierExpression)a);
            }
        }
        body.acceptVisitor((IAstVisitor<? super Object, ?>)new ContextTrackingVisitor<Void>(this.context) {
            @Override
            public Void visitIdentifier(final Identifier node, final Void _) {
                final String oldName = node.getName();
                if (oldName != null) {
                    final IdentifierExpression newName = renamedVariables.get(oldName);
                    if (newName != null && newName.getIdentifier() != null) {
                        node.setName(newName.getIdentifier());
                    }
                }
                return super.visitIdentifier(node, _);
            }
            
            @Override
            public Void visitIdentifierExpression(final IdentifierExpression node, final Void _) {
                final String oldName = node.getIdentifier();
                if (oldName != null) {
                    final IdentifierExpression newName = renamedVariables.get(oldName);
                    if (newName != null) {
                        node.replaceWith(newName.clone());
                        return null;
                    }
                }
                return super.visitIdentifierExpression(node, _);
            }
        }, (Object)null);
        final LambdaExpression lambda = new LambdaExpression(offset);
        final DynamicCallSite callSite = methodGroup.getUserData(Keys.DYNAMIC_CALL_SITE);
        TypeReference lambdaType = methodGroup.getUserData(Keys.TYPE_REFERENCE);
        if (callSite != null) {
            lambda.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
        }
        if (lambdaType != null) {
            lambda.putUserData(Keys.TYPE_REFERENCE, lambdaType);
        }
        else {
            if (callSite == null) {
                return;
            }
            lambdaType = callSite.getMethodType().getReturnType();
        }
        body.remove();
        if (body.getStatements().size() == 1 && (firstStatement instanceof ExpressionStatement || firstStatement instanceof ReturnStatement)) {
            final Expression simpleBody = firstStatement.getChildByRole(Roles.EXPRESSION);
            simpleBody.remove();
            lambda.setBody(simpleBody);
        }
        else {
            lambda.setBody(body);
        }
        int parameterCount = 0;
        int parametersToSkip = closureArguments.size();
        for (final ParameterDeclaration p2 : declaration.getParameters()) {
            if (parametersToSkip-- > 0) {
                continue;
            }
            final ParameterDeclaration lambdaParameter = (ParameterDeclaration)p2.clone();
            lambdaParameter.setType(AstType.NULL);
            lambda.addChild(lambdaParameter, Roles.PARAMETER);
            ++parameterCount;
        }
        if (!MetadataHelper.isRawType(lambdaType)) {
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
                if (functionMethod != null && functionMethod.containsGenericParameters() && functionMethod.getParameters().size() == parameterCount) {
                    final TypeReference asMemberOf = MetadataHelper.asSuper(functionMethod.getDeclaringType(), lambdaType);
                    if (asMemberOf != null && !MetadataHelper.isRawType(asMemberOf)) {
                        functionMethod = MetadataHelper.asMemberOf(functionMethod, MetadataHelper.isRawType(asMemberOf) ? MetadataHelper.erase(asMemberOf) : asMemberOf);
                        lambda.putUserData(Keys.MEMBER_REFERENCE, functionMethod);
                        if (functionMethod != null) {
                            final List<ParameterDefinition> fp = functionMethod.getParameters();
                            int i = 0;
                            for (ParameterDeclaration p3 = lambda.getParameters().firstOrNullObject(); i < parameterCount; ++i, p3 = p3.getNextSibling(Roles.PARAMETER)) {
                                p3.putUserData(Keys.PARAMETER_DEFINITION, fp.get(i));
                            }
                        }
                    }
                }
            }
        }
        methodGroup.replaceWith(lambda);
        lambda.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
    }
    
    private static String makeMethodKey(final MethodReference method) {
        return String.valueOf(method.getFullName()) + ":" + method.getErasedSignature();
    }
    
    static /* synthetic */ Map access$1(final LambdaTransform param_0) {
        return param_0._methodDeclarations;
    }
    
    static /* synthetic */ String access$2(final MethodReference param_0) {
        return makeMethodKey(param_0);
    }
}
