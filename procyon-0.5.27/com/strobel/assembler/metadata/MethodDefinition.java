package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.annotations.*;
import java.lang.ref.*;
import com.strobel.core.*;
import javax.lang.model.element.*;
import java.util.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.ir.*;

public class MethodDefinition extends MethodReference implements IMemberDefinition
{
    private final GenericParameterCollection _genericParameters;
    private final ParameterDefinitionCollection _parameters;
    private final AnonymousLocalTypeCollection _declaredTypes;
    private final Collection<TypeReference> _thrownTypes;
    private final Collection<CustomAnnotation> _customAnnotations;
    private final Collection<SourceAttribute> _sourceAttributes;
    private final List<GenericParameter> _genericParametersView;
    private final List<TypeDefinition> _declaredTypesView;
    private final List<ParameterDefinition> _parametersView;
    private final List<TypeReference> _thrownTypesView;
    private final List<CustomAnnotation> _customAnnotationsView;
    private final List<SourceAttribute> _sourceAttributesView;
    private SoftReference<MethodBody> _body;
    private String _name;
    private String _fullName;
    private String _erasedSignature;
    private String _signature;
    private TypeReference _returnType;
    private TypeDefinition _declaringType;
    private long _flags;
    
    protected MethodDefinition() {
        super();
        this._genericParameters = new GenericParameterCollection(this);
        this._parameters = new ParameterDefinitionCollection(this);
        this._declaredTypes = new AnonymousLocalTypeCollection(this);
        this._thrownTypes = new Collection<TypeReference>();
        this._customAnnotations = new Collection<CustomAnnotation>();
        this._sourceAttributes = new Collection<SourceAttribute>();
        this._genericParametersView = Collections.unmodifiableList((List<? extends GenericParameter>)this._genericParameters);
        this._parametersView = Collections.unmodifiableList((List<? extends ParameterDefinition>)this._parameters);
        this._declaredTypesView = Collections.unmodifiableList((List<? extends TypeDefinition>)this._declaredTypes);
        this._thrownTypesView = Collections.unmodifiableList((List<? extends TypeReference>)this._thrownTypes);
        this._customAnnotationsView = Collections.unmodifiableList((List<? extends CustomAnnotation>)this._customAnnotations);
        this._sourceAttributesView = Collections.unmodifiableList((List<? extends SourceAttribute>)this._sourceAttributes);
    }
    
    public final boolean hasBody() {
        final SoftReference<MethodBody> bodyCache = this._body;
        return bodyCache != null && bodyCache.get() != null;
    }
    
    public final MethodBody getBody() {
        MethodBody body = null;
        final SoftReference<MethodBody> cachedBody = this._body;
        if (cachedBody != null) {
            if ((body = this._body.get()) != null) {
                return body;
            }
        }
        try {
            return this.tryLoadBody();
        }
        catch (Throwable t) {
            this.setFlags(this.getFlags() | 0x400000000000L);
        }
        return body;
    }
    
    public final boolean hasThis() {
        return !this.isStatic();
    }
    
    protected final void setBody(final MethodBody body) {
        this._body = new SoftReference<MethodBody>(body);
    }
    
    @Override
    public final boolean isDefinition() {
        return true;
    }
    
    public final boolean isAnonymousClassConstructor() {
        return Flags.testAny(this._flags, 536870912L);
    }
    
    public final List<TypeDefinition> getDeclaredTypes() {
        return this._declaredTypesView;
    }
    
    protected final AnonymousLocalTypeCollection getDeclaredTypesInternal() {
        return this._declaredTypes;
    }
    
    @Override
    public final List<GenericParameter> getGenericParameters() {
        return this._genericParametersView;
    }
    
    @Override
    public final List<TypeReference> getThrownTypes() {
        return this._thrownTypesView;
    }
    
    @Override
    public final TypeDefinition getDeclaringType() {
        return this._declaringType;
    }
    
    @Override
    public final List<CustomAnnotation> getAnnotations() {
        return this._customAnnotationsView;
    }
    
    public final List<SourceAttribute> getSourceAttributes() {
        return this._sourceAttributesView;
    }
    
    @Override
    public final String getName() {
        return this._name;
    }
    
