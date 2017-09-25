package com.strobel.assembler.metadata.signatures;

public final class BooleanSignature implements BaseType
{
    private static final BooleanSignature _singleton;
    
    static {
        _singleton = new BooleanSignature();
    }
    
    public static BooleanSignature make() {
        return BooleanSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitBooleanSignature(this);
    }
}
