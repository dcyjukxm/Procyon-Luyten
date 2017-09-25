package com.strobel.decompiler.languages.java.ast;

import com.strobel.decompiler.languages.java.ast.transforms.*;
import javax.lang.model.element.*;
import java.lang.ref.*;
import com.strobel.assembler.ir.attributes.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.decompiler.ast.*;
import com.strobel.decompiler.languages.*;
import com.strobel.decompiler.languages.java.*;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.*;
import com.strobel.util.*;
import com.strobel.assembler.metadata.annotations.*;

public final class AstBuilder
{
    private final DecompilerContext _context;
    private final CompilationUnit _compileUnit;
    private final Map<String, Reference<TypeDeclaration>> _typeDeclarations;
    private final Map<String, String> _unqualifiedTypeNames;
    private final TextNode _packagePlaceholder;
    private boolean _decompileMethodBodies;
    private boolean _haveTransformationsRun;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType;
    
    public AstBuilder(final DecompilerContext context) {
        super();
        this._compileUnit = new CompilationUnit();
        this._typeDeclarations = new LinkedHashMap<String, Reference<TypeDeclaration>>();
        this._unqualifiedTypeNames = new LinkedHashMap<String, String>();
        this._decompileMethodBodies = true;
        this._context = VerifyArgument.notNull(context, "context");
        final String headerText = context.getSettings().getOutputFileHeaderText();
        if (!StringUtilities.isNullOrWhitespace(headerText)) {
            final List<String> lines = StringUtilities.split(headerText, false, '\n', new char[0]);
            for (final String line : lines) {
                this._compileUnit.addChild(new Comment(" " + line.trim(), CommentType.SingleLine), Roles.COMMENT);
            }
            this._compileUnit.addChild(new UnixNewLine(), Roles.NEW_LINE);
        }
        this._packagePlaceholder = new TextNode();
        this._compileUnit.addChild(this._packagePlaceholder, Roles.TEXT);
        if (this._context.getUserData(Keys.AST_BUILDER) == null) {
            this._context.putUserData(Keys.AST_BUILDER, this);
        }
    }
    
    final DecompilerContext getContext() {
        return this._context;
    }
    
    public final boolean getDecompileMethodBodies() {
        return this._decompileMethodBodies;
    }
    
    public final void setDecompileMethodBodies(final boolean decompileMethodBodies) {
        this._decompileMethodBodies = decompileMethodBodies;
    }
    
    public final CompilationUnit getCompilationUnit() {
        return this._compileUnit;
    }
    
    public final void runTransformations() {
        this.runTransformations(null);
    }
    
    public final void runTransformations(final Predicate<IAstTransform> transformAbortCondition) {
        TransformationPipeline.runTransformationsUntil(this._compileUnit, transformAbortCondition, this._context);
        this._compileUnit.acceptVisitor((IAstVisitor<? super Object, ?>)new InsertParenthesesVisitor(), (Object)null);
        this._haveTransformationsRun = true;
    }
    
    public final void addType(final TypeDefinition type) {
        final TypeDeclaration astType = this.createType(type);
        final String packageName = type.getPackageName();
        if (this._compileUnit.getPackage().isNull() && !StringUtilities.isNullOrWhitespace(packageName)) {
            this._compileUnit.insertChildBefore(this._packagePlaceholder, new PackageDeclaration(packageName), Roles.PACKAGE);
            this._packagePlaceholder.remove();
        }
        this._compileUnit.addChild(astType, CompilationUnit.MEMBER_ROLE);
    }
    
    public final TypeDeclaration createType(final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        final Reference<TypeDeclaration> existingDeclaration = this._typeDeclarations.get(type.getInternalName());
        final TypeDeclaration d;
        if (existingDeclaration != null && (d = existingDeclaration.get()) != null) {
            return d;
        }
        return this.createTypeNoCache(type);
    }
    
    protected final TypeDeclaration createTypeNoCache(final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        final TypeDefinition oldCurrentType = this._context.getCurrentType();
        this._context.setCurrentType(type);
        try {
            return this.createTypeCore(type);
        }
        finally {
            this._context.setCurrentType(oldCurrentType);
        }
    }
    
    public AstType convertType(final TypeReference type) {
        return this.convertType(type, new ConvertTypeOptions());
    }
    
