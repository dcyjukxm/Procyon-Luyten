package com.strobel.assembler.metadata;

import java.util.*;

public final class CompoundTypeReference extends TypeReference
{
    private final TypeReference _baseType;
    private final List<TypeReference> _interfaces;
    
    public CompoundTypeReference(final TypeReference baseType, final List<TypeReference> interfaces) {
        super();
        this._baseType = baseType;
        this._interfaces = interfaces;
    }
    
    public final TypeReference getBaseType() {
        return this._baseType;
    }
    
    public final List<TypeReference> getInterfaces() {
        return this._interfaces;
    }
    
    @Override
    public TypeReference getDeclaringType() {
        return null;
    }
    
    @Override
    public String getSimpleName() {
        if (this._baseType != null) {
            return this._baseType.getSimpleName();
        }
        return this._interfaces.get(0).getSimpleName();
    }
    
    @Override
    public boolean containsGenericParameters() {
        final TypeReference baseType = this.getBaseType();
        if (baseType != null && baseType.containsGenericParameters()) {
            return true;
        }
        for (final TypeReference t : this._interfaces) {
            if (t.containsGenericParameters()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String getName() {
        if (this._baseType != null) {
            return this._baseType.getName();
        }
        return this._interfaces.get(0).getName();
    }
    
    @Override
    public String getFullName() {
        if (this._baseType != null) {
            return this._baseType.getFullName();
        }
        return this._interfaces.get(0).getFullName();
    }
    
    @Override
    public String getInternalName() {
        if (this._baseType != null) {
            return this._baseType.getInternalName();
        }
        return this._interfaces.get(0).getInternalName();
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitCompoundType(this, parameter);
    }
    
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        final TypeReference baseType = this._baseType;
        final List<TypeReference> interfaces = this._interfaces;
        StringBuilder s = sb;
        if (baseType != null) {
            s = baseType.appendBriefDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }
        for (int i = 0, n = interfaces.size(); i < n; ++i) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendBriefDescription(s);
        }
        return s;
    }
    
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        final TypeReference baseType = this._baseType;
        final List<TypeReference> interfaces = this._interfaces;
        StringBuilder s = sb;
        if (baseType != null) {
            s = baseType.appendSimpleDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }
        for (int i = 0, n = interfaces.size(); i < n; ++i) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendSimpleDescription(s);
        }
        return s;
    }
    
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        final TypeReference baseType = this._baseType;
        final List<TypeReference> interfaces = this._interfaces;
        StringBuilder s = sb;
        if (baseType != null) {
            s = baseType.appendErasedDescription(s);
            if (!interfaces.isEmpty()) {
                s.append(" & ");
            }
        }
        for (int i = 0, n = interfaces.size(); i < n; ++i) {
            if (i != 0) {
                s.append(" & ");
            }
            s = interfaces.get(i).appendErasedDescription(s);
        }
        return s;
    }
    
    public StringBuilder appendDescription(final StringBuilder sb) {
        return this.appendBriefDescription(sb);
    }
    
    public StringBuilder appendSignature(final StringBuilder sb) {
        StringBuilder s = sb;
        if (this._baseType != null) {
            s = this._baseType.appendSignature(s);
        }
        if (this._interfaces.isEmpty()) {
            return s;
        }
        for (final TypeReference interfaceType : this._interfaces) {
            s.append(':');
            s = interfaceType.appendSignature(s);
        }
        return s;
    }
    
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (this._baseType != null) {
            return this._baseType.appendErasedSignature(sb);
        }
        if (!this._interfaces.isEmpty()) {
            return this._interfaces.get(0).appendErasedSignature(sb);
        }
        return BuiltinTypes.Object.appendErasedSignature(sb);
    }
}
