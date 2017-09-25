package com.strobel.assembler.metadata.signatures;

public final class BottomSignature implements FieldTypeSignature
{
    private static final BottomSignature _singleton;
    
    static {
        _singleton = new BottomSignature();
    }
    
    public static BottomSignature make() {
        return BottomSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitBottomSignature(this);
    }
}
