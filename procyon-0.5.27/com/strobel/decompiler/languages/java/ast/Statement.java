package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public abstract class Statement extends AstNode
{
    private int _offset;
    public static final Statement NULL;
    
    static {
        NULL = new NullStatement(-34);
    }
    
    protected Statement(final int offset) {
        super();
        this._offset = offset;
    }
    
    @Override
    public Statement clone() {
        return (Statement)super.clone();
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.STATEMENT;
    }
    
    public boolean isEmbeddable() {
        return false;
    }
    
    public final Statement getNextStatement() {
        AstNode next;
        for (next = this.getNextSibling(); next != null && !(next instanceof Statement); next = next.getNextSibling()) {}
        return (Statement)next;
    }
    
    public final Statement getPreviousStatement() {
        AstNode previous;
        for (previous = this.getPreviousSibling(); previous != null && !(previous instanceof Statement); previous = previous.getPreviousSibling()) {}
        return (Statement)previous;
    }
    
    public static Statement forPattern(final Pattern pattern) {
        return new PatternPlaceholder(-34, VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public int getOffset() {
        return this._offset;
    }
    
    private static final class NullStatement extends Statement
    {
        public NullStatement(final int offset) {
            super(offset);
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
    
    private static final class PatternPlaceholder extends Statement
    {
        final Pattern child;
        
        PatternPlaceholder(final int offset, final Pattern child) {
            super(offset);
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
