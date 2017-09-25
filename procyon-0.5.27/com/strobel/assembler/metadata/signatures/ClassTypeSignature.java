package com.strobel.assembler.metadata.signatures;

import java.util.*;

public final class ClassTypeSignature implements FieldTypeSignature
{
    private final List<SimpleClassTypeSignature> _path;
    
    private ClassTypeSignature(final List<SimpleClassTypeSignature> path) {
        super();
        this._path = path;
    }
    
    public static ClassTypeSignature make(final List<SimpleClassTypeSignature> p) {
        return new ClassTypeSignature(p);
    }
    
    public List<SimpleClassTypeSignature> getPath() {
        return this._path;
    }
    
    @Override
    public void accept(final TypeTreeVisitor<?> v) {
        v.visitClassTypeSignature(this);
    }
}
