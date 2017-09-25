package com.strobel.assembler.metadata.annotations;

public abstract class AnnotationElement
{
    private final AnnotationElementType _elementType;
    
    protected AnnotationElement(final AnnotationElementType elementType) {
        super();
        this._elementType = elementType;
    }
    
    public AnnotationElementType getElementType() {
        return this._elementType;
    }
}
