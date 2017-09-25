package com.strobel.decompiler.languages;

import com.strobel.core.*;
import com.strobel.annotations.*;
import java.util.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.decompiler.*;
import com.strobel.assembler.ir.*;
import com.strobel.assembler.metadata.*;

public class BytecodeLanguage extends Language
{
    @Override
    public String getName() {
        return "Bytecode";
    }
    
    @Override
    public String getFileExtension() {
        return ".class";
    }
    
    @Override
    public TypeDecompilationResults decompileType(final TypeDefinition type, final ITextOutput output, final DecompilationOptions options) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(options, "options");
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
        output.writeLine();
        output.indent();
        try {
            this.writeTypeHeader(output, type);
            for (final SourceAttribute attribute : type.getSourceAttributes()) {
                this.writeTypeAttribute(output, type, attribute);
            }
            final ConstantPool constantPool = type.getConstantPool();
            if (constantPool != null) {
                constantPool.accept(new ConstantPoolPrinter(output, options.getSettings()));
            }
            for (final FieldDefinition field : type.getDeclaredFields()) {
                output.writeLine();
                this.decompileField(field, output, options);
            }
            for (final MethodDefinition method : type.getDeclaredMethods()) {
                output.writeLine();
                this.decompileMethod(method, output, options);
            }
        }
        finally {
            output.unindent();
        }
        output.unindent();
        if (!options.getSettings().getExcludeNestedTypes()) {
            for (final TypeDefinition innerType : type.getDeclaredTypes()) {
                output.writeLine();
                this.decompileType(innerType, output, options);
            }
        }
        return new TypeDecompilationResults(null);
    }
    
    private void writeTypeAttribute(final ITextOutput output, final TypeDefinition type, final SourceAttribute attribute) {
        if (attribute instanceof BlobAttribute) {
            return;
        }
        final String loc_0;
        switch (loc_0 = attribute.getName()) {
            case "Signature": {
                break;
            }
            case "Deprecated": {
                output.writeAttribute("Deprecated");
                output.writeLine();
                return;
            }
            case "SourceFile": {
                output.writeAttribute("SourceFile");
                output.write(": ");
                output.writeTextLiteral(((SourceFileAttribute)attribute).getSourceFile());
                output.writeLine();
                return;
            }
            case "EnclosingMethod": {
                final TypeReference enclosingType = ((EnclosingMethodAttribute)attribute).getEnclosingType();
                final MethodReference enclosingMethod = ((EnclosingMethodAttribute)attribute).getEnclosingMethod();
                if (enclosingType != null) {
                    output.writeAttribute("EnclosingType");
                    output.write(": ");
                    output.writeReference(enclosingType.getInternalName(), enclosingType);
                    output.writeLine();
                }
                if (enclosingMethod != null) {
                    final TypeReference declaringType = enclosingMethod.getDeclaringType();
                    output.writeAttribute("EnclosingMethod");
                    output.write(": ");
                    output.writeReference(declaringType.getInternalName(), declaringType);
                    output.writeDelimiter(".");
                    output.writeReference(enclosingMethod.getName(), enclosingMethod);
                    output.writeDelimiter(":");
                    DecompilerHelpers.writeMethodSignature(output, enclosingMethod);
                    output.writeLine();
                }
                return;
            }
            case "InnerClasses": {
                final InnerClassesAttribute innerClasses = (InnerClassesAttribute)attribute;
                final List<InnerClassEntry> entries = innerClasses.getEntries();
                output.writeAttribute("InnerClasses");
                output.writeLine(": ");
                output.indent();
                try {
                    for (final InnerClassEntry entry : entries) {
                        this.writeInnerClassEntry(output, type, entry);
                    }
                }
                finally {
                    output.unindent();
                }
                output.unindent();
                break;
            }
        }
        output.writeAttribute("Signature");
        output.write(": ");
        DecompilerHelpers.writeGenericSignature(output, type);
        output.writeLine();
    }
    
    private void writeInnerClassEntry(final ITextOutput output, final TypeDefinition type, final InnerClassEntry entry) {
        final String shortName = entry.getShortName();
        final String innerClassName = entry.getInnerClassName();
        final String outerClassName = entry.getOuterClassName();
        final EnumSet<Flags.Flag> flagsSet = Flags.asFlagSet(entry.getAccessFlags(), Flags.Kind.InnerClass);
        for (final Flags.Flag flag : flagsSet) {
            output.writeKeyword(flag.toString());
            output.write(' ');
        }
        final MetadataParser parser = new MetadataParser(type);
        if (this.tryWriteType(output, parser, shortName, innerClassName)) {
            output.writeDelimiter(" = ");
        }
        if (!this.tryWriteType(output, parser, innerClassName, innerClassName)) {
            output.writeError("?");
        }
        if (!StringUtilities.isNullOrEmpty(outerClassName)) {
            output.writeDelimiter(" of ");
            if (!this.tryWriteType(output, parser, outerClassName, outerClassName)) {
                output.writeError("?");
            }
        }
        output.writeLine();
    }
    
    private boolean tryWriteType(@NotNull final ITextOutput output, @NotNull final MetadataParser parser, final String text, final String descriptor) {
        if (StringUtilities.isNullOrEmpty(text)) {
            return false;
        }
        if (StringUtilities.isNullOrEmpty(descriptor)) {
            output.writeError(text);
            return true;
        }
        try {
            final TypeReference type = parser.parseTypeDescriptor(descriptor);
            output.writeReference(text, type);
            return true;
        }
        catch (Throwable loc_0) {
            try {
                output.writeReference(text, new DummyTypeReference(descriptor));
                return true;
            }
            catch (Throwable loc_1) {
                output.writeError(text);
                return true;
            }
        }
    }
    
    private void writeTypeHeader(final ITextOutput output, final TypeDefinition type) {
        output.writeAttribute("Minor version");
        output.write(": ");
        output.writeLiteral(type.getCompilerMinorVersion());
        output.writeLine();
        output.writeAttribute("Major version");
        output.write(": ");
        output.writeLiteral(type.getCompilerMajorVersion());
        output.writeLine();
        final long flags = type.getFlags();
        final List<String> flagStrings = new ArrayList<String>();
        final EnumSet<Flags.Flag> flagsSet = Flags.asFlagSet(flags, type.isInnerClass() ? Flags.Kind.InnerClass : Flags.Kind.Class);
        for (final Flags.Flag flag : flagsSet) {
            flagStrings.add(flag.name());
        }
        if (!flagStrings.isEmpty()) {
            output.writeAttribute("Flags");
            output.write(": ");
            for (int i = 0; i < flagStrings.size(); ++i) {
                if (i != 0) {
                    output.write(", ");
                }
                output.writeLiteral(flagStrings.get(i));
            }
            output.writeLine();
        }
    }
    
    @Override
    public void decompileField(final FieldDefinition field, final ITextOutput output, final DecompilationOptions options) {
        final long flags = field.getFlags();
        final EnumSet<Flags.Flag> flagSet = Flags.asFlagSet(flags & 0x40DFL & 0xFFFFFFFFFFFFBFFFL, Flags.Kind.Field);
        final List<String> flagStrings = new ArrayList<String>();
        for (final Flags.Flag flag : flagSet) {
            flagStrings.add(flag.toString());
        }
        if (flagSet.size() > 0) {
            for (int i = 0; i < flagStrings.size(); ++i) {
                output.writeKeyword(flagStrings.get(i));
                output.write(' ');
            }
        }
        DecompilerHelpers.writeType(output, field.getFieldType(), NameSyntax.TYPE_NAME);
        output.write(' ');
        output.writeDefinition(field.getName(), field);
        output.writeDelimiter(";");
        output.writeLine();
        flagStrings.clear();
        for (final Flags.Flag flag : Flags.asFlagSet(flags & 0xFFFFFFFFFFFFF0DFL, Flags.Kind.Field)) {
            flagStrings.add(flag.name());
        }
        if (flagStrings.isEmpty()) {
            return;
        }
        output.indent();
        try {
            output.writeAttribute("Flags");
            output.write(": ");
            for (int i = 0; i < flagStrings.size(); ++i) {
                if (i != 0) {
                    output.write(", ");
                }
                output.writeLiteral(flagStrings.get(i));
            }
            output.writeLine();
            for (final SourceAttribute attribute : field.getSourceAttributes()) {
                this.writeFieldAttribute(output, field, attribute);
            }
        }
        finally {
            output.unindent();
        }
        output.unindent();
    }
    
    private void writeFieldAttribute(final ITextOutput output, final FieldDefinition field, final SourceAttribute attribute) {
        final String loc_0;
        switch (loc_0 = attribute.getName()) {
            case "ConstantValue": {
                final Object constantValue = ((ConstantValueAttribute)attribute).getValue();
                output.writeAttribute("ConstantValue");
                output.write(": ");
                if (constantValue != null) {
                    final String typeDescriptor = constantValue.getClass().getName().replace('.', '/');
                    final TypeReference valueType = field.getDeclaringType().getResolver().lookupType(typeDescriptor);
                    if (valueType != null) {
                        DecompilerHelpers.writeType(output, MetadataHelper.getUnderlyingPrimitiveTypeOrSelf(valueType), NameSyntax.TYPE_NAME);
                        output.write(' ');
                    }
                }
                DecompilerHelpers.writeOperand(output, constantValue);
                output.writeLine();
                break;
            }
            case "Signature": {
                output.writeAttribute("Signature");
                output.write(": ");
                DecompilerHelpers.writeType(output, field.getFieldType(), NameSyntax.SIGNATURE, false);
                output.writeLine();
                break;
            }
            default:
                break;
        }
    }
    
    @Override
    public void decompileMethod(final MethodDefinition method, final ITextOutput output, final DecompilationOptions options) {
        this.writeMethodHeader(output, method);
        this.writeMethodBody(output, method, options);
        for (final SourceAttribute attribute : method.getSourceAttributes()) {
            this.writeMethodAttribute(output, method, attribute);
        }
        this.writeMethodEnd(output, method, options);
    }
    
    private void writeMethodHeader(final ITextOutput output, final MethodDefinition method) {
        final String name = method.getName();
        final long flags = Flags.fromStandardFlags(method.getFlags(), Flags.Kind.Method);
        final List<String> flagStrings = new ArrayList<String>();
        if ("<clinit>".equals(name)) {
            output.writeKeyword("static");
            output.write(" {}");
        }
        else {
            final EnumSet<Flags.Flag> flagSet = Flags.asFlagSet(flags & 0xD3FL, Flags.Kind.Method);
            for (final Flags.Flag flag : flagSet) {
                flagStrings.add(flag.toString());
            }
            if (flagSet.size() > 0) {
                for (int i = 0; i < flagStrings.size(); ++i) {
                    output.writeKeyword(flagStrings.get(i));
                    output.write(' ');
                }
            }
            final List<GenericParameter> genericParameters = method.getGenericParameters();
            if (!genericParameters.isEmpty()) {
                output.writeDelimiter("<");
                for (int j = 0; j < genericParameters.size(); ++j) {
                    if (j != 0) {
                        output.writeDelimiter(", ");
                    }
                    DecompilerHelpers.writeType(output, genericParameters.get(j), NameSyntax.TYPE_NAME, true);
                }
                output.writeDelimiter(">");
                output.write(' ');
            }
            DecompilerHelpers.writeType(output, method.getReturnType(), NameSyntax.TYPE_NAME, false);
            output.write(' ');
            output.writeDefinition(name, method);
            output.writeDelimiter("(");
            final List<ParameterDefinition> parameters = method.getParameters();
            for (int k = 0; k < parameters.size(); ++k) {
                if (k != 0) {
                    output.writeDelimiter(", ");
                }
                final ParameterDefinition parameter = parameters.get(k);
                if (Flags.testAny(flags, 17179869312L) && k == parameters.size() - 1) {
                    DecompilerHelpers.writeType(output, parameter.getParameterType().getElementType(), NameSyntax.TYPE_NAME, false);
                    output.writeDelimiter("...");
                }
                else {
                    DecompilerHelpers.writeType(output, parameter.getParameterType(), NameSyntax.TYPE_NAME, false);
                }
                output.write(' ');
                final String parameterName = parameter.getName();
                if (StringUtilities.isNullOrEmpty(parameterName)) {
                    output.write("p%d", k);
                }
                else {
                    output.write(parameterName);
                }
            }
            output.writeDelimiter(")");
            final List<TypeReference> thrownTypes = method.getThrownTypes();
            if (!thrownTypes.isEmpty()) {
                output.writeKeyword(" throws ");
                for (int l = 0; l < thrownTypes.size(); ++l) {
                    if (l != 0) {
                        output.writeDelimiter(", ");
                    }
                    DecompilerHelpers.writeType(output, thrownTypes.get(l), NameSyntax.TYPE_NAME, false);
                }
            }
        }
        output.writeDelimiter(";");
        output.writeLine();
        flagStrings.clear();
        for (final Flags.Flag flag2 : Flags.asFlagSet(flags & 0xFFFFFFFFFFFFFD3FL, Flags.Kind.Method)) {
            flagStrings.add(flag2.name());
        }
        if (flagStrings.isEmpty()) {
            return;
        }
        output.indent();
        try {
            output.writeAttribute("Flags");
            output.write(": ");
            for (int m = 0; m < flagStrings.size(); ++m) {
                if (m != 0) {
                    output.write(", ");
                }
                output.writeLiteral(flagStrings.get(m));
            }
            output.writeLine();
        }
        finally {
            output.unindent();
        }
        output.unindent();
    }
    
    private void writeMethodAttribute(final ITextOutput output, final MethodDefinition method, final SourceAttribute attribute) {
        final String loc_0;
        switch (loc_0 = attribute.getName()) {
            case "MethodParameters": {
                final MethodParametersAttribute parameters = (MethodParametersAttribute)attribute;
                final List<MethodParameterEntry> entries = parameters.getEntries();
                int longestName = "Name".length();
                int longestFlags = "Flags".length();
                for (final MethodParameterEntry entry : entries) {
                    final String name = entry.getName();
                    final String flags = Flags.toString(entry.getFlags());
                    if (name != null && name.length() > longestName) {
                        longestName = name.length();
                    }
                    if (flags != null && flags.length() > longestFlags) {
                        longestFlags = flags.length();
                    }
                }
                output.indent();
                try {
                    output.writeAttribute(attribute.getName());
                    output.writeLine(":");
                    output.indent();
                    try {
                        output.write("%1$-" + longestName + "s  %2$-" + longestFlags + "s  ", "Name", "Flags");
                        output.writeLine();
                        output.write("%1$-" + longestName + "s  %2$-" + longestFlags + "s", StringUtilities.repeat('-', longestName), StringUtilities.repeat('-', longestFlags));
                        output.writeLine();
                        for (int i = 0; i < entries.size(); ++i) {
                            final MethodParameterEntry entry2 = entries.get(i);
                            final List<ParameterDefinition> parameterDefinitions = method.getParameters();
                            output.writeReference(String.format("%1$-" + longestName + "s  ", entry2.getName()), (i < parameterDefinitions.size()) ? parameterDefinitions.get(i) : null);
                            final EnumSet<Flags.Flag> flags2 = Flags.asFlagSet(entry2.getFlags());
                            boolean firstFlag = true;
                            for (final Flags.Flag flag : flags2) {
                                if (!firstFlag) {
                                    output.writeDelimiter(", ");
                                }
                                output.writeLiteral(flag.name());
                                firstFlag = false;
                            }
                            output.writeLine();
                        }
                    }
                    finally {
                        output.unindent();
                    }
                }
                finally {
                    output.unindent();
                }
                output.unindent();
                return;
            }
            case "Signature": {
                output.indent();
                try {
                    final String signature = ((SignatureAttribute)attribute).getSignature();
                    output.writeAttribute(attribute.getName());
                    output.writeLine(":");
                    output.indent();
                    final PlainTextOutput temp = new PlainTextOutput();
                    DecompilerHelpers.writeMethodSignature(temp, method);
                    DecompilerHelpers.writeMethodSignature(output, method);
                    if (!StringUtilities.equals(temp.toString(), signature)) {
                        output.write(' ');
                        output.writeDelimiter("[");
                        output.write("from metadata: ");
                        output.writeError(signature);
                        output.writeDelimiter("]");
                        output.writeLine();
                    }
                    output.writeLine();
                }
                finally {
                    output.unindent();
                }
                output.unindent();
                return;
            }
            case "LocalVariableTypeTable": {
                break;
            }
            case "Exceptions": {
                final ExceptionsAttribute exceptionsAttribute = (ExceptionsAttribute)attribute;
                final List<TypeReference> exceptionTypes = exceptionsAttribute.getExceptionTypes();
                if (!exceptionTypes.isEmpty()) {
                    output.indent();
                    try {
                        output.writeAttribute("Exceptions");
                        output.writeLine(":");
                        output.indent();
                        try {
                            for (final TypeReference exceptionType : exceptionTypes) {
                                output.writeKeyword("throws");
                                output.write(' ');
                                DecompilerHelpers.writeType(output, exceptionType, NameSyntax.TYPE_NAME);
                                output.writeLine();
                            }
                        }
                        finally {
                            output.unindent();
                        }
                    }
                    finally {
                        output.unindent();
                    }
                    output.unindent();
                }
                return;
            }
            case "LocalVariableTable": {
                break;
            }
        }
        final LocalVariableTableAttribute localVariables = (LocalVariableTableAttribute)attribute;
        final List<LocalVariableTableEntry> entries2 = localVariables.getEntries();
        int longestName = "Name".length();
        int longestSignature = "Signature".length();
        for (final LocalVariableTableEntry entry3 : entries2) {
            final String name = entry3.getName();
            final TypeReference type = entry3.getType();
            if (type != null) {
                String signature2;
                if (attribute.getName().equals("LocalVariableTypeTable")) {
                    signature2 = type.getSignature();
                }
                else {
                    signature2 = type.getErasedSignature();
                }
                if (signature2.length() > longestSignature) {
                    longestSignature = signature2.length();
                }
            }
            if (name != null && name.length() > longestName) {
                longestName = name.length();
            }
        }
        output.indent();
        try {
            output.writeAttribute(attribute.getName());
            output.writeLine(":");
            output.indent();
            try {
                output.write("Start  Length  Slot  %1$-" + longestName + "s  Signature", "Name");
                output.writeLine();
                output.write("-----  ------  ----  %1$-" + longestName + "s  %2$-" + longestSignature + "s", StringUtilities.repeat('-', longestName), StringUtilities.repeat('-', longestSignature));
                output.writeLine();
                final MethodBody body = method.getBody();
                for (final LocalVariableTableEntry entry4 : entries2) {
                    final VariableDefinitionCollection variables = (body != null) ? body.getVariables() : null;
                    NameSyntax nameSyntax;
                    if (attribute.getName().equals("LocalVariableTypeTable")) {
                        nameSyntax = NameSyntax.SIGNATURE;
                    }
                    else {
                        nameSyntax = NameSyntax.ERASED_SIGNATURE;
                    }
                    output.writeLiteral(String.format("%1$-5d", entry4.getScopeOffset()));
                    output.write("  ");
                    output.writeLiteral(String.format("%1$-6d", entry4.getScopeLength()));
                    output.write("  ");
                    output.writeLiteral(String.format("%1$-4d", entry4.getIndex()));
                    output.writeReference(String.format("  %1$-" + longestName + "s  ", entry4.getName()), (variables != null) ? variables.tryFind(entry4.getIndex(), entry4.getScopeOffset()) : null);
                    DecompilerHelpers.writeType(output, entry4.getType(), nameSyntax);
                    output.writeLine();
                }
            }
            finally {
                output.unindent();
            }
        }
        finally {
            output.unindent();
        }
        output.unindent();
    }
    
    private void writeMethodBody(final ITextOutput output, final MethodDefinition method, final DecompilationOptions options) {
        final MethodBody body = method.getBody();
        if (body == null) {
            return;
        }
        output.indent();
        try {
            output.writeAttribute("Code");
            output.writeLine(":");
            output.indent();
            try {
                output.write("stack=");
                output.writeLiteral(body.getMaxStackSize());
                output.write(", locals=");
                output.writeLiteral(body.getMaxLocals());
                output.write(", arguments=");
                output.writeLiteral(method.getParameters().size());
                output.writeLine();
            }
            finally {
                output.unindent();
            }
            output.unindent();
            final InstructionCollection instructions = body.getInstructions();
            if (!instructions.isEmpty()) {
                int[] lineNumbers;
                if (options.getSettings().getIncludeLineNumbersInBytecode()) {
                    final LineNumberTableAttribute lineNumbersAttribute = SourceAttribute.find("LineNumberTable", method.getSourceAttributes());
                    if (lineNumbersAttribute != null) {
                        lineNumbers = new int[body.getCodeSize()];
                        Arrays.fill(lineNumbers, -1);
                        for (final LineNumberTableEntry entry : lineNumbersAttribute.getEntries()) {
                            if (entry.getOffset() >= lineNumbers.length) {
                                lineNumbers = Arrays.copyOf(lineNumbers, entry.getOffset() + 1);
                            }
                            lineNumbers[entry.getOffset()] = entry.getLineNumber();
                        }
                    }
                    else {
                        lineNumbers = null;
                    }
                }
                else {
                    lineNumbers = null;
                }
                final InstructionPrinter printer = new InstructionPrinter(output, method, options.getSettings(), lineNumbers, null);
                for (final Instruction instruction : instructions) {
                    printer.visit(instruction);
                }
            }
        }
        finally {
            output.unindent();
        }
        output.unindent();
    }
    
    private void writeMethodEnd(final ITextOutput output, final MethodDefinition method, final DecompilationOptions options) {
        final MethodBody body = method.getBody();
        if (body == null) {
            return;
        }
        final List<ExceptionHandler> handlers = body.getExceptionHandlers();
        final List<StackMapFrame> stackMapFrames = body.getStackMapFrames();
        if (!handlers.isEmpty()) {
            output.indent();
            try {
                int longestType = "Type".length();
                for (final ExceptionHandler handler : handlers) {
                    final TypeReference catchType = handler.getCatchType();
                    if (catchType != null) {
                        final String signature = catchType.getSignature();
                        if (signature.length() <= longestType) {
                            continue;
                        }
                        longestType = signature.length();
                    }
                }
                output.writeAttribute("Exceptions");
                output.writeLine(":");
                output.indent();
                try {
                    output.write("Try           Handler");
                    output.writeLine();
                    output.write("Start  End    Start  End    %1$-" + longestType + "s", "Type");
                    output.writeLine();
                    output.write("-----  -----  -----  -----  %1$-" + longestType + "s", StringUtilities.repeat('-', longestType));
                    output.writeLine();
                    for (final ExceptionHandler handler : handlers) {
                        TypeReference catchType2 = handler.getCatchType();
                        boolean isFinally;
                        if (catchType2 != null) {
                            isFinally = false;
                        }
                        else {
                            catchType2 = getResolver(body).lookupType("java/lang/Throwable");
                            isFinally = true;
                        }
                        output.writeLiteral(String.format("%1$-5d", handler.getTryBlock().getFirstInstruction().getOffset()));
                        output.write("  ");
                        output.writeLiteral(String.format("%1$-5d", handler.getTryBlock().getLastInstruction().getEndOffset()));
                        output.write("  ");
                        output.writeLiteral(String.format("%1$-5d", handler.getHandlerBlock().getFirstInstruction().getOffset()));
                        output.write("  ");
                        output.writeLiteral(String.format("%1$-5d", handler.getHandlerBlock().getLastInstruction().getEndOffset()));
                        output.write("  ");
                        if (isFinally) {
                            output.writeReference("Any", catchType2);
                        }
                        else {
                            DecompilerHelpers.writeType(output, catchType2, NameSyntax.SIGNATURE);
                        }
                        output.writeLine();
                    }
                }
                finally {
                    output.unindent();
                }
            }
            finally {
                output.unindent();
            }
            output.unindent();
        }
        if (!stackMapFrames.isEmpty()) {
            output.indent();
            try {
                output.writeAttribute("Stack Map Frames");
                output.writeLine(":");
                output.indent();
                try {
                    for (final StackMapFrame frame : stackMapFrames) {
                        DecompilerHelpers.writeOffsetReference(output, frame.getStartInstruction());
                        output.write(' ');
                        DecompilerHelpers.writeFrame(output, frame.getFrame());
                        output.writeLine();
                    }
                }
                finally {
                    output.unindent();
                }
            }
            finally {
                output.unindent();
            }
            output.unindent();
        }
    }
    
    private static IMetadataResolver getResolver(final MethodBody body) {
        final MethodReference method = body.getMethod();
        if (method != null) {
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod != null) {
                final TypeDefinition declaringType = resolvedMethod.getDeclaringType();
                if (declaringType != null) {
                    return declaringType.getResolver();
                }
            }
        }
        return MetadataSystem.instance();
    }
    
    private static final class InstructionPrinter implements InstructionVisitor
    {
        private static final int MAX_OPCODE_LENGTH;
        private static final String[] OPCODE_NAMES;
        private static final String LINE_NUMBER_CODE = "linenumber";
        private final DecompilerSettings _settings;
        private final ITextOutput _output;
        private final MethodBody _body;
        private final int[] _lineNumbers;
        private int _currentOffset;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
        
        static {
            int maxLength = "linenumber".length();
            final OpCode[] values = OpCode.values();
            final String[] names = new String[values.length];
            for (int i = 0; i < values.length; ++i) {
                final OpCode op = values[i];
                final int length = op.name().length();
                if (length > maxLength) {
                    maxLength = length;
                }
                names[i] = op.name().toLowerCase();
            }
            MAX_OPCODE_LENGTH = maxLength;
            OPCODE_NAMES = names;
        }
        
        private InstructionPrinter(final ITextOutput output, final MethodDefinition method, final DecompilerSettings settings, final int[] lineNumbers) {
            super();
            this._currentOffset = -1;
            this._settings = settings;
            this._output = VerifyArgument.notNull(output, "output");
            this._body = VerifyArgument.notNull(method, "method").getBody();
            this._lineNumbers = lineNumbers;
        }
        
        private void printOpCode(final OpCode opCode) {
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[opCode.ordinal()]) {
                case 171:
                case 172: {
                    this._output.writeReference(InstructionPrinter.OPCODE_NAMES[opCode.ordinal()], opCode);
                    break;
                }
                default: {
                    this._output.writeReference(String.format("%1$-" + InstructionPrinter.MAX_OPCODE_LENGTH + "s", InstructionPrinter.OPCODE_NAMES[opCode.ordinal()]), opCode);
                    break;
                }
            }
        }
        
        @Override
        public void visit(final Instruction instruction) {
            VerifyArgument.notNull(instruction, "instruction");
            if (this._lineNumbers != null) {
                final int lineNumber = this._lineNumbers[instruction.getOffset()];
                if (lineNumber >= 0) {
                    this._output.write("          ");
                    this._output.write("%1$-" + InstructionPrinter.MAX_OPCODE_LENGTH + "s", "linenumber");
                    this._output.write(' ');
                    this._output.writeLiteral(lineNumber);
                    this._output.writeLine();
                }
            }
            this._currentOffset = instruction.getOffset();
            try {
                this._output.writeLabel(String.format("%1$8d", instruction.getOffset()));
                this._output.write(": ");
                instruction.accept(this);
            }
            catch (Throwable t) {
                this.printOpCode(instruction.getOpCode());
                boolean foundError = false;
                for (int i = 0; i < instruction.getOperandCount(); ++i) {
                    final Object operand = instruction.getOperand(i);
                    if (operand instanceof ErrorOperand) {
                        this._output.write(String.valueOf(operand));
                        foundError = true;
                        break;
                    }
                }
                if (!foundError) {
                    this._output.write("!!! ERROR");
                }
                this._output.writeLine();
                return;
            }
            finally {
                this._currentOffset = -1;
            }
            this._currentOffset = -1;
        }
        
        @Override
        public void visit(final OpCode op) {
            this.printOpCode(op);
            final int slot = OpCodeHelpers.getLoadStoreMacroArgumentIndex(op);
            if (slot >= 0) {
                final VariableDefinitionCollection variables = this._body.getVariables();
                if (slot < variables.size()) {
                    final VariableDefinition variable = this.findVariable(op, slot, this._currentOffset);
                    if (variable != null && variable.hasName() && variable.isFromMetadata()) {
                        this._output.writeComment(" /* %s */", StringUtilities.escape(variable.getName(), false, this._settings.isUnicodeOutputEnabled()));
                    }
                }
            }
            this._output.writeLine();
        }
        
        private VariableDefinition findVariable(final OpCode op, final int slot, final int offset) {
            VariableDefinition variable = this._body.getVariables().tryFind(slot, offset);
            if (variable == null && op.isStore()) {
                variable = this._body.getVariables().tryFind(slot, offset + op.getSize() + op.getOperandType().getBaseSize());
            }
            return variable;
        }
        
        @Override
        public void visitConstant(final OpCode op, final TypeReference value) {
            this.printOpCode(op);
            this._output.write(' ');
            DecompilerHelpers.writeType(this._output, value, NameSyntax.ERASED_SIGNATURE);
            this._output.write(".class");
            this._output.writeLine();
        }
        
        @Override
        public void visitConstant(final OpCode op, final int value) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeLiteral(value);
            this._output.writeLine();
        }
        
        @Override
        public void visitConstant(final OpCode op, final long value) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeLiteral(value);
            this._output.writeLine();
        }
        
        @Override
        public void visitConstant(final OpCode op, final float value) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeLiteral(value);
            this._output.writeLine();
        }
        
        @Override
        public void visitConstant(final OpCode op, final double value) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeLiteral(value);
            this._output.writeLine();
        }
        
        @Override
        public void visitConstant(final OpCode op, final String value) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeTextLiteral(StringUtilities.escape(value, true, this._settings.isUnicodeOutputEnabled()));
            this._output.writeLine();
        }
        
        @Override
        public void visitBranch(final OpCode op, final Instruction target) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeLabel(String.valueOf(target.getOffset()));
            this._output.writeLine();
        }
        
        @Override
        public void visitVariable(final OpCode op, final VariableReference variable) {
            this.printOpCode(op);
            this._output.write(' ');
            final VariableDefinition definition = this.findVariable(op, variable.getSlot(), this._currentOffset);
            if (definition != null && definition.hasName() && definition.isFromMetadata()) {
                this._output.writeReference(variable.getName(), variable);
            }
            else {
                this._output.writeLiteral(variable.getSlot());
            }
            this._output.writeLine();
        }
        
        @Override
        public void visitVariable(final OpCode op, final VariableReference variable, final int operand) {
            this.printOpCode(op);
            this._output.write(' ');
            VariableDefinition definition;
            if (variable instanceof VariableDefinition) {
                definition = (VariableDefinition)variable;
            }
            else {
                definition = this.findVariable(op, variable.getSlot(), this._currentOffset);
            }
            if (definition != null && definition.hasName() && definition.isFromMetadata()) {
                this._output.writeReference(variable.getName(), variable);
            }
            else {
                this._output.writeLiteral(variable.getSlot());
            }
            this._output.write(", ");
            this._output.writeLiteral(String.valueOf(operand));
            this._output.writeLine();
        }
        
        @Override
        public void visitType(final OpCode op, final TypeReference type) {
            this.printOpCode(op);
            this._output.write(' ');
            DecompilerHelpers.writeType(this._output, type, NameSyntax.SIGNATURE);
            this._output.writeLine();
        }
        
        @Override
        public void visitMethod(final OpCode op, final MethodReference method) {
            this.printOpCode(op);
            this._output.write(' ');
            DecompilerHelpers.writeMethod(this._output, method);
            this._output.writeLine();
        }
        
        @Override
        public void visitDynamicCallSite(final OpCode op, final DynamicCallSite callSite) {
            this.printOpCode(op);
            this._output.write(' ');
            this._output.writeReference(callSite.getMethodName(), callSite.getMethodType());
            this._output.writeDelimiter(":");
            DecompilerHelpers.writeMethodSignature(this._output, callSite.getMethodType());
            this._output.writeLine();
        }
        
        @Override
        public void visitField(final OpCode op, final FieldReference field) {
            this.printOpCode(op);
            this._output.write(' ');
            DecompilerHelpers.writeField(this._output, field);
            this._output.writeLine();
        }
        
        @Override
        public void visitLabel(final Label label) {
        }
        
        @Override
        public void visitSwitch(final OpCode op, final SwitchInfo switchInfo) {
            this.printOpCode(op);
            this._output.write(" {");
            this._output.writeLine();
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[op.ordinal()]) {
                case 171: {
                    final Instruction[] targets = switchInfo.getTargets();
                    int caseValue = switchInfo.getLowValue();
                    Instruction[] loc_1;
                    for (int loc_0 = (loc_1 = targets).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                        final Instruction target = loc_1[loc_2];
                        this._output.write("            ");
                        this._output.writeLiteral(String.format("%1$7d", switchInfo.getLowValue() + caseValue++));
                        this._output.write(": ");
                        this._output.writeLabel(String.valueOf(target.getOffset()));
                        this._output.writeLine();
                    }
                    this._output.write("            ");
                    this._output.writeKeyword("default");
                    this._output.write(": ");
                    this._output.writeLabel(String.valueOf(switchInfo.getDefaultTarget().getOffset()));
                    this._output.writeLine();
                    break;
                }
                case 172: {
                    final int[] keys = switchInfo.getKeys();
                    final Instruction[] targets2 = switchInfo.getTargets();
                    for (int i = 0; i < keys.length; ++i) {
                        final int key = keys[i];
                        final Instruction target2 = targets2[i];
                        this._output.write("            ");
                        this._output.writeLiteral(String.format("%1$7d", key));
                        this._output.write(": ");
                        this._output.writeLabel(String.valueOf(target2.getOffset()));
                        this._output.writeLine();
                    }
                    this._output.write("            ");
                    this._output.writeKeyword("default");
                    this._output.write(": ");
                    this._output.writeLabel(String.valueOf(switchInfo.getDefaultTarget().getOffset()));
                    this._output.writeLine();
                    break;
                }
            }
            this._output.write("          }");
            this._output.writeLine();
        }
        
        @Override
        public void visitEnd() {
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode() {
            final int[] loc_0 = InstructionPrinter.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[OpCode.values().length];
            try {
                loc_1[OpCode.AALOAD.ordinal()] = 51;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[OpCode.AASTORE.ordinal()] = 84;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[OpCode.ACONST_NULL.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[OpCode.ALOAD.ordinal()] = 26;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[OpCode.ALOAD_0.ordinal()] = 43;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[OpCode.ALOAD_1.ordinal()] = 44;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[OpCode.ALOAD_2.ordinal()] = 45;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[OpCode.ALOAD_3.ordinal()] = 46;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[OpCode.ALOAD_W.ordinal()] = 207;
            }
            catch (NoSuchFieldError loc_10) {}
            try {
                loc_1[OpCode.ANEWARRAY.ordinal()] = 190;
            }
            catch (NoSuchFieldError loc_11) {}
            try {
                loc_1[OpCode.ARETURN.ordinal()] = 177;
            }
            catch (NoSuchFieldError loc_12) {}
            try {
                loc_1[OpCode.ARRAYLENGTH.ordinal()] = 191;
            }
            catch (NoSuchFieldError loc_13) {}
            try {
                loc_1[OpCode.ASTORE.ordinal()] = 59;
            }
            catch (NoSuchFieldError loc_14) {}
            try {
                loc_1[OpCode.ASTORE_0.ordinal()] = 76;
            }
            catch (NoSuchFieldError loc_15) {}
            try {
                loc_1[OpCode.ASTORE_1.ordinal()] = 77;
            }
            catch (NoSuchFieldError loc_16) {}
            try {
                loc_1[OpCode.ASTORE_2.ordinal()] = 78;
            }
            catch (NoSuchFieldError loc_17) {}
            try {
                loc_1[OpCode.ASTORE_3.ordinal()] = 79;
            }
            catch (NoSuchFieldError loc_18) {}
            try {
                loc_1[OpCode.ASTORE_W.ordinal()] = 212;
            }
            catch (NoSuchFieldError loc_19) {}
            try {
                loc_1[OpCode.ATHROW.ordinal()] = 192;
            }
            catch (NoSuchFieldError loc_20) {}
            try {
                loc_1[OpCode.BALOAD.ordinal()] = 52;
            }
            catch (NoSuchFieldError loc_21) {}
            try {
                loc_1[OpCode.BASTORE.ordinal()] = 85;
            }
            catch (NoSuchFieldError loc_22) {}
            try {
                loc_1[OpCode.BIPUSH.ordinal()] = 17;
            }
            catch (NoSuchFieldError loc_23) {}
            try {
                loc_1[OpCode.BREAKPOINT.ordinal()] = 202;
            }
            catch (NoSuchFieldError loc_24) {}
            try {
                loc_1[OpCode.CALOAD.ordinal()] = 53;
            }
            catch (NoSuchFieldError loc_25) {}
            try {
                loc_1[OpCode.CASTORE.ordinal()] = 86;
            }
            catch (NoSuchFieldError loc_26) {}
            try {
                loc_1[OpCode.CHECKCAST.ordinal()] = 193;
            }
            catch (NoSuchFieldError loc_27) {}
            try {
                loc_1[OpCode.D2F.ordinal()] = 145;
            }
            catch (NoSuchFieldError loc_28) {}
            try {
                loc_1[OpCode.D2I.ordinal()] = 143;
            }
            catch (NoSuchFieldError loc_29) {}
            try {
                loc_1[OpCode.D2L.ordinal()] = 144;
            }
            catch (NoSuchFieldError loc_30) {}
            try {
                loc_1[OpCode.DADD.ordinal()] = 100;
            }
            catch (NoSuchFieldError loc_31) {}
            try {
                loc_1[OpCode.DALOAD.ordinal()] = 50;
            }
            catch (NoSuchFieldError loc_32) {}
            try {
                loc_1[OpCode.DASTORE.ordinal()] = 83;
            }
            catch (NoSuchFieldError loc_33) {}
            try {
                loc_1[OpCode.DCMPG.ordinal()] = 153;
            }
            catch (NoSuchFieldError loc_34) {}
            try {
                loc_1[OpCode.DCMPL.ordinal()] = 152;
            }
            catch (NoSuchFieldError loc_35) {}
            try {
                loc_1[OpCode.DCONST_0.ordinal()] = 15;
            }
            catch (NoSuchFieldError loc_36) {}
            try {
                loc_1[OpCode.DCONST_1.ordinal()] = 16;
            }
            catch (NoSuchFieldError loc_37) {}
            try {
                loc_1[OpCode.DDIV.ordinal()] = 112;
            }
            catch (NoSuchFieldError loc_38) {}
            try {
                loc_1[OpCode.DLOAD.ordinal()] = 25;
            }
            catch (NoSuchFieldError loc_39) {}
            try {
                loc_1[OpCode.DLOAD_0.ordinal()] = 39;
            }
            catch (NoSuchFieldError loc_40) {}
            try {
                loc_1[OpCode.DLOAD_1.ordinal()] = 40;
            }
            catch (NoSuchFieldError loc_41) {}
            try {
                loc_1[OpCode.DLOAD_2.ordinal()] = 41;
            }
            catch (NoSuchFieldError loc_42) {}
            try {
                loc_1[OpCode.DLOAD_3.ordinal()] = 42;
            }
            catch (NoSuchFieldError loc_43) {}
            try {
                loc_1[OpCode.DLOAD_W.ordinal()] = 206;
            }
            catch (NoSuchFieldError loc_44) {}
            try {
                loc_1[OpCode.DMUL.ordinal()] = 108;
            }
            catch (NoSuchFieldError loc_45) {}
            try {
                loc_1[OpCode.DNEG.ordinal()] = 120;
            }
            catch (NoSuchFieldError loc_46) {}
            try {
                loc_1[OpCode.DREM.ordinal()] = 116;
            }
            catch (NoSuchFieldError loc_47) {}
            try {
                loc_1[OpCode.DRETURN.ordinal()] = 176;
            }
            catch (NoSuchFieldError loc_48) {}
            try {
                loc_1[OpCode.DSTORE.ordinal()] = 58;
            }
            catch (NoSuchFieldError loc_49) {}
            try {
                loc_1[OpCode.DSTORE_0.ordinal()] = 72;
            }
            catch (NoSuchFieldError loc_50) {}
            try {
                loc_1[OpCode.DSTORE_1.ordinal()] = 73;
            }
            catch (NoSuchFieldError loc_51) {}
            try {
                loc_1[OpCode.DSTORE_2.ordinal()] = 74;
            }
            catch (NoSuchFieldError loc_52) {}
            try {
                loc_1[OpCode.DSTORE_3.ordinal()] = 75;
            }
            catch (NoSuchFieldError loc_53) {}
            try {
                loc_1[OpCode.DSTORE_W.ordinal()] = 211;
            }
            catch (NoSuchFieldError loc_54) {}
            try {
                loc_1[OpCode.DSUB.ordinal()] = 104;
            }
            catch (NoSuchFieldError loc_55) {}
            try {
                loc_1[OpCode.DUP.ordinal()] = 90;
            }
            catch (NoSuchFieldError loc_56) {}
            try {
                loc_1[OpCode.DUP2.ordinal()] = 93;
            }
            catch (NoSuchFieldError loc_57) {}
            try {
                loc_1[OpCode.DUP2_X1.ordinal()] = 94;
            }
            catch (NoSuchFieldError loc_58) {}
            try {
                loc_1[OpCode.DUP2_X2.ordinal()] = 95;
            }
            catch (NoSuchFieldError loc_59) {}
            try {
                loc_1[OpCode.DUP_X1.ordinal()] = 91;
            }
            catch (NoSuchFieldError loc_60) {}
            try {
                loc_1[OpCode.DUP_X2.ordinal()] = 92;
            }
            catch (NoSuchFieldError loc_61) {}
            try {
                loc_1[OpCode.ENDFINALLY.ordinal()] = 216;
            }
            catch (NoSuchFieldError loc_62) {}
            try {
                loc_1[OpCode.F2D.ordinal()] = 142;
            }
            catch (NoSuchFieldError loc_63) {}
            try {
                loc_1[OpCode.F2I.ordinal()] = 140;
            }
            catch (NoSuchFieldError loc_64) {}
            try {
                loc_1[OpCode.F2L.ordinal()] = 141;
            }
            catch (NoSuchFieldError loc_65) {}
            try {
                loc_1[OpCode.FADD.ordinal()] = 99;
            }
            catch (NoSuchFieldError loc_66) {}
            try {
                loc_1[OpCode.FALOAD.ordinal()] = 49;
            }
            catch (NoSuchFieldError loc_67) {}
            try {
                loc_1[OpCode.FASTORE.ordinal()] = 82;
            }
            catch (NoSuchFieldError loc_68) {}
            try {
                loc_1[OpCode.FCMPG.ordinal()] = 151;
            }
            catch (NoSuchFieldError loc_69) {}
            try {
                loc_1[OpCode.FCMPL.ordinal()] = 150;
            }
            catch (NoSuchFieldError loc_70) {}
            try {
                loc_1[OpCode.FCONST_0.ordinal()] = 12;
            }
            catch (NoSuchFieldError loc_71) {}
            try {
                loc_1[OpCode.FCONST_1.ordinal()] = 13;
            }
            catch (NoSuchFieldError loc_72) {}
            try {
                loc_1[OpCode.FCONST_2.ordinal()] = 14;
            }
            catch (NoSuchFieldError loc_73) {}
            try {
                loc_1[OpCode.FDIV.ordinal()] = 111;
            }
            catch (NoSuchFieldError loc_74) {}
            try {
                loc_1[OpCode.FLOAD.ordinal()] = 24;
            }
            catch (NoSuchFieldError loc_75) {}
            try {
                loc_1[OpCode.FLOAD_0.ordinal()] = 35;
            }
            catch (NoSuchFieldError loc_76) {}
            try {
                loc_1[OpCode.FLOAD_1.ordinal()] = 36;
            }
            catch (NoSuchFieldError loc_77) {}
            try {
                loc_1[OpCode.FLOAD_2.ordinal()] = 37;
            }
            catch (NoSuchFieldError loc_78) {}
            try {
                loc_1[OpCode.FLOAD_3.ordinal()] = 38;
            }
            catch (NoSuchFieldError loc_79) {}
            try {
                loc_1[OpCode.FLOAD_W.ordinal()] = 205;
            }
            catch (NoSuchFieldError loc_80) {}
            try {
                loc_1[OpCode.FMUL.ordinal()] = 107;
            }
            catch (NoSuchFieldError loc_81) {}
            try {
                loc_1[OpCode.FNEG.ordinal()] = 119;
            }
            catch (NoSuchFieldError loc_82) {}
            try {
                loc_1[OpCode.FREM.ordinal()] = 115;
            }
            catch (NoSuchFieldError loc_83) {}
            try {
                loc_1[OpCode.FRETURN.ordinal()] = 175;
            }
            catch (NoSuchFieldError loc_84) {}
            try {
                loc_1[OpCode.FSTORE.ordinal()] = 57;
            }
            catch (NoSuchFieldError loc_85) {}
            try {
                loc_1[OpCode.FSTORE_0.ordinal()] = 68;
            }
            catch (NoSuchFieldError loc_86) {}
            try {
                loc_1[OpCode.FSTORE_1.ordinal()] = 69;
            }
            catch (NoSuchFieldError loc_87) {}
            try {
                loc_1[OpCode.FSTORE_2.ordinal()] = 70;
            }
            catch (NoSuchFieldError loc_88) {}
            try {
                loc_1[OpCode.FSTORE_3.ordinal()] = 71;
            }
            catch (NoSuchFieldError loc_89) {}
            try {
                loc_1[OpCode.FSTORE_W.ordinal()] = 210;
            }
            catch (NoSuchFieldError loc_90) {}
            try {
                loc_1[OpCode.FSUB.ordinal()] = 103;
            }
            catch (NoSuchFieldError loc_91) {}
            try {
                loc_1[OpCode.GETFIELD.ordinal()] = 181;
            }
            catch (NoSuchFieldError loc_92) {}
            try {
                loc_1[OpCode.GETSTATIC.ordinal()] = 179;
            }
            catch (NoSuchFieldError loc_93) {}
            try {
                loc_1[OpCode.GOTO.ordinal()] = 168;
            }
            catch (NoSuchFieldError loc_94) {}
            try {
                loc_1[OpCode.GOTO_W.ordinal()] = 200;
            }
            catch (NoSuchFieldError loc_95) {}
            try {
                loc_1[OpCode.I2B.ordinal()] = 146;
            }
            catch (NoSuchFieldError loc_96) {}
            try {
                loc_1[OpCode.I2C.ordinal()] = 147;
            }
            catch (NoSuchFieldError loc_97) {}
            try {
                loc_1[OpCode.I2D.ordinal()] = 136;
            }
            catch (NoSuchFieldError loc_98) {}
            try {
                loc_1[OpCode.I2F.ordinal()] = 135;
            }
            catch (NoSuchFieldError loc_99) {}
            try {
                loc_1[OpCode.I2L.ordinal()] = 134;
            }
            catch (NoSuchFieldError loc_100) {}
            try {
                loc_1[OpCode.I2S.ordinal()] = 148;
            }
            catch (NoSuchFieldError loc_101) {}
            try {
                loc_1[OpCode.IADD.ordinal()] = 97;
            }
            catch (NoSuchFieldError loc_102) {}
            try {
                loc_1[OpCode.IALOAD.ordinal()] = 47;
            }
            catch (NoSuchFieldError loc_103) {}
            try {
                loc_1[OpCode.IAND.ordinal()] = 127;
            }
            catch (NoSuchFieldError loc_104) {}
            try {
                loc_1[OpCode.IASTORE.ordinal()] = 80;
            }
            catch (NoSuchFieldError loc_105) {}
            try {
                loc_1[OpCode.ICONST_0.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_106) {}
            try {
                loc_1[OpCode.ICONST_1.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_107) {}
            try {
                loc_1[OpCode.ICONST_2.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_108) {}
            try {
                loc_1[OpCode.ICONST_3.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_109) {}
            try {
                loc_1[OpCode.ICONST_4.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_110) {}
            try {
                loc_1[OpCode.ICONST_5.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_111) {}
            try {
                loc_1[OpCode.ICONST_M1.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_112) {}
            try {
                loc_1[OpCode.IDIV.ordinal()] = 109;
            }
            catch (NoSuchFieldError loc_113) {}
            try {
                loc_1[OpCode.IFEQ.ordinal()] = 154;
            }
            catch (NoSuchFieldError loc_114) {}
            try {
                loc_1[OpCode.IFGE.ordinal()] = 157;
            }
            catch (NoSuchFieldError loc_115) {}
            try {
                loc_1[OpCode.IFGT.ordinal()] = 158;
            }
            catch (NoSuchFieldError loc_116) {}
            try {
                loc_1[OpCode.IFLE.ordinal()] = 159;
            }
            catch (NoSuchFieldError loc_117) {}
            try {
                loc_1[OpCode.IFLT.ordinal()] = 156;
            }
            catch (NoSuchFieldError loc_118) {}
            try {
                loc_1[OpCode.IFNE.ordinal()] = 155;
            }
            catch (NoSuchFieldError loc_119) {}
            try {
                loc_1[OpCode.IFNONNULL.ordinal()] = 199;
            }
            catch (NoSuchFieldError loc_120) {}
            try {
                loc_1[OpCode.IFNULL.ordinal()] = 198;
            }
            catch (NoSuchFieldError loc_121) {}
            try {
                loc_1[OpCode.IF_ACMPEQ.ordinal()] = 166;
            }
            catch (NoSuchFieldError loc_122) {}
            try {
                loc_1[OpCode.IF_ACMPNE.ordinal()] = 167;
            }
            catch (NoSuchFieldError loc_123) {}
            try {
                loc_1[OpCode.IF_ICMPEQ.ordinal()] = 160;
            }
            catch (NoSuchFieldError loc_124) {}
            try {
                loc_1[OpCode.IF_ICMPGE.ordinal()] = 163;
            }
            catch (NoSuchFieldError loc_125) {}
            try {
                loc_1[OpCode.IF_ICMPGT.ordinal()] = 164;
            }
            catch (NoSuchFieldError loc_126) {}
            try {
                loc_1[OpCode.IF_ICMPLE.ordinal()] = 165;
            }
            catch (NoSuchFieldError loc_127) {}
            try {
                loc_1[OpCode.IF_ICMPLT.ordinal()] = 162;
            }
            catch (NoSuchFieldError loc_128) {}
            try {
                loc_1[OpCode.IF_ICMPNE.ordinal()] = 161;
            }
            catch (NoSuchFieldError loc_129) {}
            try {
                loc_1[OpCode.IINC.ordinal()] = 133;
            }
            catch (NoSuchFieldError loc_130) {}
            try {
                loc_1[OpCode.IINC_W.ordinal()] = 213;
            }
            catch (NoSuchFieldError loc_131) {}
            try {
                loc_1[OpCode.ILOAD.ordinal()] = 22;
            }
            catch (NoSuchFieldError loc_132) {}
            try {
                loc_1[OpCode.ILOAD_0.ordinal()] = 27;
            }
            catch (NoSuchFieldError loc_133) {}
            try {
                loc_1[OpCode.ILOAD_1.ordinal()] = 28;
            }
            catch (NoSuchFieldError loc_134) {}
            try {
                loc_1[OpCode.ILOAD_2.ordinal()] = 29;
            }
            catch (NoSuchFieldError loc_135) {}
            try {
                loc_1[OpCode.ILOAD_3.ordinal()] = 30;
            }
            catch (NoSuchFieldError loc_136) {}
            try {
                loc_1[OpCode.ILOAD_W.ordinal()] = 203;
            }
            catch (NoSuchFieldError loc_137) {}
            try {
                loc_1[OpCode.IMUL.ordinal()] = 105;
            }
            catch (NoSuchFieldError loc_138) {}
            try {
                loc_1[OpCode.INEG.ordinal()] = 117;
            }
            catch (NoSuchFieldError loc_139) {}
            try {
                loc_1[OpCode.INSTANCEOF.ordinal()] = 194;
            }
            catch (NoSuchFieldError loc_140) {}
            try {
                loc_1[OpCode.INVOKEDYNAMIC.ordinal()] = 187;
            }
            catch (NoSuchFieldError loc_141) {}
            try {
                loc_1[OpCode.INVOKEINTERFACE.ordinal()] = 186;
            }
            catch (NoSuchFieldError loc_142) {}
            try {
                loc_1[OpCode.INVOKESPECIAL.ordinal()] = 184;
            }
            catch (NoSuchFieldError loc_143) {}
            try {
                loc_1[OpCode.INVOKESTATIC.ordinal()] = 185;
            }
            catch (NoSuchFieldError loc_144) {}
            try {
                loc_1[OpCode.INVOKEVIRTUAL.ordinal()] = 183;
            }
            catch (NoSuchFieldError loc_145) {}
            try {
                loc_1[OpCode.IOR.ordinal()] = 129;
            }
            catch (NoSuchFieldError loc_146) {}
            try {
                loc_1[OpCode.IREM.ordinal()] = 113;
            }
            catch (NoSuchFieldError loc_147) {}
            try {
                loc_1[OpCode.IRETURN.ordinal()] = 173;
            }
            catch (NoSuchFieldError loc_148) {}
            try {
                loc_1[OpCode.ISHL.ordinal()] = 121;
            }
            catch (NoSuchFieldError loc_149) {}
            try {
                loc_1[OpCode.ISHR.ordinal()] = 123;
            }
            catch (NoSuchFieldError loc_150) {}
            try {
                loc_1[OpCode.ISTORE.ordinal()] = 55;
            }
            catch (NoSuchFieldError loc_151) {}
            try {
                loc_1[OpCode.ISTORE_0.ordinal()] = 60;
            }
            catch (NoSuchFieldError loc_152) {}
            try {
                loc_1[OpCode.ISTORE_1.ordinal()] = 61;
            }
            catch (NoSuchFieldError loc_153) {}
            try {
                loc_1[OpCode.ISTORE_2.ordinal()] = 62;
            }
            catch (NoSuchFieldError loc_154) {}
            try {
                loc_1[OpCode.ISTORE_3.ordinal()] = 63;
            }
            catch (NoSuchFieldError loc_155) {}
            try {
                loc_1[OpCode.ISTORE_W.ordinal()] = 208;
            }
            catch (NoSuchFieldError loc_156) {}
            try {
                loc_1[OpCode.ISUB.ordinal()] = 101;
            }
            catch (NoSuchFieldError loc_157) {}
            try {
                loc_1[OpCode.IUSHR.ordinal()] = 125;
            }
            catch (NoSuchFieldError loc_158) {}
            try {
                loc_1[OpCode.IXOR.ordinal()] = 131;
            }
            catch (NoSuchFieldError loc_159) {}
            try {
                loc_1[OpCode.JSR.ordinal()] = 169;
            }
            catch (NoSuchFieldError loc_160) {}
            try {
                loc_1[OpCode.JSR_W.ordinal()] = 201;
            }
            catch (NoSuchFieldError loc_161) {}
            try {
                loc_1[OpCode.L2D.ordinal()] = 139;
            }
            catch (NoSuchFieldError loc_162) {}
            try {
                loc_1[OpCode.L2F.ordinal()] = 138;
            }
            catch (NoSuchFieldError loc_163) {}
            try {
                loc_1[OpCode.L2I.ordinal()] = 137;
            }
            catch (NoSuchFieldError loc_164) {}
            try {
                loc_1[OpCode.LADD.ordinal()] = 98;
            }
            catch (NoSuchFieldError loc_165) {}
            try {
                loc_1[OpCode.LALOAD.ordinal()] = 48;
            }
            catch (NoSuchFieldError loc_166) {}
            try {
                loc_1[OpCode.LAND.ordinal()] = 128;
            }
            catch (NoSuchFieldError loc_167) {}
            try {
                loc_1[OpCode.LASTORE.ordinal()] = 81;
            }
            catch (NoSuchFieldError loc_168) {}
            try {
                loc_1[OpCode.LCMP.ordinal()] = 149;
            }
            catch (NoSuchFieldError loc_169) {}
            try {
                loc_1[OpCode.LCONST_0.ordinal()] = 10;
            }
            catch (NoSuchFieldError loc_170) {}
            try {
                loc_1[OpCode.LCONST_1.ordinal()] = 11;
            }
            catch (NoSuchFieldError loc_171) {}
            try {
                loc_1[OpCode.LDC.ordinal()] = 19;
            }
            catch (NoSuchFieldError loc_172) {}
            try {
                loc_1[OpCode.LDC2_W.ordinal()] = 21;
            }
            catch (NoSuchFieldError loc_173) {}
            try {
                loc_1[OpCode.LDC_W.ordinal()] = 20;
            }
            catch (NoSuchFieldError loc_174) {}
            try {
                loc_1[OpCode.LDIV.ordinal()] = 110;
            }
            catch (NoSuchFieldError loc_175) {}
            try {
                loc_1[OpCode.LEAVE.ordinal()] = 215;
            }
            catch (NoSuchFieldError loc_176) {}
            try {
                loc_1[OpCode.LLOAD.ordinal()] = 23;
            }
            catch (NoSuchFieldError loc_177) {}
            try {
                loc_1[OpCode.LLOAD_0.ordinal()] = 31;
            }
            catch (NoSuchFieldError loc_178) {}
            try {
                loc_1[OpCode.LLOAD_1.ordinal()] = 32;
            }
            catch (NoSuchFieldError loc_179) {}
            try {
                loc_1[OpCode.LLOAD_2.ordinal()] = 33;
            }
            catch (NoSuchFieldError loc_180) {}
            try {
                loc_1[OpCode.LLOAD_3.ordinal()] = 34;
            }
            catch (NoSuchFieldError loc_181) {}
            try {
                loc_1[OpCode.LLOAD_W.ordinal()] = 204;
            }
            catch (NoSuchFieldError loc_182) {}
            try {
                loc_1[OpCode.LMUL.ordinal()] = 106;
            }
            catch (NoSuchFieldError loc_183) {}
            try {
                loc_1[OpCode.LNEG.ordinal()] = 118;
            }
            catch (NoSuchFieldError loc_184) {}
            try {
                loc_1[OpCode.LOOKUPSWITCH.ordinal()] = 172;
            }
            catch (NoSuchFieldError loc_185) {}
            try {
                loc_1[OpCode.LOR.ordinal()] = 130;
            }
            catch (NoSuchFieldError loc_186) {}
            try {
                loc_1[OpCode.LREM.ordinal()] = 114;
            }
            catch (NoSuchFieldError loc_187) {}
            try {
                loc_1[OpCode.LRETURN.ordinal()] = 174;
            }
            catch (NoSuchFieldError loc_188) {}
            try {
                loc_1[OpCode.LSHL.ordinal()] = 122;
            }
            catch (NoSuchFieldError loc_189) {}
            try {
                loc_1[OpCode.LSHR.ordinal()] = 124;
            }
            catch (NoSuchFieldError loc_190) {}
            try {
                loc_1[OpCode.LSTORE.ordinal()] = 56;
            }
            catch (NoSuchFieldError loc_191) {}
            try {
                loc_1[OpCode.LSTORE_0.ordinal()] = 64;
            }
            catch (NoSuchFieldError loc_192) {}
            try {
                loc_1[OpCode.LSTORE_1.ordinal()] = 65;
            }
            catch (NoSuchFieldError loc_193) {}
            try {
                loc_1[OpCode.LSTORE_2.ordinal()] = 66;
            }
            catch (NoSuchFieldError loc_194) {}
            try {
                loc_1[OpCode.LSTORE_3.ordinal()] = 67;
            }
            catch (NoSuchFieldError loc_195) {}
            try {
                loc_1[OpCode.LSTORE_W.ordinal()] = 209;
            }
            catch (NoSuchFieldError loc_196) {}
            try {
                loc_1[OpCode.LSUB.ordinal()] = 102;
            }
            catch (NoSuchFieldError loc_197) {}
            try {
                loc_1[OpCode.LUSHR.ordinal()] = 126;
            }
            catch (NoSuchFieldError loc_198) {}
            try {
                loc_1[OpCode.LXOR.ordinal()] = 132;
            }
            catch (NoSuchFieldError loc_199) {}
            try {
                loc_1[OpCode.MONITORENTER.ordinal()] = 195;
            }
            catch (NoSuchFieldError loc_200) {}
            try {
                loc_1[OpCode.MONITOREXIT.ordinal()] = 196;
            }
            catch (NoSuchFieldError loc_201) {}
            try {
                loc_1[OpCode.MULTIANEWARRAY.ordinal()] = 197;
            }
            catch (NoSuchFieldError loc_202) {}
            try {
                loc_1[OpCode.NEW.ordinal()] = 188;
            }
            catch (NoSuchFieldError loc_203) {}
            try {
                loc_1[OpCode.NEWARRAY.ordinal()] = 189;
            }
            catch (NoSuchFieldError loc_204) {}
            try {
                loc_1[OpCode.NOP.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_205) {}
            try {
                loc_1[OpCode.POP.ordinal()] = 88;
            }
            catch (NoSuchFieldError loc_206) {}
            try {
                loc_1[OpCode.POP2.ordinal()] = 89;
            }
            catch (NoSuchFieldError loc_207) {}
            try {
                loc_1[OpCode.PUTFIELD.ordinal()] = 182;
            }
            catch (NoSuchFieldError loc_208) {}
            try {
                loc_1[OpCode.PUTSTATIC.ordinal()] = 180;
            }
            catch (NoSuchFieldError loc_209) {}
            try {
                loc_1[OpCode.RET.ordinal()] = 170;
            }
            catch (NoSuchFieldError loc_210) {}
            try {
                loc_1[OpCode.RETURN.ordinal()] = 178;
            }
            catch (NoSuchFieldError loc_211) {}
            try {
                loc_1[OpCode.RET_W.ordinal()] = 214;
            }
            catch (NoSuchFieldError loc_212) {}
            try {
                loc_1[OpCode.SALOAD.ordinal()] = 54;
            }
            catch (NoSuchFieldError loc_213) {}
            try {
                loc_1[OpCode.SASTORE.ordinal()] = 87;
            }
            catch (NoSuchFieldError loc_214) {}
            try {
                loc_1[OpCode.SIPUSH.ordinal()] = 18;
            }
            catch (NoSuchFieldError loc_215) {}
            try {
                loc_1[OpCode.SWAP.ordinal()] = 96;
            }
            catch (NoSuchFieldError loc_216) {}
            try {
                loc_1[OpCode.TABLESWITCH.ordinal()] = 171;
            }
            catch (NoSuchFieldError loc_217) {}
            return InstructionPrinter.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode = loc_1;
        }
    }
    
    private static final class DummyTypeReference extends TypeReference
    {
        private final String _descriptor;
        private final String _fullName;
        private final String _simpleName;
        
        public DummyTypeReference(final String descriptor) {
            super();
            this._descriptor = VerifyArgument.notNull(descriptor, "descriptor");
            this._fullName = descriptor.replace('/', '.');
            final int delimiterIndex = this._fullName.lastIndexOf(46);
            if (delimiterIndex < 0 || delimiterIndex == this._fullName.length() - 1) {
                this._simpleName = this._fullName;
            }
            else {
                this._simpleName = this._fullName.substring(delimiterIndex + 1);
            }
        }
        
        @Override
        public final String getSimpleName() {
            return this._simpleName;
        }
        
        @Override
        public final String getFullName() {
            return this._fullName;
        }
        
        @Override
        public final String getInternalName() {
            return this._descriptor;
        }
        
        @Override
        public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
            return visitor.visitClassType(this, parameter);
        }
    }
}
