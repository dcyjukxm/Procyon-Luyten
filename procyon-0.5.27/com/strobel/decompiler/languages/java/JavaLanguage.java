package com.strobel.decompiler.languages.java;

import com.strobel.core.*;
import com.strobel.decompiler.languages.java.ast.transforms.*;
import com.strobel.assembler.metadata.*;
import java.util.*;
import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.*;
import com.strobel.decompiler.languages.java.ast.*;

public class JavaLanguage extends Language
{
    private final String _name;
    private final Predicate<IAstTransform> _transformAbortCondition;
    
    public JavaLanguage() {
        this("Java", null);
    }
    
    private JavaLanguage(final String name, final Predicate<IAstTransform> transformAbortCondition) {
        super();
        this._name = name;
        this._transformAbortCondition = transformAbortCondition;
    }
    
    @Override
    public final String getName() {
        return this._name;
    }
    
    @Override
    public final String getFileExtension() {
        return ".java";
    }
    
    @Override
    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        final AstBuilder astBuilder = this.buildAst(type, options);
        final List<LineNumberPosition> lineNumberPositions = astBuilder.generateCode(output);
        return new TypeDecompilationResults(lineNumberPositions);
    }
    
    public CompilationUnit decompileTypeToAst(final TypeDefinition type, final DecompilationOptions options) {
        return this.buildAst(type, options).getCompilationUnit();
    }
    
    private AstBuilder buildAst(final TypeDefinition type, final DecompilationOptions options) {
        final AstBuilder builder = this.createAstBuilder(options, type, false);
        builder.addType(type);
        this.runTransforms(builder, options, null);
        return builder;
    }
    
    private AstBuilder createAstBuilder(final DecompilationOptions options, final TypeDefinition currentType, final boolean isSingleMember) {
        final DecompilerSettings settings = options.getSettings();
        final DecompilerContext context = new DecompilerContext();
        context.setCurrentType(currentType);
        context.setSettings(settings);
        return new AstBuilder(context);
    }
    
    private void runTransforms(final AstBuilder astBuilder, final DecompilationOptions options, final IAstTransform additionalTransform) {
        astBuilder.runTransformations(this._transformAbortCondition);
        if (additionalTransform != null) {
            additionalTransform.run(astBuilder.getCompilationUnit());
        }
    }
}
