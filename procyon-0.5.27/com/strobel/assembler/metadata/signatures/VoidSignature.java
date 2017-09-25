package com.strobel.assembler.metadata.signatures;

public final class VoidSignature implements BaseType
{
    private static final VoidSignature _singleton;
    
    static {
        _singleton = new VoidSignature();
    }
    
    public static VoidSignature make() {
        return VoidSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitVoidSignature(this);
    }
}
