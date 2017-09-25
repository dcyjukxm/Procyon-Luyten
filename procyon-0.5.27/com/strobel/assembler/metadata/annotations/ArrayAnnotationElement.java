package com.strobel.assembler.metadata.annotations;

import com.strobel.core.*;

public final class ArrayAnnotationElement extends AnnotationElement
{
    private final AnnotationElement[] _elements;
    
    public ArrayAnnotationElement(final AnnotationElement[] elements) {
        super(AnnotationElementType.Array);
        this._elements = VerifyArgument.notNull(elements, "elements");
    }
    
    public AnnotationElement[] getElements() {
        return this._elements;
    }
}
