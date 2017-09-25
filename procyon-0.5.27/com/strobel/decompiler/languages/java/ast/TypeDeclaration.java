package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.patterns.*;

public class TypeDeclaration extends EntityDeclaration
{
    private ClassType _classType;
    public static final TypeDeclaration NULL;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType;
    
    static {
        NULL = new NullTypeDeclaration(null);
    }
    
    public final JavaTokenNode getTypeKeyword() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType()[this._classType.ordinal()]) {
            case 1: {
                return this.getChildByRole((Role<JavaTokenNode>)Roles.CLASS_KEYWORD);
            }
            case 2: {
                return this.getChildByRole((Role<JavaTokenNode>)Roles.INTERFACE_KEYWORD);
            }
            case 3: {
                return this.getChildByRole((Role<JavaTokenNode>)Roles.ANNOTATION_KEYWORD);
            }
            case 4: {
                return this.getChildByRole((Role<JavaTokenNode>)Roles.ENUM_KEYWORD);
            }
            default: {
                return JavaTokenNode.NULL;
            }
        }
    }
    
    public final ClassType getClassType() {
        return this._classType;
    }
    
    public final void setClassType(final ClassType classType) {
        this.verifyNotFrozen();
        this._classType = classType;
    }
    
    public final AstNodeCollection<TypeParameterDeclaration> getTypeParameters() {
        return this.getChildrenByRole(Roles.TYPE_PARAMETER);
    }
    
    public final AstNodeCollection<AstType> getInterfaces() {
        return this.getChildrenByRole(Roles.IMPLEMENTED_INTERFACE);
    }
    
    public final AstType getBaseType() {
        return this.getChildByRole(Roles.BASE_TYPE);
    }
    
    public final void setBaseType(final AstType value) {
        this.setChildByRole(Roles.BASE_TYPE, value);
    }
    
    public final JavaTokenNode getLeftBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.LEFT_BRACE);
    }
    
    public final AstNodeCollection<EntityDeclaration> getMembers() {
        return this.getChildrenByRole(Roles.TYPE_MEMBER);
    }
    
    public final JavaTokenNode getRightBraceToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.RIGHT_BRACE);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.TYPE_DECLARATION;
    }
    
    @Override
    public EntityType getEntityType() {
        return EntityType.TYPE_DEFINITION;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitTypeDeclaration(this, (Object)data);
    }
    
    @Override
    public TypeDeclaration clone() {
        final TypeDeclaration copy = (TypeDeclaration)super.clone();
        copy._classType = this._classType;
        return copy;
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof TypeDeclaration) {
            final TypeDeclaration otherDeclaration = (TypeDeclaration)other;
            return !otherDeclaration.isNull() && this._classType == otherDeclaration._classType && AstNode.matchString(this.getName(), otherDeclaration.getName()) && this.matchAnnotationsAndModifiers(otherDeclaration, match) && this.getTypeParameters().matches(otherDeclaration.getTypeParameters(), match) && this.getBaseType().matches(otherDeclaration.getBaseType(), match) && this.getInterfaces().matches(otherDeclaration.getInterfaces(), match) && this.getMembers().matches(otherDeclaration.getMembers(), match);
        }
        return false;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType() {
        final int[] loc_0 = TypeDeclaration.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ClassType.values().length];
        try {
            loc_1[ClassType.ANNOTATION.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ClassType.CLASS.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ClassType.ENUM.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ClassType.INTERFACE.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_5) {}
        return TypeDeclaration.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$ClassType = loc_1;
    }
    
    private static final class NullTypeDeclaration extends TypeDeclaration
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
