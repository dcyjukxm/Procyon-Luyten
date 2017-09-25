package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class ArrayType extends TypeReference
{
    private final TypeReference _elementType;
    private String _internalName;
    private String _fullName;
    private String _simpleName;
    
    ArrayType(final TypeReference elementType) {
        super();
        this._elementType = VerifyArgument.notNull(elementType, "elementType");
        this.setName(String.valueOf(elementType.getName()) + "[]");
    }
    
    @Override
    public boolean containsGenericParameters() {
        return this._elementType.containsGenericParameters();
    }
    
    @Override
    public String getPackageName() {
        return this._elementType.getPackageName();
    }
    
    @Override
    public String getSimpleName() {
        if (this._simpleName == null) {
            this._simpleName = String.valueOf(this._elementType.getSimpleName()) + "[]";
        }
        return this._simpleName;
    }
    
    @Override
    public String getFullName() {
        if (this._fullName == null) {
            this._fullName = String.valueOf(this._elementType.getFullName()) + "[]";
        }
        return this._fullName;
    }
    
    @Override
    public String getInternalName() {
        if (this._internalName == null) {
            this._internalName = "[" + this._elementType.getInternalName();
        }
        return this._internalName;
    }
    
    @Override
    public final boolean isArray() {
        return true;
    }
    
    @Override
    public final TypeReference getElementType() {
        return this._elementType;
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitArrayType(this, parameter);
    }
    
    @Override
    public final TypeReference getUnderlyingType() {
        return this._elementType.getUnderlyingType();
    }
    
    public final StringBuilder appendSignature(final StringBuilder sb) {
        sb.append('[');
        return this._elementType.appendSignature(sb);
    }
    
    public final StringBuilder appendErasedSignature(final StringBuilder sb) {
        return this._elementType.appendErasedSignature(sb.append('['));
    }
    
    public final StringBuilder appendBriefDescription(final StringBuilder sb) {
        return this._elementType.appendBriefDescription(sb).append("[]");
    }
    
    public final StringBuilder appendSimpleDescription(final StringBuilder sb) {
        return this._elementType.appendSimpleDescription(sb).append("[]");
    }
    
    public final StringBuilder appendDescription(final StringBuilder sb) {
        return this.appendBriefDescription(sb);
    }
    
    public static ArrayType create(final TypeReference elementType) {
        return new ArrayType(elementType);
    }
    
    @Override
    public final TypeDefinition resolve() {
        final TypeDefinition resolvedElementType = this._elementType.resolve();
        if (resolvedElementType != null) {
            return resolvedElementType;
        }
        return super.resolve();
    }
}
