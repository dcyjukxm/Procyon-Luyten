package com.strobel.decompiler.languages.java.ast;

import com.strobel.assembler.metadata.*;
import com.strobel.core.*;

public final class JavaPrimitiveCast
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    public static Object cast(final JvmType targetType, final Object input) {
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[targetType.ordinal()]) {
            case 1: {
                if (input instanceof Boolean) {
                    return input;
                }
                if (input instanceof Number) {
                    if (input instanceof Float) {
                        break;
                    }
                    if (input instanceof Double) {
                        break;
                    }
                    if (((Number)input).longValue() != 0L) {
                        return true;
                    }
                    return false;
                }
                else if (input instanceof Character) {
                    if ((char)input != '\0') {
                        return true;
                    }
                    return false;
                }
                else {
                    if (input instanceof String) {
                        return StringUtilities.isTrue((String)input);
                    }
                    break;
                }
                break;
            }
            case 2: {
                if (input instanceof Number) {
                    return ((Number)input).byteValue();
                }
                if (input instanceof Character) {
                    return (byte)(char)input;
                }
                if (input instanceof String) {
                    return Byte.parseByte((String)input);
                }
                break;
            }
            case 3: {
                if (input instanceof Character) {
                    return input;
                }
                if (input instanceof Number) {
                    return (char)((Number)input).intValue();
                }
                if (input instanceof String) {
                    final String stringValue = (String)input;
                    return (stringValue.length() == 0) ? '\0' : stringValue.charAt(0);
                }
                break;
            }
            case 4: {
                if (input instanceof Number) {
                    return ((Number)input).shortValue();
                }
                if (input instanceof Character) {
                    return (short)(char)input;
                }
                if (input instanceof String) {
                    return Short.parseShort((String)input);
                }
                break;
            }
            case 5: {
                if (input instanceof Number) {
                    return ((Number)input).intValue();
                }
                if (input instanceof Boolean) {
                    return ((boolean)input) ? 1 : 0;
                }
                if (input instanceof String) {
                    return Integer.parseInt((String)input);
                }
                if (input instanceof Character) {
                    return input;
                }
                break;
            }
            case 6: {
                if (input instanceof Number) {
                    return ((Number)input).longValue();
                }
                if (input instanceof Character) {
                    return input;
                }
                if (input instanceof String) {
                    return Long.parseLong((String)input);
                }
                break;
            }
            case 7: {
                if (input instanceof Number) {
                    return ((Number)input).floatValue();
                }
                if (input instanceof Character) {
                    return input;
                }
                if (input instanceof String) {
                    return Float.parseFloat((String)input);
                }
                break;
            }
            case 8: {
                if (input instanceof Number) {
                    return ((Number)input).doubleValue();
                }
                if (input instanceof Character) {
                    return input;
                }
                if (input instanceof String) {
                    return Double.parseDouble((String)input);
                }
                break;
            }
            default: {
                return input;
            }
        }
        throw new ClassCastException();
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = JavaPrimitiveCast.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return JavaPrimitiveCast.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
}
