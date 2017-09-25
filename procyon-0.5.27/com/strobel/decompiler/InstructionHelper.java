package com.strobel.decompiler;

import com.strobel.core.*;
import com.strobel.util.*;
import java.util.*;
import com.strobel.assembler.metadata.*;
import com.strobel.assembler.ir.*;

public final class InstructionHelper
{
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior;
    
    public static int getLoadOrStoreSlot(final Instruction instruction) {
        final OpCode code = instruction.getOpCode();
        if (!code.isLoad() && !code.isStore()) {
            return -1;
        }
        if (code.getOpCodeType() == OpCodeType.Macro) {
            return OpCodeHelpers.getLoadStoreMacroArgumentIndex(code);
        }
        final VariableReference variable = instruction.getOperand(0);
        return variable.getSlot();
    }
    
    public static int getPopDelta(final Instruction instruction, final MethodBody body) {
        VerifyArgument.notNull(instruction, "instruction");
        VerifyArgument.notNull(body, "body");
        final OpCode code = instruction.getOpCode();
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior()[code.getStackBehaviorPop().ordinal()]) {
            case 1: {
                return 0;
            }
            case 2: {
                if (code == OpCode.PUTSTATIC) {
                    final FieldReference field = instruction.getOperand(0);
                    if (field.getFieldType().getSimpleType().isDoubleWord()) {
                        return 2;
                    }
                }
                return 1;
            }
            case 3: {
                return 2;
            }
            case 4: {
                return 2;
            }
            case 5: {
                return 3;
            }
            case 6: {
                if (code == OpCode.PUTFIELD) {
                    final FieldReference field = instruction.getOperand(0);
                    if (field.getFieldType().getSimpleType().isDoubleWord()) {
                        return 3;
                    }
                }
                return 2;
            }
            case 7: {
                return 3;
            }
            case 8: {
                return 4;
            }
            case 9: {
                return 1;
            }
            case 10: {
                return 2;
            }
            case 11: {
                return 1;
            }
            case 12: {
                return 2;
            }
            case 13: {
                return 1;
            }
            case 14: {
                return 2;
            }
            case 15: {
                return 3;
            }
            case 16: {
                return 4;
            }
            case 17: {
                return 2;
            }
            case 18: {
                return 4;
            }
            case 19: {
                return 2;
            }
            case 20: {
                return 3;
            }
            case 21: {
                return 4;
            }
            case 22: {
                return 3;
            }
            case 23: {
                return 4;
            }
            case 24: {
                return 3;
            }
            case 25: {
                return 2;
            }
            case 41: {
                if (code == OpCode.ATHROW) {
                    return 1;
                }
                if (code == OpCode.MULTIANEWARRAY) {
                    return instruction.getOperand(1);
                }
                if (code.getFlowControl() != FlowControl.Call) {
                    break;
                }
                IMethodSignature signature;
                if (code == OpCode.INVOKEDYNAMIC) {
                    signature = instruction.getOperand(0).getMethodType();
                }
                else {
                    signature = instruction.getOperand(0);
                }
                final List<ParameterDefinition> parameters = signature.getParameters();
                int count = parameters.size();
                if (code != OpCode.INVOKESTATIC && code != OpCode.INVOKEDYNAMIC) {
                    ++count;
                }
                for (int i = 0; i < parameters.size(); ++i) {
                    if (parameters.get(i).getParameterType().getSimpleType().isDoubleWord()) {
                        ++count;
                    }
                }
                return count;
            }
        }
        throw ContractUtils.unsupported();
    }
    
    public static int getPushDelta(final Instruction instruction, final MethodBody body) {
        VerifyArgument.notNull(instruction, "instruction");
        VerifyArgument.notNull(body, "body");
        final OpCode code = instruction.getOpCode();
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior()[code.getStackBehaviorPush().ordinal()]) {
            case 26: {
                return 0;
            }
            case 27: {
                if (code == OpCode.GETFIELD || code == OpCode.GETSTATIC) {
                    final FieldReference field = instruction.getOperand(0);
                    if (field.getFieldType().getSimpleType().isDoubleWord()) {
                        return 2;
                    }
                }
                return 1;
            }
            case 28: {
                return 2;
            }
            case 29: {
                return 3;
            }
            case 30: {
                return 4;
            }
            case 31: {
                return 2;
            }
            case 32: {
                return 4;
            }
            case 33: {
                return 5;
            }
            case 34: {
                return 6;
            }
            case 35: {
                return 1;
            }
            case 36: {
                return 2;
            }
            case 37: {
                return 1;
            }
            case 38: {
                return 2;
            }
            case 39: {
                return 1;
            }
            case 40: {
                return 1;
            }
            case 42: {
                if (code.getFlowControl() != FlowControl.Call) {
                    break;
                }
                IMethodSignature signature;
                if (code == OpCode.INVOKEDYNAMIC) {
                    signature = instruction.getOperand(0).getMethodType();
                }
                else {
                    signature = instruction.getOperand(0);
                }
                final TypeReference returnType = signature.getReturnType();
                final JvmType jvmType = returnType.getSimpleType();
                if (jvmType == JvmType.Void) {
                    return 0;
                }
                return jvmType.isDoubleWord() ? 2 : 1;
            }
        }
        throw ContractUtils.unsupported();
    }
    
    public static Instruction reverseLoadOrStore(final Instruction instruction) {
        VerifyArgument.notNull(instruction, "instruction");
        final OpCode oldCode = instruction.getOpCode();
        OpCode newCode;
        if (oldCode.isStore()) {
            newCode = OpCode.valueOf(oldCode.name().replace("STORE", "LOAD"));
        }
        else {
            if (!oldCode.isLoad()) {
                throw new IllegalArgumentException("Instruction is neither a load nor store: " + instruction.getOpCode());
            }
            newCode = OpCode.valueOf(oldCode.name().replace("LOAD", "STORE"));
        }
        if (instruction.getOperandCount() == 1) {
            return new Instruction(newCode, instruction.getOperand(0));
        }
        return new Instruction(newCode);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior() {
        final int[] loc_0 = InstructionHelper.$SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[StackBehavior.values().length];
        try {
            loc_1[StackBehavior.Pop0.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[StackBehavior.Pop1.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[StackBehavior.Pop1_Pop1.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[StackBehavior.Pop1_Pop2.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[StackBehavior.Pop1_PopA.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[StackBehavior.Pop2.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[StackBehavior.Pop2_Pop1.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[StackBehavior.Pop2_Pop2.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[StackBehavior.PopA.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[StackBehavior.PopA_PopA.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[StackBehavior.PopA_PopI4_PopA.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[StackBehavior.PopI4.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[StackBehavior.PopI4_PopA.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[StackBehavior.PopI4_PopI4.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[StackBehavior.PopI4_PopI4_PopA.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[StackBehavior.PopI4_PopI8.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[StackBehavior.PopI8.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[StackBehavior.PopI8_PopI4_PopA.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[StackBehavior.PopI8_PopI8.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[StackBehavior.PopR4.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[StackBehavior.PopR4_PopI4_PopA.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[StackBehavior.PopR4_PopR4.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[StackBehavior.PopR8.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[StackBehavior.PopR8_PopI4_PopA.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[StackBehavior.PopR8_PopR8.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[StackBehavior.Push0.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[StackBehavior.Push1.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[StackBehavior.Push1_Push1.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[StackBehavior.Push1_Push1_Push1.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[StackBehavior.Push1_Push2_Push1.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[StackBehavior.Push2.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[StackBehavior.Push2_Push1_Push2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[StackBehavior.Push2_Push2.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[StackBehavior.Push2_Push2_Push2.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[StackBehavior.PushA.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[StackBehavior.PushAddress.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[StackBehavior.PushI4.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[StackBehavior.PushI8.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[StackBehavior.PushR4.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[StackBehavior.PushR8.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_41) {}
        try {
            loc_1[StackBehavior.VarPop.ordinal()] = 41;
        }
        catch (NoSuchFieldError loc_42) {}
        try {
            loc_1[StackBehavior.VarPush.ordinal()] = 42;
        }
        catch (NoSuchFieldError loc_43) {}
        return InstructionHelper.$SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior = loc_1;
    }
}
