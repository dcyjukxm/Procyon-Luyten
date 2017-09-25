package com.strobel.decompiler.languages.java.ast;

import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.patterns.*;

public class PackageDeclaration extends AstNode
{
    public static final PackageDeclaration NULL;
    
    static {
        NULL = new NullPackageDeclaration((NullPackageDeclaration)null);
    }
    
    public PackageDeclaration() {
        super();
    }
    
    public PackageDeclaration(final String name) {
        super();
        this.setName(name);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    public final JavaTokenNode getPackageToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.PACKAGE_KEYWORD);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    public final AstNodeCollection<Identifier> getIdentifiers() {
        return this.getChildrenByRole(Roles.IDENTIFIER);
    }
    
    public final String getName() {
        final StringBuilder sb = new StringBuilder();
        for (final Identifier identifier : this.getIdentifiers()) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(identifier.getName());
        }
        return sb.toString();
    }
    
    public final void setName(final String name) {
        if (name == null) {
            this.getChildrenByRole(Roles.IDENTIFIER).clear();
            return;
        }
        final String[] parts = name.split("\\.");
        final Identifier[] identifiers = new Identifier[parts.length];
        for (int i = 0; i < identifiers.length; ++i) {
            identifiers[i] = Identifier.create(parts[i]);
        }
        this.getChildrenByRole(Roles.IDENTIFIER).replaceWith(ArrayUtilities.asUnmodifiableList(identifiers));
    }
    
    public static String BuildQualifiedName(final String name1, final String name2) {
        if (StringUtilities.isNullOrEmpty(name1)) {
            return name2;
        }
        if (StringUtilities.isNullOrEmpty(name2)) {
            return name1;
        }
        return String.valueOf(name1) + "." + name2;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitPackageDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof PackageDeclaration && !other.isNull() && AstNode.matchString(this.getName(), ((PackageDeclaration)other).getName());
    }
    
    private static final class NullPackageDeclaration extends PackageDeclaration
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
