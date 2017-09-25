package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class EnclosingMethodAttribute extends SourceAttribute
{
    private final TypeReference _enclosingType;
    private final MethodReference _enclosingMethod;
    
    public EnclosingMethodAttribute(final TypeReference enclosingType, final MethodReference enclosingMethod) {
        super("EnclosingMethod", 4);
        this._enclosingType = VerifyArgument.notNull(enclosingType, "enclosingType");
        this._enclosingMethod = enclosingMethod;
    }
    
    public TypeReference getEnclosingType() {
        return this._enclosingType;
    }
    
    public MethodReference getEnclosingMethod() {
        return this._enclosingMethod;
    }
}
