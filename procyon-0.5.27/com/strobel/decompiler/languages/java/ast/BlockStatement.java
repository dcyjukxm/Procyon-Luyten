package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class BlockStatement extends Statement implements Iterable<Statement>
{
    public static final Role<Statement> STATEMENT_ROLE;
    public static final BlockStatement NULL;
    
    static {
        STATEMENT_ROLE = new Role<Statement>("Statement", Statement.class, Statement.NULL);
        NULL = new NullBlockStatement(null);
    }
    
    public BlockStatement() {
        super(-34);
    }
    
    public BlockStatement(final Iterable<Statement> statements) {
        super(-34);
        if (statements != null) {
            for (final Statement statement : statements) {
                this.getStatements().add(statement);
            }
        }
    }
    
    public BlockStatement(final Statement... statements) {
        super(-34);
        Collections.addAll(this.getStatements(), statements);
    }
    
    public final JavaTokenNode getLeftBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACE);
    }
    
    public final AstNodeCollection<Statement> getStatements() {
        return this.getChildrenByRole(BlockStatement.STATEMENT_ROLE);
    }
    
    public final JavaTokenNode getRightBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACE);
    }
    
    public final void add(final Statement statement) {
        this.addChild(statement, BlockStatement.STATEMENT_ROLE);
    }
    
    public final void add(final Expression expression) {
        this.addChild(new ExpressionStatement(expression), BlockStatement.STATEMENT_ROLE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitBlockStatement(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof BlockStatement && !other.isNull() && this.getStatements().matches(((BlockStatement)other).getStatements(), match);
    }
    
    @Override
    public final Iterator<Statement> iterator() {
        return this.getStatements().iterator();
    }
    
    public static BlockStatement forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    private static final class NullBlockStatement extends BlockStatement
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
    
    private static final class PatternPlaceholder extends BlockStatement
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
