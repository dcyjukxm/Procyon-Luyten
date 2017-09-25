package com.strobel.assembler.metadata.signatures;

public final class DoubleSignature implements BaseType
{
    private static final DoubleSignature _singleton;
    
    static {
        _singleton = new DoubleSignature();
    }
    
    public static DoubleSignature make() {
        return DoubleSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitDoubleSignature(this);
    }
}
