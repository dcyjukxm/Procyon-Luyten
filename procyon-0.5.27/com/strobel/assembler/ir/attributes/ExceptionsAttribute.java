package com.strobel.assembler.ir.attributes;

import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class ExceptionsAttribute extends SourceAttribute
{
    private final List<TypeReference> _exceptionTypes;
    
    public ExceptionsAttribute(final TypeReference... exceptionTypes) {
        super("Exceptions", 2 * (1 + VerifyArgument.noNullElements(exceptionTypes, "exceptionTypes").length));
        this._exceptionTypes = ArrayUtilities.asUnmodifiableList(exceptionTypes);
    }
    
    public List<TypeReference> getExceptionTypes() {
        return this._exceptionTypes;
    }
}
