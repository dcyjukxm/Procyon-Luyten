package com.strobel.decompiler.languages.java.ast;

import javax.lang.model.element.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class VariableDeclarationStatement extends Statement
{
    public static final Role<JavaModifierToken> MODIFIER_ROLE;
    private boolean _anyModifiers;
    
    static {
        MODIFIER_ROLE = EntityDeclaration.MODIFIER_ROLE;
    }
    
    public VariableDeclarationStatement() {
        super(-34);
    }
    
    public VariableDeclarationStatement(final AstType type, final String name, final int offset) {
        this(type, name, offset, null);
    }
    
    public VariableDeclarationStatement(final AstType type, final String name, final Expression initializer) {
        this(type, name, -34, initializer);
    }
    
    public VariableDeclarationStatement(final AstType type, final String name, final int offset, final Expression initializer) {
        super((initializer == null) ? offset : initializer.getOffset());
        this.setType(type);
        this.getVariables().add(new VariableInitializer(name, initializer));
    }
    
    public final boolean isAnyModifiers() {
        return this._anyModifiers;
    }
    
    public final void setAnyModifiers(final boolean value) {
        this.verifyNotFrozen();
        this._anyModifiers = value;
    }
    
    public final List<Modifier> getModifiers() {
        return EntityDeclaration.getModifiers(this);
    }
    
    public final void addModifier(final Modifier modifier) {
        EntityDeclaration.addModifier(this, modifier);
    }
    
    public final void removeModifier(final Modifier modifier) {
        EntityDeclaration.removeModifier(this, modifier);
    }
    
    public final void setModifiers(final List<Modifier> modifiers) {
        EntityDeclaration.setModifiers(this, modifiers);
    }
    
    public final AstType getType() {
        return this.getChildByRole(Roles.TYPE);
    }
    
    public final void setType(final AstType value) {
        this.setChildByRole(Roles.TYPE, value);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    public final AstNodeCollection<VariableInitializer> getVariables() {
        return this.getChildrenByRole(Roles.VARIABLE);
    }
    
    public final VariableInitializer getVariable(final String name) {
        return this.getVariables().firstOrNullObject(new Predicate<VariableInitializer>() {
            @Override
            public boolean test(final VariableInitializer variable) {
                return StringUtilities.equals(variable.getName(), name);
            }
        });
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitVariableDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        if (other instanceof VariableDeclarationStatement) {
            final VariableDeclarationStatement otherDeclaration = (VariableDeclarationStatement)other;
            return !other.isNull() && this.getType().matches(otherDeclaration.getType(), match) && (this.isAnyModifiers() || otherDeclaration.isAnyModifiers() || this.getChildrenByRole(VariableDeclarationStatement.MODIFIER_ROLE).matches(otherDeclaration.getChildrenByRole(VariableDeclarationStatement.MODIFIER_ROLE), match)) && this.getVariables().matches(otherDeclaration.getVariables(), match);
        }
        return false;
    }
}
