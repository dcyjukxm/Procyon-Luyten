package com.strobel.decompiler.languages;

import java.util.*;
import com.strobel.annotations.*;

public class TypeDecompilationResults
{
    private final List<LineNumberPosition> _lineNumberPositions;
    
    public TypeDecompilationResults(@Nullable final List<LineNumberPosition> lineNumberPositions) {
        super();
        this._lineNumberPositions = lineNumberPositions;
    }
    
    @NotNull
    public List<LineNumberPosition> getLineNumberPositions() {
        if (this._lineNumberPositions == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this._lineNumberPositions);
    }
}
