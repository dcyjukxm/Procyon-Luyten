package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;
import com.strobel.util.*;

public abstract class MethodReference extends MemberReference implements IMethodSignature, IGenericParameterProvider, IGenericContext
{
    protected static final String CONSTRUCTOR_NAME = "<init>";
    protected static final String STATIC_INITIALIZER_NAME = "<clinit>";
    
    @Override
    public abstract TypeReference getReturnType();
    
    @Override
    public boolean hasParameters() {
        return !this.getParameters().isEmpty();
    }
    
    @Override
    public abstract List<ParameterDefinition> getParameters();
    
    @Override
    public List<TypeReference> getThrownTypes() {
        return Collections.emptyList();
    }
    
    @Override
    public boolean isSpecialName() {
        return "<init>".equals(this.getName()) || "<clinit>".equals(this.getName());
    }
    
    @Override
    public boolean containsGenericParameters() {
        if (super.containsGenericParameters() || this.hasGenericParameters()) {
            return true;
        }
        if (this.getReturnType().containsGenericParameters()) {
            return true;
        }
        if (this.hasParameters()) {
            final List<ParameterDefinition> parameters = this.getParameters();
            for (int i = 0, n = parameters.size(); i < n; ++i) {
                if (parameters.get(i).getParameterType().containsGenericParameters()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        if (super.isEquivalentTo(member)) {
            return true;
        }
        if (member instanceof MethodReference) {
            final MethodReference method = (MethodReference)member;
            return StringUtilities.equals(method.getName(), this.getName()) && StringUtilities.equals(method.getErasedSignature(), this.getErasedSignature()) && MetadataResolver.areEquivalent(method.getDeclaringType(), this.getDeclaringType());
        }
        return false;
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeReference declaringType = this.getDeclaringType();
            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append('.').append(this.getName());
            }
        }
        return sb.append(this.getName());
    }
    
    public boolean isConstructor() {
        return "<init>".equals(this.getName());
    }
    
    public boolean isTypeInitializer() {
        return "<clinit>".equals(this.getName());
    }
    
    public boolean isGenericMethod() {
        return this.hasGenericParameters();
    }
    
    @Override
    public boolean hasGenericParameters() {
        return !this.getGenericParameters().isEmpty();
    }
    
    @Override
    public boolean isGenericDefinition() {
        return this.hasGenericParameters() && this.isDefinition();
    }
    
    @Override
    public List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
    }
    
    @Override
    public GenericParameter findTypeVariable(final String name) {
        for (final GenericParameter genericParameter : this.getGenericParameters()) {
            if (StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType != null) {
            return declaringType.findTypeVariable(name);
        }
        return null;
    }
    
    public MethodDefinition resolve() {
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType == null) {
            throw ContractUtils.unsupported();
        }
        return declaringType.resolve(this);
    }
    
    public StringBuilder appendSignature(final StringBuilder sb) {
        final List<ParameterDefinition> parameters = this.getParameters();
        StringBuilder s = sb;
        s.append('(');
        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);
            s = p.getParameterType().appendSignature(s);
        }
        s.append(')');
        s = this.getReturnType().appendSignature(s);
        return s;
    }
    
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        StringBuilder s = sb;
        s.append('(');
        final List<ParameterDefinition> parameterTypes = this.getParameters();
        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            s = parameterTypes.get(i).getParameterType().appendErasedSignature(s);
        }
        s.append(')');
        s = this.getReturnType().appendErasedSignature(s);
        return s;
    }
}
