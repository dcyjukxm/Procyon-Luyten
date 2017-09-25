package com.strobel.assembler.metadata.signatures;

public interface TypeTree extends Tree
{
    void accept(TypeTreeVisitor<?> param_0);
}
