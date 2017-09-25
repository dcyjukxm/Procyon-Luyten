package com.strobel.assembler.metadata.signatures;

public final class ByteSignature implements BaseType
{
    private static final ByteSignature _singleton;
    
    static {
        _singleton = new ByteSignature();
    }
    
    public static ByteSignature make() {
        return ByteSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitByteSignature(this);
    }
}
