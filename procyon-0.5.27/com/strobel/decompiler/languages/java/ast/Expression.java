package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public abstract class Expression extends AstNode
{
    public static final Expression NULL;
    public static final int MYSTERY_OFFSET = -34;
    private int _offset;
    
    static {
        NULL = new NullExpression();
    }
    
    protected Expression(final int offset) {
        super();
        this._offset = offset;
    }
    
    public int getOffset() {
        return this._offset;
    }
    
    public void setOffset(final int offset) {
        this._offset = offset;
    }
    
    @Override
    public Expression clone() {
        return (Expression)super.clone();
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.EXPRESSION;
    }
    
    public static Expression forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    public InvocationExpression invoke(final Expression... arguments) {
        return new InvocationExpression(this.getOffset(), this, arguments);
    }
    
    public InvocationExpression invoke(final Iterable<Expression> arguments) {
        return new InvocationExpression(this.getOffset(), this, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Expression... arguments) {
        return this.invoke(methodName, (Iterable<AstType>)null, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<Expression> arguments) {
        return this.invoke(methodName, null, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Expression... arguments) {
        final MemberReferenceExpression mre = new MemberReferenceExpression(this.getOffset(), this, methodName, typeArguments);
        return new InvocationExpression(this.getOffset(), mre, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Iterable<Expression> arguments) {
        final MemberReferenceExpression mre = new MemberReferenceExpression(this.getOffset(), this, methodName, typeArguments);
        return new InvocationExpression(this.getOffset(), mre, arguments);
    }
    
    public MemberReferenceExpression member(final String memberName) {
        return new MemberReferenceExpression(this.getOffset(), this, memberName, new AstType[0]);
    }
    
    private static final class NullExpression extends Expression
    {
        public NullExpression() {
            super(-34);
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
    
    private static final class PatternPlaceholder extends Expression
    {
        final Pattern child;
        
        PatternPlaceholder(final Pattern child) {
            super(-34);
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
