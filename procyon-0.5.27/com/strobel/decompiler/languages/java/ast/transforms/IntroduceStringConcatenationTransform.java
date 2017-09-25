package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

public class IntroduceStringConcatenationTransform extends ContextTrackingVisitor<Void>
{
    private final INode _stringBuilderArgumentPattern;
    
    public IntroduceStringConcatenationTransform(final DecompilerContext context) {
        super(context);
        this._stringBuilderArgumentPattern = new OptionalNode(new TypedExpression("firstArgument", CommonTypeReferences.String, new JavaResolver(context)));
    }
    
    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void data) {
        final AstNodeCollection<Expression> arguments = node.getArguments();
        if (arguments.isEmpty() || arguments.hasSingleElement()) {
            Expression firstArgument;
            if (arguments.hasSingleElement()) {
                final Match m = this._stringBuilderArgumentPattern.match(arguments.firstOrNullObject());
                if (!m.success()) {
                    return super.visitObjectCreationExpression(node, data);
                }
                firstArgument = CollectionUtilities.firstOrDefault(m.get("firstArgument"));
            }
            else {
                firstArgument = null;
            }
            final TypeReference typeReference = node.getType().toTypeReference();
            if (typeReference != null && this.isStringBuilder(typeReference)) {
                this.convertStringBuilderToConcatenation(node, firstArgument);
            }
        }
        return super.visitObjectCreationExpression(node, data);
    }
    
    private boolean isStringBuilder(final TypeReference typeReference) {
        return StringUtilities.equals(typeReference.getInternalName(), "java/lang/StringBuilder") || (this.context.getCurrentType() != null && this.context.getCurrentType().getCompilerMajorVersion() < 49 && StringUtilities.equals(typeReference.getInternalName(), "java/lang/StringBuffer"));
    }
    
    private void convertStringBuilderToConcatenation(final ObjectCreationExpression node, final Expression firstArgument) {
        if (node.getParent() == null || node.getParent().getParent() == null) {
            return;
        }
        final ArrayList<Expression> operands = new ArrayList<Expression>();
        if (firstArgument != null) {
            operands.add(firstArgument);
        }
        AstNode current;
        AstNode parent;
        for (current = node.getParent(), parent = current.getParent(); current instanceof MemberReferenceExpression && parent instanceof InvocationExpression && parent.getParent() != null; current = parent.getParent(), parent = current.getParent()) {
            final String memberName = ((MemberReferenceExpression)current).getMemberName();
            final AstNodeCollection<Expression> arguments = ((InvocationExpression)parent).getArguments();
            if (!StringUtilities.equals(memberName, "append") || arguments.size() != 1) {
                break;
            }
            operands.add(arguments.firstOrNullObject());
        }
        if (operands.size() > 1 && this.anyIsString(operands.subList(0, 2)) && current instanceof MemberReferenceExpression && parent instanceof InvocationExpression && !(parent.getParent() instanceof ExpressionStatement) && StringUtilities.equals(((MemberReferenceExpression)current).getMemberName(), "toString") && ((InvocationExpression)parent).getArguments().isEmpty()) {
            for (final Expression operand : operands) {
                operand.remove();
            }
            Expression concatenation = new BinaryOperatorExpression(operands.get(0), BinaryOperatorType.ADD, operands.get(1));
            for (int i = 2; i < operands.size(); ++i) {
                concatenation = new BinaryOperatorExpression(concatenation, BinaryOperatorType.ADD, operands.get(i));
            }
            parent.replaceWith(concatenation);
        }
    }
    
    private boolean anyIsString(final List<Expression> expressions) {
        final JavaResolver resolver = new JavaResolver(this.context);
        for (int i = 0; i < expressions.size(); ++i) {
            final ResolveResult result = resolver.apply((AstNode)expressions.get(i));
            if (result != null && result.getType() != null && CommonTypeReferences.String.isEquivalentTo(result.getType())) {
                return true;
            }
        }
        return false;
    }
}
