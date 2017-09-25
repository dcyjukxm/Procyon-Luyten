package com.strobel.decompiler.semantics;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.languages.*;

public class ResolveResult
{
    private final TypeReference _type;
    
    public ResolveResult(final TypeReference type) {
        super();
        this._type = VerifyArgument.notNull(type, "type");
    }
    
    public final TypeReference getType() {
        return this._type;
    }
    
    public boolean isCompileTimeConstant() {
        return false;
    }
    
    public Object getConstantValue() {
        return null;
    }
    
    public boolean isError() {
        return false;
    }
    
    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + " " + this._type + "]";
    }
    
    public final Iterable<ResolveResult> getChildResults() {
        return (Iterable<ResolveResult>)Collections.emptyList();
    }
    
    public final Region getDefinitionRegion() {
        return Region.EMPTY;
    }
}
