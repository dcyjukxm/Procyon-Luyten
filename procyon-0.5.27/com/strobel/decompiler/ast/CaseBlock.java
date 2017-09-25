package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.decompiler.*;
import java.util.*;

public final class CaseBlock extends Block
{
    private final List<Integer> _values;
    
    public CaseBlock() {
        super();
        this._values = new Collection<Integer>();
    }
    
    public final List<Integer> getValues() {
        return this._values;
    }
    
    public final boolean isDefault() {
        return this._values.isEmpty();
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        if (this.isDefault()) {
            output.writeKeyword("default");
            output.writeLine(":");
        }
        else {
            for (final Integer value : this._values) {
                output.writeKeyword("case");
                output.writeLine(" %d:", value);
            }
        }
        output.indent();
        super.writeTo(output);
        output.unindent();
    }
}
