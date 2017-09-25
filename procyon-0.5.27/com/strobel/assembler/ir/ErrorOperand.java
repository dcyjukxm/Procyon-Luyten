package com.strobel.assembler.ir;

public final class ErrorOperand
{
    private final String _message;
    
    public ErrorOperand(final String message) {
        super();
        this._message = message;
    }
    
    @Override
    public String toString() {
        if (this._message != null) {
            return this._message;
        }
        return "!!! BAD OPERAND !!!";
    }
}
