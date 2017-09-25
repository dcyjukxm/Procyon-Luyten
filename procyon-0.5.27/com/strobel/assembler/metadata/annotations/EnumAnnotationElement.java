package com.strobel.assembler.metadata.annotations;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class EnumAnnotationElement extends AnnotationElement
{
    private final TypeReference _enumType;
    private final String _enumConstantName;
    
    public EnumAnnotationElement(final TypeReference enumType, final String enumConstantName) {
        super(AnnotationElementType.Enum);
        this._enumType = VerifyArgument.notNull(enumType, "enumType");
        this._enumConstantName = VerifyArgument.notNull(enumConstantName, "enumConstantName");
    }
    
    public TypeReference getEnumType() {
        return this._enumType;
    }
    
    public String getEnumConstantName() {
        return this._enumConstantName;
    }
}
