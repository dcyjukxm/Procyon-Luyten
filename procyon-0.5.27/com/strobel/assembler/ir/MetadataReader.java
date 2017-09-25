package com.strobel.assembler.ir;

import com.strobel.assembler.ir.attributes.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.core.*;

public abstract class MetadataReader
{
    protected abstract IMetadataScope getScope();
    
    protected abstract MetadataParser getParser();
    
    public void readAttributes(final Buffer input, final SourceAttribute[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            attributes[i] = this.readAttribute(input);
        }
    }
    
    public SourceAttribute readAttribute(final Buffer buffer) {
        final int nameIndex = buffer.readUnsignedShort();
        final int length = buffer.readInt();
        final IMetadataScope scope = this.getScope();
        final String name = scope.lookupConstant(nameIndex);
        return this.readAttributeCore(name, buffer, -1, length);
    }
    
    protected SourceAttribute readAttributeCore(final String name, final Buffer buffer, final int originalOffset, final int length) {
        final IMetadataScope scope = this.getScope();
        if (length == 0) {
            return SourceAttribute.create(name);
        }
        Label_1540: {
            Label_0963: {
                Label_0913: {
                    switch (name) {
                        case "ConstantValue": {
                            final int token = buffer.readUnsignedShort();
                            final Object constantValue = scope.lookupConstant(token);
                            return new ConstantValueAttribute(constantValue);
                        }
                        case "MethodParameters": {
                            final int methodParameterCount = buffer.readUnsignedByte();
                            final int computedCount = (length - 1) / 4;
                            final MethodParameterEntry[] entries = new MethodParameterEntry[methodParameterCount];
                            for (int i = 0; i < entries.length; ++i) {
                                int nameIndex;
                                int flags;
                                if (i < computedCount) {
                                    nameIndex = buffer.readUnsignedShort();
                                    flags = buffer.readUnsignedShort();
                                }
                                else {
                                    nameIndex = 0;
                                    flags = 0;
                                }
                                entries[i] = new MethodParameterEntry((nameIndex != 0) ? this.getScope().lookupConstant(nameIndex) : null, flags);
                            }
                            return new MethodParametersAttribute(ArrayUtilities.asUnmodifiableList(entries));
                        }
                        case "Signature": {
                            final int token = buffer.readUnsignedShort();
                            final String signature = scope.lookupConstant(token);
                            return new SignatureAttribute(signature);
                        }
                        case "RuntimeVisibleParameterAnnotations": {
                            break Label_0963;
                        }
                        case "RuntimeInvisibleParameterAnnotations": {
                            break Label_0963;
                        }
                        case "RuntimeVisibleAnnotations": {
                            break Label_0913;
                        }
                        case "Code": {
                            final int maxStack = buffer.readUnsignedShort();
                            final int maxLocals = buffer.readUnsignedShort();
                            final int codeLength = buffer.readInt();
                            final int relativeOffset = buffer.position();
                            final int codeOffset = (originalOffset >= 0) ? (originalOffset - 2 + relativeOffset) : relativeOffset;
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
                                    catchType = scope.lookupType(catchTypeToken);
                                }
                                exceptionTable[k] = new ExceptionTableEntry(startOffset, endOffset, handlerOffset, catchType);
                            }
                            final int attributeCount = buffer.readUnsignedShort();
                            final SourceAttribute[] attributes = new SourceAttribute[attributeCount];
                            this.readAttributes(buffer, attributes);
                            return new CodeAttribute(length, maxStack, maxLocals, codeOffset, codeLength, buffer, exceptionTable, attributes);
                        }
                        case "BootstrapMethods": {
                            final BootstrapMethodsTableEntry[] methods = new BootstrapMethodsTableEntry[buffer.readUnsignedShort()];
                            for (int j = 0; j < methods.length; ++j) {
                                final MethodReference bootstrapMethod = scope.lookupMethod(buffer.readUnsignedShort());
                                final Object[] arguments = new Object[buffer.readUnsignedShort()];
                                final List<ParameterDefinition> parameters = bootstrapMethod.getParameters();
                                if (parameters.size() != arguments.length + 3) {
                                    final MethodDefinition resolved = bootstrapMethod.resolve();
                                    if (resolved == null || !resolved.isVarArgs() || parameters.size() >= arguments.length + 3) {
                                        throw Error.invalidBootstrapMethodEntry(bootstrapMethod, parameters.size(), arguments.length);
                                    }
                                }
                                for (int l = 0; l < arguments.length; ++l) {
                                    final int token2 = buffer.readUnsignedShort();
                                    final int parameterIndex = l + 3;
                                    TypeReference parameterType;
                                    if (parameterIndex < parameters.size()) {
                                        parameterType = parameters.get(parameterIndex).getParameterType();
                                    }
                                    else {
                                        parameterType = BuiltinTypes.Object;
                                    }
                                    final String loc_0;
                                    switch (loc_0 = parameterType.getInternalName()) {
                                        case "java/lang/invoke/MethodHandle": {
                                            arguments[l] = scope.lookupMethodHandle(token2);
                                            continue;
                                        }
                                        case "java/lang/invoke/MethodType": {
                                            arguments[l] = scope.lookupMethodType(token2);
                                            continue;
                                        }
                                        default:
                                            break;
                                    }
                                    arguments[l] = scope.lookup(token2);
                                }
                                methods[j] = new BootstrapMethodsTableEntry(bootstrapMethod, arguments);
                            }
                            return new BootstrapMethodsAttribute(methods);
                        }
                        case "LocalVariableTypeTable": {
                            break;
                        }
                        case "Exceptions": {
                            final int exceptionCount = buffer.readUnsignedShort();
                            final TypeReference[] exceptionTypes = new TypeReference[exceptionCount];
                            for (int m = 0; m < exceptionTypes.length; ++m) {
                                exceptionTypes[m] = scope.lookupType(buffer.readUnsignedShort());
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
                            final LineNumberTableEntry[] entries2 = new LineNumberTableEntry[entryCount];
                            for (int m = 0; m < entries2.length; ++m) {
                                entries2[m] = new LineNumberTableEntry(buffer.readUnsignedShort(), buffer.readUnsignedShort());
                            }
                            return new LineNumberTableAttribute(entries2);
                        }
                        case "RuntimeInvisibleAnnotations": {
                            break Label_0913;
                        }
                        default:
                            break Label_1540;
                    }
                    final int entryCount = buffer.readUnsignedShort();
                    final LocalVariableTableEntry[] entries3 = new LocalVariableTableEntry[entryCount];
                    for (int m = 0; m < entries3.length; ++m) {
                        final int scopeOffset = buffer.readUnsignedShort();
                        final int scopeLength = buffer.readUnsignedShort();
                        final int nameToken = buffer.readUnsignedShort();
                        final int typeToken2 = buffer.readUnsignedShort();
                        final int variableIndex = buffer.readUnsignedShort();
                        final String variableName = scope.lookupConstant(nameToken);
                        final String descriptor = scope.lookupConstant(typeToken2);
                        entries3[m] = new LocalVariableTableEntry(variableIndex, variableName, this.getParser().parseTypeSignature(descriptor), scopeOffset, scopeLength);
                    }
                    return new LocalVariableTableAttribute(name, entries3);
                }
                final CustomAnnotation[] annotations = new CustomAnnotation[buffer.readUnsignedShort()];
                for (int j = 0; j < annotations.length; ++j) {
                    annotations[j] = AnnotationReader.read(scope, buffer);
                }
                return new AnnotationsAttribute(name, length, annotations);
            }
            final CustomAnnotation[][] annotations2 = new CustomAnnotation[buffer.readUnsignedByte()][];
            for (int j = 0; j < annotations2.length; ++j) {
                final CustomAnnotation[] parameterAnnotations = new CustomAnnotation[buffer.readUnsignedShort()];
                for (int j2 = 0; j2 < parameterAnnotations.length; ++j2) {
                    parameterAnnotations[j2] = AnnotationReader.read(scope, buffer);
                }
                annotations2[j] = parameterAnnotations;
            }
            return new ParameterAnnotationsAttribute(name, length, annotations2);
        }
        final byte[] blob = new byte[length];
        final int offset = buffer.position();
        buffer.read(blob, 0, blob.length);
        return new BlobAttribute(name, blob, offset);
    }
    
    protected void inflateAttributes(final SourceAttribute[] attributes) {
        VerifyArgument.noNullElements(attributes, "attributes");
        if (attributes.length == 0) {
            return;
        }
        Buffer buffer = null;
        for (int i = 0; i < attributes.length; ++i) {
            final SourceAttribute attribute = attributes[i];
            if (attribute instanceof BlobAttribute) {
                if (buffer == null) {
                    buffer = new Buffer(attribute.getLength());
                }
                attributes[i] = this.inflateAttribute(buffer, attribute);
            }
        }
    }
    
    protected final SourceAttribute inflateAttribute(final SourceAttribute attribute) {
        return this.inflateAttribute(new Buffer(0), attribute);
    }
    
    protected final SourceAttribute inflateAttribute(final Buffer buffer, final SourceAttribute attribute) {
        if (attribute instanceof BlobAttribute) {
            buffer.reset(attribute.getLength());
            final BlobAttribute blobAttribute = (BlobAttribute)attribute;
            System.arraycopy(blobAttribute.getData(), 0, buffer.array(), 0, attribute.getLength());
            return this.readAttributeCore(attribute.getName(), buffer, blobAttribute.getDataOffset(), attribute.getLength());
        }
        return attribute;
    }
    
    protected void inflateAttributes(final List<SourceAttribute> attributes) {
        VerifyArgument.noNullElements(attributes, "attributes");
        if (attributes.isEmpty()) {
            return;
        }
        Buffer buffer = null;
        for (int i = 0; i < attributes.size(); ++i) {
            final SourceAttribute attribute = attributes.get(i);
            if (attribute instanceof BlobAttribute) {
                if (buffer == null) {
                    buffer = new Buffer(attribute.getLength());
                }
                else if (buffer.size() < attribute.getLength()) {
                    buffer.reset(attribute.getLength());
                }
                else {
                    buffer.position(0);
                }
                final BlobAttribute blobAttribute = (BlobAttribute)attribute;
                System.arraycopy(blobAttribute.getData(), 0, buffer.array(), 0, attribute.getLength());
                attributes.set(i, this.readAttributeCore(attribute.getName(), buffer, blobAttribute.getDataOffset(), attribute.getLength()));
            }
        }
    }
}
