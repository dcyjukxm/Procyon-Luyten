package com.strobel.assembler.metadata;

import com.strobel.core.*;
import com.strobel.assembler.metadata.signatures.*;
import com.strobel.assembler.ir.attributes.*;
import java.util.*;

public class CoreMetadataFactory implements MetadataFactory
{
    private final TypeDefinition _owner;
    private final IMetadataResolver _resolver;
    private final IGenericContext _scope;
    private final Stack<GenericParameter> _tempScope;
    
    private CoreMetadataFactory(final TypeDefinition owner, final IMetadataResolver resolver, final IGenericContext scope) {
        super();
        this._owner = owner;
        this._resolver = resolver;
        this._scope = scope;
        this._tempScope = new Stack<GenericParameter>();
    }
    
    public static CoreMetadataFactory make(final TypeDefinition owner, final IGenericContext scope) {
        return new CoreMetadataFactory(VerifyArgument.notNull(owner, "owner"), owner.getResolver(), scope);
    }
    
    public static CoreMetadataFactory make(final IMetadataResolver resolver, final IGenericContext scope) {
        return new CoreMetadataFactory(null, resolver, scope);
    }
    
    private IGenericContext getScope() {
        return this._scope;
    }
    
    @Override
    public GenericParameter makeTypeVariable(final String name, final FieldTypeSignature[] bounds) {
        final GenericParameter genericParameter = new GenericParameter(name);
        if (ArrayUtilities.isNullOrEmpty(bounds)) {
            return genericParameter;
        }
        this._tempScope.push(genericParameter);
        try {
            final TypeReference extendsBound = this.makeTypeBound(bounds);
            genericParameter.setExtendsBound(extendsBound);
            return genericParameter;
        }
        finally {
            this._tempScope.pop();
        }
    }
    
    @Override
    public WildcardType makeWildcard(final FieldTypeSignature superBound, final FieldTypeSignature extendsBound) {
        if (superBound != null && superBound != BottomSignature.make()) {
            return WildcardType.makeSuper(this.makeTypeBound(superBound));
        }
        if (extendsBound == null || (extendsBound instanceof SimpleClassTypeSignature && StringUtilities.equals("java.lang.Object", ((SimpleClassTypeSignature)extendsBound).getName()))) {
            return WildcardType.unbounded();
        }
        return WildcardType.makeExtends(this.makeTypeBound(extendsBound));
    }
    
    protected TypeReference makeTypeBound(final FieldTypeSignature... bounds) {
        Reifier reifier = null;
        if (ArrayUtilities.isNullOrEmpty(bounds)) {
            return null;
        }
        TypeReference baseType;
        if (bounds[0] != BottomSignature.make()) {
            reifier = Reifier.make(this);
            bounds[0].accept(reifier);
            baseType = reifier.getResult();
            assert baseType != null;
        }
        else {
            baseType = null;
        }
        if (bounds.length == 1) {
            return baseType;
        }
        if (reifier == null) {
            reifier = Reifier.make(this);
        }
        if (bounds.length != 2 || baseType != null) {
            final TypeReference[] it = new TypeReference[bounds.length - 1];
            for (int i = 0; i < it.length; ++i) {
                bounds[i + 1].accept(reifier);
                it[i] = reifier.getResult();
                assert it[i] != null;
            }
            final List<TypeReference> interfaceTypes = ArrayUtilities.asUnmodifiableList(it);
            return new CompoundTypeReference(baseType, interfaceTypes);
        }
        bounds[1].accept(reifier);
        final TypeReference singleInterface = reifier.getResult();
        assert singleInterface != null;
        return singleInterface;
    }
    
    @Override
    public TypeReference makeParameterizedType(final TypeReference declaration, final TypeReference owner, final TypeReference... typeArguments) {
        if (typeArguments.length == 0) {
            return declaration;
        }
        return declaration.makeGenericType(typeArguments);
    }
    
