package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class InliningHelper
{
    public static AstNode inlineMethod(final MethodDeclaration method, final Map<ParameterDefinition, ? extends AstNode> argumentMappings) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(argumentMappings, "argumentMappings");
        final DecompilerContext context = new DecompilerContext();
        final MethodDefinition definition = method.getUserData(Keys.METHOD_DEFINITION);
        if (definition != null) {
            context.setCurrentType(definition.getDeclaringType());
        }
        final InliningVisitor visitor = new InliningVisitor(context, argumentMappings);
        visitor.run(method);
        return visitor.getInlinedBody();
    }
    
    private static class InliningVisitor extends ContextTrackingVisitor<Void>
    {
        private final Map<ParameterDefinition, ? extends AstNode> _argumentMappings;
        private AstNode _result;
        
        public InliningVisitor(final DecompilerContext context, final Map<ParameterDefinition, ? extends AstNode> argumentMappings) {
            super(context);
            this._argumentMappings = VerifyArgument.notNull(argumentMappings, "argumentMappings");
        }
        
        public final AstNode getInlinedBody() {
            return this._result;
        }
        
        @Override
        public void run(final AstNode root) {
            if (!(root instanceof MethodDeclaration)) {
                throw new IllegalArgumentException("InliningVisitor must be run against a MethodDeclaration.");
            }
            final MethodDeclaration clone = (MethodDeclaration)root.clone();
            super.run(clone);
            final BlockStatement body = clone.getBody();
            final AstNodeCollection<Statement> statements = body.getStatements();
            if (statements.size() == 1) {
                final Statement firstStatement = statements.firstOrNullObject();
                if (firstStatement instanceof ExpressionStatement || firstStatement instanceof ReturnStatement) {
                    (this._result = firstStatement.getChildByRole(Roles.EXPRESSION)).remove();
                    return;
                }
            }
            (this._result = body).remove();
        }
        
        @Override
        public Void visitIdentifierExpression(final IdentifierExpression node, final Void _) {
            final Variable variable = node.getUserData(Keys.VARIABLE);
            if (variable != null && variable.isParameter()) {
                final ParameterDefinition parameter = variable.getOriginalParameter();
                if (this.areMethodsEquivalent((MethodReference)parameter.getMethod(), this.context.getCurrentMethod())) {
                    final AstNode replacement = (AstNode)this._argumentMappings.get(parameter);
                    if (replacement != null) {
                        node.replaceWith(replacement.clone());
                        return null;
                    }
                }
            }
            return super.visitIdentifierExpression(node, _);
        }
        
        private boolean areMethodsEquivalent(final MethodReference m1, final MethodDefinition m2) {
            return m1 == m2 || (m1 != null && m2 != null && (StringUtilities.equals(m1.getFullName(), m2.getFullName()) && StringUtilities.equals(m1.getErasedSignature(), m2.getErasedSignature())));
        }
    }
}
