package com.strobel.assembler.ir.attributes;

import java.util.*;
import com.strobel.core.*;

public final class InnerClassesAttribute extends SourceAttribute
{
    private final List<InnerClassEntry> _entries;
    
    public InnerClassesAttribute(final int length, final List<InnerClassEntry> entries) {
        super("InnerClasses", length);
        this._entries = VerifyArgument.notNull(entries, "entries");
    }
    
    public List<InnerClassEntry> getEntries() {
        return this._entries;
    }
}
