package us.deathmarine.luyten;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.*;
import java.util.*;

public final class LuytenTypeLoader implements ITypeLoader
{
    private final List<ITypeLoader> _typeLoaders;
    
    public LuytenTypeLoader() {
        super();
        (this._typeLoaders = new ArrayList<ITypeLoader>()).add(new InputTypeLoader());
    }
    
    public final List<ITypeLoader> getTypeLoaders() {
        return this._typeLoaders;
    }
    
    @Override
    public boolean tryLoadType(final String internalName, final Buffer buffer) {
        for (final ITypeLoader typeLoader : this._typeLoaders) {
            if (typeLoader.tryLoadType(internalName, buffer)) {
                return true;
            }
            buffer.reset();
        }
        return false;
    }
}
