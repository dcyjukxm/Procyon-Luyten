package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;

public class FlattenElseIfStatementsTransform extends ContextTrackingVisitor<Void>
{
    public FlattenElseIfStatementsTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitIfElseStatement(final IfElseStatement node, final Void data) {
        super.visitIfElseStatement(node, data);
        final Statement trueStatement = node.getTrueStatement();
        final Statement falseStatement = node.getFalseStatement();
        if (trueStatement instanceof BlockStatement && falseStatement instanceof BlockStatement && ((BlockStatement)trueStatement).getStatements().isEmpty() && !((BlockStatement)falseStatement).getStatements().isEmpty()) {
            final Expression condition = node.getCondition();
            condition.remove();
            node.setCondition(new UnaryOperatorExpression(UnaryOperatorType.NOT, condition));
            falseStatement.remove();
            node.setTrueStatement(falseStatement);
            node.setFalseStatement(null);
            return null;
        }
        if (falseStatement instanceof BlockStatement) {
            final BlockStatement falseBlock = (BlockStatement)falseStatement;
            final AstNodeCollection<Statement> falseStatements = falseBlock.getStatements();
            if (falseStatements.hasSingleElement() && falseStatements.firstOrNullObject() instanceof IfElseStatement) {
                final Statement elseIf = falseStatements.firstOrNullObject();
                elseIf.remove();
                falseStatement.replaceWith(elseIf);
                return null;
            }
        }
        return null;
    }
}
