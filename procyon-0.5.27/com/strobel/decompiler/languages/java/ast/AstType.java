package com.strobel.decompiler.languages.java.ast;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import com.strobel.util.*;
import com.strobel.decompiler.patterns.*;

public abstract class AstType extends AstNode
{
    public static final AstType[] EMPTY_TYPES;
    public static final AstType NULL;
    
    static {
        EMPTY_TYPES = new AstType[0];
        NULL = new NullAstType(null);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.TYPE_REFERENCE;
    }
    
    public TypeReference toTypeReference() {
        return this.getUserData(Keys.TYPE_REFERENCE);
    }
    
    public static AstType forPattern(final Pattern pattern) {
        return new PatternPlaceholder(VerifyArgument.notNull(pattern, "pattern"));
    }
    
    @Override
    public AstType clone() {
        return (AstType)super.clone();
    }
    
    public AstType makeArrayType() {
        final ComposedType composedType = new ComposedType();
        composedType.setBaseType(this);
        final TypeReference typeReference = this.getUserData(Keys.TYPE_REFERENCE);
        if (typeReference != null) {
            composedType.putUserData(Keys.TYPE_REFERENCE, typeReference);
        }
        composedType.makeArrayType();
        return composedType;
    }
    
    public InvocationExpression invoke(final String methodName, final Expression... arguments) {
        return new TypeReferenceExpression(-34, this).invoke(methodName, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<Expression> arguments) {
        return new TypeReferenceExpression(-34, this).invoke(methodName, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Expression... arguments) {
        return new TypeReferenceExpression(-34, this).invoke(methodName, typeArguments, arguments);
    }
    
    public InvocationExpression invoke(final String methodName, final Iterable<AstType> typeArguments, final Iterable<Expression> arguments) {
        return new TypeReferenceExpression(-34, this).invoke(methodName, typeArguments, arguments);
    }
    
    public MemberReferenceExpression member(final String memberName) {
        return new TypeReferenceExpression(-34, this).member(memberName);
    }
    
    private static final class NullAstType extends AstType
    {
        @Override
        public boolean isNull() {
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
        
        @Override
        public TypeReference toTypeReference() {
            throw ContractUtils.unreachable();
        }
    }
    
    private static final class PatternPlaceholder extends AstType
    {
        private final Pattern _child;
        
        PatternPlaceholder(final Pattern child) {
            super();
            this._child = child;
        }
        
        @Override
        public NodeType getNodeType() {
            return NodeType.PATTERN;
        }
        
        @Override
        public TypeReference toTypeReference() {
            throw ContractUtils.unsupported();
        }
        
        @Override
        public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
            return (R)visitor.visitPatternPlaceholder((AstNode)this, this._child, (Object)data);
        }
        
        @Override
        public boolean matches(final INode other, final Match match) {
            return this._child.matches(other, match);
        }
        
        @Override
        public boolean matchesCollection(final Role role, final INode position, final Match match, final BacktrackingInfo backtrackingInfo) {
            return this._child.matchesCollection(role, position, match, backtrackingInfo);
        }
    }
}
