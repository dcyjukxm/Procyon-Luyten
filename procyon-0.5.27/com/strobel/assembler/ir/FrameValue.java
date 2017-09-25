package com.strobel.assembler.ir;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class FrameValue
{
    public static final FrameValue[] EMPTY_VALUES;
    public static final FrameValue EMPTY;
    public static final FrameValue OUT_OF_SCOPE;
    public static final FrameValue TOP;
    public static final FrameValue INTEGER;
    public static final FrameValue FLOAT;
    public static final FrameValue LONG;
    public static final FrameValue DOUBLE;
    public static final FrameValue NULL;
    public static final FrameValue UNINITIALIZED_THIS;
    public static final FrameValue UNINITIALIZED;
    private final FrameValueType _type;
    private final Object _parameter;
    
    static {
        EMPTY_VALUES = new FrameValue[0];
        EMPTY = new FrameValue(FrameValueType.Empty);
        OUT_OF_SCOPE = new FrameValue(FrameValueType.Top);
        TOP = new FrameValue(FrameValueType.Top);
        INTEGER = new FrameValue(FrameValueType.Integer);
        FLOAT = new FrameValue(FrameValueType.Float);
        LONG = new FrameValue(FrameValueType.Long);
        DOUBLE = new FrameValue(FrameValueType.Double);
        NULL = new FrameValue(FrameValueType.Null);
        UNINITIALIZED_THIS = new FrameValue(FrameValueType.UninitializedThis);
        UNINITIALIZED = new FrameValue(FrameValueType.Uninitialized);
    }
    
    private FrameValue(final FrameValueType type) {
        super();
        this._type = type;
        this._parameter = null;
    }
    
    private FrameValue(final FrameValueType type, final Object parameter) {
        super();
        this._type = type;
        this._parameter = parameter;
    }
    
    public final FrameValueType getType() {
        return this._type;
    }
    
    public final Object getParameter() {
        return this._parameter;
    }
    
    public final boolean isUninitialized() {
        return this._type == FrameValueType.Uninitialized || this._type == FrameValueType.UninitializedThis;
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof FrameValue) {
            final FrameValue that = (FrameValue)o;
            return that._type == this._type && Comparer.equals(that._parameter, this._parameter);
        }
        return false;
    }
    
    @Override
    public final int hashCode() {
        int result = this._type.hashCode();
        result = 31 * result + ((this._parameter != null) ? this._parameter.hashCode() : 0);
        return result;
    }
    
    @Override
    public final String toString() {
        if (this._type == FrameValueType.Reference) {
            return String.format("%s(%s)", this._type, ((TypeReference)this._parameter).getSignature());
        }
        return this._type.name();
    }
    
    public static FrameValue makeReference(final TypeReference type) {
        return new FrameValue(FrameValueType.Reference, VerifyArgument.notNull(type, "type"));
    }
    
    public static FrameValue makeAddress(final Instruction target) {
        return new FrameValue(FrameValueType.Address, VerifyArgument.notNull(target, "target"));
    }
    
    public static FrameValue makeUninitializedReference(final Instruction newInstruction) {
        VerifyArgument.notNull(newInstruction, "newInstruction");
        if (newInstruction.getOpCode() != OpCode.NEW) {
            throw new IllegalArgumentException("Parameter must be a NEW instruction.");
        }
        return new FrameValue(FrameValueType.Uninitialized, newInstruction);
    }
}
