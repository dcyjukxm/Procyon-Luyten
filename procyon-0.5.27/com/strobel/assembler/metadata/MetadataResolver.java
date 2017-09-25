package com.strobel.assembler.metadata;

import java.util.*;
import com.strobel.core.*;

public abstract class MetadataResolver implements IMetadataResolver, IGenericContext
{
    private final Stack<IResolverFrame> _frames;
    
    protected MetadataResolver() {
        super();
        this._frames = new Stack<IResolverFrame>();
    }
    
    @Override
    public final TypeReference lookupType(final String descriptor) {
        for (int i = this._frames.size() - 1; i >= 0; --i) {
            final TypeReference type = this._frames.get(i).findType(descriptor);
            if (type != null) {
                return type;
            }
        }
        return this.lookupTypeCore(descriptor);
    }
    
    @Override
    public final GenericParameter findTypeVariable(final String name) {
        for (int i = this._frames.size() - 1; i >= 0; --i) {
            final GenericParameter type = this._frames.get(i).findTypeVariable(name);
            if (type != null) {
                return type;
            }
        }
        return null;
    }
    
    protected abstract TypeReference lookupTypeCore(final String param_0);
    
    @Override
    public void pushFrame(final IResolverFrame frame) {
        this._frames.push(VerifyArgument.notNull(frame, "frame"));
    }
    
    @Override
    public void popFrame() {
        this._frames.pop();
    }
    
    @Override
    public TypeDefinition resolve(final TypeReference type) {
        final TypeReference t = VerifyArgument.notNull(type, "type").getUnderlyingType();
        if (!this._frames.isEmpty()) {
            final String descriptor = type.getInternalName();
            for (int i = this._frames.size() - 1; i >= 0; --i) {
                final TypeReference resolved = this._frames.get(i).findType(descriptor);
                if (resolved instanceof TypeDefinition) {
                    return (TypeDefinition)resolved;
                }
            }
        }
        if (t.isNested()) {
            final TypeDefinition declaringType = t.getDeclaringType().resolve();
            if (declaringType == null) {
                return null;
            }
            final TypeDefinition nestedType = getNestedType(declaringType.getDeclaredTypes(), type);
            if (nestedType != null) {
                return nestedType;
            }
        }
        return this.resolveCore(t);
    }
    
    protected abstract TypeDefinition resolveCore(final TypeReference param_0);
    
    @Override
    public FieldDefinition resolve(final FieldReference field) {
        final TypeDefinition declaringType = VerifyArgument.notNull(field, "field").getDeclaringType().resolve();
        if (declaringType == null) {
            return null;
        }
        return this.getField(declaringType, field);
    }
    
    @Override
    public MethodDefinition resolve(final MethodReference method) {
        TypeReference declaringType = VerifyArgument.notNull(method, "method").getDeclaringType();
        if (declaringType.isArray()) {
            declaringType = BuiltinTypes.Object;
        }
        final TypeDefinition resolvedDeclaringType = declaringType.resolve();
        if (resolvedDeclaringType == null) {
            return null;
        }
        return this.getMethod(resolvedDeclaringType, method);
    }
    
    final FieldDefinition getField(final TypeDefinition declaringType, final FieldReference reference) {
        TypeReference baseType;
        for (TypeDefinition type = declaringType; type != null; type = this.resolve(baseType)) {
            final FieldDefinition field = getField(type.getDeclaredFields(), reference);
            if (field != null) {
                return field;
            }
            baseType = type.getBaseType();
            if (baseType == null) {
                return null;
            }
        }
        return null;
    }
    
    final MethodDefinition getMethod(final TypeDefinition declaringType, final MethodReference reference) {
        MethodDefinition method = getMethod(declaringType.getDeclaredMethods(), reference);
        if (method != null) {
            return method;
        }
        final TypeReference baseType = declaringType.getBaseType();
        if (baseType != null) {
            final TypeDefinition type = baseType.resolve();
            if (type != null) {
                method = this.getMethod(type, reference);
                if (method != null) {
                    return method;
                }
            }
        }
        for (final TypeReference interfaceType : declaringType.getExplicitInterfaces()) {
            final TypeDefinition type = interfaceType.resolve();
            if (type != null) {
                method = this.getMethod(type, reference);
                if (method != null) {
                    return method;
                }
                continue;
            }
        }
        return null;
    }
    
    static TypeDefinition getNestedType(final List<TypeDefinition> candidates, final TypeReference reference) {
        for (int i = 0, n = candidates.size(); i < n; ++i) {
            final TypeDefinition candidate = candidates.get(i);
            if (StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                return candidate;
            }
        }
        return null;
    }
    
