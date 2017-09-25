package com.strobel.decompiler.languages;

import com.strobel.decompiler.ast.*;
import java.util.*;
import javax.lang.model.element.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.*;
import com.strobel.core.*;

public class BytecodeAstLanguage extends Language
{
    private final String _name;
    private final boolean _inlineVariables;
    private final AstOptimizationStep _abortBeforeStep;
    
    public BytecodeAstLanguage() {
        this("Bytecode AST", true, AstOptimizationStep.None);
    }
    
    private BytecodeAstLanguage(final String name, final boolean inlineVariables, final AstOptimizationStep abortBeforeStep) {
        super();
        this._name = name;
        this._inlineVariables = inlineVariables;
        this._abortBeforeStep = abortBeforeStep;
    }
    
    @Override
    public String getName() {
        return this._name;
    }
    
    @Override
    public String getFileExtension() {
        return ".jvm";
    }
    
    @Override
    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        this.writeTypeHeader(type, output);
        output.writeLine(" {");
        output.indent();
        try {
            boolean first = true;
            for (final MethodDefinition method : type.getDeclaredMethods()) {
                if (!first) {
                    output.writeLine();
                }
                else {
                    first = false;
                }
                this.decompileMethod(method, output, options);
            }
            if (!options.getSettings().getExcludeNestedTypes()) {
                for (final TypeDefinition innerType : type.getDeclaredTypes()) {
                    output.writeLine();
                    this.decompileType(innerType, output, options);
                }
            }
        }
        finally {
            output.unindent();
            output.writeLine("}");
        }
        output.unindent();
        output.writeLine("}");
        return new TypeDecompilationResults(null);
    }
    
    @Override
    public void decompileMethod(final MethodDefinition method, final ITextOutput output, final DecompilationOptions options) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(options, "options");
        this.writeMethodHeader(method, output);
        final MethodBody body = method.getBody();
        if (body == null) {
            output.writeDelimiter(";");
            output.writeLine();
            return;
        }
        final DecompilerContext context = new DecompilerContext();
        context.setCurrentMethod(method);
        context.setCurrentType(method.getDeclaringType());
        final Block methodAst = new Block();
        output.writeLine(" {");
        output.indent();
        try {
            methodAst.getBody().addAll(AstBuilder.build(body, this._inlineVariables, context));
            if (this._abortBeforeStep != null) {
                AstOptimizer.optimize(context, methodAst, this._abortBeforeStep);
            }
            final Set<Variable> allVariables = new LinkedHashSet<Variable>();
            for (final Expression e : methodAst.getSelfAndChildrenRecursive(Expression.class)) {
                final Object operand = e.getOperand();
                if (operand instanceof Variable && !((Variable)operand).isParameter()) {
                    allVariables.add((Variable)operand);
                }
            }
            if (!allVariables.isEmpty()) {
                for (final Variable variable : allVariables) {
                    output.writeDefinition(variable.getName(), variable);
                    final TypeReference type = variable.getType();
                    if (type != null) {
                        output.write(" : ");
                        DecompilerHelpers.writeType(output, type, NameSyntax.SHORT_TYPE_NAME);
                    }
                    if (variable.isGenerated()) {
                        output.write(" [generated]");
                    }
                    output.writeLine();
                }
                output.writeLine();
            }
            methodAst.writeTo(output);
        }
        catch (Throwable t) {
            writeError(output, t);
            return;
        }
        finally {
            output.unindent();
            output.writeLine("}");
        }
        output.unindent();
        output.writeLine("}");
    }
    
    private static void writeError(final ITextOutput output, final Throwable t) {
        final List<String> lines = StringUtilities.split(ExceptionUtilities.getStackTraceString(t), true, '\r', '\n');
        for (final String line : lines) {
            output.writeComment("// " + line.replace("\t", "    "));
            output.writeLine();
        }
    }
    
    private void writeTypeHeader(final TypeDefinition type, final ITextOutput output) {
        long flags = type.getFlags() & 0x7E19L;
        if (type.isInterface()) {
            flags &= 0xFFFFFFFFFFFFFBFFL;
        }
        else if (type.isEnum()) {
            flags &= 0x7L;
        }
        for (final Modifier modifier : Flags.asModifierSet(flags)) {
            output.writeKeyword(modifier.toString());
            output.write(' ');
        }
        if (type.isInterface()) {
            if (type.isAnnotation()) {
                output.writeKeyword("@interface");
            }
            else {
                output.writeKeyword("interface");
            }
        }
        else if (type.isEnum()) {
            output.writeKeyword("enum");
        }
        else {
            output.writeKeyword("class");
        }
        output.write(' ');
        DecompilerHelpers.writeType(output, type, NameSyntax.TYPE_NAME, true);
    }
    
    private void writeMethodHeader(final MethodDefinition method, final ITextOutput output) {
        if (method.isTypeInitializer()) {
            output.writeKeyword("static");
            return;
        }
        if (!method.getDeclaringType().isInterface()) {
            for (final Modifier modifier : Flags.asModifierSet(method.getFlags() & 0xD3FL)) {
                output.writeKeyword(modifier.toString());
                output.write(' ');
            }
        }
        if (!method.isTypeInitializer()) {
            DecompilerHelpers.writeType(output, method.getReturnType(), NameSyntax.TYPE_NAME);
            output.write(' ');
            if (method.isConstructor()) {
                output.writeReference(method.getDeclaringType().getName(), method.getDeclaringType());
            }
            else {
                output.writeReference(method.getName(), method);
            }
            output.write("(");
            final List<ParameterDefinition> parameters = method.getParameters();
            for (int i = 0; i < parameters.size(); ++i) {
                final ParameterDefinition parameter = parameters.get(i);
                if (i != 0) {
                    output.write(", ");
                }
                DecompilerHelpers.writeType(output, parameter.getParameterType(), NameSyntax.TYPE_NAME);
                output.write(' ');
                output.writeReference(parameter.getName(), parameter);
            }
            output.write(")");
        }
    }
    
    @Override
    public String typeToString(final TypeReference type, final boolean includePackage) {
        final ITextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeType(output, type, includePackage ? NameSyntax.TYPE_NAME : NameSyntax.SHORT_TYPE_NAME);
        return output.toString();
    }
    
    public static List<BytecodeAstLanguage> getDebugLanguages() {
        final AstOptimizationStep[] steps = AstOptimizationStep.values();
        final BytecodeAstLanguage[] languages = new BytecodeAstLanguage[steps.length];
        languages[0] = new BytecodeAstLanguage("Bytecode AST (Unoptimized)", false, steps[0]);
        String nextName = "Bytecode AST (Variable Splitting)";
        for (int i = 1; i < languages.length; ++i) {
            languages[i] = new BytecodeAstLanguage(nextName, true, steps[i - 1]);
            nextName = "Bytecode AST (After " + steps[i - 1].name() + ")";
        }
        return ArrayUtilities.asUnmodifiableList(languages);
    }
}
