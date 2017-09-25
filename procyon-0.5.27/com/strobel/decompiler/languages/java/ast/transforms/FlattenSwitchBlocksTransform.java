package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;
import java.util.*;

public class FlattenSwitchBlocksTransform extends ContextTrackingVisitor<AstNode> implements IAstTransform
{
    public FlattenSwitchBlocksTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        if (this.context.getSettings().getFlattenSwitchBlocks()) {
            compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
        }
    }
    
    @Override
    public AstNode visitSwitchSection(final SwitchSection node, final Void _) {
        if (node.getStatements().size() != 1) {
            return super.visitSwitchSection(node, _);
        }
        final Statement firstStatement = node.getStatements().firstOrNullObject();
        if (firstStatement instanceof BlockStatement) {
            final BlockStatement block = (BlockStatement)firstStatement;
            final boolean declaresVariables = CollectionUtilities.any(block.getStatements(), new Predicate<Statement>() {
                @Override
                public boolean test(final Statement s) {
                    return s instanceof VariableDeclarationStatement;
                }
            });
            if (!declaresVariables) {
                block.remove();
                block.getStatements().moveTo(node.getStatements());
            }
        }
        return super.visitSwitchSection(node, _);
    }
}