    static FieldDefinition getField(final List<FieldDefinition> candidates, final FieldReference reference) {
        for (int i = 0, n = candidates.size(); i < n; ++i) {
            final FieldDefinition candidate = candidates.get(i);
            if (StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                final TypeReference referenceType = reference.getFieldType();
                final TypeReference candidateType = candidate.getFieldType();
                if (candidateType.isGenericParameter() && !referenceType.isGenericParameter()) {
                    if (areEquivalent(MetadataHelper.getUpperBound(candidateType), referenceType)) {
                        return candidate;
                    }
                }
                else if (areEquivalent(candidateType, referenceType)) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    static MethodDefinition getMethod(final List<MethodDefinition> candidates, final MethodReference reference) {
        final String erasedSignature = reference.getErasedSignature();
        for (int i = 0, n = candidates.size(); i < n; ++i) {
            final MethodDefinition candidate = candidates.get(i);
            if (StringComparator.Ordinal.equals(candidate.getName(), reference.getName())) {
                if (StringComparator.Ordinal.equals(candidate.getErasedSignature(), erasedSignature)) {
                    return candidate;
                }
                if (reference.hasGenericParameters()) {
                    if (!candidate.hasGenericParameters()) {
                        continue;
                    }
                    if (candidate.getGenericParameters().size() != reference.getGenericParameters().size()) {
                        continue;
                    }
                }
                if (StringComparator.Ordinal.equals(candidate.getErasedSignature(), erasedSignature)) {
                    return candidate;
                }
            }
        }
        return null;
    }
    
    public static boolean areEquivalent(final TypeReference a, final TypeReference b) {
        return areEquivalent(a, b, true);
    }
    
    public static boolean areEquivalent(final TypeReference a, final TypeReference b, final boolean strict) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getSimpleType() != b.getSimpleType()) {
            return false;
        }
        if (a.isArray()) {
            return areEquivalent(a.getElementType(), b.getElementType());
        }
        if (!StringUtilities.equals(a.getInternalName(), b.getInternalName())) {
            return false;
        }
        if (a instanceof CompoundTypeReference) {
            if (!(b instanceof CompoundTypeReference)) {
                return false;
            }
            final CompoundTypeReference cA = (CompoundTypeReference)a;
            final CompoundTypeReference cB = (CompoundTypeReference)b;
            return areEquivalent(cA.getBaseType(), cB.getBaseType()) && areEquivalent(cA.getInterfaces(), cB.getInterfaces());
        }
        else {
            if (b instanceof CompoundTypeReference) {
                return false;
            }
            if (a.isGenericParameter()) {
                if (b.isGenericParameter()) {
                    return areEquivalent((GenericParameter)a, (GenericParameter)b);
                }
                return areEquivalent(a.getExtendsBound(), b);
            }
            else {
                if (b.isGenericParameter()) {
                    return false;
                }
                if (a.isWildcardType()) {
                    return b.isWildcardType() && areEquivalent(a.getExtendsBound(), b.getExtendsBound()) && areEquivalent(a.getSuperBound(), b.getSuperBound());
                }
                if (b.isWildcardType()) {
                    return false;
                }
                if (b.isGenericType()) {
                    if (!a.isGenericType()) {
                        return !strict || b.isGenericDefinition();
                    }
                    if (a.isGenericDefinition() != b.isGenericDefinition()) {
                        if (a.isGenericDefinition()) {
                            return areEquivalent(a.makeGenericType(((IGenericInstance)b).getTypeArguments()), b);
                        }
                        return areEquivalent(a, b.makeGenericType(((IGenericInstance)a).getTypeArguments()));
                    }
                    else if (b instanceof IGenericInstance) {
                        return a instanceof IGenericInstance && areEquivalent((IGenericInstance)a, (IGenericInstance)b);
                    }
                }
                return true;
            }
        }
    }
    
    static boolean areParametersEquivalent(final List<ParameterDefinition> a, final List<ParameterDefinition> b) {
        final int count = a.size();
        if (b.size() != count) {
            return false;
        }
        if (count == 0) {
            return true;
        }
        for (int i = 0; i < count; ++i) {
            final ParameterDefinition pb = b.get(i);
            final ParameterDefinition pa = a.get(i);
            final TypeReference tb = pb.getParameterType();
            TypeReference ta = pa.getParameterType();
            if (ta.isGenericParameter() && !tb.isGenericParameter() && ((GenericParameter)ta).getOwner() == pa.getMethod()) {
                ta = ta.getExtendsBound();
            }
            if (!areEquivalent(ta, tb)) {
                return false;
            }
        }
        return true;
    }
    
    static <T extends TypeReference> boolean areEquivalent(final List<T> a, final List<T> b) {
        final int count = a.size();
        if (b.size() != count) {
            return false;
        }
        if (count == 0) {
            return true;
        }
        for (int i = 0; i < count; ++i) {
            if (!areEquivalent(a.get(i), b.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean areEquivalent(final IGenericInstance a, final IGenericInstance b) {
        final List<TypeReference> typeArgumentsA = a.getTypeArguments();
        final List<TypeReference> typeArgumentsB = b.getTypeArguments();
        final int arity = typeArgumentsA.size();
        if (arity != typeArgumentsB.size()) {
            return false;
        }
        for (int i = 0; i < arity; ++i) {
            if (!areEquivalent(typeArgumentsA.get(i), typeArgumentsB.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean areEquivalent(final GenericParameter a, final GenericParameter b) {
        if (a.getPosition() != b.getPosition()) {
            return false;
        }
        final IGenericParameterProvider ownerA = a.getOwner();
        final IGenericParameterProvider ownerB = b.getOwner();
        if (ownerA instanceof TypeDefinition) {
            return ownerB instanceof TypeDefinition && areEquivalent((TypeReference)ownerA, (TypeReference)ownerB);
        }
        if (!(ownerA instanceof MethodDefinition)) {
            return true;
        }
        if (!(ownerB instanceof MethodDefinition)) {
            return false;
        }
        final MethodDefinition methodA = (MethodDefinition)ownerA;
        final MethodDefinition methodB = (MethodDefinition)ownerB;
        return areEquivalent(methodA.getDeclaringType(), methodB.getDeclaringType()) && StringUtilities.equals(methodA.getErasedSignature(), methodB.getErasedSignature());
    }
    
    public static IMetadataResolver createLimitedResolver() {
        return new LimitedResolver(null);
    }
    
    private static final class LimitedResolver extends MetadataResolver
    {
        @Override
        protected TypeReference lookupTypeCore(final String descriptor) {
            return null;
        }
        
        @Override
        protected TypeDefinition resolveCore(final TypeReference type) {
            return (type instanceof TypeDefinition) ? ((TypeDefinition)type) : null;
        }
    }
}
