package com.strobel.decompiler.languages.java.analysis;

import com.strobel.decompiler.patterns.*;
import com.strobel.decompiler.languages.java.ast.*;

public final class UsageClassifier
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
    
    public static UsageType getUsageType(final Expression expression) {
        final AstNode parent = expression.getParent();
        if (parent instanceof BinaryOperatorExpression) {
            return UsageType.Read;
        }
        if (!(parent instanceof AssignmentExpression)) {
            if (parent instanceof UnaryOperatorExpression) {
                final UnaryOperatorExpression unary = (UnaryOperatorExpression)parent;
                switch ($SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType()[unary.getOperator().ordinal()]) {
                    case 1: {
                        return UsageType.ReadWrite;
                    }
                    case 2:
                    case 3:
                    case 4:
                    case 5: {
                        return UsageType.Read;
                    }
                    case 6:
                    case 7:
                    case 8:
                    case 9: {
                        return UsageType.ReadWrite;
                    }
                }
            }
            return UsageType.Read;
        }
        if (!expression.matches(((AssignmentExpression)parent).getLeft())) {
            return UsageType.Read;
        }
        final AssignmentOperatorType operator = ((AssignmentExpression)parent).getOperator();
        if (operator == AssignmentOperatorType.ANY || operator == AssignmentOperatorType.ASSIGN) {
            return UsageType.Write;
        }
        return UsageType.ReadWrite;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType() {
        final int[] loc_0 = UsageClassifier.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[UnaryOperatorType.values().length];
        try {
            loc_1[UnaryOperatorType.ANY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[UnaryOperatorType.BITWISE_NOT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[UnaryOperatorType.DECREMENT.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[UnaryOperatorType.INCREMENT.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[UnaryOperatorType.MINUS.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[UnaryOperatorType.NOT.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[UnaryOperatorType.PLUS.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[UnaryOperatorType.POST_DECREMENT.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[UnaryOperatorType.POST_INCREMENT.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_10) {}
        return UsageClassifier.$SWITCH_TABLE$com$strobel$decompiler$languages$java$ast$UnaryOperatorType = loc_1;
    }
}
