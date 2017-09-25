package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.*;

public class LabelCleanupTransform extends ContextTrackingVisitor<Void>
{
    public LabelCleanupTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitLabeledStatement(final LabeledStatement node, final Void data) {
        super.visitLabeledStatement(node, data);
        if (node.getStatement() instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement)node.getStatement();
            if (block.getStatements().hasSingleElement() && block.getStatements().firstOrNullObject() instanceof LabeledStatement) {
                final LabeledStatement nestedLabeledStatement = block.getStatements().firstOrNullObject();
                final String nextLabel = nestedLabeledStatement.getChildByRole(Roles.LABEL).getName();
                this.redirectLabels(node, node.getLabel(), nextLabel);
                nestedLabeledStatement.remove();
                node.replaceWith(nestedLabeledStatement);
            }
        }
        return null;
    }
    
    @Override
    public Void visitLabelStatement(final LabelStatement node, final Void data) {
        super.visitLabelStatement(node, data);
        final Statement next = node.getNextStatement();
        if (next == null) {
            return null;
        }
        if (next instanceof LabelStatement || next instanceof LabeledStatement) {
            final String nextLabel = next.getChildByRole(Roles.LABEL).getName();
            this.redirectLabels(node.getParent(), node.getLabel(), nextLabel);
            node.remove();
        }
        else {
            next.remove();
            node.replaceWith(new LabeledStatement(node.getLabel(), AstNode.isLoop(next) ? next : new BlockStatement(new Statement[] { next })));
        }
        return null;
    }
    
    private void redirectLabels(final AstNode node, final String labelName, final String nextLabel) {
        for (final AstNode n : node.getDescendantsAndSelf()) {
            if (AstNode.isUnconditionalBranch(n)) {
                final Identifier label = n.getChildByRole(Roles.IDENTIFIER);
                if (label.isNull() || !StringUtilities.equals(label.getName(), labelName)) {
                    continue;
                }
                label.setName(nextLabel);
            }
        }
    }
}
