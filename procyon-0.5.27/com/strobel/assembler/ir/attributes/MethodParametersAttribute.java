package com.strobel.assembler.ir.attributes;

import java.util.*;
import com.strobel.core.*;

public final class MethodParametersAttribute extends SourceAttribute
{
    private final List<MethodParameterEntry> _entries;
    
    public MethodParametersAttribute(final List<MethodParameterEntry> entries) {
        super("MethodParameters", 1 + entries.size() * 4);
        this._entries = VerifyArgument.notNull(entries, "entries");
    }
    
    public List<MethodParameterEntry> getEntries() {
        return this._entries;
    }
}
