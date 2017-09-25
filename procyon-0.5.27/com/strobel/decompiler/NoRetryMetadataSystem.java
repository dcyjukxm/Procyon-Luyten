package com.strobel.decompiler;

import java.util.*;
import com.strobel.assembler.metadata.*;

final class NoRetryMetadataSystem extends MetadataSystem
{
    private final Set<String> _failedTypes;
    
    NoRetryMetadataSystem() {
        super();
        this._failedTypes = new HashSet<String>();
    }
    
    NoRetryMetadataSystem(final String classPath) {
        super(classPath);
        this._failedTypes = new HashSet<String>();
    }
    
    NoRetryMetadataSystem(final ITypeLoader typeLoader) {
        super(typeLoader);
        this._failedTypes = new HashSet<String>();
    }
    
    @Override
    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive) {
        if (this._failedTypes.contains(descriptor)) {
            return null;
        }
        final TypeDefinition result = super.resolveType(descriptor, mightBePrimitive);
        if (result == null) {
            this._failedTypes.add(descriptor);
        }
        return result;
    }
}
