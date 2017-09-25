package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.annotations.*;

public final class AnnotationsAttribute extends SourceAttribute
{
    private final CustomAnnotation[] _annotations;
    
    public AnnotationsAttribute(final String name, final int length, final CustomAnnotation[] annotations) {
        super(name, length);
        this._annotations = annotations;
    }
    
    public CustomAnnotation[] getAnnotations() {
        return this._annotations;
    }
}
