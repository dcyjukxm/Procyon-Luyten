package com.strobel.assembler.ir.attributes;

import com.strobel.core.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.ir.*;
import com.strobel.util.*;
import com.strobel.assembler.metadata.annotations.*;

public class SourceAttribute
{
    private final String _name;
    private final int _length;
    
    public final String getName() {
        return this._name;
    }
    
    public final int getLength() {
        return this._length;
    }
    
    protected SourceAttribute(final String name, final int length) {
        super();
        this._name = name;
        this._length = length;
    }
    
    public static SourceAttribute create(final String name) {
        return new SourceAttribute(VerifyArgument.notNull(name, "name"), 0);
    }
    
    public static <T extends SourceAttribute> T find(final String name, final SourceAttribute... attributes) {
        VerifyArgument.notNull(name, "name");
        VerifyArgument.noNullElements(attributes, "attributes");
        for (final SourceAttribute attribute : attributes) {
            if (name.equals(attribute.getName())) {
                return (T)attribute;
            }
        }
        return null;
    }
    
    public static <T extends SourceAttribute> T find(final String name, final List<SourceAttribute> attributes) {
        VerifyArgument.notNull(name, "name");
        VerifyArgument.noNullElements(attributes, "attributes");
        for (final SourceAttribute attribute : attributes) {
            if (name.equals(attribute.getName())) {
                return (T)attribute;
            }
        }
        return null;
    }
    
    public static void readAttributes(final IMetadataResolver resolver, final IMetadataScope scope, final Buffer input, final SourceAttribute[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            attributes[i] = readAttribute(resolver, scope, input);
        }
    }
    
