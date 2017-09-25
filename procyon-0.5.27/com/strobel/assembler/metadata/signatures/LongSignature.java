package com.strobel.assembler.metadata.signatures;

public final class LongSignature implements BaseType
{
    private static final LongSignature _singleton;
    
    static {
        _singleton = new LongSignature();
    }
    
    public static LongSignature make() {
        return LongSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitLongSignature(this);
    }
}
