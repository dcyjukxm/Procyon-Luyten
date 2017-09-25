package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class MethodHandle
{
    private final MethodReference _method;
    private final MethodHandleType _handleType;
    
    public MethodHandle(final MethodReference method, final MethodHandleType handleType) {
        super();
        this._method = VerifyArgument.notNull(method, "method");
        this._handleType = VerifyArgument.notNull(handleType, "handleType");
    }
    
    public final MethodHandleType getHandleType() {
        return this._handleType;
    }
    
    public final MethodReference getMethod() {
        return this._method;
    }
    
    @Override
    public final String toString() {
        return this._handleType + " " + this._method.getFullName() + ":" + this._method.getSignature();
    }
}
