package com.strobel.decompiler.ast;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.decompiler.*;

public final class CatchBlock extends Block
{
    private final Collection<TypeReference> _caughtTypes;
    private TypeReference _exceptionType;
    private Variable _exceptionVariable;
    
    public CatchBlock() {
        super();
        this._caughtTypes = new Collection<TypeReference>();
    }
    
    public final List<TypeReference> getCaughtTypes() {
        return this._caughtTypes;
    }
    
    public final TypeReference getExceptionType() {
        return this._exceptionType;
    }
    
    public final void setExceptionType(final TypeReference exceptionType) {
        this._exceptionType = exceptionType;
    }
    
    public final Variable getExceptionVariable() {
        return this._exceptionVariable;
    }
    
    public final void setExceptionVariable(final Variable exceptionVariable) {
        this._exceptionVariable = exceptionVariable;
    }
    
    @Override
    public final void writeTo(final ITextOutput output) {
        output.writeKeyword("catch");
        if (!this._caughtTypes.isEmpty()) {
            output.write(" (");
            for (int i = 0; i < this._caughtTypes.size(); ++i) {
                final TypeReference caughtType = this._caughtTypes.get(i);
                if (i != 0) {
                    output.write(" | ");
                }
                output.writeReference(caughtType.getFullName(), caughtType);
            }
            if (this._exceptionVariable != null) {
                output.write(" %s", this._exceptionVariable.getName());
            }
            output.write(')');
        }
        else if (this._exceptionType != null) {
            output.write(" (");
            output.writeReference(this._exceptionType.getFullName(), this._exceptionType);
            if (this._exceptionVariable != null) {
                output.write(" %s", this._exceptionVariable.getName());
            }
            output.write(')');
        }
        output.writeLine(" {");
        output.indent();
        super.writeTo(output);
        output.unindent();
        output.writeLine("}");
    }
}
