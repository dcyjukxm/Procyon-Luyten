package com.strobel.assembler.metadata.signatures;

public interface Visitor<T> extends TypeTreeVisitor<T>
{
    void visitClassSignature(ClassSignature param_0);
    
    void visitMethodTypeSignature(MethodTypeSignature param_0);
}
