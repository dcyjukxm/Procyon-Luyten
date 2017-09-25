package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.core.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.decompiler.patterns.*;

public class JavaTokenNode extends AstNode
{
    private TextLocation _startLocation;
    public static final JavaTokenNode NULL;
    
    static {
        NULL = new NullJavaTokenNode();
    }
    
    public JavaTokenNode(final TextLocation startLocation) {
        super();
        this._startLocation = VerifyArgument.notNull(startLocation, "startLocation");
    }
    
    @Override
    public TextLocation getStartLocation() {
        return this._startLocation;
    }
    
    public void setStartLocation(final TextLocation startLocation) {
        this._startLocation = startLocation;
    }
    
    @Override
    public TextLocation getEndLocation() {
        return new TextLocation(this._startLocation.line(), this._startLocation.column() + this.getTokenLength());
    }
    
    @Override
    public String getText(final JavaFormattingOptions options) {
        final Role role = this.getRole();
        if (role instanceof TokenRole) {
            return ((TokenRole)role).getToken();
        }
        return null;
    }
    
    protected int getTokenLength() {
        final Role<?> role = this.getRole();
        if (role instanceof TokenRole) {
            return ((TokenRole)role).getLength();
        }
        return 0;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitJavaTokenNode(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.TOKEN;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof JavaTokenNode && !other.isNull() && !(other instanceof JavaModifierToken);
    }
    
    @Override
    public String toString() {
        return String.format("[JavaTokenNode: StartLocation=%s, EndLocation=%s, Role=%s]", this.getStartLocation(), this.getEndLocation(), this.getRole());
    }
    
    private static final class NullJavaTokenNode extends JavaTokenNode
    {
        public NullJavaTokenNode() {
            super(TextLocation.EMPTY);
        }
        
        @Override
        public final boolean isNull() {
            return true;
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return null;
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return other == null || other.isNull();
        }
    }
}
