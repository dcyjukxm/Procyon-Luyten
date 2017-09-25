package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.annotations.*;
import java.util.*;

public final class ParameterDefinition extends ParameterReference implements IAnnotationsProvider
{
    private final Collection<CustomAnnotation> _customAnnotations;
    private final List<CustomAnnotation> _customAnnotationsView;
    private final int _size;
    private int _slot;
    private IMethodSignature _method;
    private TypeReference _declaringType;
    private long _flags;
    
    public ParameterDefinition(final int slot, final TypeReference parameterType) {
        super("", parameterType);
        this._customAnnotations = new Collection<CustomAnnotation>();
        this._customAnnotationsView = Collections.unmodifiableList((List<? extends CustomAnnotation>)this._customAnnotations);
        this._slot = slot;
        this._size = (parameterType.getSimpleType().isDoubleWord() ? 2 : 1);
    }
    
    public ParameterDefinition(final int slot, final String name, final TypeReference parameterType) {
        super(name, parameterType);
        this._customAnnotations = new Collection<CustomAnnotation>();
        this._customAnnotationsView = Collections.unmodifiableList((List<? extends CustomAnnotation>)this._customAnnotations);
        this._slot = slot;
        this._size = (parameterType.getSimpleType().isDoubleWord() ? 2 : 1);
    }
    
    public final int getSize() {
        return this._size;
    }
    
    public final int getSlot() {
        return this._slot;
    }
    
    public final long getFlags() {
        return this._flags;
    }
    
    final void setFlags(final long flags) {
        this._flags = flags;
    }
    
    final void setSlot(final int slot) {
        this._slot = slot;
    }
    
    public final IMethodSignature getMethod() {
        return this._method;
    }
    
    final void setMethod(final IMethodSignature method) {
        this._method = method;
    }
    
    public final boolean isFinal() {
        return Flags.testAny(this._flags, 16L);
    }
    
    public final boolean isMandated() {
        return Flags.testAny(this._flags, 32768L);
    }
    
    public final boolean isSynthetic() {
        return Flags.testAny(this._flags, 4096L);
    }
    
    @Override
    public boolean hasAnnotations() {
        return !this.getAnnotations().isEmpty();
    }
    
    @Override
    public List<CustomAnnotation> getAnnotations() {
        return this._customAnnotationsView;
    }
    
    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return this._customAnnotations;
    }
    
    @Override
    public final TypeReference getDeclaringType() {
        return this._declaringType;
    }
    
    final void setDeclaringType(final TypeReference declaringType) {
        this._declaringType = declaringType;
    }
    
    @Override
    public ParameterDefinition resolve() {
        final TypeReference resolvedParameterType = super.getParameterType().resolve();
        if (resolvedParameterType != null) {
            this.setParameterType(resolvedParameterType);
        }
        return this;
    }
    
    private List<CustomAnnotation> populateCustomAnnotations() {
        return Collections.emptyList();
    }
}
