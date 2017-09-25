package com.strobel.assembler.ir.attributes;

import java.util.*;
import com.strobel.core.*;

public final class LocalVariableTableAttribute extends SourceAttribute
{
    private final List<LocalVariableTableEntry> _entries;
    
    public LocalVariableTableAttribute(final String name, final LocalVariableTableEntry[] entries) {
        super(name, 2 + entries.length * 10);
        this._entries = ArrayUtilities.asUnmodifiableList((LocalVariableTableEntry[])entries.clone());
    }
    
    public List<LocalVariableTableEntry> getEntries() {
        return this._entries;
    }
}
