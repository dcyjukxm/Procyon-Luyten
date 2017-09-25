package com.strobel.assembler.metadata.signatures;

public final class FormalTypeParameter implements TypeTree
{
    private final String _name;
    private final FieldTypeSignature[] _bounds;
    
    private FormalTypeParameter(final String name, final FieldTypeSignature[] bounds) {
        super();
        this._name = name;
        this._bounds = bounds;
    }
    
    public static FormalTypeParameter make(final String name, final FieldTypeSignature[] bounds) {
        return new FormalTypeParameter(name, bounds);
    }
    
    public FieldTypeSignature[] getBounds() {
        return this._bounds;
    }
    
    public String getName() {
        return this._name;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitFormalTypeParameter(this);
    }
}
