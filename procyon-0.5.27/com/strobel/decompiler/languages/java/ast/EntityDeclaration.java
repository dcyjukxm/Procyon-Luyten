package com.strobel.decompiler.languages.java.ast;

import javax.lang.model.element.*;
import com.strobel.decompiler.patterns.*;
import com.strobel.core.*;
import java.util.*;
import com.strobel.decompiler.languages.*;

public abstract class EntityDeclaration extends AstNode
{
    public static final Role<Annotation> ANNOTATION_ROLE;
    public static final Role<Annotation> UNATTACHED_ANNOTATION_ROLE;
    public static final Role<JavaModifierToken> MODIFIER_ROLE;
    public static final Role<AstType> PRIVATE_IMPLEMENTATION_TYPE_ROLE;
    private boolean _anyModifiers;
    
    static {
        ANNOTATION_ROLE = Roles.ANNOTATION;
        UNATTACHED_ANNOTATION_ROLE = new Role<Annotation>("UnattachedAnnotation", Annotation.class);
        MODIFIER_ROLE = new Role<JavaModifierToken>("Modifier", JavaModifierToken.class);
        PRIVATE_IMPLEMENTATION_TYPE_ROLE = new Role<AstType>("PrivateImplementationType", AstType.class, AstType.NULL);
    }
    
    public final boolean isAnyModifiers() {
        return this._anyModifiers;
    }
    
    public final void setAnyModifiers(final boolean value) {
        this.verifyNotFrozen();
        this._anyModifiers = value;
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.MEMBER;
    }
    
    public abstract EntityType getEntityType();
    
    public final AstNodeCollection<Annotation> getAnnotations() {
        return this.getChildrenByRole(EntityDeclaration.ANNOTATION_ROLE);
    }
    
    public final boolean hasModifier(final Modifier modifier) {
        for (final JavaModifierToken modifierToken : this.getModifiers()) {
            if (modifierToken.getModifier() == modifier) {
                return true;
            }
        }
        return false;
    }
    
    public final AstNodeCollection<JavaModifierToken> getModifiers() {
        return this.getChildrenByRole(EntityDeclaration.MODIFIER_ROLE);
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
    
    public final AstType getReturnType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setReturnType(final AstType type) {
        this.setChildByRole(Roles.TYPE, type);
    }
    
    @Override
    public EntityDeclaration clone() {
        final EntityDeclaration copy = (EntityDeclaration)super.clone();
        copy._anyModifiers = this._anyModifiers;
        return copy;
    }
    
    protected final boolean matchAnnotationsAndModifiers(final EntityDeclaration other, final Match match) {
        VerifyArgument.notNull(other, "other");
        return other != null && !other.isNull() && (this.isAnyModifiers() || this.getModifiers().matches(other.getModifiers(), match)) && this.getAnnotations().matches(other.getAnnotations(), match);
    }
    
    static List<Modifier> getModifiers(final AstNode node) {
        List<Modifier> modifiers = null;
        for (final JavaModifierToken modifierToken : node.getChildrenByRole(EntityDeclaration.MODIFIER_ROLE)) {
            if (modifiers == null) {
                modifiers = new ArrayList<Modifier>();
            }
            modifiers.add(modifierToken.getModifier());
        }
        return (modifiers != null) ? Collections.unmodifiableList(modifiers) : Collections.emptyList();
    }
    
    static void setModifiers(final AstNode node, final Collection<Modifier> modifiers) {
        final AstNodeCollection<JavaModifierToken> modifierTokens = node.getChildrenByRole(EntityDeclaration.MODIFIER_ROLE);
        modifierTokens.clear();
        for (final Modifier modifier : modifiers) {
            modifierTokens.add(new JavaModifierToken(TextLocation.EMPTY, modifier));
        }
    }
    
    static void addModifier(final AstNode node, final Modifier modifier) {
        final List<Modifier> modifiers = getModifiers(node);
        if (modifiers.contains(modifier)) {
            return;
        }
        node.addChild(new JavaModifierToken(TextLocation.EMPTY, modifier), EntityDeclaration.MODIFIER_ROLE);
    }
    
    static boolean removeModifier(final AstNode node, final Modifier modifier) {
        final AstNodeCollection<JavaModifierToken> modifierTokens = node.getChildrenByRole(EntityDeclaration.MODIFIER_ROLE);
        for (final JavaModifierToken modifierToken : modifierTokens) {
            if (modifierToken.getModifier() == modifier) {
                modifierToken.remove();
                return true;
            }
        }
        return false;
    }
}
