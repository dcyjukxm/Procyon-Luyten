package com.strobel.decompiler.languages.java.ast;

public enum BinaryOperatorType
{
    ANY("ANY", 0), 
    BITWISE_AND("BITWISE_AND", 1), 
    BITWISE_OR("BITWISE_OR", 2), 
    EXCLUSIVE_OR("EXCLUSIVE_OR", 3), 
    LOGICAL_AND("LOGICAL_AND", 4), 
    LOGICAL_OR("LOGICAL_OR", 5), 
    GREATER_THAN("GREATER_THAN", 6), 
    GREATER_THAN_OR_EQUAL("GREATER_THAN_OR_EQUAL", 7), 
    LESS_THAN("LESS_THAN", 8), 
    LESS_THAN_OR_EQUAL("LESS_THAN_OR_EQUAL", 9), 
    EQUALITY("EQUALITY", 10), 
    INEQUALITY("INEQUALITY", 11), 
    ADD("ADD", 12), 
    SUBTRACT("SUBTRACT", 13), 
    MULTIPLY("MULTIPLY", 14), 
    DIVIDE("DIVIDE", 15), 
    MODULUS("MODULUS", 16), 
    SHIFT_LEFT("SHIFT_LEFT", 17), 
    SHIFT_RIGHT("SHIFT_RIGHT", 18), 
    UNSIGNED_SHIFT_RIGHT("UNSIGNED_SHIFT_RIGHT", 19);
    
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
    
    public final boolean isCommutative() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType()[this.ordinal()]) {
            case 2:
            case 3:
            case 4:
            case 11:
            case 12:
            case 13:
            case 15: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType() {
        final int[] loc_0 = BinaryOperatorType.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
        try {
            loc_1[BinaryOperatorType.ADD.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[BinaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_AND.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[BinaryOperatorType.BITWISE_OR.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[BinaryOperatorType.DIVIDE.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[BinaryOperatorType.EQUALITY.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[BinaryOperatorType.EXCLUSIVE_OR.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[BinaryOperatorType.GREATER_THAN_OR_EQUAL.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[BinaryOperatorType.INEQUALITY.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[BinaryOperatorType.LESS_THAN_OR_EQUAL.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_AND.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[BinaryOperatorType.LOGICAL_OR.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[BinaryOperatorType.MODULUS.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[BinaryOperatorType.MULTIPLY.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_LEFT.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[BinaryOperatorType.SHIFT_RIGHT.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[BinaryOperatorType.SUBTRACT.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[BinaryOperatorType.UNSIGNED_SHIFT_RIGHT.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_21) {}
        return BinaryOperatorType.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$BinaryOperatorType = loc_1;
    }
}
