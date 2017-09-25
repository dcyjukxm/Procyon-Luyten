package com.strobel.assembler.metadata.signatures;

public final class ShortSignature implements BaseType
{
    private static final ShortSignature _singleton;
    
    static {
        _singleton = new ShortSignature();
    }
    
    public static ShortSignature make() {
        return ShortSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitShortSignature(this);
    }
}