    @Override
    public String getFullName() {
        if (this._fullName == null) {
            this._fullName = super.getFullName();
        }
        return this._fullName;
    }
    
    @Override
    public String getSignature() {
        if (this._signature == null) {
            this._signature = super.getSignature();
        }
        return this._signature;
    }
    
    @Override
    public String getErasedSignature() {
        if (this._erasedSignature == null) {
            this._erasedSignature = super.getErasedSignature();
        }
        return this._erasedSignature;
    }
    
    @Override
    public final TypeReference getReturnType() {
        return this._returnType;
    }
    
    @Override
    public final List<ParameterDefinition> getParameters() {
        return this._parametersView;
    }
    
    protected final void setName(final String name) {
        this._name = name;
    }
    
    protected final void setReturnType(final TypeReference returnType) {
        this._returnType = returnType;
    }
    
    protected final void setDeclaringType(final TypeDefinition declaringType) {
        this._declaringType = declaringType;
        this._parameters.setDeclaringType(declaringType);
    }
    
    protected final void setFlags(final long flags) {
        this._flags = flags;
    }
    
    protected final GenericParameterCollection getGenericParametersInternal() {
        return this._genericParameters;
    }
    
    protected final ParameterDefinitionCollection getParametersInternal() {
        return this._parameters;
    }
    
