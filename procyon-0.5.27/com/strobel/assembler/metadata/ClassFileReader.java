package com.strobel.assembler.metadata;

import com.strobel.assembler.ir.*;
import com.strobel.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.assembler.ir.attributes.*;
import java.util.*;

public final class ClassFileReader extends MetadataReader
{
    public static final int OPTION_PROCESS_ANNOTATIONS = 1;
    public static final int OPTION_PROCESS_CODE = 2;
    public static final int OPTIONS_DEFAULT = 1;
    static final long MAGIC = 0xCAFEBABEL;
    private final int _options;
    private final IMetadataResolver _resolver;
    private final Buffer _buffer;
    private final ConstantPool _constantPool;
    private final ConstantPool.TypeInfoEntry _baseClassEntry;
    private final ConstantPool.TypeInfoEntry[] _interfaceEntries;
    private final List<FieldInfo> _fields;
    private final List<MethodInfo> _methods;
    private final List<SourceAttribute> _attributes;
    private final String _internalName;
    private final TypeDefinition _typeDefinition;
    private final MetadataParser _parser;
    private final ResolverFrame _resolverFrame;
    private final Scope _scope;
    private static final MethodHandleType[] METHOD_HANDLE_TYPES;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    static {
        METHOD_HANDLE_TYPES = MethodHandleType.values();
    }
    
    private ClassFileReader(final int options, final IMetadataResolver resolver, final int majorVersion, final int minorVersion, final Buffer buffer, final ConstantPool constantPool, final int accessFlags, final ConstantPool.TypeInfoEntry thisClassEntry, final ConstantPool.TypeInfoEntry baseClassEntry, final ConstantPool.TypeInfoEntry[] interfaceEntries) {
        super();
        this._typeDefinition = new TypeDefinition();
        this._options = options;
        this._resolver = resolver;
        this._resolverFrame = new ResolverFrame((ResolverFrame)null);
        this._internalName = thisClassEntry.getName();
        this._buffer = buffer;
        this._constantPool = constantPool;
        this._baseClassEntry = baseClassEntry;
        this._interfaceEntries = VerifyArgument.notNull(interfaceEntries, "interfaceEntries");
        this._fields = new ArrayList<FieldInfo>();
        this._methods = new ArrayList<MethodInfo>();
        final int delimiter = this._internalName.lastIndexOf(47);
        if (delimiter < 0) {
            this._typeDefinition.setPackageName("");
            this._typeDefinition.setName(this._internalName);
        }
        else {
            this._typeDefinition.setPackageName(this._internalName.substring(0, delimiter).replace('/', '.'));
            this._typeDefinition.setName(this._internalName.substring(delimiter + 1));
        }
        this._attributes = this._typeDefinition.getSourceAttributesInternal();
        final int delimiterIndex = this._internalName.lastIndexOf(47);
        if (delimiterIndex < 0) {
            this._typeDefinition.setName(this._internalName);
        }
        else {
            this._typeDefinition.setPackageName(this._internalName.substring(0, delimiterIndex).replace('/', '.'));
            this._typeDefinition.setName(this._internalName.substring(delimiterIndex + 1));
        }
        this._typeDefinition.setResolver(this._resolver);
        this._typeDefinition.setFlags(accessFlags);
        this._typeDefinition.setCompilerVersion(majorVersion, minorVersion);
        this._resolverFrame.addType(this._typeDefinition);
        this._parser = new MetadataParser(this._typeDefinition);
        this._scope = new Scope(this._parser, this._typeDefinition, constantPool);
        this._constantPool.freezeIfUnfrozen();
        this._typeDefinition.setConstantPool(this._constantPool);
    }
    
    protected boolean shouldProcessAnnotations() {
        return (this._options & 0x1) == 0x1;
    }
    
    protected boolean shouldProcessCode() {
        return (this._options & 0x2) == 0x2;
    }
    
    @Override
    protected IMetadataScope getScope() {
        return this._scope;
    }
    
    public MetadataParser getParser() {
        return this._parser;
    }
    