    @Override
    public GenericParameter findTypeVariable(final String name) {
        for (int i = this._tempScope.size() - 1; i >= 0; --i) {
            final GenericParameter genericParameter = this._tempScope.get(i);
            if (genericParameter != null && StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }
        final IGenericContext scope = this.getScope();
        if (scope != null) {
            return scope.findTypeVariable(name);
        }
        return null;
    }
    
    private InnerClassEntry findInnerClassEntry(final String name) {
        if (this._owner == null) {
            return null;
        }
        final String internalName = name.replace('.', '/');
        final SourceAttribute attribute = SourceAttribute.find("InnerClasses", this._owner.getSourceAttributes());
        if (attribute instanceof InnerClassesAttribute) {
            final List<InnerClassEntry> entries = ((InnerClassesAttribute)attribute).getEntries();
            for (final InnerClassEntry entry : entries) {
                if (StringUtilities.equals(entry.getInnerClassName(), internalName)) {
                    return entry;
                }
            }
        }
        return null;
    }
    
    @Override
    public TypeReference makeNamedType(final String name) {
        final int length = name.length();
        final InnerClassEntry entry = this.findInnerClassEntry(name);
        if (entry != null) {
            final String innerClassName = entry.getInnerClassName();
            final int packageEnd = innerClassName.lastIndexOf(47);
            final String shortName = StringUtilities.isNullOrEmpty(entry.getShortName()) ? null : entry.getShortName();
            TypeReference declaringType;
            if (!StringUtilities.isNullOrEmpty(entry.getOuterClassName())) {
                declaringType = this.makeNamedType(entry.getOuterClassName().replace('/', '.'));
            }
            else {
                int lastDollarIndex = name.lastIndexOf(36);
                while (lastDollarIndex >= 1 && lastDollarIndex < length && name.charAt(lastDollarIndex - 1) == '$') {
                    if (lastDollarIndex > 1) {
                        lastDollarIndex = name.lastIndexOf(lastDollarIndex, lastDollarIndex - 2);
                    }
                    else {
                        lastDollarIndex = -1;
                    }
                }
                if (lastDollarIndex == length - 1) {
                    lastDollarIndex = -1;
                }
                declaringType = this.makeNamedType(name.substring(0, lastDollarIndex).replace('/', '.'));
            }
            return new UnresolvedType(declaringType, (packageEnd < 0) ? innerClassName : innerClassName.substring(packageEnd + 1), shortName);
        }
        final int packageEnd2 = name.lastIndexOf(46);
        if (packageEnd2 < 0) {
            return new UnresolvedType("", name, null);
        }
        return new UnresolvedType((packageEnd2 < 0) ? "" : name.substring(0, packageEnd2), (packageEnd2 < 0) ? name : name.substring(packageEnd2 + 1), null);
    }
    
    @Override
    public TypeReference makeArrayType(final TypeReference componentType) {
        return componentType.makeArrayType();
    }
    
    @Override
    public TypeReference makeByte() {
        return BuiltinTypes.Byte;
    }
    
    @Override
    public TypeReference makeBoolean() {
        return BuiltinTypes.Boolean;
    }
    
    @Override
    public TypeReference makeShort() {
        return BuiltinTypes.Short;
    }
    
    @Override
    public TypeReference makeChar() {
        return BuiltinTypes.Character;
    }
    
    @Override
    public TypeReference makeInt() {
        return BuiltinTypes.Integer;
    }
    
    @Override
    public TypeReference makeLong() {
        return BuiltinTypes.Long;
    }
    
    @Override
    public TypeReference makeFloat() {
        return BuiltinTypes.Float;
    }
    
    @Override
    public TypeReference makeDouble() {
        return BuiltinTypes.Double;
    }
    
    @Override
    public TypeReference makeVoid() {
        return BuiltinTypes.Void;
    }
    
    @Override
    public IMethodSignature makeMethodSignature(final TypeReference returnType, final List<TypeReference> parameterTypes, final List<GenericParameter> genericParameters, final List<TypeReference> thrownTypes) {
        return new MethodSignature(parameterTypes, returnType, genericParameters, thrownTypes);
    }
    
    @Override
    public IClassSignature makeClassSignature(final TypeReference baseType, final List<TypeReference> interfaceTypes, final List<GenericParameter> genericParameters) {
        return new ClassSignature(baseType, interfaceTypes, genericParameters, null);
    }
    
    static /* synthetic */ IMetadataResolver access$0(final CoreMetadataFactory param_0) {
        return param_0._resolver;
    }
    
    private static final class ClassSignature implements IClassSignature
    {
        private final TypeReference _baseType;
        private final List<TypeReference> _interfaceTypes;
        private final List<GenericParameter> _genericParameters;
        
        private ClassSignature(final TypeReference baseType, final List<TypeReference> interfaceTypes, final List<GenericParameter> genericParameters) {
            super();
            this._baseType = VerifyArgument.notNull(baseType, "baseType");
            this._interfaceTypes = VerifyArgument.noNullElements(interfaceTypes, "interfaceTypes");
            this._genericParameters = VerifyArgument.noNullElements(genericParameters, "genericParameters");
        }
        
        @Override
        public TypeReference getBaseType() {
            return this._baseType;
        }
        
        @Override
        public List<TypeReference> getExplicitInterfaces() {
            return this._interfaceTypes;
        }
        
        @Override
        public boolean hasGenericParameters() {
            return !this._genericParameters.isEmpty();
        }
        
        @Override
        public boolean isGenericDefinition() {
            return false;
        }
        
        @Override
        public List<GenericParameter> getGenericParameters() {
            return this._genericParameters;
        }
    }
    
    private static final class MethodSignature implements IMethodSignature
    {
        private final List<ParameterDefinition> _parameters;
        private final TypeReference _returnType;
        private final List<GenericParameter> _genericParameters;
        private final List<TypeReference> _thrownTypes;
        
        MethodSignature(final List<TypeReference> parameterTypes, final TypeReference returnType, final List<GenericParameter> genericParameters, final List<TypeReference> thrownTypes) {
            super();
            VerifyArgument.notNull(parameterTypes, "parameterTypes");
            VerifyArgument.notNull(returnType, "returnType");
            VerifyArgument.notNull(genericParameters, "genericParameters");
            VerifyArgument.notNull(thrownTypes, "thrownTypes");
            final ParameterDefinition[] parameters = new ParameterDefinition[parameterTypes.size()];
            for (int i = 0, slot = 0, n = parameters.length; i < n; ++i, ++slot) {
                final TypeReference parameterType = parameterTypes.get(i);
                parameters[i] = new ParameterDefinition(slot, parameterType);
                if (parameterType.getSimpleType().isDoubleWord()) {
                    ++slot;
                }
            }
            this._parameters = ArrayUtilities.asUnmodifiableList(parameters);
            this._returnType = returnType;
            this._genericParameters = genericParameters;
            this._thrownTypes = thrownTypes;
        }
        
        @Override
        public boolean hasParameters() {
            return !this._parameters.isEmpty();
        }
        
        @Override
        public List<ParameterDefinition> getParameters() {
            return this._parameters;
        }
        
        @Override
        public TypeReference getReturnType() {
            return this._returnType;
        }
        
        @Override
        public List<TypeReference> getThrownTypes() {
            return this._thrownTypes;
        }
        
        @Override
        public boolean hasGenericParameters() {
            return !this._genericParameters.isEmpty();
        }
        
        @Override
        public boolean isGenericDefinition() {
            return !this._genericParameters.isEmpty();
        }
        
        @Override
        public List<GenericParameter> getGenericParameters() {
            return this._genericParameters;
        }
        
        @Override
        public GenericParameter findTypeVariable(final String name) {
            for (final GenericParameter genericParameter : this.getGenericParameters()) {
                if (StringUtilities.equals(genericParameter.getName(), name)) {
                    return genericParameter;
                }
            }
            return null;
        }
    }
    
    private final class UnresolvedType extends TypeReference
    {
        private final String _name;
        private final String _shortName;
        private final String _packageName;
        private final GenericParameterCollection _genericParameters;
        private String _fullName;
        private String _internalName;
        private String _signature;
        private String _erasedSignature;
        
        UnresolvedType(final TypeReference declaringType, final String name, final String shortName) {
            super();
            this._name = VerifyArgument.notNull(name, "name");
            this._shortName = shortName;
            this.setDeclaringType(VerifyArgument.notNull(declaringType, "declaringType"));
            this._packageName = declaringType.getPackageName();
            (this._genericParameters = new GenericParameterCollection(this)).freeze();
        }
        
        UnresolvedType(final String packageName, final String name, final String shortName) {
            super();
            this._packageName = VerifyArgument.notNull(packageName, "packageName");
            this._name = VerifyArgument.notNull(name, "name");
            this._shortName = shortName;
            (this._genericParameters = new GenericParameterCollection(this)).freeze();
        }
        
        UnresolvedType(final TypeReference declaringType, final String name, final String shortName, final List<GenericParameter> genericParameters) {
            super();
            this._name = VerifyArgument.notNull(name, "name");
            this._shortName = shortName;
            this.setDeclaringType(VerifyArgument.notNull(declaringType, "declaringType"));
            this._packageName = declaringType.getPackageName();
            this._genericParameters = new GenericParameterCollection(this);
            for (final GenericParameter genericParameter : genericParameters) {
                this._genericParameters.add(genericParameter);
            }
            this._genericParameters.freeze();
        }
        
        UnresolvedType(final String packageName, final String name, final String shortName, final List<GenericParameter> genericParameters) {
            super();
            this._packageName = VerifyArgument.notNull(packageName, "packageName");
            this._name = VerifyArgument.notNull(name, "name");
            this._shortName = shortName;
            this._genericParameters = new GenericParameterCollection(this);
            for (final GenericParameter genericParameter : genericParameters) {
                this._genericParameters.add(genericParameter);
            }
        }
        
        @Override
        public String getName() {
            return this._name;
        }
        
        @Override
        public String getPackageName() {
            return this._packageName;
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
        
        @Override
        public String getSimpleName() {
            return (this._shortName != null) ? this._shortName : this._name;
        }
        
        @Override
        public boolean isGenericDefinition() {
            return this.hasGenericParameters();
        }
        
        @Override
        public List<GenericParameter> getGenericParameters() {
            return this._genericParameters;
        }
        
        @Override
        public TypeReference makeGenericType(final List<? extends TypeReference> typeArguments) {
            VerifyArgument.noNullElementsAndNotEmpty(typeArguments, "typeArguments");
            return new UnresolvedGenericType(this, ArrayUtilities.asUnmodifiableList((TypeReference[])typeArguments.toArray((T[])new TypeReference[typeArguments.size()])));
        }
        
        @Override
        public TypeReference makeGenericType(final TypeReference... typeArguments) {
            VerifyArgument.noNullElementsAndNotEmpty(typeArguments, "typeArguments");
            return new UnresolvedGenericType(this, ArrayUtilities.asUnmodifiableList((TypeReference[])typeArguments.clone()));
        }
        
        @Override
        public TypeDefinition resolve() {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(this);
        }
        
        @Override
        public FieldDefinition resolve(final FieldReference field) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(field);
        }
        
        @Override
        public MethodDefinition resolve(final MethodReference method) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(method);
        }
        
        @Override
        public TypeDefinition resolve(final TypeReference type) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(type);
        }
    }
    
    private final class UnresolvedGenericType extends TypeReference implements IGenericInstance
    {
        private final TypeReference _genericDefinition;
        private final List<TypeReference> _typeParameters;
        private String _signature;
        
        UnresolvedGenericType(final TypeReference genericDefinition, final List<TypeReference> typeParameters) {
            super();
            this._genericDefinition = genericDefinition;
            this._typeParameters = typeParameters;
        }
        
        @Override
        public TypeReference getElementType() {
            return null;
        }
        
        @Override
        public <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
            return visitor.visitParameterizedType(this, parameter);
        }
        
        @Override
        public String getName() {
            return this._genericDefinition.getName();
        }
        
        @Override
        public String getPackageName() {
            return this._genericDefinition.getPackageName();
        }
        
        @Override
        public TypeReference getDeclaringType() {
            return this._genericDefinition.getDeclaringType();
        }
        
        @Override
        public String getSimpleName() {
            return this._genericDefinition.getSimpleName();
        }
        
        @Override
        public String getFullName() {
            return this._genericDefinition.getFullName();
        }
        
        @Override
        public String getInternalName() {
            return this._genericDefinition.getInternalName();
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
            return this._genericDefinition.getErasedSignature();
        }
        
        @Override
        public boolean isGenericDefinition() {
            return false;
        }
        
        @Override
        public boolean isGenericType() {
            return true;
        }
        
        @Override
        public List<GenericParameter> getGenericParameters() {
            if (!this._genericDefinition.isGenericDefinition()) {
                final TypeDefinition resolvedDefinition = this._genericDefinition.resolve();
                if (resolvedDefinition != null) {
                    return resolvedDefinition.getGenericParameters();
                }
            }
            return this._genericDefinition.getGenericParameters();
        }
        
        @Override
        public boolean hasTypeArguments() {
            return true;
        }
        
        @Override
        public List<TypeReference> getTypeArguments() {
            return this._typeParameters;
        }
        
        @Override
        public IGenericParameterProvider getGenericDefinition() {
            return this._genericDefinition;
        }
        
        @Override
        public TypeReference getUnderlyingType() {
            return this._genericDefinition;
        }
        
        @Override
        public TypeDefinition resolve() {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(this);
        }
        
        @Override
        public FieldDefinition resolve(final FieldReference field) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(field);
        }
        
        @Override
        public MethodDefinition resolve(final MethodReference method) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(method);
        }
        
        @Override
        public TypeDefinition resolve(final TypeReference type) {
            return CoreMetadataFactory.access$0(CoreMetadataFactory.this).resolve(type);
        }
    }
}
