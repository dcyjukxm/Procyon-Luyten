package com.strobel.assembler.metadata;

import com.strobel.core.*;
import com.strobel.util.*;

public abstract class FieldReference extends MemberReference
{
    public abstract TypeReference getFieldType();
    
    @Override
    public boolean containsGenericParameters() {
        final TypeReference fieldType = this.getFieldType();
        return (fieldType != null && fieldType.containsGenericParameters()) || super.containsGenericParameters();
    }
    
    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        if (super.isEquivalentTo(member)) {
            return true;
        }
        if (member instanceof FieldReference) {
            final FieldReference field = (FieldReference)member;
            return StringUtilities.equals(field.getName(), this.getName()) && MetadataResolver.areEquivalent(field.getDeclaringType(), this.getDeclaringType());
        }
        return false;
    }
    
    public FieldDefinition resolve() {
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType == null) {
            throw ContractUtils.unsupported();
        }
        return declaringType.resolve(this);
    }
    
    @Override
    protected abstract StringBuilder appendName(final StringBuilder param_0, final boolean param_1, final boolean param_2);
    
    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        return this.getFieldType().appendSignature(sb);
    }
    
    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        return this.getFieldType().appendErasedSignature(sb);
    }
}
