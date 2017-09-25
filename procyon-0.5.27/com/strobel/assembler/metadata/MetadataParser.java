package com.strobel.assembler.metadata;

import java.util.concurrent.atomic.*;
import com.strobel.compilerservices.*;
import com.strobel.core.*;
import com.strobel.util.*;
import com.strobel.assembler.metadata.signatures.*;
import java.util.*;

public final class MetadataParser
{
    private final IMetadataResolver _resolver;
    private final SignatureParser _signatureParser;
    private final Stack<IGenericContext> _genericContexts;
    private final CoreMetadataFactory _factory;
    private final AtomicInteger _suppressResolveDepth;
    private static final TypeReference[] PRIMITIVE_TYPES;
    
    static {
        PRIMITIVE_TYPES = new TypeReference[16];
        RuntimeHelpers.ensureClassInitialized(MetadataSystem.class);
        final TypeReference[] allPrimitives = { BuiltinTypes.Boolean, BuiltinTypes.Byte, BuiltinTypes.Character, BuiltinTypes.Short, BuiltinTypes.Integer, BuiltinTypes.Long, BuiltinTypes.Float, BuiltinTypes.Double, BuiltinTypes.Void };
        TypeReference[] loc_1;
        for (int loc_0 = (loc_1 = allPrimitives).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final TypeReference t = loc_1[loc_2];
            MetadataParser.PRIMITIVE_TYPES[hashPrimitiveName(t.getName())] = t;
        }
    }
    
    public MetadataParser() {
        this(MetadataSystem.instance());
    }
    
    public MetadataParser(final IMetadataResolver resolver) {
        super();
        this._resolver = VerifyArgument.notNull(resolver, "resolver");
        this._signatureParser = SignatureParser.make();
        this._genericContexts = new Stack<IGenericContext>();
        this._factory = CoreMetadataFactory.make(resolver, new StackBasedGenericContext((StackBasedGenericContext)null));
        this._suppressResolveDepth = new AtomicInteger();
    }
    
    public MetadataParser(final TypeDefinition owner) {
        super();
        VerifyArgument.notNull(owner, "owner");
        this._resolver = ((owner.getResolver() != null) ? owner.getResolver() : MetadataSystem.instance());
        this._signatureParser = SignatureParser.make();
        this._genericContexts = new Stack<IGenericContext>();
        this._factory = CoreMetadataFactory.make(owner, new StackBasedGenericContext((StackBasedGenericContext)null));
        this._suppressResolveDepth = new AtomicInteger();
    }
    
    public final SafeCloseable suppressTypeResolution() {
        this._suppressResolveDepth.incrementAndGet();
        return new SafeCloseable() {
            @Override
            public void close() {
                MetadataParser.access$2(MetadataParser.this).decrementAndGet();
            }
        };
    }
    
    public final IMetadataResolver getResolver() {
        return this._resolver;
    }
    
    public void pushGenericContext(final IGenericContext context) {
        this._genericContexts.push(VerifyArgument.notNull(context, "context"));
    }
    
    public void popGenericContext() {
        this._genericContexts.pop();
    }
    
    public TypeReference parseTypeDescriptor(final String descriptor) {
        VerifyArgument.notNull(descriptor, "descriptor");
        if (descriptor.startsWith("[")) {
            return this.parseTypeSignature(descriptor);
        }
        return this.parseTypeSignature("L" + descriptor + ";");
    }
    
