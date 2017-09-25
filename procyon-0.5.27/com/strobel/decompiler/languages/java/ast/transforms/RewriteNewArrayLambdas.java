package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.patterns.*;

public class RewriteNewArrayLambdas extends ContextTrackingVisitor<Void>
{
    protected RewriteNewArrayLambdas(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitLambdaExpression(final LambdaExpression node, final Void data) {
        super.visitLambdaExpression(node, data);
        final DynamicCallSite callSite = node.getUserData(Keys.DYNAMIC_CALL_SITE);
        if (callSite != null && callSite.getBootstrapArguments().size() >= 3 && callSite.getBootstrapArguments().get(2) instanceof IMethodSignature) {
            final IMethodSignature signature = callSite.getBootstrapArguments().get(2);
            if (signature.getParameters().size() == 1 && signature.getParameters().get(0).getParameterType().getSimpleType() == JvmType.Integer && signature.getReturnType().isArray() && !signature.getReturnType().getElementType().isGenericType()) {
                final LambdaExpression pattern = new LambdaExpression(-34);
                final ParameterDeclaration size = new ParameterDeclaration();
                size.setName("$any$");
                size.setAnyModifiers(true);
                size.setType(new OptionalNode(new SimpleType("int")).toType());
                pattern.getParameters().add(new NamedNode("size", size).toParameterDeclaration());
                final ArrayCreationExpression arrayCreation = new ArrayCreationExpression(-34);
                arrayCreation.getDimensions().add(new IdentifierExpressionBackReference("size").toExpression());
                arrayCreation.setType(new NamedNode("type", new AnyNode()).toType());
                pattern.setBody(arrayCreation);
                final Match match = pattern.match(node);
                if (match.success()) {
                    final AstType type = CollectionUtilities.first(match.get("type"));
                    if (signature.getReturnType().getElementType().isEquivalentTo(type.toTypeReference())) {
                        final MethodGroupExpression replacement = new MethodGroupExpression(node.getOffset(), new TypeReferenceExpression(-34, type.clone().makeArrayType()), "new");
                        final TypeReference lambdaType = node.getUserData(Keys.TYPE_REFERENCE);
                        if (lambdaType != null) {
                            replacement.putUserData(Keys.TYPE_REFERENCE, lambdaType);
                        }
                        replacement.putUserData(Keys.DYNAMIC_CALL_SITE, callSite);
                        node.replaceWith(replacement);
                    }
                }
            }
        }
        return null;
    }
}
