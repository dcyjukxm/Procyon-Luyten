package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.decompiler.patterns.*;

public class ArrayInitializerExpression extends Expression
{
    public static final ArrayInitializerExpression NULL;
    
    static {
        NULL = new NullArrayInitializerExpression((NullArrayInitializerExpression)null);
    }
    
    public ArrayInitializerExpression() {
        super(-34);
    }
    
    public ArrayInitializerExpression(final Iterable<Expression> elements) {
        super(-34);
        if (elements != null) {
            final AstNodeCollection<Expression> elementsCollection = this.getElements();
            for (final Expression element : elements) {
                elementsCollection.add(element);
            }
        }
    }
    
    public ArrayInitializerExpression(final Expression... elements) {
        super(-34);
        if (elements != null) {
            Collections.addAll(this.getElements(), elements);
        }
    }
    
    public final JavaTokenNode getLeftBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACE);
    }
    
    public final AstNodeCollection<Expression> getElements() {
        return this.getChildrenByRole(Roles.EXPRESSION);
    }
    
    public final JavaTokenNode getRightBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitArrayInitializerExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ArrayInitializerExpression) {
            final ArrayInitializerExpression otherInitializer = (ArrayInitializerExpression)other;
            return !otherInitializer.isNull() && this.getElements().matches(otherInitializer.getElements(), match);
        }
        return false;
    }
    
    private static final class NullArrayInitializerExpression extends ArrayInitializerExpression
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
}
