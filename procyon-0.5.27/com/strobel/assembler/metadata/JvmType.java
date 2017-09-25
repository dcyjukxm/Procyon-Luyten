package com.strobel.assembler.metadata;

import java.util.*;
import com.strobel.core.*;

public enum JvmType
{
    Boolean("Boolean", 0), 
    Byte("Byte", 1), 
    Character("Character", 2), 
    Short("Short", 3), 
    Integer("Integer", 4), 
    Long("Long", 5), 
    Float("Float", 6), 
    Double("Double", 7), 
    Object("Object", 8), 
    Array("Array", 9), 
    TypeVariable("TypeVariable", 10), 
    Wildcard("Wildcard", 11), 
    Void("Void", 12);
    
    private static final Map<Class<?>, JvmType> CLASSES_TO_JVM_TYPES;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    static {
        final HashMap<Class<?>, JvmType> map = new HashMap<Class<?>, JvmType>();
        map.put(Void.class, JvmType.Void);
        map.put(Boolean.class, JvmType.Boolean);
        map.put(Character.class, JvmType.Character);
        map.put(Byte.class, JvmType.Byte);
        map.put(Short.class, JvmType.Short);
        map.put(Integer.class, JvmType.Integer);
        map.put(Long.class, JvmType.Long);
        map.put(Float.class, JvmType.Float);
        map.put(Double.class, JvmType.Double);
        map.put(java.lang.Void.TYPE, JvmType.Void);
        map.put(java.lang.Boolean.TYPE, JvmType.Boolean);
        map.put(java.lang.Character.TYPE, JvmType.Character);
        map.put(java.lang.Byte.TYPE, JvmType.Byte);
        map.put(java.lang.Short.TYPE, JvmType.Short);
        map.put(java.lang.Integer.TYPE, JvmType.Integer);
        map.put(java.lang.Long.TYPE, JvmType.Long);
        map.put(java.lang.Float.TYPE, JvmType.Float);
        map.put(java.lang.Double.TYPE, JvmType.Double);
        CLASSES_TO_JVM_TYPES = map;
    }
    
    public final String getDescriptorPrefix() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1: {
                return "Z";
            }
            case 2: {
                return "B";
            }
            case 3: {
                return "C";
            }
            case 4: {
                return "S";
            }
            case 5: {
                return "I";
            }
            case 6: {
                return "J";
            }
            case 7: {
                return "F";
            }
            case 8: {
                return "D";
            }
            case 9: {
                return "L";
            }
            case 10: {
                return "[";
            }
            case 11: {
                return "T";
            }
            case 12: {
                return "*";
            }
            case 13: {
                return "V";
            }
            default: {
                return "L";
            }
        }
    }
    
    public final String getPrimitiveName() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1: {
                return "boolean";
            }
            case 2: {
                return "byte";
            }
            case 3: {
                return "char";
            }
            case 4: {
                return "short";
            }
            case 5: {
                return "int";
            }
            case 6: {
                return "long";
            }
            case 7: {
                return "float";
            }
            case 8: {
                return "double";
            }
            case 13: {
                return "void";
            }
            default: {
                return null;
            }
        }
    }
    
    public final boolean isPrimitive() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    public final boolean isPrimitiveOrVoid() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 9:
            case 10:
            case 11:
            case 12: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    public final int bitWidth() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1: {
                return 1;
            }
            case 2: {
                return 8;
            }
            case 3:
            case 4: {
                return 16;
            }
            case 5: {
                return 32;
            }
            case 6: {
                return 64;
            }
            case 7: {
                return 32;
            }
            case 8: {
                return 64;
            }
            default: {
                return 0;
            }
        }
    }
    
    public final int stackSlots() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 6:
            case 8: {
                return 2;
            }
            case 13: {
                return 0;
            }
            default: {
                return 1;
            }
        }
    }
    
    public final boolean isSingleWord() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 6:
            case 8:
            case 13: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    public final boolean isDoubleWord() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 6:
            case 8: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isNumeric() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isIntegral() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isSubWordOrInt32() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isSigned() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            default: {
                return false;
            }
            case 2:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                return true;
            }
        }
    }
    
    public final boolean isUnsigned() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 1:
            case 3: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isFloating() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 7:
            case 8: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public final boolean isOther() {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[this.ordinal()]) {
            case 9:
            case 10:
            case 11:
            case 12:
            case 13: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static JvmType forClass(final Class<?> clazz) {
        VerifyArgument.notNull(clazz, "clazz");
        final JvmType jvmType = JvmType.CLASSES_TO_JVM_TYPES.get(clazz);
        if (jvmType != null) {
            return jvmType;
        }
        return JvmType.Object;
    }
    
    public static JvmType forValue(final Object value, final boolean unboxPrimitives) {
        if (value == null) {
            return JvmType.Object;
        }
        final Class<?> clazz = value.getClass();
        if (unboxPrimitives || clazz.isPrimitive()) {
            final JvmType jvmType = JvmType.CLASSES_TO_JVM_TYPES.get(clazz);
            if (jvmType != null) {
                return jvmType;
            }
        }
        return JvmType.Object;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = JvmType.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
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
        return JvmType.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
}
