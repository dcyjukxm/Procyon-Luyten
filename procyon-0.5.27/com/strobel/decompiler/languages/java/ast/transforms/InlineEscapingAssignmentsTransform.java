package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;

public class InlineEscapingAssignmentsTransform extends ContextTrackingVisitor<Void>
{
    public InlineEscapingAssignmentsTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitReturnStatement(final ReturnStatement node, final Void data) {
        super.visitReturnStatement(node, data);
        this.tryInlineValue(node.getPreviousStatement(), node.getExpression());
        return null;
    }
    
    @Override
    public Void visitThrowStatement(final ThrowStatement node, final Void data) {
        super.visitThrowStatement(node, data);
        this.tryInlineValue(node.getPreviousStatement(), node.getExpression());
        return null;
    }
    
    private void tryInlineValue(final Statement previous, final Expression value) {
        if (!(previous instanceof VariableDeclarationStatement) || value == null || value.isNull()) {
            return;
        }
        final VariableDeclarationStatement d = (VariableDeclarationStatement)previous;
        final AstNodeCollection<VariableInitializer> variables = d.getVariables();
        final VariableInitializer initializer = variables.firstOrNullObject();
        final Variable variable = initializer.getUserData(Keys.VARIABLE);
        if (variable != null && variable.getOriginalVariable() != null && variable.getOriginalVariable().isFromMetadata()) {
            return;
        }
        if (variables.hasSingleElement() && value instanceof IdentifierExpression && Pattern.matchString(initializer.getName(), ((IdentifierExpression)value).getIdentifier())) {
            final Expression assignedValue = initializer.getInitializer();
            previous.remove();
            assignedValue.remove();
            value.replaceWith(assignedValue);
        }
    }
}
