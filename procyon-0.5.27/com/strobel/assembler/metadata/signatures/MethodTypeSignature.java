package com.strobel.assembler.metadata.signatures;

public final class MethodTypeSignature implements Signature
{
    private final FormalTypeParameter[] formalTypeParams;
    private final TypeSignature[] parameterTypes;
    private final ReturnType returnType;
    private final FieldTypeSignature[] exceptionTypes;
    
    private MethodTypeSignature(final FormalTypeParameter[] ftps, final TypeSignature[] pts, final ReturnType rt, final FieldTypeSignature[] ets) {
        super();
        this.formalTypeParams = ftps;
        this.parameterTypes = pts;
        this.returnType = rt;
        this.exceptionTypes = ets;
    }
    
    public static MethodTypeSignature make(final FormalTypeParameter[] ftps, final TypeSignature[] pts, final ReturnType rt, final FieldTypeSignature[] ets) {
        return new MethodTypeSignature(ftps, pts, rt, ets);
    }
    
    @Override
    public FormalTypeParameter[] getFormalTypeParameters() {
        return this.formalTypeParams;
    }
    
    public TypeSignature[] getParameterTypes() {
        return this.parameterTypes;
    }
    
    public ReturnType getReturnType() {
        return this.returnType;
    }
    
    public FieldTypeSignature[] getExceptionTypes() {
        return this.exceptionTypes;
    }
    
    public void accept(final Visitor v) {
        v.visitMethodTypeSignature(this);
    }
}
