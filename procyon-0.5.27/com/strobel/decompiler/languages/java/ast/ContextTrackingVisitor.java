package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.java.ast.transforms.*;
import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public abstract class ContextTrackingVisitor<TResult> extends DepthFirstAstVisitor<Void, TResult> implements IAstTransform
{
    protected final DecompilerContext context;
    
    protected ContextTrackingVisitor(final DecompilerContext context) {
        super();
        this.context = VerifyArgument.notNull(context, "context");
    }
    
    @Override
    public TResult visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void _) {
        final TypeDefinition oldType = this.context.getCurrentType();
        final MethodDefinition oldMethod = this.context.getCurrentMethod();
        try {
            this.context.setCurrentType(typeDeclaration.getUserData(Keys.TYPE_DEFINITION));
            this.context.setCurrentMethod(null);
            return super.visitTypeDeclaration(typeDeclaration, _);
        }
        finally {
            this.context.setCurrentType(oldType);
            this.context.setCurrentMethod(oldMethod);
        }
    }
    
    @Override
    public TResult visitMethodDeclaration(final MethodDeclaration node, final Void _) {
        assert this.context.getCurrentMethod() == null;
        try {
            this.context.setCurrentMethod(node.getUserData(Keys.METHOD_DEFINITION));
            return super.visitMethodDeclaration(node, _);
        }
        finally {
            this.context.setCurrentMethod(null);
        }
    }
    
    @Override
    public TResult visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
        assert this.context.getCurrentMethod() == null;
        try {
            this.context.setCurrentMethod(node.getUserData(Keys.METHOD_DEFINITION));
            return super.visitConstructorDeclaration(node, _);
        }
        finally {
            this.context.setCurrentMethod(null);
        }
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)this, (Object)null);
    }
}
