package com.strobel.decompiler.languages.java.ast;

import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.patterns.*;

public class ImportDeclaration extends AstNode
{
    public static final TokenRole IMPORT_KEYWORD_RULE;
    public static final ImportDeclaration NULL;
    
    static {
        IMPORT_KEYWORD_RULE = new TokenRole("import", 1);
        NULL = new NullImportDeclaration((NullImportDeclaration)null);
    }
    
    public ImportDeclaration() {
        super();
    }
    
    public ImportDeclaration(final String packageOrTypeName) {
        super();
        this.setImport(packageOrTypeName);
    }
    
    public ImportDeclaration(final PackageReference pkg) {
        super();
        this.setImport(String.valueOf(VerifyArgument.notNull(pkg, "pkg").getFullName()) + ".*");
        this.putUserData(Keys.PACKAGE_REFERENCE, pkg);
    }
    
    public ImportDeclaration(final TypeReference type) {
        super();
        this.setImport(String.valueOf(VerifyArgument.notNull(type, "pkg").getFullName()) + ".*");
        this.putUserData(Keys.TYPE_REFERENCE, type);
    }
    
    public ImportDeclaration(final AstType type) {
        super();
        final TypeReference typeReference = VerifyArgument.notNull(type, "type").toTypeReference();
        if (typeReference != null) {
            this.setImport(typeReference.getFullName());
            this.putUserData(Keys.TYPE_REFERENCE, typeReference);
        }
        else {
            this.setImport(type.toString());
        }
    }
    
    public final String getImport() {
        return this.getChildByRole(Roles.IDENTIFIER).getName();
    }
    
    public final void setImport(final String value) {
        this.setChildByRole(Roles.IDENTIFIER, Identifier.create(value));
    }
    
    public final Identifier getImportIdentifier() {
        return this.getChildByRole(Roles.IDENTIFIER);
    }
    
    public final void setImportIdentifier(final Identifier value) {
        this.setChildByRole(Roles.IDENTIFIER, value);
    }
    
    public final JavaTokenNode getImportToken() {
        return this.getChildByRole((Role<JavaTokenNode>)ImportDeclaration.IMPORT_KEYWORD_RULE);
    }
    
    public final JavaTokenNode getSemicolonToken() {
        return this.getChildByRole((Role<JavaTokenNode>)Roles.SEMICOLON);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.UNKNOWN;
    }
    
    @Override
    public <T, R> R acceptVisitor(final IAstVisitor<? super T, ? extends R> visitor, final T data) {
        return (R)visitor.visitImportDeclaration(this, (Object)data);
    }
    
    @Override
    public boolean matches(final INode other, final Match match) {
        return other instanceof ImportDeclaration && this.getImportIdentifier().matches(((ImportDeclaration)other).getImportIdentifier(), match);
    }
    
    private static final class NullImportDeclaration extends ImportDeclaration
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
