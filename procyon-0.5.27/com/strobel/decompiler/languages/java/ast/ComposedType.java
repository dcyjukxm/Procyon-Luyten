package com.strobel.decompiler.languages.java.ast;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.patterns.*;

public class ComposedType extends AstType
{
    public static final Role<ArraySpecifier> ARRAY_SPECIFIER_ROLE;
    
    static {
        ARRAY_SPECIFIER_ROLE = new Role<ArraySpecifier>("ArraySpecifier", ArraySpecifier.class);
    }
    
    public ComposedType() {
        super();
    }
    
    public ComposedType(final AstType baseType) {
        super();
        this.setBaseType(baseType);
    }
    
    public final AstType getBaseType() {
        return this.getChildByRole(Roles.BASE_TYPE);
    }
    
    public final void setBaseType(final AstType value) {
        this.setChildByRole(Roles.BASE_TYPE, value);
    }
    
    public final AstNodeCollection<ArraySpecifier> getArraySpecifiers() {
        return this.getChildrenByRole(ComposedType.ARRAY_SPECIFIER_ROLE);
    }
    
    @Override
    public TypeReference toTypeReference() {
        TypeReference typeReference = this.getBaseType().toTypeReference();
        for (ArraySpecifier specifier = this.getArraySpecifiers().firstOrNullObject(); specifier != null; specifier = specifier.getNextSibling(ComposedType.ARRAY_SPECIFIER_ROLE)) {
            typeReference = typeReference.makeArrayType();
        }
        return typeReference;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitComposedType(this, (Object)data);
    }
    
    @Override
    public AstType makeArrayType() {
        this.insertChildBefore(CollectionUtilities.firstOrDefault(this.getArraySpecifiers()), new ArraySpecifier(), ComposedType.ARRAY_SPECIFIER_ROLE);
        final TypeReference typeReference = this.getUserData(Keys.TYPE_REFERENCE);
        if (typeReference != null) {
            this.putUserData(Keys.TYPE_REFERENCE, typeReference.makeArrayType());
        }
        return this;
    }
    
    @Override
    public String toString() {
        final AstNodeCollection<ArraySpecifier> arraySpecifiers = this.getArraySpecifiers();
        final StringBuilder sb = new StringBuilder();
        sb.append(this.getBaseType());
        for (final ArraySpecifier arraySpecifier : arraySpecifiers) {
            sb.append(arraySpecifier);
        }
        return sb.toString();
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ComposedType && this.getArraySpecifiers().matches(((ComposedType)other).getArraySpecifiers(), match);
    }
}
