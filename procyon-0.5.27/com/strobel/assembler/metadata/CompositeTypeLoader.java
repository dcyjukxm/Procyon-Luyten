package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class CompositeTypeLoader implements ITypeLoader
{
    private final ITypeLoader[] _typeLoaders;
    
    public CompositeTypeLoader(final ITypeLoader... typeLoaders) {
        super();
        this._typeLoaders = VerifyArgument.noNullElementsAndNotEmpty(typeLoaders, "typeLoaders").clone();
    }
    
    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        ITypeLoader[] loc_1;
        for (int loc_0 = (loc_1 = this._typeLoaders).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final ITypeLoader typeLoader = loc_1[loc_2];
            if (typeLoader.tryLoadType(internalName, buffer)) {
                return true;
            }
            buffer.reset();
        }
        return false;
    }
}
