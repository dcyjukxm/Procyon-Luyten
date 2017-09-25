package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.languages.java.ast.*;

public class RewriteLocalClassesTransform extends ContextTrackingVisitor<Void>
{
    private final Map<TypeReference, TypeDeclaration> _localTypes;
    private final Map<TypeReference, List<ObjectCreationExpression>> _instantiations;
    
    public RewriteLocalClassesTransform(final DecompilerContext context) {
        super(context);
        this._localTypes = new LinkedHashMap<TypeReference, TypeDeclaration>();
        this._instantiations = new LinkedHashMap<TypeReference, List<ObjectCreationExpression>>();
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        final PhaseOneVisitor phaseOneVisitor = new PhaseOneVisitor(this.context);
        compilationUnit.acceptVisitor((IAstVisitor<? super Object, ?>)phaseOneVisitor, (Object)null);
        super.run(compilationUnit);
        for (final TypeReference localType : this._localTypes.keySet()) {
            final TypeDeclaration declaration = this._localTypes.get(localType);
            final List<ObjectCreationExpression> instantiations = this._instantiations.get(localType);
            LocalClassHelper.replaceClosureMembers(this.context, declaration, (instantiations != null) ? instantiations : Collections.emptyList());
        }
    }
    
    @Override
    public Void visitObjectCreationExpression(final ObjectCreationExpression node, final Void _) {
        super.visitObjectCreationExpression(node, _);
        final TypeReference type = node.getType().getUserData(Keys.TYPE_REFERENCE);
        final TypeDefinition resolvedType = (type != null) ? type.resolve() : null;
        if (resolvedType != null && isLocalOrAnonymous(resolvedType)) {
            List<ObjectCreationExpression> instantiations = this._instantiations.get(type);
            if (instantiations == null) {
                this._instantiations.put(type, instantiations = new ArrayList<ObjectCreationExpression>());
            }
            instantiations.add(node);
        }
        return null;
    }
    
    private static boolean isLocalOrAnonymous(final TypeDefinition type) {
        return type != null && (type.isLocalClass() || type.isAnonymous());
    }
    
    @Override
    public Void visitAnonymousObjectCreationExpression(final AnonymousObjectCreationExpression node, final Void _) {
        super.visitAnonymousObjectCreationExpression(node, _);
        final TypeDefinition resolvedType = node.getTypeDeclaration().getUserData(Keys.TYPE_DEFINITION);
        if (resolvedType != null && isLocalOrAnonymous(resolvedType)) {
            List<ObjectCreationExpression> instantiations = this._instantiations.get(resolvedType);
            if (instantiations == null) {
                this._instantiations.put(resolvedType, instantiations = new ArrayList<ObjectCreationExpression>());
            }
            instantiations.add(node);
        }
        return null;
    }
    
    static /* synthetic */ boolean access$2(final TypeDefinition param_0) {
        return isLocalOrAnonymous(param_0);
    }
    
    static /* synthetic */ Map access$3(final RewriteLocalClassesTransform param_0) {
        return param_0._localTypes;
    }
    
    private final class PhaseOneVisitor extends ContextTrackingVisitor<Void>
    {
        protected PhaseOneVisitor(final DecompilerContext context) {
            super(context);
        }
        
        @Override
        public Void visitTypeDeclaration(final TypeDeclaration typeDeclaration, final Void _) {
            final TypeDefinition type = typeDeclaration.getUserData(Keys.TYPE_DEFINITION);
            if (type != null && (RewriteLocalClassesTransform.access$2(type) || type.isAnonymous())) {
                RewriteLocalClassesTransform.access$3(RewriteLocalClassesTransform.this).put(type, typeDeclaration);
            }
            return super.visitTypeDeclaration(typeDeclaration, _);
        }
    }
}
