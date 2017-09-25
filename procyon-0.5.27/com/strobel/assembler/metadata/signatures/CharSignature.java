package com.strobel.assembler.metadata.signatures;

public final class CharSignature implements BaseType
{
    private static final CharSignature _singleton;
    
    static {
        _singleton = new CharSignature();
    }
    
    public static CharSignature make() {
        return CharSignature._singleton;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitCharSignature(this);
    }
}
