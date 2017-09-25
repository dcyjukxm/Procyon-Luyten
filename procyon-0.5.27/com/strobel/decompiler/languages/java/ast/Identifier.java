package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;

public class Identifier extends AstNode
{
    private TextLocation _startLocation;
    private String _name;
    public static final Identifier NULL;
    
    static {
        NULL = new NullIdentifier(null);
    }
    
    private Identifier() {
        this("", TextLocation.EMPTY);
    }
    
    protected Identifier(final String name, final TextLocation location) {
        super();
        this._name = VerifyArgument.notNull(name, "name");
        this._startLocation = VerifyArgument.notNull(location, "location");
    }
    
    public final String getName() {
        return this._name;
    }
    
    public final void setName(final String name) {
        this.verifyNotFrozen();
        this._name = VerifyArgument.notNull(name, "name");
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
        final String name = this._name;
        return new TextLocation(this._startLocation.line(), this._startLocation.column() + ((name != null) ? name.length() : 0));
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitIdentifier(this, (Object)data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.TOKEN;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof Identifier && !other.isNull() && AstNode.matchString(this.getName(), ((Identifier)other).getName());
    }
    
    public static Identifier create(final String name) {
        return create(name, TextLocation.EMPTY);
    }
    
    public static Identifier create(final String name, final TextLocation location) {
        if (StringUtilities.isNullOrEmpty(name)) {
            return Identifier.NULL;
        }
        return new Identifier(name, location);
    }
    
    private static final class NullIdentifier extends Identifier
    {
        private NullIdentifier() {
            super(null);
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
