package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.*;

public class AddReferenceQualifiersTransform extends ContextTrackingVisitor<Void>
{
    private final Set<AstNode> _addQualifierCandidates;
    private final Set<AstNode> _removeQualifierCandidates;
    private final boolean _simplifyMemberReferences;
    
    public AddReferenceQualifiersTransform(final DecompilerContext context) {
        super(context);
        this._addQualifierCandidates = new LinkedHashSet<AstNode>();
        this._removeQualifierCandidates = new LinkedHashSet<AstNode>();
        this._simplifyMemberReferences = context.getSettings().getSimplifyMemberReferences();
    }
    
    @Override
    public void run(final AstNode compilationUnit) {
        super.run(compilationUnit);
        for (final AstNode candidate : this._addQualifierCandidates) {
            if (candidate instanceof SimpleType) {
                final SimpleType type = (SimpleType)candidate;
                TypeReference referencedType = type.getUserData(Keys.ANONYMOUS_BASE_TYPE_REFERENCE);
                if (referencedType == null) {
                    referencedType = type.getUserData(Keys.TYPE_REFERENCE);
                }
                final String s = this.qualifyReference(candidate, referencedType);
                if (StringUtilities.isNullOrEmpty(s)) {
                    continue;
                }
                type.setIdentifier(s);
            }
        }
        for (final AstNode candidate : this._removeQualifierCandidates) {
            if (candidate instanceof MemberReferenceExpression) {
                final FieldReference field = candidate.getUserData(Keys.MEMBER_REFERENCE);
                if (field == null) {
                    continue;
                }
                final IdentifierExpression identifier = new IdentifierExpression(((Expression)candidate).getOffset(), field.getName());
                identifier.copyUserDataFrom(candidate);
                candidate.replaceWith(identifier);
            }
        }
    }
    
    private static NameResolveMode modeForType(final AstNode type) {
        if (type != null && type.getParent() instanceof TypeReferenceExpression && ((TypeReferenceExpression)type.getParent()).getType() == type) {
            return NameResolveMode.EXPRESSION;
        }
        return NameResolveMode.TYPE;
    }
    
    private String qualifyReference(final AstNode node, final TypeReference type) {
        if (type == null || type.isGenericParameter() || type.isWildcardType()) {
            return null;
        }
        final TypeDefinition resolvedType = type.resolve();
        final TypeReference t = (resolvedType != null) ? resolvedType : (type.isGenericType() ? type.getUnderlyingType() : type);
        final Object resolvedObject = this.resolveName(node, t.getSimpleName(), modeForType(node));
        if (resolvedObject instanceof TypeReference && MetadataHelper.isSameType(t, (TypeReference)resolvedObject)) {
            return t.getSimpleName();
        }
        if (t.isNested()) {
            final String outerReference = this.qualifyReference(node, t.getDeclaringType());
            if (outerReference != null) {
                return String.valueOf(outerReference) + "." + t.getSimpleName();
            }
        }
        if (resolvedObject != null) {
            return t.getFullName();
        }
        return null;
    }
    
    @Override
    public Void visitSimpleType(final SimpleType node, final Void data) {
        final AstNode parent = node.getParent();
        if (parent instanceof ObjectCreationExpression && ((ObjectCreationExpression)parent).getTarget() != null && !((ObjectCreationExpression)parent).getTarget().isNull()) {
            return super.visitSimpleType(node, data);
        }
        String name = node.getIdentifier();
        TypeReference type = node.getUserData(Keys.TYPE_REFERENCE);
        if (type.isGenericParameter()) {
            return super.visitSimpleType(node, data);
        }
        int i;
        while (type.isNested() && (i = name.lastIndexOf(46)) > 0 && i < name.length() - 1) {
            type = type.getDeclaringType();
            name = name.substring(0, i);
        }
        if (type != null && !type.isPrimitive()) {
            final Object resolvedObject = this.resolveName(node, name, modeForType(node));
            if (resolvedObject == null || !(resolvedObject instanceof TypeReference) || !MetadataHelper.isSameType(type, (TypeReference)resolvedObject)) {
                this._addQualifierCandidates.add(node);
            }
        }
        return super.visitSimpleType(node, data);
    }
    
    @Override
    public Void visitMemberReferenceExpression(final MemberReferenceExpression node, final Void data) {
        if (this._simplifyMemberReferences) {
            final MemberReference member = node.getUserData(Keys.MEMBER_REFERENCE);
            if (member instanceof FieldReference && this.context.getCurrentType() != null && MetadataHelper.isEnclosedBy(this.context.getCurrentType(), member.getDeclaringType())) {
                final Object resolvedObject = this.resolveName(node, member.getName(), NameResolveMode.EXPRESSION);
                if (resolvedObject instanceof FieldReference && MetadataHelper.isSameType(((FieldReference)resolvedObject).getDeclaringType(), member.getDeclaringType())) {
                    this._removeQualifierCandidates.add(node);
                }
            }
        }
        return super.visitMemberReferenceExpression(node, data);
    }
    
    protected Object resolveName(final AstNode location, final String name, final NameResolveMode mode) {
        if (location == null || location.isNull() || name == null) {
            return null;
        }
        NameResolveResult result;
        if (mode == NameResolveMode.TYPE) {
            result = JavaNameResolver.resolveAsType(name, location);
        }
        else {
            result = JavaNameResolver.resolve(name, location);
        }
        if (result.hasMatch() && !result.isAmbiguous()) {
            return CollectionUtilities.first(result.getCandidates());
        }
        return null;
    }
}