    public AstType convertType(final TypeReference type, final ConvertTypeOptions options) {
        return this.convertType(type, new MutableInteger(0), options);
    }
    
    public final List<ParameterDeclaration> createParameters(final Iterable<ParameterDefinition> parameters) {
        final List<ParameterDeclaration> declarations = new ArrayList<ParameterDeclaration>();
        for (final ParameterDefinition p : parameters) {
            final TypeReference type = p.getParameterType();
            final AstType astType = this.convertType(type);
            final ParameterDeclaration d = new ParameterDeclaration(p.getName(), astType);
            d.putUserData(Keys.PARAMETER_DEFINITION, p);
            for (final CustomAnnotation annotation : p.getAnnotations()) {
                d.getAnnotations().add(this.createAnnotation(annotation));
            }
            declarations.add(d);
            if (p.isFinal()) {
                EntityDeclaration.addModifier(d, Modifier.FINAL);
            }
        }
        return Collections.unmodifiableList(declarations);
    }
    
    final AstType convertType(final TypeReference type, final MutableInteger typeIndex, final ConvertTypeOptions options) {
        if (type == null) {
            return AstType.NULL;
        }
        if (type.isArray()) {
            return this.convertType(type.getElementType(), typeIndex.increment(), options).makeArrayType();
        }
        if (type.isGenericParameter()) {
            final SimpleType simpleType = new SimpleType(type.getSimpleName());
            simpleType.putUserData(Keys.TYPE_REFERENCE, type);
            return simpleType;
        }
        if (type.isPrimitive()) {
            final SimpleType simpleType = new SimpleType(type.getSimpleName());
            simpleType.putUserData(Keys.TYPE_REFERENCE, type.resolve());
            return simpleType;
        }
        if (type.isWildcardType()) {
            if (options.getAllowWildcards()) {
                final WildcardType wildcardType = new WildcardType();
                if (type.hasExtendsBound()) {
                    wildcardType.addChild(this.convertType(type.getExtendsBound()), Roles.EXTENDS_BOUND);
                }
                else if (type.hasSuperBound()) {
                    wildcardType.addChild(this.convertType(type.getSuperBound()), Roles.SUPER_BOUND);
                }
                wildcardType.putUserData(Keys.TYPE_REFERENCE, type);
                return wildcardType;
            }
            if (type.hasExtendsBound()) {
                return this.convertType(type.getExtendsBound(), options);
            }
            return this.convertType(BuiltinTypes.Object, options);
        }
        else {
            final boolean includeTypeArguments = options == null || options.getIncludeTypeArguments();
            final boolean includeTypeParameterDefinitions = options == null || options.getIncludeTypeParameterDefinitions();
            final boolean allowWildcards = options == null || options.getAllowWildcards();
            if (type instanceof IGenericInstance && includeTypeArguments) {
                final IGenericInstance genericInstance = (IGenericInstance)type;
                if (options != null) {
                    options.setIncludeTypeParameterDefinitions(false);
                }
                AstType baseType;
                try {
                    baseType = this.convertType((TypeReference)genericInstance.getGenericDefinition(), typeIndex.increment(), options);
                }
                finally {
                    if (options != null) {
                        options.setIncludeTypeParameterDefinitions(includeTypeParameterDefinitions);
                    }
                }
                if (options != null) {
                    options.setIncludeTypeParameterDefinitions(includeTypeParameterDefinitions);
                }
                if (options != null) {
                    options.setAllowWildcards(true);
                }
                final List<AstType> typeArguments = new ArrayList<AstType>();
                try {
                    for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                        typeArguments.add(this.convertType(typeArgument, typeIndex.increment(), options));
                    }
                }
                finally {
                    if (options != null) {
                        options.setAllowWildcards(allowWildcards);
                    }
                }
                if (options != null) {
                    options.setAllowWildcards(allowWildcards);
                }
                applyTypeArguments(baseType, typeArguments);
                baseType.putUserData(Keys.TYPE_REFERENCE, type);
                return baseType;
            }
            String name = null;
            final PackageDeclaration packageDeclaration = this._compileUnit.getPackage();
            final TypeDefinition resolvedType = type.resolve();
            final TypeReference nameSource = (resolvedType != null) ? resolvedType : type;
            if (options == null || options.getIncludePackage()) {
                final String packageName = nameSource.getPackageName();
                name = (StringUtilities.isNullOrEmpty(packageName) ? nameSource.getSimpleName() : (String.valueOf(packageName) + "." + nameSource.getSimpleName()));
            }
            else {
                if (packageDeclaration != null && StringUtilities.equals(packageDeclaration.getName(), nameSource.getPackageName())) {
                    final String unqualifiedName = name = nameSource.getSimpleName();
                }
                String unqualifiedName;
                TypeReference typeToImport;
                if (nameSource.isNested()) {
                    unqualifiedName = nameSource.getSimpleName();
                    TypeReference current = nameSource;
                    while (current.isNested()) {
                        current = current.getDeclaringType();
                        if (this.isContextWithinType(current)) {
                            break;
                        }
                        unqualifiedName = String.valueOf(current.getSimpleName()) + "." + unqualifiedName;
                    }
                    name = unqualifiedName;
                    typeToImport = current;
                }
                else {
                    typeToImport = nameSource;
                    unqualifiedName = nameSource.getSimpleName();
                }
                if (options.getAddImports() && !this._typeDeclarations.containsKey(typeToImport.getInternalName())) {
                    String importedName = this._unqualifiedTypeNames.get(typeToImport.getSimpleName());
                    if (importedName == null) {
                        final SimpleType importedType = new SimpleType(typeToImport.getFullName());
                        importedType.putUserData(Keys.TYPE_REFERENCE, typeToImport);
                        if (packageDeclaration != null) {
                            this._compileUnit.insertChildAfter(packageDeclaration, new ImportDeclaration(importedType), CompilationUnit.IMPORT_ROLE);
                        }
                        else {
                            this._compileUnit.getImports().add(new ImportDeclaration(importedType));
                        }
                        this._unqualifiedTypeNames.put(typeToImport.getSimpleName(), typeToImport.getFullName());
                        importedName = typeToImport.getFullName();
                    }
                    if (name == null) {
                        if (importedName.equals(typeToImport.getFullName())) {
                            name = unqualifiedName;
                        }
                        else {
                            final String packageName2 = nameSource.getPackageName();
                            name = (StringUtilities.isNullOrEmpty(packageName2) ? nameSource.getSimpleName() : (String.valueOf(packageName2) + "." + nameSource.getSimpleName()));
                        }
                    }
                }
                else if (name != null) {
                    name = nameSource.getSimpleName();
                }
            }
            final SimpleType astType = new SimpleType(name);
            astType.putUserData(Keys.TYPE_REFERENCE, type);
            return astType;
        }
    }
    
    private boolean isContextWithinType(final TypeReference type) {
        TypeReference current;
        for (TypeReference scope = current = this._context.getCurrentType(); current != null; current = current.getDeclaringType()) {
            if (MetadataResolver.areEquivalent(current, type)) {
                return true;
            }
            final TypeDefinition resolved = current.resolve();
            if (resolved != null) {
                TypeDefinition resolvedBaseType;
                for (TypeReference baseType = resolved.getBaseType(); baseType != null; baseType = ((resolvedBaseType != null) ? resolvedBaseType.getBaseType() : null)) {
                    if (MetadataResolver.areEquivalent(baseType, type)) {
                        return true;
                    }
                    resolvedBaseType = baseType.resolve();
                }
                for (final TypeReference ifType : MetadataHelper.getInterfaces(current)) {
                    if (MetadataResolver.areEquivalent(ifType, type)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private TypeDeclaration createTypeCore(final TypeDefinition type) {
        final TypeDeclaration astType = new TypeDeclaration();
        final String packageName = type.getPackageName();
        if (this._compileUnit.getPackage().isNull() && !StringUtilities.isNullOrWhitespace(packageName)) {
            final PackageDeclaration packageDeclaration = new PackageDeclaration(packageName);
            packageDeclaration.putUserData(Keys.PACKAGE_REFERENCE, PackageReference.parse(packageName));
            this._compileUnit.insertChildBefore(this._packagePlaceholder, packageDeclaration, Roles.PACKAGE);
            this._packagePlaceholder.remove();
        }
        this._typeDeclarations.put(type.getInternalName(), new SoftReference<TypeDeclaration>(astType));
        long flags = type.getFlags();
        if (type.isInterface() || type.isEnum()) {
            flags &= 0x7L;
        }
        else {
            flags &= 0x7E1FL;
        }
        EntityDeclaration.setModifiers(astType, Flags.asModifierSet(this.scrubAccessModifiers(flags)));
        astType.setName(type.getSimpleName());
        astType.putUserData(Keys.TYPE_DEFINITION, type);
        astType.putUserData(Keys.TYPE_REFERENCE, type);
        if (type.isEnum()) {
            astType.setClassType(ClassType.ENUM);
        }
        else if (type.isAnnotation()) {
            astType.setClassType(ClassType.ANNOTATION);
        }
        else if (type.isInterface()) {
            astType.setClassType(ClassType.INTERFACE);
        }
        else {
            astType.setClassType(ClassType.CLASS);
        }
        final List<TypeParameterDeclaration> typeParameters = this.createTypeParameters(type.getGenericParameters());
        if (!typeParameters.isEmpty()) {
            astType.getTypeParameters().addAll((Collection<?>)typeParameters);
        }
        final TypeReference baseType = type.getBaseType();
        if (baseType != null && !type.isEnum() && !BuiltinTypes.Object.equals(baseType)) {
            astType.addChild(this.convertType(baseType), Roles.BASE_TYPE);
        }
        for (final TypeReference interfaceType : type.getExplicitInterfaces()) {
            if (type.isAnnotation() && "java/lang/annotations/Annotation".equals(interfaceType.getInternalName())) {
                continue;
            }
            astType.addChild(this.convertType(interfaceType), Roles.IMPLEMENTED_INTERFACE);
        }
        for (final CustomAnnotation annotation : type.getAnnotations()) {
            astType.getAnnotations().add(this.createAnnotation(annotation));
        }
        this.addTypeMembers(astType, type);
        return astType;
    }
    
    private long scrubAccessModifiers(final long flags) {
        final long result = flags & 0xFFFFFFFFFFFFFFF8L;
        if ((flags & 0x2L) != 0x0L) {
            return result | 0x2L;
        }
        if ((flags & 0x4L) != 0x0L) {
            return result | 0x4L;
        }
        if ((flags & 0x1L) != 0x0L) {
            return result | 0x1L;
        }
        return result;
    }
    
    private void addTypeMembers(final TypeDeclaration astType, final TypeDefinition type) {
        for (final FieldDefinition field : type.getDeclaredFields()) {
            astType.addChild(this.createField(field), Roles.TYPE_MEMBER);
        }
        for (final MethodDefinition method : type.getDeclaredMethods()) {
            if (method.isConstructor()) {
                astType.addChild(this.createConstructor(method), Roles.TYPE_MEMBER);
            }
            else {
                astType.addChild(this.createMethod(method), Roles.TYPE_MEMBER);
            }
        }
        final List<TypeDefinition> nestedTypes = new ArrayList<TypeDefinition>();
        for (final TypeDefinition nestedType : type.getDeclaredTypes()) {
            final TypeReference declaringType = nestedType.getDeclaringType();
            if (!nestedType.isLocalClass() && type.isEquivalentTo(declaringType)) {
                if (nestedType.isAnonymous()) {
                    this._typeDeclarations.put(type.getInternalName(), new SoftReference<TypeDeclaration>(astType));
                }
                else {
                    nestedTypes.add(nestedType);
                }
            }
        }
        sortNestedTypes(nestedTypes);
        for (final TypeDefinition nestedType : nestedTypes) {
            astType.addChild(this.createTypeNoCache(nestedType), Roles.TYPE_MEMBER);
        }
    }
    
    private static void sortNestedTypes(final List<TypeDefinition> types) {
        final IdentityHashMap<TypeDefinition, Integer> minOffsets = new IdentityHashMap<TypeDefinition, Integer>();
        for (final TypeDefinition type : types) {
            minOffsets.put(type, findFirstLineNumber(type));
        }
        Collections.sort(types, new Comparator<TypeDefinition>() {
            @Override
            public int compare(final TypeDefinition o1, final TypeDefinition o2) {
                return Integer.compare(minOffsets.get(o1), minOffsets.get(o2));
            }
        });
    }
    
    private static Integer findFirstLineNumber(final TypeDefinition type) {
        int minLineNumber = Integer.MAX_VALUE;
        for (final MethodDefinition method : type.getDeclaredMethods()) {
            final LineNumberTableAttribute attribute = SourceAttribute.find("LineNumberTable", method.getSourceAttributes());
            if (attribute != null && !attribute.getEntries().isEmpty()) {
                final int firstLineNumber = attribute.getEntries().get(0).getLineNumber();
                if (firstLineNumber >= minLineNumber) {
                    continue;
                }
                minLineNumber = firstLineNumber;
            }
        }
        return minLineNumber;
    }
    
    private FieldDeclaration createField(final FieldDefinition field) {
        final FieldDeclaration astField = new FieldDeclaration();
        final VariableInitializer initializer = new VariableInitializer(field.getName());
        astField.setName(field.getName());
        astField.addChild(initializer, Roles.VARIABLE);
        astField.setReturnType(this.convertType(field.getFieldType()));
        astField.putUserData(Keys.FIELD_DEFINITION, field);
        astField.putUserData(Keys.MEMBER_REFERENCE, field);
        EntityDeclaration.setModifiers(astField, Flags.asModifierSet(this.scrubAccessModifiers(field.getFlags() & 0x40DFL)));
        if (field.hasConstantValue()) {
            initializer.setInitializer(new PrimitiveExpression(-34, field.getConstantValue()));
            initializer.putUserData(Keys.FIELD_DEFINITION, field);
            initializer.putUserData(Keys.MEMBER_REFERENCE, field);
        }
        for (final CustomAnnotation annotation : field.getAnnotations()) {
            astField.getAnnotations().add(this.createAnnotation(annotation));
        }
        return astField;
    }
    
    private MethodDeclaration createMethod(final MethodDefinition method) {
        final MethodDeclaration astMethod = new MethodDeclaration();
        Set<Modifier> modifiers;
        if (method.isTypeInitializer()) {
            modifiers = Collections.singleton(Modifier.STATIC);
        }
        else if (method.getDeclaringType().isInterface()) {
            modifiers = Collections.emptySet();
        }
        else {
            modifiers = Flags.asModifierSet(this.scrubAccessModifiers(method.getFlags() & 0xD3FL));
        }
        EntityDeclaration.setModifiers(astMethod, modifiers);
        astMethod.setName(method.getName());
        astMethod.getParameters().addAll((Collection<?>)this.createParameters(method.getParameters()));
        astMethod.getTypeParameters().addAll((Collection<?>)this.createTypeParameters(method.getGenericParameters()));
        astMethod.setReturnType(this.convertType(method.getReturnType()));
        astMethod.putUserData(Keys.METHOD_DEFINITION, method);
        astMethod.putUserData(Keys.MEMBER_REFERENCE, method);
        for (final TypeDefinition declaredType : method.getDeclaredTypes()) {
            if (!declaredType.isAnonymous()) {
                astMethod.getDeclaredTypes().add(this.createType(declaredType));
            }
        }
        if (!method.getDeclaringType().isInterface() || method.isTypeInitializer() || method.isDefault()) {
            astMethod.setBody(this.createMethodBody(method, astMethod.getParameters()));
        }
        for (final TypeReference thrownType : method.getThrownTypes()) {
            astMethod.addChild(this.convertType(thrownType), Roles.THROWN_TYPE);
        }
        for (final CustomAnnotation annotation : method.getAnnotations()) {
            astMethod.getAnnotations().add(this.createAnnotation(annotation));
        }
        final AnnotationDefaultAttribute defaultAttribute = SourceAttribute.find("AnnotationDefault", method.getSourceAttributes());
        if (defaultAttribute != null) {
            final Expression defaultValue = this.createAnnotationElement(defaultAttribute.getDefaultValue());
            if (defaultValue != null && !defaultValue.isNull()) {
                astMethod.setDefaultValue(defaultValue);
            }
        }
        return astMethod;
    }
    
    private ConstructorDeclaration createConstructor(final MethodDefinition method) {
        final ConstructorDeclaration astMethod = new ConstructorDeclaration();
        EntityDeclaration.setModifiers(astMethod, Flags.asModifierSet(this.scrubAccessModifiers(method.getFlags() & 0x7L)));
        astMethod.setName(method.getDeclaringType().getName());
        astMethod.getParameters().addAll((Collection<?>)this.createParameters(method.getParameters()));
        astMethod.setBody(this.createMethodBody(method, astMethod.getParameters()));
        astMethod.putUserData(Keys.METHOD_DEFINITION, method);
        astMethod.putUserData(Keys.MEMBER_REFERENCE, method);
        for (final TypeReference thrownType : method.getThrownTypes()) {
            astMethod.addChild(this.convertType(thrownType), Roles.THROWN_TYPE);
        }
        return astMethod;
    }
    
    final List<TypeParameterDeclaration> createTypeParameters(final List<GenericParameter> genericParameters) {
        if (genericParameters.isEmpty()) {
            return Collections.emptyList();
        }
        final int count = genericParameters.size();
        final TypeParameterDeclaration[] typeParameters = new TypeParameterDeclaration[genericParameters.size()];
        for (int i = 0; i < count; ++i) {
            final GenericParameter genericParameter = genericParameters.get(i);
            final TypeParameterDeclaration typeParameter = new TypeParameterDeclaration(genericParameter.getName());
            if (genericParameter.hasExtendsBound()) {
                typeParameter.setExtendsBound(this.convertType(genericParameter.getExtendsBound()));
            }
            typeParameter.putUserData(Keys.TYPE_REFERENCE, genericParameter);
            typeParameter.putUserData(Keys.TYPE_DEFINITION, genericParameter);
            typeParameters[i] = typeParameter;
        }
        return ArrayUtilities.asUnmodifiableList(typeParameters);
    }
    
    static void addTypeArguments(final TypeReference type, final AstType astType) {
        if (type.hasGenericParameters()) {
            final List<GenericParameter> genericParameters = type.getGenericParameters();
            final int count = genericParameters.size();
            final AstType[] typeArguments = new AstType[count];
            for (int i = 0; i < count; ++i) {
                final GenericParameter genericParameter = genericParameters.get(i);
                final SimpleType typeParameter = new SimpleType(genericParameter.getName());
                typeParameter.putUserData(Keys.TYPE_REFERENCE, genericParameter);
                typeArguments[i] = typeParameter;
            }
            applyTypeArguments(astType, ArrayUtilities.asUnmodifiableList(typeArguments));
        }
    }
    
    static void applyTypeArguments(final AstType baseType, final List<AstType> typeArguments) {
        if (baseType instanceof SimpleType) {
            final SimpleType st = (SimpleType)baseType;
            st.getTypeArguments().addAll((Collection<?>)typeArguments);
        }
    }
    
    private BlockStatement createMethodBody(final MethodDefinition method, final Iterable<ParameterDeclaration> parameters) {
        if (this._decompileMethodBodies) {
            return AstMethodBodyBuilder.createMethodBody(this, method, this._context, parameters);
        }
        return null;
    }
    
    public static Expression makePrimitive(final long val, final TypeReference type) {
        if (TypeAnalysis.isBoolean(type)) {
            if (val == 0L) {
                return new PrimitiveExpression(-34, Boolean.FALSE);
            }
            return new PrimitiveExpression(-34, Boolean.TRUE);
        }
        else {
            if (type != null) {
                return new PrimitiveExpression(-34, JavaPrimitiveCast.cast(type.getSimpleType(), val));
            }
            return new PrimitiveExpression(-34, JavaPrimitiveCast.cast(JvmType.Integer, val));
        }
    }
    
    public static Expression makeDefaultValue(final TypeReference type) {
        if (type == null) {
            return new NullReferenceExpression(-34);
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
            case 1: {
                return new PrimitiveExpression(-34, Boolean.FALSE);
            }
            case 2: {
                return new PrimitiveExpression(-34, (byte)0);
            }
            case 3: {
                return new PrimitiveExpression(-34, '\0');
            }
            case 4: {
                return new PrimitiveExpression(-34, (short)0);
            }
            case 5: {
                return new PrimitiveExpression(-34, 0);
            }
            case 6: {
                return new PrimitiveExpression(-34, 0L);
            }
            case 7: {
                return new PrimitiveExpression(-34, 0.0f);
            }
            case 8: {
                return new PrimitiveExpression(-34, 0.0);
            }
            default: {
                return new NullReferenceExpression(-34);
            }
        }
    }
    
    public List<LineNumberPosition> generateCode(final ITextOutput output) {
        if (!this._haveTransformationsRun) {
            this.runTransformations();
        }
        final JavaOutputVisitor visitor = new JavaOutputVisitor(output, this._context.getSettings());
        this._compileUnit.acceptVisitor((IAstVisitor<? super Object, ?>)visitor, (Object)null);
        return visitor.getLineNumberPositions();
    }
    
    public static boolean isMemberHidden(final IMemberDefinition member, final DecompilerContext context) {
        final DecompilerSettings settings = context.getSettings();
        if (member.isSynthetic() && !settings.getShowSyntheticMembers()) {
            return !context.getForcedVisibleMembers().contains(member);
        }
        if (member instanceof TypeReference && ((TypeReference)member).isNested() && settings.getExcludeNestedTypes()) {
            final TypeDefinition resolvedType = ((TypeReference)member).resolve();
            return resolvedType == null || (!resolvedType.isAnonymous() && findLocalType(resolvedType) == null);
        }
        return false;
    }
    
    private static TypeReference findLocalType(final TypeReference type) {
        if (type != null) {
            final TypeDefinition resolvedType = type.resolve();
            if (resolvedType != null && resolvedType.isLocalClass()) {
                return resolvedType;
            }
            final TypeReference declaringType = type.getDeclaringType();
            if (declaringType != null) {
                return findLocalType(declaringType);
            }
        }
        return null;
    }
    
    public Annotation createAnnotation(final CustomAnnotation annotation) {
        final Annotation a = new Annotation();
        final AstNodeCollection<Expression> arguments = a.getArguments();
        a.setType(this.convertType(annotation.getAnnotationType()));
        final List<AnnotationParameter> parameters = annotation.getParameters();
        for (final AnnotationParameter p : parameters) {
            final String member = p.getMember();
            final Expression value = this.createAnnotationElement(p.getValue());
            if (StringUtilities.isNullOrEmpty(member) || (parameters.size() == 1 && "value".equals(member))) {
                arguments.add(value);
            }
            else {
                arguments.add(new AssignmentExpression(new IdentifierExpression(value.getOffset(), member), value));
            }
        }
        return a;
    }
    
    public Expression createAnnotationElement(final AnnotationElement element) {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType()[element.getElementType().ordinal()]) {
            case 1: {
                final ConstantAnnotationElement constant = (ConstantAnnotationElement)element;
                return new PrimitiveExpression(-34, constant.getConstantValue());
            }
            case 2: {
                final EnumAnnotationElement enumElement = (EnumAnnotationElement)element;
                return new TypeReferenceExpression(-34, this.convertType(enumElement.getEnumType())).member(enumElement.getEnumConstantName());
            }
            case 3: {
                final ArrayAnnotationElement arrayElement = (ArrayAnnotationElement)element;
                final ArrayInitializerExpression initializer = new ArrayInitializerExpression();
                final AstNodeCollection<Expression> elements = initializer.getElements();
                AnnotationElement[] loc_1;
                for (int loc_0 = (loc_1 = arrayElement.getElements()).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final AnnotationElement e = loc_1[loc_2];
                    elements.add(this.createAnnotationElement(e));
                }
                return initializer;
            }
            case 4: {
                return new ClassOfExpression(-34, this.convertType(((ClassAnnotationElement)element).getClassType()));
            }
            case 5: {
                return this.createAnnotation(((AnnotationAnnotationElement)element).getAnnotation());
            }
            default: {
                throw ContractUtils.unreachable();
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType() {
        final int[] loc_0 = AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[AnnotationElementType.values().length];
        try {
            loc_1[AnnotationElementType.Annotation.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AnnotationElementType.Array.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AnnotationElementType.Class.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AnnotationElementType.Constant.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AnnotationElementType.Enum.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_6) {}
        return AstBuilder.$SWITCH_TABLE$com$strobel$assembler$metadata$annotations$AnnotationElementType = loc_1;
    }
}
