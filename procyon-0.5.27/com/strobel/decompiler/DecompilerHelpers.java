package com.strobel.decompiler;

import com.strobel.core.*;
import com.strobel.decompiler.ast.*;
import com.strobel.assembler.ir.*;
import java.util.*;
import com.strobel.assembler.metadata.*;

public final class DecompilerHelpers
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$NameSyntax;
    
    public static void writeType(final ITextOutput writer, final TypeReference type) {
        writeType(writer, type, NameSyntax.SIGNATURE);
    }
    
    public static void writeGenericSignature(final ITextOutput writer, final TypeReference type) {
        formatGenericSignature(writer, type, new Stack<TypeReference>());
    }
    
    public static void writeType(final ITextOutput writer, final TypeReference type, final NameSyntax syntax) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(writer, "writer");
        VerifyArgument.notNull(syntax, "syntax");
        formatType(writer, type, syntax, type.isDefinition(), new Stack<TypeReference>());
    }
    
    public static void writeType(final ITextOutput writer, final TypeReference type, final NameSyntax syntax, final boolean isDefinition) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(writer, "writer");
        VerifyArgument.notNull(syntax, "syntax");
        formatType(writer, type, syntax, isDefinition, new Stack<TypeReference>());
    }
    
    public static void writeMethod(final ITextOutput writer, final MethodReference method) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(writer, "writer");
        final Stack<TypeReference> typeStack = new Stack<TypeReference>();
        formatType(writer, method.getDeclaringType(), NameSyntax.DESCRIPTOR, false, typeStack);
        writer.writeDelimiter(".");
        writer.writeReference(method.getName(), method);
        writer.writeDelimiter(":");
        formatMethodSignature(writer, method, typeStack);
    }
    
    public static void writeMethodSignature(final ITextOutput writer, final IMethodSignature signature) {
        VerifyArgument.notNull(signature, "signature");
        VerifyArgument.notNull(writer, "writer");
        final Stack<TypeReference> typeStack = new Stack<TypeReference>();
        formatMethodSignature(writer, signature, typeStack);
    }
    
    public static void writeField(final ITextOutput writer, final FieldReference field) {
        VerifyArgument.notNull(field, "field");
        VerifyArgument.notNull(writer, "writer");
        final Stack<TypeReference> typeStack = new Stack<TypeReference>();
        formatType(writer, field.getDeclaringType(), NameSyntax.DESCRIPTOR, false, typeStack);
        writer.writeDelimiter(".");
        writer.writeReference(field.getName(), field);
        writer.writeDelimiter(":");
        formatType(writer, field.getFieldType(), NameSyntax.SIGNATURE, false, typeStack);
    }
    
    public static void writeOperand(final ITextOutput writer, final Object operand) {
        writeOperand(writer, operand, false);
    }
    
    public static void writeOperand(final ITextOutput writer, final Object operand, final boolean isUnicodeSupported) {
        VerifyArgument.notNull(writer, "writer");
        VerifyArgument.notNull(operand, "operand");
        if (operand instanceof Instruction) {
            final Instruction targetInstruction = (Instruction)operand;
            writeOffsetReference(writer, targetInstruction);
            return;
        }
        if (operand instanceof Instruction[]) {
            final Instruction[] targetInstructions = (Instruction[])operand;
            writeLabelList(writer, targetInstructions);
            return;
        }
        if (operand instanceof SwitchInfo) {
            final SwitchInfo switchInfo = (SwitchInfo)operand;
            writer.write('[');
            writeOffsetReference(writer, switchInfo.getDefaultTarget());
            Instruction[] loc_1;
            for (int loc_0 = (loc_1 = switchInfo.getTargets()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final Instruction target = loc_1[loc_2];
                writer.write(", ");
                writeOffsetReference(writer, target);
            }
            writer.write(']');
            return;
        }
        if (operand instanceof VariableReference) {
            final VariableReference variable = (VariableReference)operand;
            if (variable.hasName()) {
                writer.writeReference(escapeIdentifier(variable.getName()), variable);
            }
            else {
                writer.writeReference("$" + String.valueOf(variable.getSlot()), variable);
            }
            return;
        }
        if (operand instanceof ParameterReference) {
            final ParameterReference parameter = (ParameterReference)operand;
            final String parameterName = parameter.getName();
            if (StringUtilities.isNullOrEmpty(parameterName)) {
                writer.writeReference(String.valueOf(parameter.getPosition()), parameter);
            }
            else {
                writer.writeReference(escapeIdentifier(parameterName), parameter);
            }
            return;
        }
        if (operand instanceof Variable) {
            final Variable variable2 = (Variable)operand;
            if (variable2.isParameter()) {
                writer.writeReference(variable2.getName(), variable2.getOriginalParameter());
            }
            else {
                writer.writeReference(variable2.getName(), variable2.getOriginalVariable());
            }
            return;
        }
        if (operand instanceof MethodReference) {
            writeMethod(writer, (MethodReference)operand);
            return;
        }
        if (operand instanceof TypeReference) {
            writeType(writer, (TypeReference)operand, NameSyntax.TYPE_NAME);
            writer.write('.');
            writer.writeKeyword("class");
            return;
        }
        if (operand instanceof FieldReference) {
            writeField(writer, (FieldReference)operand);
            return;
        }
        if (operand instanceof DynamicCallSite) {
            writeDynamicCallSite(writer, (DynamicCallSite)operand);
            return;
        }
        writePrimitiveValue(writer, operand);
    }
    
    public static void writeDynamicCallSite(final ITextOutput output, final DynamicCallSite operand) {
        output.writeReference(operand.getMethodName(), operand.getMethodType());
        output.writeDelimiter(":");
        writeMethodSignature(output, operand.getMethodType());
    }
    
    public static String offsetToString(final int offset) {
        return String.format("#%1$04d", offset);
    }
    
    public static void writeExceptionHandler(final ITextOutput output, final ExceptionHandler handler) {
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(handler, "handler");
        output.write("Try ");
        writeOffsetReference(output, handler.getTryBlock().getFirstInstruction());
        output.write(" - ");
        writeEndOffsetReference(output, handler.getTryBlock().getLastInstruction());
        output.write(' ');
        output.write(String.valueOf(handler.getHandlerType()));
        final TypeReference catchType = handler.getCatchType();
        if (catchType != null) {
            output.write(' ');
            writeType(output, catchType);
        }
        final InstructionBlock handlerBlock = handler.getHandlerBlock();
        output.write(' ');
        writeOffsetReference(output, handlerBlock.getFirstInstruction());
        if (handlerBlock.getLastInstruction() != null) {
            output.write(" - ");
            writeEndOffsetReference(output, handlerBlock.getLastInstruction());
        }
    }
    
    public static void writeInstruction(final ITextOutput writer, final Instruction instruction) {
        VerifyArgument.notNull(writer, "writer");
        VerifyArgument.notNull(instruction, "instruction");
        writer.writeDefinition(offsetToString(instruction.getOffset()), instruction);
        writer.write(": ");
        writer.writeReference(instruction.getOpCode().name(), instruction.getOpCode());
        if (instruction.hasOperand()) {
            writer.write(' ');
            writeOperandList(writer, instruction);
        }
    }
    
    public static void writeOffsetReference(final ITextOutput writer, final Instruction instruction) {
        VerifyArgument.notNull(writer, "writer");
        writer.writeLabel(offsetToString(instruction.getOffset()));
    }
    
    public static void writeEndOffsetReference(final ITextOutput writer, final Instruction instruction) {
        VerifyArgument.notNull(writer, "writer");
        writer.writeLabel(offsetToString(instruction.getEndOffset()));
    }
    
    public static String escapeIdentifier(final String name) {
        VerifyArgument.notNull(name, "name");
        StringBuilder sb = null;
        for (int i = 0, n = name.length(); i < n; ++i) {
            final char ch = name.charAt(i);
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(ch)) {
                    sb = new StringBuilder(name.length() * 2);
                    sb.append(String.format("\\u%1$04x", (int)ch));
                }
            }
            else if (Character.isJavaIdentifierPart(ch)) {
                if (sb != null) {
                    sb.append(ch);
                }
            }
            else {
                if (sb == null) {
                    sb = new StringBuilder(name.length() * 2);
                }
                sb.append(String.format("\\u%1$04x", (int)ch));
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return name;
    }
    
    public static void writeFrame(final ITextOutput writer, final Frame frame) {
        VerifyArgument.notNull(writer, "writer");
        VerifyArgument.notNull(frame, "frame");
        final FrameType frameType = frame.getFrameType();
        writer.writeLiteral(String.valueOf(frameType));
        final List<FrameValue> localValues = frame.getLocalValues();
        final List<FrameValue> stackValues = frame.getStackValues();
        if (!localValues.isEmpty()) {
            writer.writeLine();
            writer.indent();
            writer.write("Locals: ");
            writer.writeDelimiter("[");
            for (int i = 0; i < localValues.size(); ++i) {
                final FrameValue value = localValues.get(i);
                if (i != 0) {
                    writer.writeDelimiter(", ");
                }
                if (value.getType() == FrameValueType.Reference) {
                    writer.writeLiteral("Reference");
                    writer.writeDelimiter("(");
                    writeType(writer, (TypeReference)value.getParameter(), NameSyntax.SIGNATURE);
                    writer.writeDelimiter(")");
                }
                else {
                    writer.writeLiteral(String.valueOf(value.getType()));
                }
            }
            writer.writeDelimiter("]");
            writer.unindent();
        }
        if (!stackValues.isEmpty()) {
            writer.writeLine();
            writer.indent();
            writer.write("Stack: ");
            writer.writeDelimiter("[");
            for (int i = 0; i < stackValues.size(); ++i) {
                final FrameValue value = stackValues.get(i);
                if (i != 0) {
                    writer.writeDelimiter(", ");
                }
                if (value.getType() == FrameValueType.Reference) {
                    writer.writeLiteral("Reference");
                    writer.writeDelimiter("(");
                    writeType(writer, (TypeReference)value.getParameter(), NameSyntax.SIGNATURE);
                    writer.writeDelimiter(")");
                }
                else {
                    writer.writeLiteral(String.valueOf(value.getType()));
                }
            }
            writer.writeDelimiter("]");
            writer.unindent();
        }
    }
    
    private static void writeLabelList(final ITextOutput writer, final Instruction[] instructions) {
        writer.write('(');
        for (int i = 0; i < instructions.length; ++i) {
            if (i != 0) {
                writer.write(", ");
            }
            writeOffsetReference(writer, instructions[i]);
        }
        writer.write(')');
    }
    
    private static void writeOperandList(final ITextOutput writer, final Instruction instruction) {
        for (int i = 0, n = instruction.getOperandCount(); i < n; ++i) {
            if (i != 0) {
                writer.write(", ");
            }
            writeOperand(writer, instruction.getOperand(i));
        }
    }
    
    private static void formatMethodSignature(final ITextOutput writer, final IMethodSignature signature, final Stack<TypeReference> typeStack) {
        if (signature.isGenericDefinition()) {
            final List<GenericParameter> genericParameters = signature.getGenericParameters();
            final int count = genericParameters.size();
            if (count > 0) {
                writer.writeDelimiter("<");
                for (int i = 0; i < count; ++i) {
                    formatGenericSignature(writer, genericParameters.get(i), typeStack);
                }
                writer.writeDelimiter(">");
            }
        }
        final List<ParameterDefinition> parameters = signature.getParameters();
        writer.writeDelimiter("(");
        for (int j = 0, n = parameters.size(); j < n; ++j) {
            final ParameterDefinition p = parameters.get(j);
            if (!p.isSynthetic()) {
                formatType(writer, p.getParameterType(), NameSyntax.SIGNATURE, false, typeStack);
            }
        }
        writer.writeDelimiter(")");
        formatType(writer, signature.getReturnType(), NameSyntax.SIGNATURE, false, typeStack);
    }
    
    private static void formatType(final ITextOutput writer, final TypeReference type, final NameSyntax syntax, final boolean isDefinition, final Stack<TypeReference> stack) {
        if (type.isGenericParameter()) {
            switch ($SWITCH_TABLE$com$strobel$decompiler$NameSyntax()[syntax.ordinal()]) {
                case 1:
                case 2:
                case 3: {
                    writer.writeDelimiter("T");
                    writer.writeReference(type.getSimpleName(), type);
                    writer.writeDelimiter(";");
                }
                default: {
                    writer.writeReference(type.getName(), type);
                    if (isDefinition && type.hasExtendsBound() && !stack.contains(type.getExtendsBound()) && !BuiltinTypes.Object.equals(type.getExtendsBound())) {
                        writer.writeKeyword(" extends ");
                        stack.push(type);
                        try {
                            formatType(writer, type.getExtendsBound(), syntax, false, stack);
                        }
                        finally {
                            stack.pop();
                        }
                        stack.pop();
                    }
                }
            }
        }
        else if (type.isWildcardType()) {
            switch ($SWITCH_TABLE$com$strobel$decompiler$NameSyntax()[syntax.ordinal()]) {
                case 3: {
                    formatType(writer, type.getExtendsBound(), syntax, false, stack);
                }
                case 1:
                case 2: {
                    if (type.hasSuperBound()) {
                        writer.write('-');
                        formatType(writer, type.getSuperBound(), syntax, false, stack);
                    }
                    else if (type.hasExtendsBound()) {
                        writer.write('+');
                        formatType(writer, type.getExtendsBound(), syntax, false, stack);
                    }
                    else {
                        writer.write('*');
                    }
                }
                default: {
                    writer.write("?");
                    if (type.hasSuperBound()) {
                        writer.writeKeyword(" super ");
                        formatType(writer, type.getSuperBound(), syntax, false, stack);
                    }
                    else if (type.hasExtendsBound()) {
                        writer.writeKeyword(" extends ");
                        formatType(writer, type.getExtendsBound(), syntax, false, stack);
                    }
                }
            }
        }
        else {
            if (type instanceof CompoundTypeReference) {
                final CompoundTypeReference compoundType = (CompoundTypeReference)type;
                final TypeReference baseType = compoundType.getBaseType();
                final List<TypeReference> interfaces = compoundType.getInterfaces();
                switch ($SWITCH_TABLE$com$strobel$decompiler$NameSyntax()[syntax.ordinal()]) {
                    case 1: {
                        if (baseType != null) {
                            formatType(writer, baseType, syntax, false, stack);
                        }
                        for (final TypeReference interfaceType : interfaces) {
                            writer.writeDelimiter(":");
                            formatType(writer, interfaceType, syntax, false, stack);
                        }
                        break;
                    }
                    case 2:
                    case 3: {
                        TypeReference erasedType;
                        if (baseType != null) {
                            erasedType = baseType;
                        }
                        else if (!interfaces.isEmpty()) {
                            erasedType = interfaces.get(0);
                        }
                        else {
                            erasedType = BuiltinTypes.Object;
                        }
                        formatType(writer, erasedType, syntax, false, stack);
                        break;
                    }
                    case 4:
                    case 5: {
                        boolean first = true;
                        if (baseType != null) {
                            formatType(writer, baseType, syntax, false, stack);
                            first = false;
                        }
                        for (final TypeReference interfaceType2 : interfaces) {
                            if (!first) {
                                writer.writeDelimiter(" & ");
                            }
                            formatType(writer, interfaceType2, syntax, false, stack);
                            first = false;
                        }
                        break;
                    }
                }
                return;
            }
            if (type.isArray()) {
                switch ($SWITCH_TABLE$com$strobel$decompiler$NameSyntax()[syntax.ordinal()]) {
                    case 1:
                    case 2:
                    case 3: {
                        writer.writeDelimiter("[");
                        formatType(writer, type.getElementType(), syntax, false, stack);
                        break;
                    }
                    case 4:
                    case 5: {
                        formatType(writer, type.getElementType(), syntax, false, stack);
                        writer.writeDelimiter("[]");
                        break;
                    }
                }
                return;
            }
            stack.push(type);
            final TypeDefinition resolvedType = type.resolve();
            final TypeReference nameSource = (resolvedType != null) ? resolvedType : type;
            try {
                String name = null;
                switch ($SWITCH_TABLE$com$strobel$decompiler$NameSyntax()[syntax.ordinal()]) {
                    case 4: {
                        name = nameSource.getFullName();
                        break;
                    }
                    case 5: {
                        name = nameSource.getSimpleName();
                        break;
                    }
                    case 3: {
                        name = nameSource.getInternalName();
                        break;
                    }
                    default: {
                        if (nameSource.isPrimitive()) {
                            name = nameSource.getInternalName();
                            break;
                        }
                        writer.writeDelimiter("L");
                        name = nameSource.getInternalName();
                        break;
                    }
                }
                if (type.isPrimitive() && (syntax == NameSyntax.TYPE_NAME || syntax == NameSyntax.SHORT_TYPE_NAME)) {
                    writer.writeKeyword(name);
                }
                else if (isDefinition) {
                    writer.writeDefinition(name, type);
                }
                else {
                    writer.writeReference(name, type);
                }
                if (type.isGenericType() && syntax != NameSyntax.DESCRIPTOR && syntax != NameSyntax.ERASED_SIGNATURE) {
                    stack.push(type);
                    try {
                        List<? extends TypeReference> typeArguments;
                        if (type instanceof IGenericInstance) {
                            typeArguments = ((IGenericInstance)type).getTypeArguments();
                        }
                        else {
                            typeArguments = type.getGenericParameters();
                        }
                        final int count = typeArguments.size();
                        if (count > 0) {
                            writer.writeDelimiter("<");
                            for (int i = 0; i < count; ++i) {
                                if (syntax != NameSyntax.SIGNATURE && i != 0) {
                                    writer.writeDelimiter(", ");
                                }
                                final TypeReference typeArgument = (TypeReference)typeArguments.get(i);
                                formatType(writer, typeArgument, syntax, false, stack);
                            }
                            writer.writeDelimiter(">");
                        }
                    }
                    finally {
                        stack.pop();
                    }
                    stack.pop();
                }
                if (!type.isPrimitive() && (syntax == NameSyntax.SIGNATURE || syntax == NameSyntax.ERASED_SIGNATURE)) {
                    writer.writeDelimiter(";");
                }
            }
            finally {
                stack.pop();
            }
            stack.pop();
        }
    }
    
    private static void formatGenericSignature(final ITextOutput writer, final TypeReference type, final Stack<TypeReference> stack) {
        if (type.isGenericParameter()) {
            final TypeReference extendsBound = type.getExtendsBound();
            final TypeDefinition resolvedBound = extendsBound.resolve();
            writer.writeDefinition(type.getName(), type);
            if (resolvedBound != null && resolvedBound.isInterface()) {
                writer.writeDelimiter(":");
            }
            writer.writeDelimiter(":");
            formatType(writer, extendsBound, NameSyntax.SIGNATURE, false, stack);
            return;
        }
        if (type.isGenericType()) {
            List<? extends TypeReference> typeArguments;
            if (type instanceof IGenericInstance) {
                typeArguments = ((IGenericInstance)type).getTypeArguments();
            }
            else {
                typeArguments = type.getGenericParameters();
            }
            final int count = typeArguments.size();
            if (count > 0) {
                writer.writeDelimiter("<");
                for (int i = 0; i < count; ++i) {
                    formatGenericSignature(writer, (TypeReference)typeArguments.get(i), stack);
                }
                writer.writeDelimiter(">");
            }
        }
        final TypeDefinition definition = type.resolve();
        if (definition == null) {
            return;
        }
        final TypeReference baseType = definition.getBaseType();
        final List<TypeReference> interfaces = definition.getExplicitInterfaces();
        if (baseType == null) {
            formatType(writer, BuiltinTypes.Object, NameSyntax.SIGNATURE, false, stack);
        }
        else {
            formatType(writer, baseType, NameSyntax.SIGNATURE, false, stack);
        }
        for (final TypeReference interfaceType : interfaces) {
            formatType(writer, interfaceType, NameSyntax.SIGNATURE, false, stack);
        }
    }
    
    public static void writePrimitiveValue(final ITextOutput output, final Object value) {
        if (value == null) {
            output.writeKeyword("null");
            return;
        }
        if (value instanceof Boolean) {
            if (value) {
                output.writeKeyword("true");
            }
            else {
                output.writeKeyword("false");
            }
            return;
        }
        if (value instanceof String) {
            output.writeTextLiteral(StringUtilities.escape(value.toString(), true, true));
        }
        else if (value instanceof Character) {
            output.writeTextLiteral(StringUtilities.escape((char)value, true, true));
        }
        else if (value instanceof Float) {
            final float f = (float)value;
            if (Float.isInfinite(f) || Float.isNaN(f)) {
                output.writeReference("Float", MetadataSystem.instance().lookupType("java/lang/Float"));
                output.writeDelimiter(".");
                if (f == Float.POSITIVE_INFINITY) {
                    output.write("POSITIVE_INFINITY");
                }
                else if (f == Float.NEGATIVE_INFINITY) {
                    output.write("NEGATIVE_INFINITY");
                }
                else {
                    output.write("NaN");
                }
                return;
            }
            output.writeLiteral(String.valueOf(Float.toString(f)) + "f");
        }
        else if (value instanceof Double) {
            final double d = (double)value;
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                final TypeReference doubleType = MetadataSystem.instance().lookupType("java/lang/Double");
                output.writeReference("Double", doubleType);
                output.writeDelimiter(".");
                if (d == Double.POSITIVE_INFINITY) {
                    output.write("POSITIVE_INFINITY");
                }
                else if (d == Double.NEGATIVE_INFINITY) {
                    output.write("NEGATIVE_INFINITY");
                }
                else {
                    output.write("NaN");
                }
                return;
            }
            String number = Double.toString(d);
            if (number.indexOf(46) < 0 && number.indexOf(69) < 0) {
                number = String.valueOf(number) + "d";
            }
            output.writeLiteral(number);
        }
        else if (value instanceof Long) {
            output.writeLiteral(String.valueOf(String.valueOf(value)) + "L");
        }
        else {
            output.writeLiteral(String.valueOf(value));
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$NameSyntax() {
        final int[] loc_0 = DecompilerHelpers.$SWITCH_TABLE$com$strobel$decompiler$NameSyntax;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[NameSyntax.values().length];
        try {
            loc_1[NameSyntax.DESCRIPTOR.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[NameSyntax.ERASED_SIGNATURE.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[NameSyntax.SHORT_TYPE_NAME.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[NameSyntax.SIGNATURE.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[NameSyntax.TYPE_NAME.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_6) {}
        return DecompilerHelpers.$SWITCH_TABLE$com$strobel$decompiler$NameSyntax = loc_1;
    }
}
