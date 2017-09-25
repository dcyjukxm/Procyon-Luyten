package com.strobel.assembler.metadata.signatures;

public final class TypeVariableSignature implements FieldTypeSignature
{
    private final String _name;
    
    private TypeVariableSignature(final String name) {
        super();
        this._name = name;
    }
    
    public static TypeVariableSignature make(final String name) {
        return new TypeVariableSignature(name);
    }
    
    public String getName() {
        return this._name;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitTypeVariableSignature(this);
    }
    
    @Override
    public String toString() {
        return "T" + this._name + ";";
    }
}
