package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.annotations.*;
import com.strobel.core.*;

public final class AnnotationDefaultAttribute extends SourceAttribute
{
    private final AnnotationElement _defaultValue;
    
    public AnnotationDefaultAttribute(final int length, final AnnotationElement defaultValue) {
        super("AnnotationDefault", length);
        this._defaultValue = VerifyArgument.notNull(defaultValue, "defaultValue");
    }
    
    public AnnotationElement getDefaultValue() {
        return this._defaultValue;
    }
}
