package com.strobel.decompiler.languages.java;

import com.strobel.core.*;
import com.strobel.assembler.ir.attributes.*;
import java.util.*;

public class LineNumberTableConverter implements OffsetToLineNumberConverter
{
    private final int[] _offset2LineNo;
    private final int _maxOffset;
    
    public LineNumberTableConverter(final LineNumberTableAttribute lineNumberTable) {
        super();
        VerifyArgument.notNull(lineNumberTable, "lineNumberTable");
        this._maxOffset = lineNumberTable.getMaxOffset();
        Arrays.fill(this._offset2LineNo = new int[this._maxOffset + 1], -100);
        for (final LineNumberTableEntry entry : lineNumberTable.getEntries()) {
            this._offset2LineNo[entry.getOffset()] = entry.getLineNumber();
        }
        int lastLine = this._offset2LineNo[0];
        for (int i = 1; i < this._maxOffset + 1; ++i) {
            final int thisLine = this._offset2LineNo[i];
            if (thisLine == -100) {
                this._offset2LineNo[i] = lastLine;
            }
            else {
                lastLine = thisLine;
            }
        }
    }
    
    @Override
    public int getLineForOffset(int offset) {
        VerifyArgument.isNonNegative(offset, "offset");
        assert offset >= 0 : "offset must be >= 0; received an offset of " + offset;
        if (offset > this._maxOffset) {
            offset = this._maxOffset;
        }
        return this._offset2LineNo[offset];
    }
}
