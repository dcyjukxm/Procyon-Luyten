package com.strobel.assembler.metadata.annotations;

import com.strobel.core.*;

public final class AnnotationParameter
{
    private final AnnotationElement _value;
    private final String _member;
    
    public AnnotationParameter(final String member, final AnnotationElement value) {
        super();
        this._member = VerifyArgument.notNull(member, "member");
        this._value = VerifyArgument.notNull(value, "value");
    }
    
    public final String getMember() {
        return this._member;
    }
    
    public final AnnotationElement getValue() {
        return this._value;
    }
}
