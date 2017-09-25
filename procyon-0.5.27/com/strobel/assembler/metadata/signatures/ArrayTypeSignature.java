package com.strobel.assembler.metadata.signatures;

public final class ArrayTypeSignature implements FieldTypeSignature
{
    private final TypeSignature _componentType;
    
    private ArrayTypeSignature(final TypeSignature componentType) {
        super();
        this._componentType = componentType;
    }
    
    public static ArrayTypeSignature make(final TypeSignature ct) {
        return new ArrayTypeSignature(ct);
    }
    
    public TypeSignature getComponentType() {
        return this._componentType;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitArrayTypeSignature(this);
    }
}
