package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.assembler.metadata.annotations.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.ir.*;
import com.strobel.core.*;
import java.util.*;
import javax.lang.model.element.*;

public class TypeDefinition extends TypeReference implements IMemberDefinition
{
    private final GenericParameterCollection _genericParameters;
    private final Collection<TypeDefinition> _declaredTypes;
    private final Collection<FieldDefinition> _declaredFields;
    private final Collection<MethodDefinition> _declaredMethods;
    private final Collection<TypeReference> _explicitInterfaces;
    private final Collection<CustomAnnotation> _customAnnotations;
    private final Collection<SourceAttribute> _sourceAttributes;
    private final List<GenericParameter> _genericParametersView;
    private final List<TypeDefinition> _declaredTypesView;
    private final List<FieldDefinition> _declaredFieldsView;
    private final List<MethodDefinition> _declaredMethodsView;
    private final List<TypeReference> _explicitInterfacesView;
    private final List<CustomAnnotation> _customAnnotationsView;
    private final List<SourceAttribute> _sourceAttributesView;
    private IMetadataResolver _resolver;
    private String _simpleName;
    private String _packageName;
    private String _internalName;
    private String _fullName;
    private String _signature;
    private String _erasedSignature;
    private TypeReference _baseType;
    private long _flags;
    private int _compilerVersion;
    private List<Enum> _enumConstants;
    private TypeReference _rawType;
    private MethodReference _declaringMethod;
    private ConstantPool _constantPool;
    private ITypeLoader _typeLoader;
    
    public TypeDefinition() {
        super();
        this._genericParameters = new GenericParameterCollection(this);
        this._declaredTypes = new Collection<TypeDefinition>();
        this._declaredFields = new Collection<FieldDefinition>();
        this._declaredMethods = new Collection<MethodDefinition>();
        this._explicitInterfaces = new Collection<TypeReference>();
        this._customAnnotations = new Collection<CustomAnnotation>();
        this._sourceAttributes = new Collection<SourceAttribute>();
        this._genericParametersView = Collections.unmodifiableList((List<? extends GenericParameter>)this._genericParameters);
        this._declaredTypesView = Collections.unmodifiableList((List<? extends TypeDefinition>)this._declaredTypes);
        this._declaredFieldsView = Collections.unmodifiableList((List<? extends FieldDefinition>)this._declaredFields);
        this._declaredMethodsView = Collections.unmodifiableList((List<? extends MethodDefinition>)this._declaredMethods);
        this._explicitInterfacesView = Collections.unmodifiableList((List<? extends TypeReference>)this._explicitInterfaces);
        this._customAnnotationsView = Collections.unmodifiableList((List<? extends CustomAnnotation>)this._customAnnotations);
        this._sourceAttributesView = Collections.unmodifiableList((List<? extends SourceAttribute>)this._sourceAttributes);
    }
    
    public TypeDefinition(final IMetadataResolver resolver) {
        this();
        this._resolver = VerifyArgument.notNull(resolver, "resolver");
    }
    
    final ITypeLoader getTypeLoader() {
        return this._typeLoader;
    }
    
    final void setTypeLoader(final ITypeLoader typeLoader) {
        this._typeLoader = typeLoader;
    }
    
    public final int getCompilerMajorVersion() {
        return this._compilerVersion >>> 16;
    }
    
    public final int getCompilerMinorVersion() {
        return this._compilerVersion & 0xFFFF;
    }
    
    public final ConstantPool getConstantPool() {
        return this._constantPool;
    }
    
    protected final void setConstantPool(final ConstantPool constantPool) {
        this._constantPool = constantPool;
    }
    
    protected final void setCompilerVersion(final int majorVersion, final int minorVersion) {
        this._compilerVersion = ((majorVersion & 0xFFFF) << 16 | (minorVersion & 0xFFFF));
    }
    
    public final IMetadataResolver getResolver() {
        return this._resolver;
    }
    
    protected final void setResolver(final IMetadataResolver resolver) {
        this._resolver = resolver;
    }
    
