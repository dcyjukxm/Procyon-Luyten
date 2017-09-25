package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.patterns.*;

public class ArrayCreationExpression extends Expression
{
    public static final TokenRole NEW_KEYWORD_ROLE;
    public static final Role<ArraySpecifier> ADDITIONAL_ARRAY_SPECIFIER_ROLE;
    public static final Role<ArrayInitializerExpression> INITIALIZER_ROLE;
    
    static {
        NEW_KEYWORD_ROLE = new TokenRole("new", 1);
        ADDITIONAL_ARRAY_SPECIFIER_ROLE = new Role<ArraySpecifier>("AdditionalArraySpecifier", ArraySpecifier.class);
        INITIALIZER_ROLE = new Role<ArrayInitializerExpression>("Initializer", ArrayInitializerExpression.class, ArrayInitializerExpression.NULL);
    }
    
    public ArrayCreationExpression(final int offset) {
        super(offset);
    }
    
    public final AstNodeCollection<Expression> getDimensions() {
        return this.getChildrenByRole(Roles.ARGUMENT);
    }
    
    public final ArrayInitializerExpression getInitializer() {
        return this.getChildByRole(ArrayCreationExpression.INITIALIZER_ROLE);
    }
    
    public final void setInitializer(final ArrayInitializerExpression value) {
        this.setChildByRole(ArrayCreationExpression.INITIALIZER_ROLE, value);
    }
    
    public final AstNodeCollection<ArraySpecifier> getAdditionalArraySpecifiers() {
        return this.getChildrenByRole(ArrayCreationExpression.ADDITIONAL_ARRAY_SPECIFIER_ROLE);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    public final JavaTokenNode getNewToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ArrayCreationExpression.NEW_KEYWORD_ROLE);
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitArrayCreationExpression(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof ArrayCreationExpression) {
            final ArrayCreationExpression otherExpression = (ArrayCreationExpression)other;
            return !otherExpression.isNull() && this.getType().matches(otherExpression.getType(), match) && this.getDimensions().matches(otherExpression.getDimensions(), match) && this.getInitializer().matches(otherExpression.getInitializer(), match) && this.getAdditionalArraySpecifiers().matches(otherExpression.getAdditionalArraySpecifiers(), match);
        }
        return false;
    }
}
