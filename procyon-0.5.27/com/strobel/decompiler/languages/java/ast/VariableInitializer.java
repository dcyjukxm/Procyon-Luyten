package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class VariableInitializer extends AstNode
{
    public static final VariableInitializer NULL;
    
    static {
        NULL = new NullVariableInitializer((NullVariableInitializer)null);
    }
    
    public VariableInitializer() {
        super();
    }
    
    public VariableInitializer(final String name) {
        super();
        this.setName(name);
    }
    
    public VariableInitializer(final String name, final Expression initializer) {
        super();
        this.setName(name);
        this.setInitializer(initializer);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    public final Expression getInitializer() {
        return this.getChildByRole(Roles.EXPRESSION);
    }
    
    public final void setInitializer(final Expression value) {
        this.setChildByRole(Roles.EXPRESSION, value);
    }
    
    public final String getName() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setName(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final Identifier getNameToken() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setNameToken(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    public final JavaTokenNode getAssignToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.ASSIGN);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitVariableInitializer(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof VariableInitializer) {
            final VariableInitializer otherInitializer = (VariableInitializer)other;
            return !other.isNull() && AstNode.matchString(this.getName(), otherInitializer.getName()) && this.getInitializer().matches(otherInitializer.getInitializer(), match);
        }
        return false;
    }
    
    @Override
    public String toString() {
        final Expression initializer = this.getInitializer();
        if (initializer.isNull()) {
            return "[VariableInitializer " + this.getName() + "]";
        }
        return "[VariableInitializer " + this.getName() + " = " + initializer + "]";
    }
    
    public static VariableInitializer forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    private static final class NullVariableInitializer extends VariableInitializer
    {
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
    
    private static final class PatternPlaceholder extends VariableInitializer
    {
        final Pattern child;
        
        PatternPlaceholder(final Pattern child) {
            super();
            this.child = child;
        }
        
        @Override
        public NodeType getNodeType() {
            return NodeType.PATTERN;
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return (R)visitor.visitPatternPlaceholder((AstNode)this, this.child, (Object)data);
        }
        
        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return this.child.matchesCollection(role, position, match, backtrackingInfo);
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return this.child.matches(other, match);
        }
    }
}
