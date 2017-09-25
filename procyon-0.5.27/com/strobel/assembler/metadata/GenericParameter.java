package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

public final class GenericParameter extends TypeDefinition
{
    private int _position;
    private GenericParameterType _type;
    private IGenericParameterProvider _owner;
    private TypeReference _extendsBound;
    
    public GenericParameter(final String name) {
        super();
        this._type = GenericParameterType.Type;
        this._extendsBound = BuiltinTypes.Object;
        this.setName((name != null) ? name : "");
    }
    
    public GenericParameter(final String name, final TypeReference extendsBound) {
        super();
        this._type = GenericParameterType.Type;
        this._extendsBound = VerifyArgument.notNull(extendsBound, "extendsBound");
        this.setName((name != null) ? name : "");
    }
    
    protected final void setPosition(final int position) {
        this._position = position;
    }
    
    protected final void setOwner(final IGenericParameterProvider owner) {
        this._owner = owner;
        this._type = ((owner instanceof MethodReference) ? GenericParameterType.Method : GenericParameterType.Type);
    }
    
    protected final void setExtendsBound(final TypeReference extendsBound) {
        this._extendsBound = extendsBound;
    }
    
    @Override
    public String getName() {
        final String name = super.getName();
        if (!StringUtilities.isNullOrEmpty(name)) {
            return name;
        }
        return "T" + this._position;
    }
    
    @Override
    public String getFullName() {
        return this.getName();
    }
    
    @Override
    public String getInternalName() {
        return this.getName();
    }
    
    @Override
    public TypeReference getUnderlyingType() {
        final TypeReference extendsBound = this.getExtendsBound();
        return (extendsBound != null) ? extendsBound : BuiltinTypes.Object;
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitGenericParameter(this, parameter);
    }
    
    @Override
    public boolean isUnbounded() {
        return !this.hasExtendsBound();
    }
    
    @Override
    public boolean isGenericParameter() {
        return true;
    }
    
    @Override
    public boolean containsGenericParameters() {
        return true;
    }
    
    @Override
    public TypeReference getDeclaringType() {
        final IGenericParameterProvider owner = this._owner;
        if (owner instanceof TypeReference) {
            return (TypeReference)owner;
        }
        return null;
    }
    
    public int getPosition() {
        return this._position;
    }
    
    public GenericParameterType getType() {
        return this._type;
    }
    
    public IGenericParameterProvider getOwner() {
        return this._owner;
    }
    
    @Override
    public boolean hasExtendsBound() {
        return this._extendsBound != null && !MetadataResolver.areEquivalent(this._extendsBound, BuiltinTypes.Object);
    }
    
    @Override
    public TypeReference getExtendsBound() {
        return this._extendsBound;
    }
    
    @Override
    public boolean hasAnnotations() {
        return !this.getAnnotations().isEmpty();
    }
    
    @Override
    public TypeDefinition resolve() {
        if (this._owner instanceof TypeReference && !(this._owner instanceof TypeDefinition)) {
            final TypeDefinition resolvedOwner = ((TypeReference)this._owner).resolve();
            if (resolvedOwner != null) {
                final List<GenericParameter> genericParameters = resolvedOwner.getGenericParameters();
                if (this._position >= 0 && this._position < genericParameters.size()) {
                    return genericParameters.get(this._position);
                }
            }
        }
        return null;
    }
    
    @Override
    protected StringBuilder appendDescription(final StringBuilder sb) {
        sb.append(this.getFullName());
        final TypeReference upperBound = this.getExtendsBound();
        if (upperBound == null || upperBound.equals(BuiltinTypes.Object)) {
            return sb;
        }
        sb.append(" extends ");
        if (upperBound.isGenericParameter() || upperBound.equals(this.getDeclaringType())) {
            return sb.append(upperBound.getFullName());
        }
        return upperBound.appendErasedDescription(sb);
    }
    
    @Override
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        sb.append(this.getFullName());
        final TypeReference upperBound = this.getExtendsBound();
        if (upperBound == null || upperBound.equals(BuiltinTypes.Object)) {
            return sb;
        }
        sb.append(" extends ");
        if (upperBound.isGenericParameter() || upperBound.equals(this.getDeclaringType())) {
            return sb.append(upperBound.getName());
        }
        return upperBound.appendErasedDescription(sb);
    }
    
    @Override
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return this.getExtendsBound().appendErasedDescription(sb);
    }
    
    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return sb.append('T').append(this.getName()).append(';');
    }
    
    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return this.getExtendsBound().appendErasedSignature(sb);
    }
    
    @Override
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        sb.append(this.getFullName());
        final TypeReference upperBound = this.getExtendsBound();
        if (upperBound == null || upperBound.equals(BuiltinTypes.Object)) {
            return sb;
        }
        sb.append(" extends ");
        if (upperBound.isGenericParameter() || upperBound.equals(this.getOwner())) {
            return sb.append(upperBound.getSimpleName());
        }
        return upperBound.appendSimpleDescription(sb);
    }
}