    protected final Collection<TypeReference> getThrownTypesInternal() {
        return this._thrownTypes;
    }
    
    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return this._customAnnotations;
    }
    
    protected final Collection<SourceAttribute> getSourceAttributesInternal() {
        return this._sourceAttributes;
    }
    
    @Override
    public int hashCode() {
        return HashUtilities.hashCode(this.getFullName());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MethodDefinition) {
            final MethodDefinition other = (MethodDefinition)obj;
            return StringUtilities.equals(this.getName(), other.getName()) && StringUtilities.equals(this.getErasedSignature(), other.getErasedSignature()) && this.typeNamesMatch(this.getDeclaringType(), other.getDeclaringType());
        }
        return false;
    }
    
    private boolean typeNamesMatch(final TypeReference t1, final TypeReference t2) {
        return t1 != null && t2 != null && StringUtilities.equals(t1.getFullName(), t2.getFullName());
    }
    
    public final boolean isAbstract() {
        return Flags.testAny(this.getFlags(), 1024L);
    }
    
    public final boolean isDefault() {
        return Flags.testAny(this.getFlags(), 8796093022208L);
    }
    
    public final boolean isBridgeMethod() {
        return Flags.testAny(this.getFlags(), 2147483712L);
    }
    
    public final boolean isVarArgs() {
        return Flags.testAny(this.getFlags(), 17179869312L);
    }
    
    @Override
    public final long getFlags() {
        return this._flags;
    }
    
    @Override
    public final int getModifiers() {
        return Flags.toModifiers(this.getFlags());
    }
    
    @Override
    public final boolean isFinal() {
        return Flags.testAny(this.getFlags(), 16L);
    }
    
    @Override
    public final boolean isNonPublic() {
        return !Flags.testAny(this.getFlags(), 1L);
    }
    
    @Override
    public final boolean isPrivate() {
        return Flags.testAny(this.getFlags(), 2L);
    }
    
    @Override
    public final boolean isProtected() {
        return Flags.testAny(this.getFlags(), 4L);
    }
    
    @Override
    public final boolean isPublic() {
        return Flags.testAny(this.getFlags(), 1L);
    }
    
    @Override
    public final boolean isStatic() {
        return Flags.testAny(this.getFlags(), 8L);
    }
    
    @Override
    public final boolean isSynthetic() {
        return Flags.testAny(this.getFlags(), 4096L);
    }
    
    @Override
    public final boolean isDeprecated() {
        return Flags.testAny(this.getFlags(), 131072L);
    }
    
    @Override
    public final boolean isPackagePrivate() {
        return !Flags.testAny(this.getFlags(), 7L);
    }
    
    @Override
    public String getBriefDescription() {
        return this.appendBriefDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getDescription() {
        return this.appendDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getErasedDescription() {
        return this.appendErasedDescription(new StringBuilder()).toString();
    }
    
    @Override
    public String getSimpleDescription() {
        return this.appendSimpleDescription(new StringBuilder()).toString();
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName) {
            final TypeDefinition declaringType = this.getDeclaringType();
            if (declaringType != null) {
                return declaringType.appendName(sb, true, false).append('.').append(this.getName());
            }
        }
        return sb.append(this._name);
    }
    
    public StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers() & 0xFFFFFF7F)) {
            s.append(modifier.toString());
            s.append(' ');
        }
        List<? extends TypeReference> typeArguments;
        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance)this).getTypeArguments();
        }
        else if (this.hasGenericParameters()) {
            typeArguments = this.getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }
        if (!typeArguments.isEmpty()) {
            final int count = typeArguments.size();
            s.append('<');
            for (int i = 0; i < count; ++i) {
                if (i != 0) {
                    s.append(", ");
                }
                s = ((TypeReference)typeArguments.get(i)).appendSimpleDescription(s);
            }
            s.append('>');
            s.append(' ');
        }
        TypeReference returnType;
        for (returnType = this.getReturnType(); returnType.isWildcardType(); returnType = returnType.getExtendsBound()) {}
        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        s.append('(');
        final List<ParameterDefinition> parameters = this.getParameters();
        for (int j = 0, n = parameters.size(); j < n; ++j) {
            final ParameterDefinition p = parameters.get(j);
            if (j != 0) {
                s.append(", ");
            }
            TypeReference parameterType;
            for (parameterType = p.getParameterType(); parameterType.isWildcardType(); parameterType = parameterType.getExtendsBound()) {}
            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
            s.append(" ").append(p.getName());
        }
        s.append(')');
        final List<TypeReference> thrownTypes = this.getThrownTypes();
        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");
            for (int k = 0, n2 = thrownTypes.size(); k < n2; ++k) {
                final TypeReference t = thrownTypes.get(k);
                if (k != 0) {
                    s.append(", ");
                }
                s = t.appendBriefDescription(s);
            }
        }
        return s;
    }
    
    public StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers() & 0xFFFFFF7F)) {
            s.append(modifier.toString());
            s.append(' ');
        }
        List<? extends TypeReference> typeArguments;
        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance)this).getTypeArguments();
        }
        else if (this.hasGenericParameters()) {
            typeArguments = this.getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }
        if (!typeArguments.isEmpty()) {
            s.append('<');
            for (int i = 0, n = typeArguments.size(); i < n; ++i) {
                if (i != 0) {
                    s.append(", ");
                }
                final TypeReference typeArgument = (TypeReference)typeArguments.get(i);
                if (typeArgument instanceof GenericParameter) {
                    s.append(typeArgument.getSimpleName());
                }
                else {
                    s = typeArgument.appendSimpleDescription(s);
                }
            }
            s.append('>');
            s.append(' ');
        }
        TypeReference returnType;
        for (returnType = this.getReturnType(); returnType.isWildcardType(); returnType = returnType.getExtendsBound()) {}
        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendSimpleDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        s.append('(');
        final List<ParameterDefinition> parameters = this.getParameters();
        for (int j = 0, n2 = parameters.size(); j < n2; ++j) {
            final ParameterDefinition p = parameters.get(j);
            if (j != 0) {
                s.append(", ");
            }
            TypeReference parameterType;
            for (parameterType = p.getParameterType(); parameterType.isWildcardType(); parameterType = parameterType.getExtendsBound()) {}
            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendSimpleDescription(s);
            }
        }
        s.append(')');
        final List<TypeReference> thrownTypes = this.getThrownTypes();
        if (!thrownTypes.isEmpty()) {
            s.append(" throws ");
            for (int k = 0, n3 = thrownTypes.size(); k < n3; ++k) {
                final TypeReference t = thrownTypes.get(k);
                if (k != 0) {
                    s.append(", ");
                }
                s = t.appendSimpleDescription(s);
            }
        }
        return s;
    }
    
    public StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        TypeReference returnType;
        for (returnType = this.getReturnType(); returnType.isWildcardType(); returnType = returnType.getExtendsBound()) {}
        if (returnType.isGenericParameter()) {
            s.append(returnType.getName());
        }
        else {
            s = returnType.appendBriefDescription(s);
        }
        s.append(' ');
        s.append(this.getName());
        s.append('(');
        final List<ParameterDefinition> parameters = this.getParameters();
        for (int i = 0, n = parameters.size(); i < n; ++i) {
            final ParameterDefinition p = parameters.get(i);
            if (i != 0) {
                s.append(", ");
            }
            TypeReference parameterType;
            for (parameterType = p.getParameterType(); parameterType.isWildcardType(); parameterType = parameterType.getExtendsBound()) {}
            if (parameterType.isGenericParameter()) {
                s.append(parameterType.getName());
            }
            else {
                s = parameterType.appendBriefDescription(s);
            }
        }
        s.append(')');
        return s;
    }
    
    public StringBuilder appendErasedDescription(final StringBuilder sb) {
        if (this.hasGenericParameters() && !this.isGenericDefinition()) {
            final MethodDefinition definition = this.resolve();
            if (definition != null) {
                return definition.appendErasedDescription(sb);
            }
        }
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers() & 0xFFFFFF7F)) {
            sb.append(modifier.toString());
            sb.append(' ');
        }
        final List<ParameterDefinition> parameterTypes = this.getParameters();
        StringBuilder s = this.getReturnType().appendErasedDescription(sb);
        s.append(' ');
        s.append(this.getName());
        s.append('(');
        for (int i = 0, n = parameterTypes.size(); i < n; ++i) {
            if (i != 0) {
                s.append(", ");
            }
            s = parameterTypes.get(i).getParameterType().appendErasedDescription(s);
        }
        s.append(')');
        return s;
    }
    
    @Override
    public String toString() {
        return this.getSimpleDescription();
    }
    
    private MethodBody tryLoadBody() {
        if (Flags.testAny(this._flags, 70368744177664L)) {
            return null;
        }
        final CodeAttribute codeAttribute = SourceAttribute.find("Code", this._sourceAttributes);
        if (codeAttribute == null) {
            return null;
        }
        final int codeAttributeIndex = this._sourceAttributes.indexOf(codeAttribute);
        Buffer code = codeAttribute.getCode();
        ConstantPool constantPool = this._declaringType.getConstantPool();
        if (code == null) {
            final ITypeLoader typeLoader = this._declaringType.getTypeLoader();
            if (typeLoader == null) {
                this._flags |= 0x400000000000L;
                return null;
            }
            code = new Buffer();
            if (!typeLoader.tryLoadType(this._declaringType.getInternalName(), code)) {
                this._flags |= 0x400000000000L;
                return null;
            }
            final List<ExceptionTableEntry> exceptionTableEntries = codeAttribute.getExceptionTableEntries();
            final List<SourceAttribute> codeAttributes = codeAttribute.getAttributes();
            final CodeAttribute newCode = new CodeAttribute(codeAttribute.getLength(), codeAttribute.getMaxStack(), codeAttribute.getMaxLocals(), codeAttribute.getCodeOffset(), codeAttribute.getCodeSize(), code, exceptionTableEntries.toArray(new ExceptionTableEntry[exceptionTableEntries.size()]), codeAttributes.toArray(new SourceAttribute[codeAttributes.size()]));
            this._sourceAttributes.set(codeAttributeIndex, newCode);
            if (constantPool == null) {
                final long magic = code.readInt() & 0xFFFFFFFFL;
                assert magic == 0xCAFEBABEL;
                if (magic != 0xCAFEBABEL) {
                    this._flags |= 0x400000000000L;
                    return null;
                }
                code.readUnsignedShort();
                code.readUnsignedShort();
                constantPool = ConstantPool.read(code);
            }
        }
        final MetadataParser parser = new MetadataParser(this._declaringType);
        final IMetadataScope scope = new ClassFileReader.Scope(parser, this._declaringType, constantPool);
        final MethodBody body = new MethodReader(this, scope).readBody();
        this._body = new SoftReference<MethodBody>(body);
        this._sourceAttributes.set(codeAttributeIndex, codeAttribute);
        body.tryFreeze();
        return body;
    }
}
