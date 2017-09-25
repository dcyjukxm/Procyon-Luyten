package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public final class EmptyStatement extends Statement
{
    private TextLocation _location;
    
    public EmptyStatement() {
        super(-34);
    }
    
    public TextLocation getLocation() {
        return this._location;
    }
    
    public void setLocation(final TextLocation location) {
        this.verifyNotFrozen();
        this._location = location;
    }
    
    @Override
    public TextLocation getStartLocation() {
        return this.getLocation();
    }
    
    @Override
    public TextLocation getEndLocation() {
        final TextLocation location = this.getLocation();
        return new TextLocation(location.line(), location.column() + 1);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitEmptyStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof EmptyStatement;
    }
}
