package com.strobel.assembler.ir.attributes;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import java.util.*;

public final class BootstrapMethodsTableEntry
{
    private final MethodReference _method;
    private final List<Object> _arguments;
    
    public BootstrapMethodsTableEntry(final MethodReference method, final List<Object> arguments) {
        this(method, VerifyArgument.notNull(arguments, "arguments").toArray());
    }
    
    public BootstrapMethodsTableEntry(final MethodReference method, final Object... arguments) {
        super();
        this._method = VerifyArgument.notNull(method, "method");
        this._arguments = (ArrayUtilities.isNullOrEmpty(arguments) ? Collections.emptyList() : ArrayUtilities.asUnmodifiableList(arguments));
    }
    
    public final List<Object> getArguments() {
        return this._arguments;
    }
    
    public final MethodReference getMethod() {
        return this._method;
    }
}
