package com.strobel.assembler.ir.attributes;

import java.util.*;
import com.strobel.core.*;

public final class LineNumberTableAttribute extends SourceAttribute
{
    private final List<LineNumberTableEntry> _entries;
    private final int _maxOffset;
    
    public LineNumberTableAttribute(final LineNumberTableEntry[] entries) {
        super("LineNumberTable", 2 + VerifyArgument.notNull(entries, "entries").length * 4);
        this._entries = ArrayUtilities.asUnmodifiableList((LineNumberTableEntry[])entries.clone());
        int max = Integer.MIN_VALUE;
        for (final LineNumberTableEntry entry : entries) {
            final int offset = entry.getOffset();
            if (offset > max) {
                max = offset;
            }
        }
        this._maxOffset = max;
    }
    
    public List<LineNumberTableEntry> getEntries() {
        return this._entries;
    }
    
    public int getMaxOffset() {
        return this._maxOffset;
    }
}
