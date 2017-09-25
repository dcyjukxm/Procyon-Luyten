package com.strobel.assembler.metadata;

import com.strobel.assembler.metadata.annotations.*;
import java.util.*;

public abstract class MemberReference implements IAnnotationsProvider, IMetadataTypeMember
{
    public boolean isSpecialName() {
        return false;
    }
    
    public boolean isDefinition() {
        return false;
    }
    
    public boolean containsGenericParameters() {
        final TypeReference declaringType = this.getDeclaringType();
        return declaringType != null && declaringType.containsGenericParameters();
    }
    
    @Override
    public abstract TypeReference getDeclaringType();
    
    public boolean isEquivalentTo(final MemberReference member) {
        return member == this;
    }
    
    @Override
    public boolean hasAnnotations() {
        return !this.getAnnotations().isEmpty();
    }
    
    @Override
    public List<CustomAnnotation> getAnnotations() {
        return Collections.emptyList();
    }
    
    @Override
    public abstract String getName();
    
    public String getFullName() {
        final StringBuilder name = new StringBuilder();
        this.appendName(name, true, false);
        return name.toString();
    }
    
    public String getSignature() {
        return this.appendSignature(new StringBuilder()).toString();
    }
    
    public String getErasedSignature() {
        return this.appendErasedSignature(new StringBuilder()).toString();
    }
    
    protected abstract StringBuilder appendName(final StringBuilder param_0, final boolean param_1, final boolean param_2);
    
    protected abstract StringBuilder appendSignature(final StringBuilder param_0);
    
    protected abstract StringBuilder appendErasedSignature(final StringBuilder param_0);
    
    @Override
    public String toString() {
        return String.valueOf(this.getFullName()) + ":" + this.getSignature();
    }
}
