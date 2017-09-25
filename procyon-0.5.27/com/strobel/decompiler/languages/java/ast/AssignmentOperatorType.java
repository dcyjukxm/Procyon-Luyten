package com.strobel.decompiler.languages.java.ast;

public enum AssignmentOperatorType
{
    ASSIGN("ASSIGN", 0), 
    ADD("ADD", 1), 
    SUBTRACT("SUBTRACT", 2), 
    MULTIPLY("MULTIPLY", 3), 
    DIVIDE("DIVIDE", 4), 
    MODULUS("MODULUS", 5), 
    SHIFT_LEFT("SHIFT_LEFT", 6), 
    SHIFT_RIGHT("SHIFT_RIGHT", 7), 
    UNSIGNED_SHIFT_RIGHT("UNSIGNED_SHIFT_RIGHT", 8), 
    BITWISE_AND("BITWISE_AND", 9), 
    BITWISE_OR("BITWISE_OR", 10), 
    EXCLUSIVE_OR("EXCLUSIVE_OR", 11), 
    ANY("ANY", 12);
    
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
    
    public final boolean isCompoundAssignment() {
        switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType()[this.ordinal()]) {
            case 1:
            case 13: {
                return false;
            }
            default: {
                return true;
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType() {
        final int[] loc_0 = AssignmentOperatorType.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[values().length];
        try {
            loc_1[AssignmentOperatorType.ADD.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[AssignmentOperatorType.ANY.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[AssignmentOperatorType.ASSIGN.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[AssignmentOperatorType.BITWISE_AND.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[AssignmentOperatorType.BITWISE_OR.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[AssignmentOperatorType.DIVIDE.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[AssignmentOperatorType.EXCLUSIVE_OR.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[AssignmentOperatorType.MODULUS.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[AssignmentOperatorType.MULTIPLY.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[AssignmentOperatorType.SHIFT_LEFT.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[AssignmentOperatorType.SHIFT_RIGHT.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[AssignmentOperatorType.SUBTRACT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[AssignmentOperatorType.UNSIGNED_SHIFT_RIGHT.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_14) {}
        return AssignmentOperatorType.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$AssignmentOperatorType = loc_1;
    }
}
