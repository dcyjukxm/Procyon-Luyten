package com.strobel.assembler.metadata;

import java.util.*;
import com.strobel.core.*;

public final class DynamicCallSite
{
    private final MethodReference _bootstrapMethod;
    private final List<Object> _bootstrapArguments;
    private final String _methodName;
    private final IMethodSignature _methodType;
    
    public DynamicCallSite(final MethodReference method, final List<Object> bootstrapArguments, final String methodName, final IMethodSignature methodType) {
        super();
        this._bootstrapMethod = VerifyArgument.notNull(method, "method");
        this._bootstrapArguments = VerifyArgument.notNull(bootstrapArguments, "bootstrapArguments");
        this._methodName = VerifyArgument.notNull(methodName, "methodName");
        this._methodType = VerifyArgument.notNull(methodType, "methodType");
    }
    
    public final String getMethodName() {
        return this._methodName;
    }
    
    public final IMethodSignature getMethodType() {
        return this._methodType;
    }
    
    public final List<Object> getBootstrapArguments() {
        return this._bootstrapArguments;
    }
    
    public final MethodReference getBootstrapMethod() {
        return this._bootstrapMethod;
    }
}
