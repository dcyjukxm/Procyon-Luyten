package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;

public class PrimitiveExpression extends Expression
{
    public static final Object ANY_VALUE;
    public static final String ANY_STRING = "$any$";
    private TextLocation _startLocation;
    private TextLocation _endLocation;
    private String _literalValue;
    private Object _value;
    
    static {
        ANY_VALUE = new Object();
    }
    
    public PrimitiveExpression(final int offset, final Object value) {
        super(offset);
        this._value = value;
        this._startLocation = TextLocation.EMPTY;
        this._literalValue = "";
    }
    
    public PrimitiveExpression(final int offset, final Object value, final String literalValue) {
        super(offset);
        this._value = value;
        this._startLocation = TextLocation.EMPTY;
        this._literalValue = ((literalValue != null) ? literalValue : "");
    }
    
    public PrimitiveExpression(final int offset, final Object value, final TextLocation startLocation, final String literalValue) {
        super(offset);
        this._value = value;
        this._startLocation = startLocation;
        this._literalValue = ((literalValue != null) ? literalValue : "");
    }
    
    @Override
    public TextLocation getStartLocation() {
        final TextLocation startLocation = this._startLocation;
        return (startLocation != null) ? startLocation : TextLocation.EMPTY;
    }
    
    @Override
    public TextLocation getEndLocation() {
        if (this._endLocation == null) {
            final TextLocation startLocation = this.getStartLocation();
            if (this._literalValue == null) {
                return startLocation;
            }
            this._endLocation = new TextLocation(this._startLocation.line(), this._startLocation.column() + this._literalValue.length());
        }
        return this._endLocation;
    }
    
    public final void setStartLocation(final TextLocation startLocation) {
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
        this._endLocation = null;
    }
    
    public final String getLiteralValue() {
        return this._literalValue;
    }
    
    public final void setLiteralValue(final String literalValue) {
        this.verifyNotFrozen();
        this._literalValue = literalValue;
        this._endLocation = null;
    }
    
    public final Object getValue() {
        return this._value;
    }
    
    public final void setValue(final Object value) {
        this.verifyNotFrozen();
        this._value = value;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitPrimitiveExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof PrimitiveExpression) {
            final PrimitiveExpression otherPrimitive = (PrimitiveExpression)other;
            return !other.isNull() && (this._value == PrimitiveExpression.ANY_VALUE || (this._value == "$any$" && otherPrimitive._value instanceof String) || Comparer.equals(this._value, otherPrimitive._value));
        }
        return false;
    }
}
