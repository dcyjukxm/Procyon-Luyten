package com.strobel.decompiler.languages.java.ast.transforms;

import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.decompiler.languages.java.ast.*;

public class CollapseImportsTransform implements IAstTransform
{
    private final DecompilerSettings _settings;
    
    public CollapseImportsTransform(final DecompilerContext context) {
        super();
        this._settings = context.getSettings();
    }
    
    @Override
    public void run(final AstNode root) {
        if (!(root instanceof CompilationUnit)) {
            return;
        }
        final CompilationUnit compilationUnit = (CompilationUnit)root;
        if (this._settings.getForceExplicitImports()) {
            this.removeRedundantImports(compilationUnit);
            return;
        }
        final AstNodeCollection<ImportDeclaration> imports = compilationUnit.getImports();
        final PackageDeclaration packageDeclaration = compilationUnit.getChildByRole(Roles.PACKAGE);
        final String filePackage = packageDeclaration.isNull() ? null : packageDeclaration.getName();
        if (imports.isEmpty()) {
            return;
        }
        final Set<String> newImports = new LinkedHashSet<String>();
        final List<ImportDeclaration> removedImports = new ArrayList<ImportDeclaration>();
        for (final ImportDeclaration oldImport : imports) {
            final Identifier importedType = oldImport.getImportIdentifier();
            if (importedType != null && !importedType.isNull()) {
                final TypeReference type = oldImport.getUserData(Keys.TYPE_REFERENCE);
                if (type == null) {
                    continue;
                }
                final String packageName = type.getPackageName();
                if (!StringUtilities.isNullOrEmpty(packageName) && !StringUtilities.equals(packageName, "java.lang") && !StringUtilities.equals(packageName, filePackage)) {
                    newImports.add(packageName);
                }
                removedImports.add(oldImport);
            }
        }
        if (removedImports.isEmpty()) {
            return;
        }
        final ImportDeclaration lastRemoved = removedImports.get(removedImports.size() - 1);
        for (final String packageName2 : newImports) {
            compilationUnit.insertChildAfter(lastRemoved, new ImportDeclaration(PackageReference.parse(packageName2)), CompilationUnit.IMPORT_ROLE);
        }
        for (final ImportDeclaration removedImport : removedImports) {
            removedImport.remove();
        }
    }
    
    private void removeRedundantImports(final CompilationUnit compilationUnit) {
        final AstNodeCollection<ImportDeclaration> imports = compilationUnit.getImports();
        for (final ImportDeclaration node : imports) {
            if (StringUtilities.startsWith(node.getImport(), "java.lang.")) {
                node.remove();
            }
        }
    }
}