    @Override
    public String getPackageName() {
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType != null) {
            return declaringType.getPackageName();
        }
        return (this._packageName != null) ? this._packageName : "";
    }
    
    @Override
    public String getSimpleName() {
        return (this._simpleName != null) ? this._simpleName : this.getName();
    }
    
    protected final void setSimpleName(final String simpleName) {
        this._simpleName = simpleName;
    }
    
    protected void setPackageName(final String packageName) {
        this._packageName = packageName;
        this._fullName = null;
        this._internalName = null;
    }
    
    @Override
    public String getFullName() {
        if (this._fullName == null) {
            this._fullName = super.getFullName();
        }
        return this._fullName;
    }
    
    @Override
    public String getErasedSignature() {
        if (this._erasedSignature == null) {
            this._erasedSignature = super.getErasedSignature();
        }
        return this._erasedSignature;
    }
    
    @Override
    public String getSignature() {
        if (this._signature == null) {
            this._signature = super.getSignature();
        }
        return this._signature;
    }
    
    @Override
    public String getInternalName() {
        if (this._internalName == null) {
            this._internalName = super.getInternalName();
        }
        return this._internalName;
    }
    
    @Override
    public <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitClassType(this, parameter);
    }
    
    public final MethodReference getDeclaringMethod() {
        return this._declaringMethod;
    }
    
    protected final void setDeclaringMethod(final MethodReference declaringMethod) {
        this._declaringMethod = declaringMethod;
    }
    
    public final TypeReference getBaseType() {
        return this._baseType;
    }
    
    protected final void setBaseType(final TypeReference baseType) {
        this._baseType = baseType;
    }
    
    public final List<Enum> getEnumConstants() {
        if (this.isEnum()) {
            return (this._enumConstants != null) ? this._enumConstants : Collections.emptyList();
        }
        throw Error.notEnumType(this);
    }
    
    protected final void setEnumConstants(final Enum... values) {
        VerifyArgument.notNull(values, "values");
        this._enumConstants = ((values.length == 0) ? null : ArrayUtilities.asUnmodifiableList((Enum[])values));
    }
    
    public final List<TypeReference> getExplicitInterfaces() {
        return this._explicitInterfacesView;
    }
    
    @Override
    public final List<CustomAnnotation> getAnnotations() {
        return this._customAnnotationsView;
    }
    
    public final List<SourceAttribute> getSourceAttributes() {
        return this._sourceAttributesView;
    }
    
    @Override
    public final List<GenericParameter> getGenericParameters() {
        return this._genericParametersView;
    }
    
    @Override
    public TypeReference getRawType() {
        if (this.isGenericType()) {
            if (this._rawType == null) {
                synchronized (this) {
                    if (this._rawType == null) {
                        this._rawType = new RawType(this);
                    }
                }
            }
            return this._rawType;
        }
        return this;
    }
    
    @Override
    public GenericParameter findTypeVariable(final String name) {
        for (final GenericParameter genericParameter : this.getGenericParameters()) {
            if (StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }
        final MethodReference declaringMethod = this.getDeclaringMethod();
        if (declaringMethod != null) {
            return declaringMethod.findTypeVariable(name);
        }
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType != null && !this.isStatic()) {
            return declaringType.findTypeVariable(name);
        }
        return null;
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        if (fullName && dottedName && this.isNested() && !this.isAnonymous() && this._simpleName != null) {
            return this.getDeclaringType().appendName(sb, true, true).append('.').append(this._simpleName);
        }
        return super.appendName(sb, fullName, dottedName);
    }
    
    protected final GenericParameterCollection getGenericParametersInternal() {
        return this._genericParameters;
    }
    
    protected final Collection<TypeDefinition> getDeclaredTypesInternal() {
        return this._declaredTypes;
    }
    
    protected final Collection<FieldDefinition> getDeclaredFieldsInternal() {
        return this._declaredFields;
    }
    
    protected final Collection<MethodDefinition> getDeclaredMethodsInternal() {
        return this._declaredMethods;
    }
    
    protected final Collection<TypeReference> getExplicitInterfacesInternal() {
        return this._explicitInterfaces;
    }
    
    protected final Collection<CustomAnnotation> getAnnotationsInternal() {
        return this._customAnnotations;
    }
    
    protected final Collection<SourceAttribute> getSourceAttributesInternal() {
        return this._sourceAttributes;
    }
    
    @Override
    public TypeDefinition resolve() {
        return this;
    }
    
    @Override
    public final long getFlags() {
        return this._flags;
    }
    
    protected final void setFlags(final long flags) {
        this._flags = flags;
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
    public JvmType getSimpleType() {
        return JvmType.Object;
    }
    
    public final boolean isAnnotation() {
        return this.isInterface() && Flags.testAny(this.getFlags(), 8192L);
    }
    
    public final boolean isClass() {
        return !this.isPrimitive() && !this.isInterface() && !this.isEnum();
    }
    
    public final boolean isInterface() {
        return Flags.testAny(this.getFlags(), 512L);
    }
    
    public final boolean isEnum() {
        return Flags.testAny(this.getFlags(), 16384L);
    }
    
    public final boolean isAnonymous() {
        return Flags.testAny(this.getFlags(), 17592186044416L);
    }
    
    public final boolean isInnerClass() {
        return this.getDeclaringType() != null;
    }
    
    public final boolean isLocalClass() {
        return this.getDeclaringMethod() != null;
    }
    
    @Override
    public boolean isNested() {
        return this.isInnerClass() || this.isLocalClass();
    }
    
    @Override
    public boolean isArray() {
        return this.getSimpleType() == JvmType.Array;
    }
    
    @Override
    public boolean isPrimitive() {
        return false;
    }
    
    @Override
    public final boolean isDefinition() {
        return true;
    }
    
    public final List<FieldDefinition> getDeclaredFields() {
        return this._declaredFieldsView;
    }
    
    public final List<MethodDefinition> getDeclaredMethods() {
        return this._declaredMethodsView;
    }
    
    public final List<TypeDefinition> getDeclaredTypes() {
        return this._declaredTypesView;
    }
    
    @Override
    public boolean isCompoundType() {
        return Flags.testAny(this.getFlags(), 16777216L);
    }
    
    @Override
    protected StringBuilder appendDescription(final StringBuilder sb) {
        for (final Modifier modifier : Flags.asModifierSet(this.getModifiers() & 0xFFFFFF7F)) {
            sb.append(modifier.toString());
            sb.append(' ');
        }
        if (this.isEnum()) {
            sb.append("enum ");
        }
        else if (this.isInterface()) {
            sb.append("interface ");
            if (this.isAnnotation()) {
                sb.append('@');
            }
        }
        else {
            sb.append("class ");
        }
        StringBuilder s = super.appendDescription(sb);
        final TypeReference baseType = this.getBaseType();
        if (baseType != null) {
            s.append(" extends ");
            s = baseType.appendBriefDescription(s);
        }
        final List<TypeReference> interfaces = this.getExplicitInterfaces();
        final int interfaceCount = interfaces.size();
        if (interfaceCount > 0) {
            s.append(" implements ");
            for (int i = 0; i < interfaceCount; ++i) {
                if (i != 0) {
                    s.append(",");
                }
                s = interfaces.get(i).appendBriefDescription(s);
            }
        }
        return s;
    }
    
    @Override
    protected StringBuilder appendGenericSignature(final StringBuilder sb) {
        StringBuilder s = super.appendGenericSignature(sb);
        final TypeReference baseType = this.getBaseType();
        final List<TypeReference> interfaces = this.getExplicitInterfaces();
        if (baseType == null) {
            if (interfaces.isEmpty()) {
                s = BuiltinTypes.Object.appendSignature(s);
            }
        }
        else {
            s = baseType.appendSignature(s);
        }
        for (final TypeReference interfaceType : interfaces) {
            s = interfaceType.appendSignature(s);
        }
        return s;
    }
}
