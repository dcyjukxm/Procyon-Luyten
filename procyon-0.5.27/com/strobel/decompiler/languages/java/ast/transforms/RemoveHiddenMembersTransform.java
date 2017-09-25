package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;
import com.strobel.core.*;

public class RemoveHiddenMembersTransform extends ContextTrackingVisitor<Void>
{
    private static final INode DEFAULT_CONSTRUCTOR_BODY;
    
    static {
        DEFAULT_CONSTRUCTOR_BODY = new BlockStatement(new Statement[] { new ExpressionStatement(new InvocationExpression(-34, new SuperReferenceExpression(-34), new Expression[0])) });
    }
    
    public RemoveHiddenMembersTransform(final DecompilerContext context) {
        super(context);
    }
    
    @Override
    public Void visitTypeDeclaration(final TypeDeclaration node, final Void _) {
        if (!(node.getParent() instanceof CompilationUnit)) {
            final TypeDefinition type = node.getUserData(Keys.TYPE_DEFINITION);
            if (type != null && AstBuilder.isMemberHidden(type, this.context)) {
                node.remove();
                return null;
            }
        }
        return super.visitTypeDeclaration(node, _);
    }
    
    @Override
    public Void visitFieldDeclaration(final FieldDeclaration node, final Void data) {
        final FieldDefinition field = node.getUserData(Keys.FIELD_DEFINITION);
        if (field != null && AstBuilder.isMemberHidden(field, this.context)) {
            node.remove();
            return null;
        }
        return super.visitFieldDeclaration(node, data);
    }
    
    @Override
    public Void visitMethodDeclaration(final MethodDeclaration node, final Void _) {
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
        if (method != null) {
            if (AstBuilder.isMemberHidden(method, this.context)) {
                node.remove();
                return null;
            }
            if (method.isTypeInitializer() && node.getBody().getStatements().isEmpty()) {
                node.remove();
                return null;
            }
        }
        return super.visitMethodDeclaration(node, _);
    }
    
    @Override
    public Void visitConstructorDeclaration(final ConstructorDeclaration node, final Void _) {
        final MethodDefinition method = node.getUserData(Keys.METHOD_DEFINITION);
        if (method != null) {
            if (AstBuilder.isMemberHidden(method, this.context)) {
                if (method.getDeclaringType().isEnum() && method.getDeclaringType().isAnonymous() && !node.getBody().getStatements().isEmpty()) {
                    return super.visitConstructorDeclaration(node, _);
                }
                node.remove();
                return null;
            }
            else if (!this.context.getSettings().getShowSyntheticMembers() && node.getParameters().isEmpty() && RemoveHiddenMembersTransform.DEFAULT_CONSTRUCTOR_BODY.matches(node.getBody())) {
                final TypeDefinition declaringType = method.getDeclaringType();
                if (declaringType != null) {
                    final boolean hasOtherConstructors = CollectionUtilities.any(declaringType.getDeclaredMethods(), new Predicate<MethodDefinition>() {
                        @Override
                        public boolean test(final MethodDefinition m) {
                            return m.isConstructor() && !m.isSynthetic() && !StringUtilities.equals(m.getErasedSignature(), method.getErasedSignature());
                        }
                    });
                    if (!hasOtherConstructors) {
                        node.remove();
                        return null;
                    }
                }
            }
        }
        return super.visitConstructorDeclaration(node, _);
    }
}
