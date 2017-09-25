package com.strobel.assembler.metadata.signatures;

public final class SimpleClassTypeSignature implements FieldTypeSignature
{
    private final boolean _dollar;
    private final String _name;
    private final TypeArgument[] _typeArguments;
    
    private SimpleClassTypeSignature(final String n, final boolean dollar, final TypeArgument[] tas) {
        super();
        this._name = n;
        this._dollar = dollar;
        this._typeArguments = tas;
    }
    
    public static SimpleClassTypeSignature make(final String n, final boolean dollar, final TypeArgument[] tas) {
        return new SimpleClassTypeSignature(n, dollar, tas);
    }
    
    public boolean useDollar() {
        return this._dollar;
    }
    
    public String getName() {
        return this._name;
    }
    
    public TypeArgument[] getTypeArguments() {
        return this._typeArguments;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitSimpleClassTypeSignature(this);
    }
}
