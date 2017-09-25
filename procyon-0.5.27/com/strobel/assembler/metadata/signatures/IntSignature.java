package com.strobel.assembler.metadata.signatures;

public final class IntSignature implements BaseType
{
    private static final IntSignature _singleton;
    
    static {
        _singleton = new IntSignature();
    }
    
    public static IntSignature make() {
        return IntSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitIntSignature(this);
    }
}
