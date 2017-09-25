package com.strobel.assembler.metadata.signatures;

public final class ClassSignature implements Signature
{
    private final FormalTypeParameter[] _formalTypeParameters;
    private final ClassTypeSignature _baseClass;
    private final ClassTypeSignature[] _interfaces;
    
    private ClassSignature(final FormalTypeParameter[] ftps, final ClassTypeSignature sc, final ClassTypeSignature[] sis) {
        super();
        this._formalTypeParameters = ftps;
        this._baseClass = sc;
        this._interfaces = sis;
    }
    
    public static ClassSignature make(final FormalTypeParameter[] ftps, final ClassTypeSignature sc, final ClassTypeSignature[] sis) {
        return new ClassSignature(ftps, sc, sis);
    }
    
    @Override
    public FormalTypeParameter[] getFormalTypeParameters() {
        return this._formalTypeParameters;
    }
    
    public ClassTypeSignature getSuperType() {
        return this._baseClass;
    }
    
    public ClassTypeSignature[] getInterfaces() {
        return this._interfaces;
    }
    
    public void accept(final Visitor v) {
        v.visitClassSignature(this);
    }
}