    @Override
    protected SourceAttribute readAttributeCore(final String name, final Buffer buffer, final int originalOffset, final int length) {
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(buffer, "buffer");
        VerifyArgument.isNonNegative(length, "length");
        switch (name) {
            case "Code": {
                final int maxStack = buffer.readUnsignedShort();
                final int maxLocals = buffer.readUnsignedShort();
                final int codeLength = buffer.readInt();
                final int codeOffset = buffer.position();
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
                        catchType = this._scope.lookupType(catchTypeToken);
                    }
                    exceptionTable[k] = new ExceptionTableEntry(startOffset, endOffset, handlerOffset, catchType);
                }
                final int attributeCount = buffer.readUnsignedShort();
                final SourceAttribute[] attributes = new SourceAttribute[attributeCount];
                this.readAttributes(buffer, attributes);
                if (this.shouldProcessCode()) {
                    return new CodeAttribute(length, maxStack, maxLocals, codeOffset, codeLength, buffer, exceptionTable, attributes);
                }
                return new CodeAttribute(length, originalOffset + codeOffset, codeLength, maxStack, maxLocals, exceptionTable, attributes);
            }
            case "InnerClasses": {
                final InnerClassEntry[] tmp_entries = new InnerClassEntry[buffer.readUnsignedShort()];
                int j = 0;
                for (int i = 0; i < tmp_entries.length; ++i) {
                    final int innerClassIndex = buffer.readUnsignedShort();
                    final int outerClassIndex = buffer.readUnsignedShort();
                    final int shortNameIndex = buffer.readUnsignedShort();
                    final int accessFlags = buffer.readUnsignedShort();
                    final ConstantPool.TypeInfoEntry innerClass = this._constantPool.getEntry(innerClassIndex);
                    ConstantPool.TypeInfoEntry outerClass;
                    if (outerClassIndex != 0) {
                        outerClass = this._constantPool.getEntry(outerClassIndex);
                    }
                    else {
                        outerClass = null;
                    }
                    if (innerClass.getName().lastIndexOf(36) >= 0) {
                        tmp_entries[j] = new InnerClassEntry(innerClass.getName(), (outerClass != null) ? outerClass.getName() : null, (shortNameIndex != 0) ? this._constantPool.lookupConstant(shortNameIndex) : null, accessFlags);
                        ++j;
                    }
                }
                final InnerClassEntry[] entries = new InnerClassEntry[j];
                for (int l = 0; l < j; ++l) {
                    entries[l] = tmp_entries[l];
                }
                return new InnerClassesAttribute(length, ArrayUtilities.asUnmodifiableList(entries));
            }
            default:
                break;
        }
        return super.readAttributeCore(name, buffer, originalOffset, length);
    }
    
    private void readAttributesPhaseOne(final Buffer buffer, final SourceAttribute[] attributes) {
        for (int i = 0; i < attributes.length; ++i) {
            final int nameIndex = buffer.readUnsignedShort();
            final int length = buffer.readInt();
            final IMetadataScope scope = this.getScope();
            final String name = scope.lookupConstant(nameIndex);
            final String loc_0;
            switch (loc_0 = name) {
                case "ConstantValue": {
                    final int token = buffer.readUnsignedShort();
                    final Object constantValue = scope.lookupConstant(token);
                    attributes[i] = new ConstantValueAttribute(constantValue);
                    continue;
                }
                case "MethodParameters": {
                    attributes[i] = this.readAttributeCore(name, buffer, buffer.position(), length);
                    continue;
                }
                case "Signature": {
                    final int token = buffer.readUnsignedShort();
                    final String signature = scope.lookupConstant(token);
                    attributes[i] = new SignatureAttribute(signature);
                    continue;
                }
                case "SourceFile": {
                    final int token = buffer.readUnsignedShort();
                    final String sourceFile = scope.lookupConstant(token);
                    attributes[i] = new SourceFileAttribute(sourceFile);
                    continue;
                }
                case "LineNumberTable": {
                    final int entryCount = buffer.readUnsignedShort();
                    final LineNumberTableEntry[] entries = new LineNumberTableEntry[entryCount];
                    for (int j = 0; j < entries.length; ++j) {
                        entries[j] = new LineNumberTableEntry(buffer.readUnsignedShort(), buffer.readUnsignedShort());
                    }
                    attributes[i] = new LineNumberTableAttribute(entries);
                    continue;
                }
                case "InnerClasses": {
                    attributes[i] = this.readAttributeCore(name, buffer, buffer.position(), length);
                    continue;
                }
                default:
                    break;
            }
            final int offset = buffer.position();
            final byte[] blob = new byte[length];
            buffer.read(blob, 0, blob.length);
            attributes[i] = new BlobAttribute(name, blob, offset);
        }
    }
    
    public static TypeDefinition readClass(final IMetadataResolver resolver, final Buffer b) {
        return readClass(1, resolver, b);
    }
    
    public static TypeDefinition readClass(final int options, final IMetadataResolver resolver, final Buffer b) {
        final long magic = b.readInt() & 0xFFFFFFFFL;
        if (magic != 0xCAFEBABEL) {
            throw new IllegalStateException("Wrong magic number: " + magic);
        }
        final int minorVersion = b.readUnsignedShort();
        final int majorVersion = b.readUnsignedShort();
        final ConstantPool constantPool = ConstantPool.read(b);
        final int accessFlags = b.readUnsignedShort();
        final ConstantPool.TypeInfoEntry thisClass = (ConstantPool.TypeInfoEntry)constantPool.get(b.readUnsignedShort(), ConstantPool.Tag.TypeInfo);
        final int baseClassToken = b.readUnsignedShort();
        ConstantPool.TypeInfoEntry baseClass;
        if (baseClassToken == 0) {
            baseClass = null;
        }
        else {
            baseClass = constantPool.getEntry(baseClassToken);
        }
        final ConstantPool.TypeInfoEntry[] interfaces = new ConstantPool.TypeInfoEntry[b.readUnsignedShort()];
        for (int i = 0; i < interfaces.length; ++i) {
            interfaces[i] = (ConstantPool.TypeInfoEntry)constantPool.get(b.readUnsignedShort(), ConstantPool.Tag.TypeInfo);
        }
        return new ClassFileReader(options, resolver, majorVersion, minorVersion, b, constantPool, accessFlags, thisClass, baseClass, interfaces).readClass();
    }
    
    final TypeDefinition readClass() {
        this._parser.pushGenericContext(this._typeDefinition);
        try {
            this._resolver.pushFrame(this._resolverFrame);
            try {
                this.populateMemberInfo();
                SourceAttribute enclosingMethod = SourceAttribute.find("EnclosingMethod", this._attributes);
                MethodReference declaringMethod;
                try {
                    if (enclosingMethod instanceof BlobAttribute) {
                        enclosingMethod = this.inflateAttribute(enclosingMethod);
                    }
                    if (enclosingMethod instanceof EnclosingMethodAttribute) {
                        MethodReference method = ((EnclosingMethodAttribute)enclosingMethod).getEnclosingMethod();
                        if (method != null) {
                            final MethodDefinition resolvedMethod = method.resolve();
                            if (resolvedMethod != null) {
                                method = resolvedMethod;
                                final AnonymousLocalTypeCollection enclosedTypes = resolvedMethod.getDeclaredTypesInternal();
                                if (!enclosedTypes.contains(this._typeDefinition)) {
                                    enclosedTypes.add(this._typeDefinition);
                                }
                            }
                            this._typeDefinition.setDeclaringMethod(method);
                        }
                        declaringMethod = method;
                    }
                    else {
                        declaringMethod = null;
                    }
                }
                catch (Throwable t) {
                    throw ExceptionUtilities.asRuntimeException(t);
                }
                if (declaringMethod != null) {
                    this._parser.popGenericContext();
                    this._parser.pushGenericContext(declaringMethod);
                    this._parser.pushGenericContext(this._typeDefinition);
                }
                try {
                    this.populateDeclaringType();
                    this.populateBaseTypes();
                    this.visitAttributes();
                    this.visitFields();
                    this.defineMethods();
                    this.populateNamedInnerTypes();
                    this.populateAnonymousInnerTypes();
                    this.checkEnclosingMethodAttributes();
                }
                finally {
                    if (declaringMethod != null) {
                        this._parser.popGenericContext();
                    }
                }
                if (declaringMethod != null) {
                    this._parser.popGenericContext();
                }
            }
            finally {
                this._resolver.popFrame();
            }
            this._resolver.popFrame();
            return this._typeDefinition;
        }
        finally {
            this._parser.popGenericContext();
        }
    }
    
    private void checkEnclosingMethodAttributes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find("InnerClasses", this._attributes);
        if (innerClasses == null) {
            return;
        }
        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String outerClassName = entry.getOuterClassName();
            final String innerClassName = entry.getInnerClassName();
            if (outerClassName != null) {
                continue;
            }
            if (!StringUtilities.startsWith(innerClassName, String.valueOf(this._internalName) + "$")) {
                continue;
            }
            final TypeReference innerType = this._parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();
            if (resolvedInnerType == null || resolvedInnerType.getDeclaringMethod() != null) {
                continue;
            }
            final SourceAttribute rawEnclosingMethodAttribute = SourceAttribute.find("EnclosingMethod", resolvedInnerType.getSourceAttributes());
            EnclosingMethodAttribute enclosingMethodAttribute;
            if (rawEnclosingMethodAttribute instanceof EnclosingMethodAttribute) {
                enclosingMethodAttribute = (EnclosingMethodAttribute)rawEnclosingMethodAttribute;
            }
            else {
                enclosingMethodAttribute = null;
            }
            MethodReference method;
            if (enclosingMethodAttribute == null || (method = enclosingMethodAttribute.getEnclosingMethod()) == null) {
                continue;
            }
            final MethodDefinition resolvedMethod = method.resolve();
            if (resolvedMethod != null) {
                method = resolvedMethod;
                final AnonymousLocalTypeCollection enclosedTypes = resolvedMethod.getDeclaredTypesInternal();
                if (!enclosedTypes.contains(this._typeDefinition)) {
                    enclosedTypes.add(this._typeDefinition);
                }
            }
            resolvedInnerType.setDeclaringMethod(method);
        }
    }
    
    private void populateMemberInfo() {
        for (int fieldCount = this._buffer.readUnsignedShort(), i = 0; i < fieldCount; ++i) {
            final int accessFlags = this._buffer.readUnsignedShort();
            final String name = this._constantPool.lookupUtf8Constant(this._buffer.readUnsignedShort());
            final String descriptor = this._constantPool.lookupUtf8Constant(this._buffer.readUnsignedShort());
            final int attributeCount = this._buffer.readUnsignedShort();
            SourceAttribute[] attributes;
            if (attributeCount > 0) {
                attributes = new SourceAttribute[attributeCount];
                this.readAttributesPhaseOne(this._buffer, attributes);
            }
            else {
                attributes = EmptyArrayCache.fromElementType(SourceAttribute.class);
            }
            final FieldInfo field = new FieldInfo(accessFlags, name, descriptor, attributes);
            this._fields.add(field);
        }
        for (int methodCount = this._buffer.readUnsignedShort(), j = 0; j < methodCount; ++j) {
            final int accessFlags2 = this._buffer.readUnsignedShort();
            final String name2 = this._constantPool.lookupUtf8Constant(this._buffer.readUnsignedShort());
            final String descriptor2 = this._constantPool.lookupUtf8Constant(this._buffer.readUnsignedShort());
            final int attributeCount2 = this._buffer.readUnsignedShort();
            SourceAttribute[] attributes2;
            if (attributeCount2 > 0) {
                attributes2 = new SourceAttribute[attributeCount2];
                this.readAttributesPhaseOne(this._buffer, attributes2);
            }
            else {
                attributes2 = EmptyArrayCache.fromElementType(SourceAttribute.class);
            }
            final MethodInfo method = new MethodInfo(accessFlags2, name2, descriptor2, attributes2);
            this._methods.add(method);
        }
        final int typeAttributeCount = this._buffer.readUnsignedShort();
        if (typeAttributeCount > 0) {
            final SourceAttribute[] typeAttributes = new SourceAttribute[typeAttributeCount];
            this.readAttributesPhaseOne(this._buffer, typeAttributes);
            Collections.addAll(this._attributes, typeAttributes);
        }
    }
    
    private void populateDeclaringType() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find("InnerClasses", this._attributes);
        if (innerClasses == null) {
            return;
        }
        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String innerClassName = entry.getInnerClassName();
            final String shortName = entry.getShortName();
            String outerClassName = entry.getOuterClassName();
            if (Comparer.equals(innerClassName, this._internalName)) {
                if (outerClassName == null) {
                    final int delimiterIndex = innerClassName.lastIndexOf(36);
                    if (delimiterIndex < 0) {
                        continue;
                    }
                    outerClassName = innerClassName.substring(0, delimiterIndex);
                }
                if (StringUtilities.isNullOrEmpty(shortName)) {
                    this._typeDefinition.setFlags(this._typeDefinition.getFlags() | 0x100000000000L);
                }
                else {
                    this._typeDefinition.setSimpleName(shortName);
                }
                this._typeDefinition.setFlags((this._typeDefinition.getFlags() & 0xFFFFFFFFFFFFFFF8L) | entry.getAccessFlags());
                final TypeReference outerType = this._parser.parseTypeDescriptor(outerClassName);
                final TypeDefinition resolvedOuterType = outerType.resolve();
                if (resolvedOuterType != null) {
                    if (this._typeDefinition.getDeclaringType() == null) {
                        this._typeDefinition.setDeclaringType(resolvedOuterType);
                        final com.strobel.assembler.Collection<TypeDefinition> declaredTypes = resolvedOuterType.getDeclaredTypesInternal();
                        if (!declaredTypes.contains(this._typeDefinition)) {
                            declaredTypes.add(this._typeDefinition);
                        }
                    }
                }
                else if (this._typeDefinition.getDeclaringType() == null) {
                    this._typeDefinition.setDeclaringType(outerType);
                }
            }
        }
    }
    
    private void populateBaseTypes() {
        final SignatureAttribute signature = SourceAttribute.find("Signature", this._attributes);
        final String[] interfaceNames = new String[this._interfaceEntries.length];
        for (int i = 0; i < this._interfaceEntries.length; ++i) {
            interfaceNames[i] = this._interfaceEntries[i].getName();
        }
        final com.strobel.assembler.Collection<TypeReference> explicitInterfaces = this._typeDefinition.getExplicitInterfacesInternal();
        final String genericSignature = (signature != null) ? signature.getSignature() : null;
        TypeReference baseType;
        if (StringUtilities.isNullOrEmpty(genericSignature)) {
            baseType = ((this._baseClassEntry != null) ? this._parser.parseTypeDescriptor(this._baseClassEntry.getName()) : null);
            String[] loc_1;
            for (int loc_0 = (loc_1 = interfaceNames).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                final String interfaceName = loc_1[loc_2];
                explicitInterfaces.add(this._parser.parseTypeDescriptor(interfaceName));
            }
        }
        else {
            final IClassSignature classSignature = this._parser.parseClassSignature(genericSignature);
            baseType = classSignature.getBaseType();
            explicitInterfaces.addAll((Collection<?>)classSignature.getExplicitInterfaces());
            this._typeDefinition.getGenericParametersInternal().addAll(classSignature.getGenericParameters());
        }
        this._typeDefinition.setBaseType(baseType);
    }
    
    private void populateNamedInnerTypes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find("InnerClasses", this._attributes);
        if (innerClasses == null) {
            return;
        }
        final com.strobel.assembler.Collection<TypeDefinition> declaredTypes = this._typeDefinition.getDeclaredTypesInternal();
        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String outerClassName = entry.getOuterClassName();
            if (outerClassName == null) {
                continue;
            }
            final String innerClassName = entry.getInnerClassName();
            if (Comparer.equals(this._internalName, innerClassName)) {
                continue;
            }
            final TypeReference innerType = this._parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();
            if (resolvedInnerType == null || !Comparer.equals(this._internalName, outerClassName) || declaredTypes.contains(resolvedInnerType)) {
                continue;
            }
            declaredTypes.add(resolvedInnerType);
            resolvedInnerType.setFlags(resolvedInnerType.getFlags() | entry.getAccessFlags());
        }
    }
    
    private void populateAnonymousInnerTypes() {
        final InnerClassesAttribute innerClasses = SourceAttribute.find("InnerClasses", this._attributes);
        if (innerClasses == null) {
            return;
        }
        final com.strobel.assembler.Collection<TypeDefinition> declaredTypes = this._typeDefinition.getDeclaredTypesInternal();
        for (final InnerClassEntry entry : innerClasses.getEntries()) {
            final String simpleName = entry.getShortName();
            if (!StringUtilities.isNullOrEmpty(simpleName)) {
                continue;
            }
            final String outerClassName = entry.getOuterClassName();
            final String innerClassName = entry.getInnerClassName();
            if (outerClassName == null) {
                continue;
            }
            if (Comparer.equals(innerClassName, this._internalName)) {
                continue;
            }
            final TypeReference innerType = this._parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();
            if (!(resolvedInnerType instanceof TypeDefinition) || !Comparer.equals(this._internalName, outerClassName) || declaredTypes.contains(resolvedInnerType)) {
                continue;
            }
            declaredTypes.add(resolvedInnerType);
        }
        final TypeReference self = this._parser.getResolver().lookupType(this._internalName);
        if (self != null && self.isNested()) {
            return;
        }
        for (final InnerClassEntry entry2 : innerClasses.getEntries()) {
            final String outerClassName = entry2.getOuterClassName();
            if (outerClassName != null) {
                continue;
            }
            final String innerClassName = entry2.getInnerClassName();
            if (Comparer.equals(innerClassName, this._internalName)) {
                continue;
            }
            final TypeReference innerType = this._parser.parseTypeDescriptor(innerClassName);
            final TypeDefinition resolvedInnerType = innerType.resolve();
            if (resolvedInnerType == null || !Comparer.equals(this._internalName, outerClassName) || declaredTypes.contains(resolvedInnerType)) {
                continue;
            }
            declaredTypes.add(resolvedInnerType);
        }
    }
    
    private void visitFields() {
        final com.strobel.assembler.Collection<FieldDefinition> declaredFields = this._typeDefinition.getDeclaredFieldsInternal();
        for (final FieldInfo field : this._fields) {
            final SignatureAttribute signature = SourceAttribute.find("Signature", field.attributes);
            final TypeReference fieldType = this.tryParseTypeSignature((signature != null) ? signature.getSignature() : null, field.descriptor);
            final FieldDefinition fieldDefinition = new FieldDefinition(this._resolver);
            fieldDefinition.setDeclaringType(this._typeDefinition);
            fieldDefinition.setFlags(Flags.fromStandardFlags(field.accessFlags, Flags.Kind.Field));
            fieldDefinition.setName(field.name);
            fieldDefinition.setFieldType(fieldType);
            declaredFields.add(fieldDefinition);
            this.inflateAttributes(field.attributes);
            final ConstantValueAttribute constantValueAttribute = SourceAttribute.find("ConstantValue", field.attributes);
            if (constantValueAttribute != null) {
                final Object constantValue = constantValueAttribute.getValue();
                if (constantValue instanceof Number) {
                    final Number number = (Number)constantValue;
                    final JvmType jvmType = fieldDefinition.getFieldType().getSimpleType();
                    switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[jvmType.ordinal()]) {
                        case 1: {
                            fieldDefinition.setConstantValue(number.longValue() != 0L);
                            break;
                        }
                        case 2: {
                            fieldDefinition.setConstantValue(number.byteValue());
                            break;
                        }
                        case 3: {
                            fieldDefinition.setConstantValue((char)number.longValue());
                            break;
                        }
                        case 4: {
                            fieldDefinition.setConstantValue(number.shortValue());
                            break;
                        }
                        case 5: {
                            fieldDefinition.setConstantValue(number.intValue());
                            break;
                        }
                        case 6: {
                            fieldDefinition.setConstantValue(number.longValue());
                            break;
                        }
                        case 7: {
                            fieldDefinition.setConstantValue(number.floatValue());
                            break;
                        }
                        case 8: {
                            fieldDefinition.setConstantValue(number.doubleValue());
                            break;
                        }
                        default: {
                            fieldDefinition.setConstantValue(constantValue);
                            break;
                        }
                    }
                }
                else {
                    fieldDefinition.setConstantValue(constantValue);
                }
            }
            if (SourceAttribute.find("Synthetic", field.attributes) != null) {
                fieldDefinition.setFlags(fieldDefinition.getFlags() | 0x1000L);
            }
            if (SourceAttribute.find("Deprecated", field.attributes) != null) {
                fieldDefinition.setFlags(fieldDefinition.getFlags() | 0x20000L);
            }
            SourceAttribute[] loc_2;
            for (int loc_1 = (loc_2 = field.attributes).length, loc_3 = 0; loc_3 < loc_1; ++loc_3) {
                final SourceAttribute attribute = loc_2[loc_3];
                fieldDefinition.getSourceAttributesInternal().add(attribute);
            }
            if (this.shouldProcessAnnotations()) {
                final com.strobel.assembler.Collection<CustomAnnotation> annotations = fieldDefinition.getAnnotationsInternal();
                final AnnotationsAttribute visibleAnnotations = SourceAttribute.find("RuntimeVisibleAnnotations", field.attributes);
                final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find("RuntimeInvisibleAnnotations", field.attributes);
                if (visibleAnnotations != null) {
                    Collections.addAll(annotations, visibleAnnotations.getAnnotations());
                }
                if (invisibleAnnotations == null) {
                    continue;
                }
                Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
            }
        }
    }
    
    private TypeReference tryParseTypeSignature(final String signature, final String fallback) {
        try {
            if (signature != null) {
                return this._parser.parseTypeSignature(signature);
            }
        }
        catch (Throwable loc_0) {}
        return this._parser.parseTypeSignature(fallback);
    }
    
    private void defineMethods() {
        try {
            Throwable loc_0 = null;
            try {
                final AutoCloseable ignored = this._parser.suppressTypeResolution();
                try {
                    for (final MethodInfo method : this._methods) {
                        final IMethodSignature methodDescriptor = this._parser.parseMethodSignature(method.descriptor);
                        final MethodDefinition methodDefinition = new MethodDefinition();
                        methodDefinition.setName(method.name);
                        methodDefinition.setFlags(Flags.fromStandardFlags(method.accessFlags, Flags.Kind.Method));
                        methodDefinition.setDeclaringType(this._typeDefinition);
                        if (this._typeDefinition.isInterface() && !Flags.testAny(method.accessFlags, 1024)) {
                            methodDefinition.setFlags(methodDefinition.getFlags() | 0x80000000000L);
                        }
                        this._typeDefinition.getDeclaredMethodsInternal().add(methodDefinition);
                        this._parser.pushGenericContext(methodDefinition);
                        try {
                            final SignatureAttribute signature = SourceAttribute.find("Signature", method.attributes);
                            final IMethodSignature methodSignature = this.tryParseMethodSignature((signature != null) ? signature.getSignature() : null, methodDescriptor);
                            final List<ParameterDefinition> signatureParameters = methodSignature.getParameters();
                            final List<ParameterDefinition> descriptorParameters = methodDescriptor.getParameters();
                            final ParameterDefinitionCollection parameters = methodDefinition.getParametersInternal();
                            methodDefinition.setReturnType(methodSignature.getReturnType());
                            parameters.addAll(signatureParameters);
                            methodDefinition.getGenericParametersInternal().addAll(methodSignature.getGenericParameters());
                            methodDefinition.getThrownTypesInternal().addAll((Collection<?>)methodSignature.getThrownTypes());
                            for (int missingParameters = descriptorParameters.size() - signatureParameters.size(), i = 0; i < missingParameters; ++i) {
                                final ParameterDefinition parameter = descriptorParameters.get(i);
                                parameter.setFlags(parameter.getFlags() | 0x1000L);
                                parameters.add(i, parameter);
                            }
                            int slot = 0;
                            if (!Flags.testAny(methodDefinition.getFlags(), 8L)) {
                                ++slot;
                            }
                            final MethodParametersAttribute methodParameters = SourceAttribute.find("MethodParameters", method.attributes);
                            final List<MethodParameterEntry> parameterEntries = (methodParameters != null) ? methodParameters.getEntries() : null;
                            final List<ParameterDefinition> parametersList = methodDefinition.getParameters();
                            for (int j = 0; j < parametersList.size(); ++j) {
                                final ParameterDefinition parameter2 = parametersList.get(j);
                                parameter2.setSlot(slot);
                                slot += parameter2.getSize();
                                if (parameterEntries != null && j < parameterEntries.size()) {
                                    final MethodParameterEntry entry = parameterEntries.get(j);
                                    final String parameterName = entry.getName();
                                    if (!StringUtilities.isNullOrWhitespace(parameterName)) {
                                        parameter2.setName(parameterName);
                                    }
                                    parameter2.setFlags(entry.getFlags());
                                }
                            }
                            this.inflateAttributes(method.attributes);
                            Collections.addAll(methodDefinition.getSourceAttributesInternal(), method.attributes);
                            method.codeAttribute = SourceAttribute.find("Code", method.attributes);
                            if (method.codeAttribute != null) {
                                methodDefinition.getSourceAttributesInternal().addAll((Collection<?>)((CodeAttribute)method.codeAttribute).getAttributes());
                            }
                            final ExceptionsAttribute exceptions = SourceAttribute.find("Exceptions", method.attributes);
                            if (exceptions != null) {
                                final com.strobel.assembler.Collection<TypeReference> thrownTypes = methodDefinition.getThrownTypesInternal();
                                for (final TypeReference thrownType : exceptions.getExceptionTypes()) {
                                    if (!thrownTypes.contains(thrownType)) {
                                        thrownTypes.add(thrownType);
                                    }
                                }
                            }
                            if ("<init>".equals(method.name)) {
                                if (Flags.testAny(this._typeDefinition.getFlags(), 17592186044416L)) {
                                    methodDefinition.setFlags(methodDefinition.getFlags() | 0x20000000L | 0x1000L);
                                }
                                if (Flags.testAny(method.accessFlags, 2048)) {
                                    this._typeDefinition.setFlags(this._typeDefinition.getFlags() | 0x800L);
                                }
                            }
                            this.readMethodBody(method, methodDefinition);
                            if (SourceAttribute.find("Synthetic", method.attributes) != null) {
                                methodDefinition.setFlags(methodDefinition.getFlags() | 0x1000L);
                            }
                            if (SourceAttribute.find("Deprecated", method.attributes) != null) {
                                methodDefinition.setFlags(methodDefinition.getFlags() | 0x20000L);
                            }
                            if (this.shouldProcessAnnotations()) {
                                final AnnotationsAttribute visibleAnnotations = SourceAttribute.find("RuntimeVisibleAnnotations", method.attributes);
                                final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find("RuntimeInvisibleAnnotations", method.attributes);
                                final com.strobel.assembler.Collection<CustomAnnotation> annotations = methodDefinition.getAnnotationsInternal();
                                if (visibleAnnotations != null) {
                                    Collections.addAll(annotations, visibleAnnotations.getAnnotations());
                                }
                                if (invisibleAnnotations != null) {
                                    Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
                                }
                                final ParameterAnnotationsAttribute visibleParameterAnnotations = SourceAttribute.find("RuntimeVisibleParameterAnnotations", method.attributes);
                                final ParameterAnnotationsAttribute invisibleParameterAnnotations = SourceAttribute.find("RuntimeInvisibleParameterAnnotations", method.attributes);
                                if (visibleParameterAnnotations != null) {
                                    for (int k = 0; k < visibleParameterAnnotations.getAnnotations().length && k < parameters.size(); ++k) {
                                        Collections.addAll(parameters.get(k).getAnnotationsInternal(), visibleParameterAnnotations.getAnnotations()[k]);
                                    }
                                }
                                if (invisibleParameterAnnotations != null) {
                                    for (int k = 0; k < invisibleParameterAnnotations.getAnnotations().length; ++k) {
                                        if (k >= parameters.size()) {
                                            break;
                                        }
                                        Collections.addAll(parameters.get(k).getAnnotationsInternal(), invisibleParameterAnnotations.getAnnotations()[k]);
                                    }
                                }
                            }
                        }
                        finally {
                            this._parser.popGenericContext();
                        }
                        this._parser.popGenericContext();
                    }
                }
                finally {
                    if (ignored != null) {
                        ignored.close();
                    }
                }
            }
            finally {
                if (loc_0 == null) {
                    final Throwable loc_3;
                    loc_0 = loc_3;
                }
                else {
                    final Throwable loc_3;
                    if (loc_0 != loc_3) {
                        loc_0.addSuppressed(loc_3);
                    }
                }
            }
        }
        catch (Throwable t) {
            throw ExceptionUtilities.asRuntimeException(t);
        }
    }
    
    private IMethodSignature tryParseMethodSignature(final String signature, final IMethodSignature fallback) {
        try {
            if (signature != null) {
                return this._parser.parseMethodSignature(signature);
            }
        }
        catch (Throwable loc_0) {}
        return fallback;
    }
    
    private void readMethodBody(final MethodInfo methodInfo, final MethodDefinition methodDefinition) {
        if (methodInfo.codeAttribute instanceof CodeAttribute) {
            if (Flags.testAny(this._options, 2)) {
                final MethodReader reader = new MethodReader(methodDefinition, this._scope);
                final MethodBody body = reader.readBody();
                methodDefinition.setBody(body);
                body.freeze();
            }
            else {
                final CodeAttribute codeAttribute = (CodeAttribute)methodInfo.codeAttribute;
                final LocalVariableTableAttribute localVariables = SourceAttribute.find("LocalVariableTable", codeAttribute.getAttributes());
                if (localVariables == null) {
                    return;
                }
                final List<ParameterDefinition> parameters = methodDefinition.getParameters();
                for (final LocalVariableTableEntry entry : localVariables.getEntries()) {
                    ParameterDefinition parameter = null;
                    for (int j = 0; j < parameters.size(); ++j) {
                        if (parameters.get(j).getSlot() == entry.getIndex()) {
                            parameter = parameters.get(j);
                            break;
                        }
                    }
                    if (parameter != null && !parameter.hasName()) {
                        parameter.setName(entry.getName());
                    }
                }
            }
        }
    }
    
    private void visitAttributes() {
        this.inflateAttributes(this._attributes);
        if (this.shouldProcessAnnotations()) {
            final AnnotationsAttribute visibleAnnotations = SourceAttribute.find("RuntimeVisibleAnnotations", this._attributes);
            final AnnotationsAttribute invisibleAnnotations = SourceAttribute.find("RuntimeInvisibleAnnotations", this._attributes);
            final com.strobel.assembler.Collection<CustomAnnotation> annotations = this._typeDefinition.getAnnotationsInternal();
            if (visibleAnnotations != null) {
                Collections.addAll(annotations, visibleAnnotations.getAnnotations());
            }
            if (invisibleAnnotations != null) {
                Collections.addAll(annotations, invisibleAnnotations.getAnnotations());
            }
        }
    }
    
    static /* synthetic */ MethodHandleType[] access$0() {
        return ClassFileReader.METHOD_HANDLE_TYPES;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = ClassFileReader.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JvmType.values().length];
        try {
            loc_1[JvmType.Array.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JvmType.Boolean.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JvmType.Byte.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JvmType.Character.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[JvmType.Double.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[JvmType.Float.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[JvmType.Integer.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[JvmType.Long.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[JvmType.Object.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[JvmType.Short.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[JvmType.TypeVariable.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[JvmType.Void.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[JvmType.Wildcard.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_14) {}
        return ClassFileReader.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    final class FieldInfo
    {
        final int accessFlags;
        final String name;
        final String descriptor;
        final SourceAttribute[] attributes;
        
        FieldInfo(final int accessFlags, final String name, final String descriptor, final SourceAttribute[] attributes) {
            super();
            this.accessFlags = accessFlags;
            this.name = name;
            this.descriptor = descriptor;
            this.attributes = attributes;
        }
    }
    
    final class MethodInfo
    {
        final int accessFlags;
        final String name;
        final String descriptor;
        final SourceAttribute[] attributes;
        SourceAttribute codeAttribute;
        
        MethodInfo(final int accessFlags, final String name, final String descriptor, final SourceAttribute[] attributes) {
            super();
            this.accessFlags = accessFlags;
            this.name = name;
            this.descriptor = descriptor;
            this.attributes = attributes;
            this.codeAttribute = SourceAttribute.find("Code", attributes);
        }
    }
    
    static class Scope implements IMetadataScope
    {
        private final MetadataParser _parser;
        private final TypeDefinition _typeDefinition;
        private final ConstantPool _constantPool;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
        
        Scope(final MetadataParser parser, final TypeDefinition typeDefinition, final ConstantPool constantPool) {
            super();
            this._parser = parser;
            this._typeDefinition = typeDefinition;
            this._constantPool = constantPool;
        }
        
        @Override
        public TypeReference lookupType(final int token) {
            final ConstantPool.Entry entry = this._constantPool.get(token);
            if (entry instanceof ConstantPool.TypeInfoEntry) {
                final ConstantPool.TypeInfoEntry typeInfo = (ConstantPool.TypeInfoEntry)entry;
                return this._parser.parseTypeDescriptor(typeInfo.getName());
            }
            final String typeName = this._constantPool.lookupConstant(token);
            return this._parser.parseTypeSignature(typeName);
        }
        
        @Override
        public FieldReference lookupField(final int token) {
            final ConstantPool.FieldReferenceEntry entry = this._constantPool.getEntry(token);
            return this.lookupField(entry.typeInfoIndex, entry.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public MethodReference lookupMethod(final int token) {
            final ConstantPool.Entry entry = this._constantPool.getEntry(token);
            ConstantPool.ReferenceEntry reference;
            if (entry instanceof ConstantPool.MethodHandleEntry) {
                final ConstantPool.MethodHandleEntry methodHandle = (ConstantPool.MethodHandleEntry)entry;
                reference = this._constantPool.getEntry(methodHandle.referenceIndex);
            }
            else {
                reference = (ConstantPool.ReferenceEntry)entry;
            }
            return this.lookupMethod(reference.typeInfoIndex, reference.nameAndTypeDescriptorIndex);
        }
        
        @Override
        public MethodHandle lookupMethodHandle(final int token) {
            final ConstantPool.MethodHandleEntry entry = this._constantPool.getEntry(token);
            final ConstantPool.ReferenceEntry reference = this._constantPool.getEntry(entry.referenceIndex);
            return new MethodHandle(this.lookupMethod(reference.typeInfoIndex, reference.nameAndTypeDescriptorIndex), ClassFileReader.access$0()[entry.referenceKind.ordinal()]);
        }
        
        @Override
        public IMethodSignature lookupMethodType(final int token) {
            final ConstantPool.MethodTypeEntry entry = this._constantPool.getEntry(token);
            return this._parser.parseMethodSignature(entry.getType());
        }
        
        @Override
        public DynamicCallSite lookupDynamicCallSite(final int token) {
            final ConstantPool.InvokeDynamicInfoEntry entry = this._constantPool.getEntry(token);
            final BootstrapMethodsAttribute attribute = SourceAttribute.find("BootstrapMethods", this._typeDefinition.getSourceAttributes());
            final BootstrapMethodsTableEntry bootstrapMethod = attribute.getBootstrapMethods().get(entry.bootstrapMethodAttributeIndex);
            final ConstantPool.NameAndTypeDescriptorEntry nameAndType = this._constantPool.getEntry(entry.nameAndTypeDescriptorIndex);
            return new DynamicCallSite(bootstrapMethod.getMethod(), bootstrapMethod.getArguments(), nameAndType.getName(), this._parser.parseMethodSignature(nameAndType.getType()));
        }
        
        @Override
        public FieldReference lookupField(final int typeToken, final int nameAndTypeToken) {
            final ConstantPool.NameAndTypeDescriptorEntry nameAndDescriptor = this._constantPool.getEntry(nameAndTypeToken);
            return this._parser.parseField(this.lookupType(typeToken), nameAndDescriptor.getName(), nameAndDescriptor.getType());
        }
        
        @Override
        public MethodReference lookupMethod(final int typeToken, final int nameAndTypeToken) {
            final ConstantPool.NameAndTypeDescriptorEntry nameAndDescriptor = this._constantPool.getEntry(nameAndTypeToken);
            return this._parser.parseMethod(this.lookupType(typeToken), nameAndDescriptor.getName(), nameAndDescriptor.getType());
        }
        
        @Override
        public <T> T lookupConstant(final int token) {
            final ConstantPool.Entry entry = this._constantPool.get(token);
            if (entry.getTag() == ConstantPool.Tag.TypeInfo) {
                return (T)this.lookupType(token);
            }
            return this._constantPool.lookupConstant(token);
        }
        
        @Override
        public Object lookup(final int token) {
            final ConstantPool.Entry entry = this._constantPool.get(token);
            if (entry == null) {
                return null;
            }
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag()[entry.getTag().ordinal()]) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 7: {
                    return this.lookupConstant(token);
                }
                case 6: {
                    return this.lookupType(token);
                }
                case 8: {
                    return this.lookupField(token);
                }
                case 9: {
                    return this.lookupMethod(token);
                }
                case 10: {
                    return this.lookupMethod(token);
                }
                case 12: {
                    return this.lookupMethodHandle(token);
                }
                case 13: {
                    return this.lookupMethodType(token);
                }
                case 14: {
                    return this.lookupDynamicCallSite(token);
                }
                default: {
                    return null;
                }
            }
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag() {
            final int[] loc_0 = Scope.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag;
            if (loc_0 != null) {
                return loc_0;
            }
            final int[] loc_1 = new int[ConstantPool.Tag.values().length];
            try {
                loc_1[ConstantPool.Tag.DoubleConstant.ordinal()] = 5;
            }
            catch (NoSuchFieldError loc_2) {}
            try {
                loc_1[ConstantPool.Tag.FieldReference.ordinal()] = 8;
            }
            catch (NoSuchFieldError loc_3) {}
            try {
                loc_1[ConstantPool.Tag.FloatConstant.ordinal()] = 3;
            }
            catch (NoSuchFieldError loc_4) {}
            try {
                loc_1[ConstantPool.Tag.IntegerConstant.ordinal()] = 2;
            }
            catch (NoSuchFieldError loc_5) {}
            try {
                loc_1[ConstantPool.Tag.InterfaceMethodReference.ordinal()] = 10;
            }
            catch (NoSuchFieldError loc_6) {}
            try {
                loc_1[ConstantPool.Tag.InvokeDynamicInfo.ordinal()] = 14;
            }
            catch (NoSuchFieldError loc_7) {}
            try {
                loc_1[ConstantPool.Tag.LongConstant.ordinal()] = 4;
            }
            catch (NoSuchFieldError loc_8) {}
            try {
                loc_1[ConstantPool.Tag.MethodHandle.ordinal()] = 12;
            }
            catch (NoSuchFieldError loc_9) {}
            try {
                loc_1[ConstantPool.Tag.MethodReference.ordinal()] = 9;
            }
            catch (NoSuchFieldError loc_10) {}
            try {
                loc_1[ConstantPool.Tag.MethodType.ordinal()] = 13;
            }
            catch (NoSuchFieldError loc_11) {}
            try {
                loc_1[ConstantPool.Tag.NameAndTypeDescriptor.ordinal()] = 11;
            }
            catch (NoSuchFieldError loc_12) {}
            try {
                loc_1[ConstantPool.Tag.StringConstant.ordinal()] = 7;
            }
            catch (NoSuchFieldError loc_13) {}
            try {
                loc_1[ConstantPool.Tag.TypeInfo.ordinal()] = 6;
            }
            catch (NoSuchFieldError loc_14) {}
            try {
                loc_1[ConstantPool.Tag.Utf8StringConstant.ordinal()] = 1;
            }
            catch (NoSuchFieldError loc_15) {}
            return Scope.$SWITCH_TABLE$com$strobel$assembler$ir$ConstantPool$Tag = loc_1;
        }
    }
    
    private final class ResolverFrame implements IResolverFrame
    {
        final HashMap<String, TypeReference> types;
        final HashMap<String, GenericParameter> typeVariables;
        
        private ResolverFrame() {
            super();
            this.types = new HashMap<String, TypeReference>();
            this.typeVariables = new HashMap<String, GenericParameter>();
        }
        
        public void addType(final TypeReference type) {
            VerifyArgument.notNull(type, "type");
            this.types.put(type.getInternalName(), type);
        }
        
        public void addTypeVariable(final GenericParameter type) {
            VerifyArgument.notNull(type, "type");
            this.typeVariables.put(type.getName(), type);
        }
        
        public void removeType(final TypeReference type) {
            VerifyArgument.notNull(type, "type");
            this.types.remove(type.getInternalName());
        }
        
        public void removeTypeVariable(final GenericParameter type) {
            VerifyArgument.notNull(type, "type");
            this.typeVariables.remove(type.getName());
        }
        
        @Override
        public TypeReference findType(final String descriptor) {
            final TypeReference type = this.types.get(descriptor);
            if (type != null) {
                return type;
            }
            return null;
        }
        
        @Override
        public GenericParameter findTypeVariable(final String name) {
            final GenericParameter typeVariable = this.typeVariables.get(name);
            if (typeVariable != null) {
                return typeVariable;
            }
            for (final String typeName : this.types.keySet()) {
                final TypeReference t = this.types.get(typeName);
                if (t.containsGenericParameters()) {
                    for (final GenericParameter p : t.getGenericParameters()) {
                        if (StringUtilities.equals(p.getName(), name)) {
                            return p;
                        }
                    }
                }
            }
            return null;
        }
    }
}
