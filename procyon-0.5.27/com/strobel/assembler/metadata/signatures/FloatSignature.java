package com.strobel.assembler.metadata.signatures;

public final class FloatSignature implements BaseType
{
    private static final FloatSignature _singleton;
    
    static {
        _singleton = new FloatSignature();
    }
    
    public static FloatSignature make() {
        return FloatSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitFloatSignature(this);
    }
}
