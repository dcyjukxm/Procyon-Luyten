package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.utilities.*;
import com.strobel.functions.*;
import com.strobel.decompiler.semantics.*;
import java.util.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.annotations.*;

public class RemoveRedundantCastsTransform extends ContextTrackingVisitor<Void>
{
    private final JavaResolver _resolver;
    
    public RemoveRedundantCastsTransform(final DecompilerContext context) {
        super(context);
        this._resolver = new JavaResolver(context);
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        if (this.context.getSettings().getRetainRedundantCasts()) {
            return;
        }
        super.run(compilationUnit);
    }
    
    @Override
    public Void visitCastExpression(final CastExpression node, final Void data) {
        super.visitCastExpression(node, data);
        final List<CastExpression> redundantCasts = RedundantCastUtility.getRedundantCastsInside(this._resolver, skipParenthesesUp(node.getParent()));
        if (redundantCasts.contains(node)) {
            RedundantCastUtility.removeCast(node);
        }
        return null;
    }
    
    @Nullable
    private static AstNode skipParenthesesUp(final AstNode e) {
        AstNode result;
        for (result = e; result instanceof ParenthesizedExpression; result = result.getParent()) {}
        return result;
    }
}
