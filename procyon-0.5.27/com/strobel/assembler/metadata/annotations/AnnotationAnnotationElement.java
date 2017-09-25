package com.strobel.assembler.metadata.annotations;

import com.strobel.core.*;

public final class AnnotationAnnotationElement extends AnnotationElement
{
    private final CustomAnnotation _annotation;
    
    public AnnotationAnnotationElement(final CustomAnnotation annotation) {
        super(AnnotationElementType.Annotation);
        this._annotation = VerifyArgument.notNull(annotation, "annotation");
    }
    
    public CustomAnnotation getAnnotation() {
        return this._annotation;
    }
}
