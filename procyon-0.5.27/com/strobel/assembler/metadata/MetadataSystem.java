package com.strobel.assembler.metadata;

import java.util.concurrent.*;
import com.strobel.compilerservices.*;
import com.strobel.core.*;

public class MetadataSystem extends MetadataResolver
{
    private static MetadataSystem _instance;
    private final ConcurrentHashMap<String, TypeDefinition> _types;
    private final ITypeLoader _typeLoader;
    private boolean _isEagerMethodLoadingEnabled;
    private static final TypeDefinition[] PRIMITIVE_TYPES_BY_NAME;
    private static final TypeDefinition[] PRIMITIVE_TYPES_BY_DESCRIPTOR;
    
    static {
        PRIMITIVE_TYPES_BY_NAME = new TypeDefinition[25];
        PRIMITIVE_TYPES_BY_DESCRIPTOR = new TypeDefinition[16];
        RuntimeHelpers.ensureClassInitialized(BuiltinTypes.class);
        final TypeDefinition[] allPrimitives = { BuiltinTypes.Boolean, BuiltinTypes.Byte, BuiltinTypes.Character, BuiltinTypes.Short, BuiltinTypes.Integer, BuiltinTypes.Long, BuiltinTypes.Float, BuiltinTypes.Double, BuiltinTypes.Void };
        TypeDefinition[] loc_1;
        for (int loc_0 = (loc_1 = allPrimitives).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final TypeDefinition t = loc_1[loc_2];
            MetadataSystem.PRIMITIVE_TYPES_BY_DESCRIPTOR[hashPrimitiveName(t.getName())] = t;
            MetadataSystem.PRIMITIVE_TYPES_BY_NAME[t.getInternalName().charAt(0) - 'B'] = t;
        }
    }
    
    public static MetadataSystem instance() {
        if (MetadataSystem._instance == null) {
            synchronized (MetadataSystem.class) {
                if (MetadataSystem._instance == null) {
                    MetadataSystem._instance = Fences.orderWrites(new MetadataSystem());
                }
            }
            // monitorexit(MetadataSystem.class)
        }
        return MetadataSystem._instance;
    }
    
    public MetadataSystem() {
        this(new ClasspathTypeLoader());
    }
    
    public MetadataSystem(final String classPath) {
        this(new ClasspathTypeLoader(VerifyArgument.notNull(classPath, "classPath")));
    }
    
    public MetadataSystem(final ITypeLoader typeLoader) {
        super();
        this._typeLoader = VerifyArgument.notNull(typeLoader, "typeLoader");
        this._types = new ConcurrentHashMap<String, TypeDefinition>();
    }
    
    public final boolean isEagerMethodLoadingEnabled() {
        return this._isEagerMethodLoadingEnabled;
    }
    
    public final void setEagerMethodLoadingEnabled(final boolean value) {
        this._isEagerMethodLoadingEnabled = value;
    }
    
    public void addTypeDefinition(final TypeDefinition type) {
        VerifyArgument.notNull(type, "type");
        this._types.putIfAbsent(type.getInternalName(), type);
    }
    
    @Override
    protected TypeDefinition resolveCore(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        return this.resolveType(type.getInternalName(), false);
    }
    
    @Override
    protected TypeReference lookupTypeCore(final String descriptor) {
        return this.resolveType(descriptor, true);
    }
    
    protected TypeDefinition resolveType(final String descriptor, final boolean mightBePrimitive) {
        VerifyArgument.notNull(descriptor, "descriptor");
        if (mightBePrimitive) {
            if (descriptor.length() == 1) {
                final int primitiveHash = descriptor.charAt(0) - 'B';
                if (primitiveHash >= 0 && primitiveHash < MetadataSystem.PRIMITIVE_TYPES_BY_DESCRIPTOR.length) {
                    final TypeDefinition primitiveType = MetadataSystem.PRIMITIVE_TYPES_BY_DESCRIPTOR[primitiveHash];
                    if (primitiveType != null) {
                        return primitiveType;
                    }
                }
            }
            else {
                final int primitiveHash = hashPrimitiveName(descriptor);
                if (primitiveHash >= 0 && primitiveHash < MetadataSystem.PRIMITIVE_TYPES_BY_NAME.length) {
                    final TypeDefinition primitiveType = MetadataSystem.PRIMITIVE_TYPES_BY_NAME[primitiveHash];
                    if (primitiveType != null && descriptor.equals(primitiveType.getName())) {
                        return primitiveType;
                    }
                }
            }
        }
        TypeDefinition cachedDefinition = this._types.get(descriptor);
        if (cachedDefinition != null) {
            return cachedDefinition;
        }
        final Buffer buffer = new Buffer(0);
        if (!this._typeLoader.tryLoadType(descriptor, buffer)) {
            return null;
        }
        final TypeDefinition typeDefinition = ClassFileReader.readClass(this._isEagerMethodLoadingEnabled ? 3 : 1, this, buffer);
        cachedDefinition = this._types.putIfAbsent(descriptor, typeDefinition);
        typeDefinition.setTypeLoader(this._typeLoader);
        if (cachedDefinition != null) {
            return cachedDefinition;
        }
        return typeDefinition;
    }
    
    private static int hashPrimitiveName(final String name) {
        if (name.length() < 3) {
            return 0;
        }
        return (name.charAt(0) + name.charAt(2)) % '\u0010';
    }
}
