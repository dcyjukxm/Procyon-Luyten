package com.strobel.assembler.metadata;

import com.strobel.util.*;

public final class BuiltinTypes
{
    public static final TypeDefinition Boolean;
    public static final TypeDefinition Byte;
    public static final TypeDefinition Character;
    public static final TypeDefinition Short;
    public static final TypeDefinition Integer;
    public static final TypeDefinition Long;
    public static final TypeDefinition Float;
    public static final TypeDefinition Double;
    public static final TypeDefinition Void;
    public static final TypeDefinition Object;
    public static final TypeDefinition Bottom;
    public static final TypeDefinition Null;
    public static final TypeDefinition Class;
    
    static {
        Boolean = new PrimitiveType(JvmType.Boolean);
        Byte = new PrimitiveType(JvmType.Byte);
        Character = new PrimitiveType(JvmType.Character);
        Short = new PrimitiveType(JvmType.Short);
        Integer = new PrimitiveType(JvmType.Integer);
        Long = new PrimitiveType(JvmType.Long);
        Float = new PrimitiveType(JvmType.Float);
        Double = new PrimitiveType(JvmType.Double);
        Void = new PrimitiveType(JvmType.Void);
        Bottom = BottomType.INSTANCE;
        Null = NullType.INSTANCE;
        final Buffer buffer = new Buffer();
        final ITypeLoader typeLoader = new ClasspathTypeLoader();
        if (!typeLoader.tryLoadType("java/lang/Object", buffer)) {
            throw Error.couldNotLoadObjectType();
        }
        final MetadataSystem metadataSystem = MetadataSystem.instance();
        Object = ClassFileReader.readClass(metadataSystem, buffer);
        buffer.reset();
        if (!typeLoader.tryLoadType("java/lang/Class", buffer)) {
            throw Error.couldNotLoadClassType();
        }
        Class = ClassFileReader.readClass(metadataSystem, buffer);
    }
    
    public static TypeDefinition fromPrimitiveTypeCode(final int code) {
        switch (code) {
            case 4: {
                return BuiltinTypes.Boolean;
            }
            case 8: {
                return BuiltinTypes.Byte;
            }
            case 9: {
                return BuiltinTypes.Short;
            }
            case 10: {
                return BuiltinTypes.Integer;
            }
            case 11: {
                return BuiltinTypes.Long;
            }
            case 5: {
                return BuiltinTypes.Character;
            }
            case 6: {
                return BuiltinTypes.Float;
            }
            case 7: {
                return BuiltinTypes.Double;
            }
            default: {
                throw ContractUtils.unreachable();
            }
        }
    }
}