    public static SourceAttribute readAttribute(final IMetadataResolver resolver, final IMetadataScope scope, final Buffer buffer) {
        final int nameIndex = buffer.readUnsignedShort();
        final int length = buffer.readInt();
        final String name = scope.lookupConstant(nameIndex);
        if (length == 0) {
            return create(name);
        }
        Label_1074: {
            Label_0943: {
                Label_0893: {
                    final String loc_0;
                    switch (loc_0 = name) {
                        case "ConstantValue": {
                            final int token = buffer.readUnsignedShort();
                            final Object constantValue = scope.lookupConstant(token);
                            return new ConstantValueAttribute(constantValue);
                        }
                        case "Signature": {
                            final int token = buffer.readUnsignedShort();
                            final String signature = scope.lookupConstant(token);
                            return new SignatureAttribute(signature);
                        }
                        case "RuntimeVisibleParameterAnnotations": {
                            break Label_0943;
                        }
                        case "RuntimeInvisibleParameterAnnotations": {
                            break Label_0943;
                        }
                        case "RuntimeVisibleAnnotations": {
                            break Label_0893;
                        }
                        case "Code": {
                            final int maxStack = buffer.readUnsignedShort();
                            final int maxLocals = buffer.readUnsignedShort();
                            final int codeOffset = buffer.position();
                            final int codeLength = buffer.readInt();
                            final byte[] code = new byte[codeLength];
                            buffer.read(code, 0, codeLength);
                            final int exceptionTableLength = buffer.readUnsignedShort();
                            final ExceptionTableEntry[] exceptionTable = new ExceptionTableEntry[exceptionTableLength];
                            for (int k = 0; k < exceptionTableLength; ++k) {
                                final int startOffset = buffer.readUnsignedShort();
                                final int endOffset = buffer.readUnsignedShort();
                                final int handlerOffset = buffer.readUnsignedShort();
                                final int catchTypeToken = buffer.readUnsignedShort();
                                TypeReference catchType;
                                if (catchTypeToken == 0) {
                                    catchType = null;
                                }
                                else {
                                    catchType = resolver.lookupType(scope.lookupConstant(catchTypeToken));
                                }
                                exceptionTable[k] = new ExceptionTableEntry(startOffset, endOffset, handlerOffset, catchType);
                            }
                            final int attributeCount = buffer.readUnsignedShort();
                            final SourceAttribute[] attributes = new SourceAttribute[attributeCount];
                            readAttributes(resolver, scope, buffer, attributes);
                            return new CodeAttribute(length, maxStack, maxLocals, codeOffset, codeLength, buffer, exceptionTable, attributes);
                        }
                        case "LocalVariableTypeTable": {
                            break;
                        }
                        case "Exceptions": {
                            final int exceptionCount = buffer.readUnsignedShort();
                            final TypeReference[] exceptionTypes = new TypeReference[exceptionCount];
                            for (int i = 0; i < exceptionTypes.length; ++i) {
                                exceptionTypes[i] = scope.lookupType(buffer.readUnsignedShort());
                            }
                            return new ExceptionsAttribute(exceptionTypes);
                        }
                        case "SourceFile": {
                            final int token = buffer.readUnsignedShort();
                            final String sourceFile = scope.lookupConstant(token);
                            return new SourceFileAttribute(sourceFile);
                        }
                        case "AnnotationDefault": {
                            final AnnotationElement defaultValue = AnnotationReader.readElement(scope, buffer);
                            return new AnnotationDefaultAttribute(length, defaultValue);
                        }
                        case "EnclosingMethod": {
                            final int typeToken = buffer.readUnsignedShort();
                            final int methodToken = buffer.readUnsignedShort();
                            return new EnclosingMethodAttribute(scope.lookupType(typeToken), (methodToken > 0) ? scope.lookupMethod(typeToken, methodToken) : null);
                        }
                        case "LocalVariableTable": {
                            break;
                        }
                        case "LineNumberTable": {
                            final int entryCount = buffer.readUnsignedShort();
                            final LineNumberTableEntry[] entries = new LineNumberTableEntry[entryCount];
                            for (int i = 0; i < entries.length; ++i) {
                                entries[i] = new LineNumberTableEntry(buffer.readUnsignedShort(), buffer.readUnsignedShort());
                            }
                            return new LineNumberTableAttribute(entries);
                        }
                        case "RuntimeInvisibleAnnotations": {
                            break Label_0893;
                        }
                        case "InnerClasses": {
                            throw ContractUtils.unreachable();
                        }
                        default:
                            break Label_1074;
                    }
                    final int entryCount = buffer.readUnsignedShort();
                    final LocalVariableTableEntry[] entries2 = new LocalVariableTableEntry[entryCount];
                    for (int i = 0; i < entries2.length; ++i) {
                        final int scopeOffset = buffer.readUnsignedShort();
                        final int scopeLength = buffer.readUnsignedShort();
                        final String variableName = scope.lookupConstant(buffer.readUnsignedShort());
                        final String descriptor = scope.lookupConstant(buffer.readUnsignedShort());
                        final int variableIndex = buffer.readUnsignedShort();
                        entries2[i] = new LocalVariableTableEntry(variableIndex, variableName, resolver.lookupType(descriptor), scopeOffset, scopeLength);
                    }
                    return new LocalVariableTableAttribute(name, entries2);
                }
                final CustomAnnotation[] annotations = new CustomAnnotation[buffer.readUnsignedShort()];
                for (int j = 0; j < annotations.length; ++j) {
                    annotations[j] = AnnotationReader.read(scope, buffer);
                }
                return new AnnotationsAttribute(name, length, annotations);
            }
            final CustomAnnotation[][] annotations2 = new CustomAnnotation[buffer.readUnsignedShort()][];
            for (int j = 0; j < annotations2.length; ++j) {
                final CustomAnnotation[] parameterAnnotations = new CustomAnnotation[buffer.readUnsignedShort()];
                for (int l = 0; l < parameterAnnotations.length; ++l) {
                    parameterAnnotations[l] = AnnotationReader.read(scope, buffer);
                }
                annotations2[j] = parameterAnnotations;
            }
            return new ParameterAnnotationsAttribute(name, length, annotations2);
        }
        final int offset = buffer.position();
        final byte[] blob = new byte[length];
        buffer.read(blob, 0, blob.length);
        return new BlobAttribute(name, blob, offset);
    }
}