    public TypeReference parseTypeSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");
        final TypeSignature typeSignature = this._signatureParser.parseTypeSignature(signature);
        final Reifier reifier = Reifier.make(this._factory);
        typeSignature.accept(reifier);
        return reifier.getResult();
    }
    
    public FieldReference parseField(final TypeReference declaringType, final String name, final String signature) {
        VerifyArgument.notNull(declaringType, "declaringType");
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(signature, "signature");
        this.pushGenericContext(declaringType);
        try {
            return new UnresolvedField(declaringType, name, this.parseTypeSignature(signature));
        }
        finally {
            this.popGenericContext();
        }
    }
    
    public MethodReference parseMethod(final TypeReference declaringType, final String name, final String descriptor) {
        VerifyArgument.notNull(declaringType, "declaringType");
        VerifyArgument.notNull(name, "name");
        VerifyArgument.notNull(descriptor, "descriptor");
        this.pushGenericContext(declaringType);
        try {
            final IMethodSignature signature = this.parseMethodSignature(descriptor);
            return this.lookupMethod(declaringType, name, signature);
        }
        finally {
            this.popGenericContext();
        }
    }
    
    public TypeReference lookupType(final String packageName, final String typeName) {
        String dottedName;
        if (StringUtilities.isNullOrEmpty(packageName)) {
            dottedName = typeName;
        }
        else {
            dottedName = String.valueOf(packageName) + "." + typeName;
        }
        final TypeReference reference = this._factory.makeNamedType(dottedName);
        if (this._suppressResolveDepth.get() > 0) {
            return reference;
        }
        return reference;
    }
    
    protected TypeReference lookupTypeVariable(final String name) {
        for (int i = 0, n = this._genericContexts.size(); i < n; ++i) {
            final IGenericContext context = this._genericContexts.get(i);
            final TypeReference typeVariable = context.findTypeVariable(name);
            if (typeVariable != null) {
                return typeVariable;
            }
        }
        if (this._resolver instanceof IGenericContext) {
            return ((IGenericContext)this._resolver).findTypeVariable(name);
        }
        return null;
    }
    
    public IMethodSignature parseMethodSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");
        final MethodTypeSignature methodTypeSignature = this._signatureParser.parseMethodSignature(signature);
        final Reifier reifier = Reifier.make(this._factory);
        final ReturnType returnTypeSignature = methodTypeSignature.getReturnType();
        final TypeSignature[] parameterTypeSignatures = methodTypeSignature.getParameterTypes();
        final FormalTypeParameter[] genericParameterSignatures = methodTypeSignature.getFormalTypeParameters();
        final FieldTypeSignature[] thrownTypeSignatures = methodTypeSignature.getExceptionTypes();
        boolean needPopGenericContext = false;
        try {
            List<GenericParameter> genericParameters;
            if (ArrayUtilities.isNullOrEmpty(genericParameterSignatures)) {
                genericParameters = Collections.emptyList();
            }
            else {
                final GenericParameter[] gp = new GenericParameter[genericParameterSignatures.length];
                this.pushGenericContext(new IGenericContext() {
                    @Override
                    public GenericParameter findTypeVariable(final String name) {
                        GenericParameter[] loc_1;
                        for (int loc_0 = (loc_1 = gp).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                            final GenericParameter g = loc_1[loc_2];
                            if (g == null) {
                                break;
                            }
                            if (StringUtilities.equals(g.getName(), name)) {
                                return g;
                            }
                        }
                        return null;
                    }
                });
                needPopGenericContext = true;
                for (int i = 0; i < gp.length; ++i) {
                    gp[i] = this._factory.makeTypeVariable(genericParameterSignatures[i].getName(), EmptyArrayCache.fromElementType(FieldTypeSignature.class));
                }
                genericParameters = ArrayUtilities.asUnmodifiableList(gp);
                for (int i = 0; i < gp.length; ++i) {
                    final FieldTypeSignature[] bounds = genericParameterSignatures[i].getBounds();
                    if (!ArrayUtilities.isNullOrEmpty(bounds)) {
                        gp[i].setExtendsBound(this._factory.makeTypeBound(bounds));
                    }
                }
            }
            returnTypeSignature.accept(reifier);
            final TypeReference returnType = reifier.getResult();
            List<TypeReference> parameterTypes;
            if (ArrayUtilities.isNullOrEmpty(parameterTypeSignatures)) {
                parameterTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] pt = new TypeReference[parameterTypeSignatures.length];
                for (int i = 0; i < pt.length; ++i) {
                    parameterTypeSignatures[i].accept(reifier);
                    pt[i] = reifier.getResult();
                }
                parameterTypes = ArrayUtilities.asUnmodifiableList(pt);
            }
            List<TypeReference> thrownTypes;
            if (ArrayUtilities.isNullOrEmpty(thrownTypeSignatures)) {
                thrownTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] tt = new TypeReference[thrownTypeSignatures.length];
                for (int i = 0; i < tt.length; ++i) {
                    thrownTypeSignatures[i].accept(reifier);
                    tt[i] = reifier.getResult();
                }
                thrownTypes = ArrayUtilities.asUnmodifiableList(tt);
            }
            return this._factory.makeMethodSignature(returnType, parameterTypes, genericParameters, thrownTypes);
        }
        finally {
            if (needPopGenericContext) {
                this.popGenericContext();
            }
        }
    }
    
    public IClassSignature parseClassSignature(final String signature) {
        VerifyArgument.notNull(signature, "signature");
        final ClassSignature classSignature = this._signatureParser.parseClassSignature(signature);
        final Reifier reifier = Reifier.make(this._factory);
        final ClassTypeSignature baseTypeSignature = classSignature.getSuperType();
        final ClassTypeSignature[] interfaceTypeSignatures = classSignature.getInterfaces();
        final FormalTypeParameter[] genericParameterSignatures = classSignature.getFormalTypeParameters();
        boolean needPopGenericContext = false;
        try {
            List<GenericParameter> genericParameters;
            if (ArrayUtilities.isNullOrEmpty(genericParameterSignatures)) {
                genericParameters = Collections.emptyList();
            }
            else {
                final GenericParameter[] gp = new GenericParameter[genericParameterSignatures.length];
                this.pushGenericContext(new IGenericContext() {
                    @Override
                    public GenericParameter findTypeVariable(final String name) {
                        GenericParameter[] loc_1;
                        for (int loc_0 = (loc_1 = gp).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                            final GenericParameter g = loc_1[loc_2];
                            if (g == null) {
                                break;
                            }
                            if (StringUtilities.equals(g.getName(), name)) {
                                return g;
                            }
                        }
                        return null;
                    }
                });
                needPopGenericContext = true;
                for (int i = 0; i < gp.length; ++i) {
                    gp[i] = this._factory.makeTypeVariable(genericParameterSignatures[i].getName(), EmptyArrayCache.fromElementType(FieldTypeSignature.class));
                }
                genericParameters = ArrayUtilities.asUnmodifiableList(gp);
                for (int i = 0; i < gp.length; ++i) {
                    final FieldTypeSignature[] bounds = genericParameterSignatures[i].getBounds();
                    if (!ArrayUtilities.isNullOrEmpty(bounds)) {
                        gp[i].setExtendsBound(this._factory.makeTypeBound(bounds));
                    }
                }
            }
            baseTypeSignature.accept(reifier);
            final TypeReference baseType = reifier.getResult();
            List<TypeReference> interfaceTypes;
            if (ArrayUtilities.isNullOrEmpty(interfaceTypeSignatures)) {
                interfaceTypes = Collections.emptyList();
            }
            else {
                final TypeReference[] it = new TypeReference[interfaceTypeSignatures.length];
                for (int i = 0; i < it.length; ++i) {
                    interfaceTypeSignatures[i].accept(reifier);
                    it[i] = reifier.getResult();
                }
                interfaceTypes = ArrayUtilities.asUnmodifiableList(it);
            }
            return this._factory.makeClassSignature(baseType, interfaceTypes, genericParameters);
        }
        finally {
            if (needPopGenericContext) {
                this.popGenericContext();
            }
        }
    }
    
    protected MethodReference lookupMethod(final TypeReference declaringType, final String name, final IMethodSignature signature) {
        final MethodReference reference = new UnresolvedMethod(declaringType, name, signature);
        if (this._suppressResolveDepth.get() > 0) {
            return reference;
        }
        return reference;
    }
    
    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % '\u0010';
    }
    
    static /* synthetic */ Stack access$0(final MetadataParser param_0) {
        return param_0._genericContexts;
    }
    
    static /* synthetic */ IMetadataResolver access$1(final MetadataParser param_0) {
        return param_0._resolver;
    }
    
    static /* synthetic */ AtomicInteger access$2(final MetadataParser param_0) {
        return param_0._suppressResolveDepth;
    }
    
    private final class StackBasedGenericContext implements IGenericContext
    {
        @Override
        public GenericParameter findTypeVariable(final String name) {
            for (int i = MetadataParser.access$0(MetadataParser.this).size() - 1; i >= 0; --i) {
                final IGenericContext context = (IGenericContext)MetadataParser.access$0(MetadataParser.this).get(i);
                final GenericParameter typeVariable = context.findTypeVariable(name);
                if (typeVariable != null) {
                    return typeVariable;
                }
            }
            if (MetadataParser.access$1(MetadataParser.this) instanceof IGenericContext) {
                return ((IGenericContext)MetadataParser.access$1(MetadataParser.this)).findTypeVariable(name);
            }
            return null;
        }
    }
    
    private final class UnresolvedMethod extends MethodReference
    {
        private final TypeReference _declaringType;
        private final String _name;
        private final IMethodSignature _signature;
        private final List<GenericParameter> _genericParameters;
        
        UnresolvedMethod(final TypeReference declaringType, final String name, final IMethodSignature signature) {
            super();
            this._declaringType = VerifyArgument.notNull(declaringType, "declaringType");
            this._name = VerifyArgument.notNull(name, "name");
            this._signature = VerifyArgument.notNull(signature, "signature");
            if (this._signature.hasGenericParameters()) {
                final GenericParameterCollection genericParameters = new GenericParameterCollection(this);
                for (final GenericParameter genericParameter : this._signature.getGenericParameters()) {
                    genericParameters.add(genericParameter);
                }
                genericParameters.freeze(false);
                this._genericParameters = genericParameters;
            }
            else {
                this._genericParameters = Collections.emptyList();
            }
        }
        
        @Override
        public String getName() {
            return this._name;
        }
        
        @Override
        public TypeReference getReturnType() {
            return this._signature.getReturnType();
        }
        
        @Override
        public List<ParameterDefinition> getParameters() {
            return this._signature.getParameters();
        }
        
        @Override
        public TypeReference getDeclaringType() {
            return this._declaringType;
        }
        
        @Override
        public List<GenericParameter> getGenericParameters() {
            return this._genericParameters;
        }
        
        @Override
        public List<TypeReference> getThrownTypes() {
            return this._signature.getThrownTypes();
        }
    }
    
    private final class UnresolvedField extends FieldReference
    {
        private final TypeReference _declaringType;
        private final String _name;
        private final TypeReference _fieldType;
        
        UnresolvedField(final TypeReference declaringType, final String name, final TypeReference fieldType) {
            super();
            this._declaringType = VerifyArgument.notNull(declaringType, "declaringType");
            this._name = VerifyArgument.notNull(name, "name");
            this._fieldType = VerifyArgument.notNull(fieldType, "fieldType");
        }
        
        @Override
        public String getName() {
            return this._name;
        }
        
        @Override
        public TypeReference getDeclaringType() {
            return this._declaringType;
        }
        
        @Override
        public TypeReference getFieldType() {
            return this._fieldType;
        }
        
        @Override
        protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
            if (fullName) {
                final TypeReference declaringType = this.getDeclaringType();
                if (declaringType != null) {
                    return declaringType.appendName(sb, true, false).append('.').append(this._name);
                }
            }
            return sb.append(this._name);
        }
    }
}
