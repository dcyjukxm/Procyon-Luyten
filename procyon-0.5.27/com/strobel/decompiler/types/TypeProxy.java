package com.strobel.decompiler.types;

import com.strobel.core.*;
import com.strobel.collections.*;
import java.util.*;

final class TypeProxy implements ITypeInfo
{
    private static final List<ITypeListener> EMPTY_LISTENERS;
    private final ITypeListener _listener;
    private List<ITypeListener> _listeners;
    private ITypeInfo _delegate;
    
    static {
        EMPTY_LISTENERS = Collections.emptyList();
    }
    
    TypeProxy(final ITypeInfo delegate) {
        super();
        VerifyArgument.notNull(delegate, "delegate");
        this._listeners = TypeProxy.EMPTY_LISTENERS;
        this._listener = new DelegateListener((DelegateListener)null);
        this.setDelegate(delegate);
    }
    
    final void setDelegate(final ITypeInfo delegate) {
        VerifyArgument.notNull(delegate, "delegate");
        if (this._delegate != null) {
            this._delegate.removeListener(this._listener);
        }
        (this._delegate = delegate).addListener(this._listener);
    }
    
    @Override
    public final String getName() {
        return this._delegate.getName();
    }
    
    @Override
    public final String getPackageName() {
        return this._delegate.getPackageName();
    }
    
    @Override
    public final String getFullName() {
        return this._delegate.getFullName();
    }
    
    @Override
    public final String getCanonicalName() {
        return this._delegate.getCanonicalName();
    }
    
    @Override
    public final String getInternalName() {
        return this._delegate.getInternalName();
    }
    
    @Override
    public final String getSignature() {
        return this._delegate.getSignature();
    }
    
    @Override
    public final boolean isArray() {
        return this._delegate.isArray();
    }
    
    @Override
    public final boolean isPrimitive() {
        return this._delegate.isPrimitive();
    }
    
    @Override
    public final boolean isPrimitiveOrVoid() {
        return this._delegate.isPrimitiveOrVoid();
    }
    
    @Override
    public final boolean isVoid() {
        return this._delegate.isVoid();
    }
    
    @Override
    public final boolean isRawType() {
        return this._delegate.isRawType();
    }
    
    @Override
    public final boolean isGenericType() {
        return this._delegate.isGenericType();
    }
    
    @Override
    public final boolean isGenericTypeInstance() {
        return this._delegate.isGenericTypeInstance();
    }
    
    @Override
    public final boolean isGenericTypeDefinition() {
        return this._delegate.isGenericTypeDefinition();
    }
    
    @Override
    public final boolean isGenericParameter() {
        return this._delegate.isGenericParameter();
    }
    
    @Override
    public final boolean isWildcard() {
        return this._delegate.isWildcard();
    }
    
    @Override
    public final boolean isUnknownType() {
        return this._delegate.isUnknownType();
    }
    
    @Override
    public final boolean isBound() {
        return this._delegate.isBound();
    }
    
    @Override
    public final boolean isLocal() {
        return this._delegate.isLocal();
    }
    
    @Override
    public final boolean isAnonymous() {
        return this._delegate.isAnonymous();
    }
    
    @Override
    public final ITypeInfo getDeclaringType() {
        return this._delegate.getDeclaringType();
    }
    
    @Override
    public final boolean hasConstraints() {
        return this._delegate.hasConstraints();
    }
    
    @Override
    public final boolean hasSuperConstraint() {
        return this._delegate.hasSuperConstraint();
    }
    
    @Override
    public final boolean hasExtendsConstraint() {
        return this._delegate.hasExtendsConstraint();
    }
    
    @Override
    public final ITypeInfo getElementType() {
        return this._delegate.getElementType();
    }
    
    @Override
    public final ITypeInfo getSuperConstraint() {
        return this._delegate.getSuperConstraint();
    }
    
    @Override
    public final ITypeInfo getExtendsConstraint() {
        return this._delegate.getExtendsConstraint();
    }
    
    @Override
    public final ITypeInfo getSuperClass() {
        return this._delegate.getSuperClass();
    }
    
    @Override
    public final ImmutableList<ITypeInfo> getSuperInterfaces() {
        return this._delegate.getSuperInterfaces();
    }
    
    @Override
    public final ImmutableList<ITypeInfo> getGenericParameters() {
        return this._delegate.getGenericParameters();
    }
    
    @Override
    public final ImmutableList<ITypeInfo> getTypeArguments() {
        return this._delegate.getTypeArguments();
    }
    
    @Override
    public final ITypeInfo getGenericDefinition() {
        return this._delegate.getGenericDefinition();
    }
    
    @Override
    public final void removeListener(final ITypeListener listener) {
        VerifyArgument.notNull(listener, "listener");
        if (this._listeners == TypeProxy.EMPTY_LISTENERS) {
            return;
        }
        this._listeners.remove(listener);
    }
    
    @Override
    public final void addListener(final ITypeListener listener) {
        VerifyArgument.notNull(listener, "listener");
        if (this._listeners == TypeProxy.EMPTY_LISTENERS) {
            this._listeners = new ArrayList<ITypeListener>();
        }
        this._listeners.add(listener);
    }
    
    final void notifyChanged() {
        final List<ITypeListener> listeners = this._listeners;
        if (listeners == TypeProxy.EMPTY_LISTENERS) {
            return;
        }
        for (final ITypeListener listener : listeners) {
            listener.onChanged();
        }
    }
    
    private final class DelegateListener implements ITypeListener
    {
        @Override
        public final void onChanged() {
            TypeProxy.this.notifyChanged();
        }
    }
}
