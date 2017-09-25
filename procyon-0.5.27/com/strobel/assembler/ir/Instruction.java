package com.strobel.assembler.ir;

import com.strobel.core.*;
import java.lang.reflect.*;
import com.strobel.decompiler.*;
import com.strobel.util.*;
import com.strobel.assembler.metadata.*;

public final class Instruction implements Comparable<Instruction>
{
    private int _offset;
    private OpCode _opCode;
    private Object _operand;
    private Label _label;
    private Instruction _previous;
    private Instruction _next;
    private static final int U1_MIN_VALUE = 0;
    private static final int U1_MAX_VALUE = 255;
    private static final int U2_MIN_VALUE = 0;
    private static final int U2_MAX_VALUE = 65535;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
    
    public Instruction(final int offset, final OpCode opCode) {
        super();
        this._offset = -1;
        this._offset = offset;
        this._opCode = opCode;
    }
    
    public Instruction(final OpCode opCode) {
        super();
        this._offset = -1;
        this._opCode = opCode;
        this._operand = null;
    }
    
    public Instruction(final OpCode opCode, final Object operand) {
        super();
        this._offset = -1;
        this._opCode = opCode;
        this._operand = operand;
    }
    
    public Instruction(final OpCode opCode, final Object... operands) {
        super();
        this._offset = -1;
        this._opCode = opCode;
        this._operand = VerifyArgument.notNull(operands, "operands");
    }
    
    public boolean hasOffset() {
        return this._offset >= 0;
    }
    
    public boolean hasOperand() {
        return this._operand != null;
    }
    
    public int getOffset() {
        return this._offset;
    }
    
    public void setOffset(final int offset) {
        this._offset = offset;
    }
    
    public int getEndOffset() {
        return this._offset + this.getSize();
    }
    
    public OpCode getOpCode() {
        return this._opCode;
    }
    
    public void setOpCode(final OpCode opCode) {
        this._opCode = opCode;
    }
    
    public int getOperandCount() {
        final Object operand = this._operand;
        if (operand == null) {
            return 0;
        }
        if (ArrayUtilities.isArray(operand)) {
            return Array.getLength(operand);
        }
        return 1;
    }
    
    public <T> T getOperand(final int index) {
        final Object operand = this._operand;
        if (ArrayUtilities.isArray(operand)) {
            VerifyArgument.inRange(0, Array.getLength(operand) - 1, index, "index");
            return (T)Array.get(operand, index);
        }
        VerifyArgument.inRange(0, 0, index, "index");
        return (T)operand;
    }
    
    public void setOperand(final Object operand) {
        this._operand = operand;
    }
    
    public boolean hasLabel() {
        return this._label != null;
    }
    
    public Label getLabel() {
        return this._label;
    }
    
    public void setLabel(final Label label) {
        this._label = label;
    }
    
    public Instruction getPrevious() {
        return this._previous;
    }
    
    public void setPrevious(final Instruction previous) {
        this._previous = previous;
    }
    
    public Instruction getNext() {
        return this._next;
    }
    
    public void setNext(final Instruction next) {
        this._next = next;
    }
    
    public Instruction clone() {
        final Instruction copy = new Instruction(this._opCode, (Object)null);
        copy._offset = this._offset;
        copy._label = ((this._label != null) ? new Label(this._label.getIndex()) : null);
        if (ArrayUtilities.isArray(this._operand)) {
            copy._operand = ((Object[])this._operand).clone();
        }
        else {
            copy._operand = this._operand;
        }
        return copy;
    }
    
    @Override
    public String toString() {
        final PlainTextOutput output = new PlainTextOutput();
        DecompilerHelpers.writeInstruction(output, this);
        return output.toString();
    }
    
    public int getSize() {
        final int opCodeSize = this._opCode.getSize();
        final OperandType operandType = this._opCode.getOperandType();
        Label_0323: {
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
                case 1: {
                    return opCodeSize;
                }
                case 2:
                case 3:
                case 4: {
                    return opCodeSize + operandType.getBaseSize();
                }
                case 5: {
                    return opCodeSize + operandType.getBaseSize();
                }
                case 6: {
                    switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this._opCode.ordinal()]) {
                        case 183:
                        case 184:
                        case 185: {
                            return opCodeSize + operandType.getBaseSize();
                        }
                        case 186: {
                            return opCodeSize + operandType.getBaseSize() + 2;
                        }
                        default: {
                            break Label_0323;
                        }
                    }
                    break;
                }
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14: {
                    return opCodeSize + operandType.getBaseSize();
                }
                case 15: {
                    final Instruction[] targets = ((SwitchInfo)this._operand).getTargets();
                    final int relativeOffset = this._offset + opCodeSize;
                    final int padding = (this._offset >= 0) ? ((4 - relativeOffset % 4) % 4) : 0;
                    switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[this._opCode.ordinal()]) {
                        case 171: {
                            return opCodeSize + padding + 12 + targets.length * 4;
                        }
                        case 172: {
                            return opCodeSize + padding + 8 + targets.length * 8;
                        }
                        default: {
                            break Label_0323;
                        }
                    }
                    break;
                }
                case 16: {
                    return opCodeSize + (this._opCode.isWide() ? 2 : 1);
                }
                case 17:
                case 18: {
                    return opCodeSize + operandType.getBaseSize();
                }
            }
        }
        throw ContractUtils.unreachable();
    }
    
    public static Instruction create(final OpCode opCode) {
        VerifyArgument.notNull(opCode, "opCode");
        if (opCode.getOperandType() != OperandType.None) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode);
    }
    
    public static Instruction create(final OpCode opCode, final Instruction target) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(target, "target");
        if (opCode.getOperandType() != OperandType.BranchTarget && opCode.getOperandType() != OperandType.BranchTargetWide) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, target);
    }
    
    public static Instruction create(final OpCode opCode, final SwitchInfo switchInfo) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(switchInfo, "switchInfo");
        if (opCode.getOperandType() != OperandType.Switch) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, switchInfo);
    }
    
    public static Instruction create(final OpCode opCode, final int value) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), value)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, (Object)value);
    }
    
    public static Instruction create(final OpCode opCode, final short value) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), value)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, (Object)value);
    }
    
    public static Instruction create(final OpCode opCode, final float value) {
        VerifyArgument.notNull(opCode, "opCode");
        if (opCode.getOperandType() != OperandType.Constant && opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, (Object)value);
    }
    
    public static Instruction create(final OpCode opCode, final double value) {
        VerifyArgument.notNull(opCode, "opCode");
        if (opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, (Object)value);
    }
    
    public static Instruction create(final OpCode opCode, final long value) {
        VerifyArgument.notNull(opCode, "opCode");
        if (opCode.getOperandType() != OperandType.I8 && opCode.getOperandType() != OperandType.WideConstant) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, (Object)value);
    }
    
    public static Instruction create(final OpCode opCode, final VariableReference variable) {
        VerifyArgument.notNull(opCode, "opCode");
        if (opCode.getOperandType() != OperandType.Local) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, variable);
    }
    
    public static Instruction create(final OpCode opCode, final VariableReference variable, final int operand) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(variable, "variable");
        if (!checkOperand(opCode.getOperandType(), operand)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, new Object[] { variable, operand });
    }
    
    public static Instruction create(final OpCode opCode, final TypeReference type) {
        VerifyArgument.notNull(opCode, "opCode");
        VerifyArgument.notNull(type, "type");
        if (!checkOperand(opCode.getOperandType(), type)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, type);
    }
    
    public static Instruction create(final OpCode opCode, final TypeReference type, final int operand) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), type) || !checkOperand(opCode.getOperandType(), operand)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, new Object[] { type, operand });
    }
    
    public static Instruction create(final OpCode opCode, final MethodReference method) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), method)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, method);
    }
    
    public static Instruction create(final OpCode opCode, final DynamicCallSite callSite) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), callSite)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, callSite);
    }
    
    public static Instruction create(final OpCode opCode, final FieldReference field) {
        VerifyArgument.notNull(opCode, "opCode");
        if (!checkOperand(opCode.getOperandType(), field)) {
            throw new IllegalArgumentException(String.format("Invalid operand for OpCode %s.", opCode));
        }
        return new Instruction(opCode, field);
    }
    
    private static boolean checkOperand(final OperandType operandType, final int value) {
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
            case 10:
            case 17: {
                return value >= -128 && value <= 127;
            }
            case 11:
            case 18: {
                return value >= -32768 && value <= 32767;
            }
            case 4: {
                return value >= 0 && value <= 255;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean checkOperand(final OperandType operandType, final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
            case 2: {
                return type.getSimpleType().isPrimitive();
            }
            case 3:
            case 4: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean checkOperand(final OperandType operandType, final DynamicCallSite callSite) {
        VerifyArgument.notNull(callSite, "callSite");
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
            case 5: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean checkOperand(final OperandType operandType, final MethodReference method) {
        VerifyArgument.notNull(method, "method");
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
            case 6: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean checkOperand(final OperandType operandType, final FieldReference field) {
        VerifyArgument.notNull(field, "field");
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[operandType.ordinal()]) {
            case 7: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public void accept(final InstructionVisitor visitor) {
        if (this.hasLabel()) {
            visitor.visitLabel(this._label);
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[this._opCode.getOperandType().ordinal()]) {
            case 1: {
                visitor.visit(this._opCode);
                break;
            }
            case 2:
            case 3:
            case 4: {
                visitor.visitType(this._opCode, this.getOperand(0));
                break;
            }
            case 5: {
                visitor.visitDynamicCallSite(this._opCode, (DynamicCallSite)this._operand);
                break;
            }
            case 6: {
                visitor.visitMethod(this._opCode, (MethodReference)this._operand);
                break;
            }
            case 7: {
                visitor.visitField(this._opCode, (FieldReference)this._operand);
                break;
            }
            case 8: {
                visitor.visitBranch(this._opCode, (Instruction)this._operand);
                break;
            }
            case 10:
            case 11: {
                visitor.visitConstant(this._opCode, ((Number)this._operand).intValue());
                break;
            }
            case 12: {
                visitor.visitConstant(this._opCode, ((Number)this._operand).longValue());
                break;
            }
            case 13:
            case 14: {
                if (this._operand instanceof String) {
                    visitor.visitConstant(this._opCode, (String)this._operand);
                    break;
                }
                if (this._operand instanceof TypeReference) {
                    visitor.visitConstant(this._opCode, (TypeReference)this._operand);
                    break;
                }
                final Number number = (Number)this._operand;
                if (this._operand instanceof Long) {
                    visitor.visitConstant(this._opCode, number.longValue());
                    break;
                }
                if (this._operand instanceof Float) {
                    visitor.visitConstant(this._opCode, number.floatValue());
                    break;
                }
                if (this._operand instanceof Double) {
                    visitor.visitConstant(this._opCode, number.doubleValue());
                    break;
                }
                visitor.visitConstant(this._opCode, number.intValue());
                break;
            }
            case 15: {
                visitor.visitSwitch(this._opCode, (SwitchInfo)this._operand);
                break;
            }
            case 16: {
                visitor.visitVariable(this._opCode, (VariableReference)this._operand);
                break;
            }
            case 17:
            case 18: {
                visitor.visitVariable(this._opCode, this.getOperand(0), this.getOperand(1).intValue());
                break;
            }
        }
    }
    
    @Override
    public final int compareTo(final Instruction o) {
        if (o == null) {
            return 1;
        }
        return Integer.compare(this._offset, o._offset);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode() {
        final int[] loc_0 = Instruction.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[OpCode.values().length];
        try {
            loc_1[OpCode.AALOAD.ordinal()] = 51;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[OpCode.AASTORE.ordinal()] = 84;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[OpCode.ACONST_NULL.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[OpCode.ALOAD.ordinal()] = 26;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[OpCode.ALOAD_0.ordinal()] = 43;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[OpCode.ALOAD_1.ordinal()] = 44;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[OpCode.ALOAD_2.ordinal()] = 45;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[OpCode.ALOAD_3.ordinal()] = 46;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[OpCode.ALOAD_W.ordinal()] = 207;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[OpCode.ANEWARRAY.ordinal()] = 190;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[OpCode.ARETURN.ordinal()] = 177;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[OpCode.ARRAYLENGTH.ordinal()] = 191;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[OpCode.ASTORE.ordinal()] = 59;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[OpCode.ASTORE_0.ordinal()] = 76;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[OpCode.ASTORE_1.ordinal()] = 77;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[OpCode.ASTORE_2.ordinal()] = 78;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[OpCode.ASTORE_3.ordinal()] = 79;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[OpCode.ASTORE_W.ordinal()] = 212;
        }
        catch (NoSuchFieldError loc_19) {}
        try {
            loc_1[OpCode.ATHROW.ordinal()] = 192;
        }
        catch (NoSuchFieldError loc_20) {}
        try {
            loc_1[OpCode.BALOAD.ordinal()] = 52;
        }
        catch (NoSuchFieldError loc_21) {}
        try {
            loc_1[OpCode.BASTORE.ordinal()] = 85;
        }
        catch (NoSuchFieldError loc_22) {}
        try {
            loc_1[OpCode.BIPUSH.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_23) {}
        try {
            loc_1[OpCode.BREAKPOINT.ordinal()] = 202;
        }
        catch (NoSuchFieldError loc_24) {}
        try {
            loc_1[OpCode.CALOAD.ordinal()] = 53;
        }
        catch (NoSuchFieldError loc_25) {}
        try {
            loc_1[OpCode.CASTORE.ordinal()] = 86;
        }
        catch (NoSuchFieldError loc_26) {}
        try {
            loc_1[OpCode.CHECKCAST.ordinal()] = 193;
        }
        catch (NoSuchFieldError loc_27) {}
        try {
            loc_1[OpCode.D2F.ordinal()] = 145;
        }
        catch (NoSuchFieldError loc_28) {}
        try {
            loc_1[OpCode.D2I.ordinal()] = 143;
        }
        catch (NoSuchFieldError loc_29) {}
        try {
            loc_1[OpCode.D2L.ordinal()] = 144;
        }
        catch (NoSuchFieldError loc_30) {}
        try {
            loc_1[OpCode.DADD.ordinal()] = 100;
        }
        catch (NoSuchFieldError loc_31) {}
        try {
            loc_1[OpCode.DALOAD.ordinal()] = 50;
        }
        catch (NoSuchFieldError loc_32) {}
        try {
            loc_1[OpCode.DASTORE.ordinal()] = 83;
        }
        catch (NoSuchFieldError loc_33) {}
        try {
            loc_1[OpCode.DCMPG.ordinal()] = 153;
        }
        catch (NoSuchFieldError loc_34) {}
        try {
            loc_1[OpCode.DCMPL.ordinal()] = 152;
        }
        catch (NoSuchFieldError loc_35) {}
        try {
            loc_1[OpCode.DCONST_0.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_36) {}
        try {
            loc_1[OpCode.DCONST_1.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_37) {}
        try {
            loc_1[OpCode.DDIV.ordinal()] = 112;
        }
        catch (NoSuchFieldError loc_38) {}
        try {
            loc_1[OpCode.DLOAD.ordinal()] = 25;
        }
        catch (NoSuchFieldError loc_39) {}
        try {
            loc_1[OpCode.DLOAD_0.ordinal()] = 39;
        }
        catch (NoSuchFieldError loc_40) {}
        try {
            loc_1[OpCode.DLOAD_1.ordinal()] = 40;
        }
        catch (NoSuchFieldError loc_41) {}
        try {
            loc_1[OpCode.DLOAD_2.ordinal()] = 41;
        }
        catch (NoSuchFieldError loc_42) {}
        try {
            loc_1[OpCode.DLOAD_3.ordinal()] = 42;
        }
        catch (NoSuchFieldError loc_43) {}
        try {
            loc_1[OpCode.DLOAD_W.ordinal()] = 206;
        }
        catch (NoSuchFieldError loc_44) {}
        try {
            loc_1[OpCode.DMUL.ordinal()] = 108;
        }
        catch (NoSuchFieldError loc_45) {}
        try {
            loc_1[OpCode.DNEG.ordinal()] = 120;
        }
        catch (NoSuchFieldError loc_46) {}
        try {
            loc_1[OpCode.DREM.ordinal()] = 116;
        }
        catch (NoSuchFieldError loc_47) {}
        try {
            loc_1[OpCode.DRETURN.ordinal()] = 176;
        }
        catch (NoSuchFieldError loc_48) {}
        try {
            loc_1[OpCode.DSTORE.ordinal()] = 58;
        }
        catch (NoSuchFieldError loc_49) {}
        try {
            loc_1[OpCode.DSTORE_0.ordinal()] = 72;
        }
        catch (NoSuchFieldError loc_50) {}
        try {
            loc_1[OpCode.DSTORE_1.ordinal()] = 73;
        }
        catch (NoSuchFieldError loc_51) {}
        try {
            loc_1[OpCode.DSTORE_2.ordinal()] = 74;
        }
        catch (NoSuchFieldError loc_52) {}
        try {
            loc_1[OpCode.DSTORE_3.ordinal()] = 75;
        }
        catch (NoSuchFieldError loc_53) {}
        try {
            loc_1[OpCode.DSTORE_W.ordinal()] = 211;
        }
        catch (NoSuchFieldError loc_54) {}
        try {
            loc_1[OpCode.DSUB.ordinal()] = 104;
        }
        catch (NoSuchFieldError loc_55) {}
        try {
            loc_1[OpCode.DUP.ordinal()] = 90;
        }
        catch (NoSuchFieldError loc_56) {}
        try {
            loc_1[OpCode.DUP2.ordinal()] = 93;
        }
        catch (NoSuchFieldError loc_57) {}
        try {
            loc_1[OpCode.DUP2_X1.ordinal()] = 94;
        }
        catch (NoSuchFieldError loc_58) {}
        try {
            loc_1[OpCode.DUP2_X2.ordinal()] = 95;
        }
        catch (NoSuchFieldError loc_59) {}
        try {
            loc_1[OpCode.DUP_X1.ordinal()] = 91;
        }
        catch (NoSuchFieldError loc_60) {}
        try {
            loc_1[OpCode.DUP_X2.ordinal()] = 92;
        }
        catch (NoSuchFieldError loc_61) {}
        try {
            loc_1[OpCode.ENDFINALLY.ordinal()] = 216;
        }
        catch (NoSuchFieldError loc_62) {}
        try {
            loc_1[OpCode.F2D.ordinal()] = 142;
        }
        catch (NoSuchFieldError loc_63) {}
        try {
            loc_1[OpCode.F2I.ordinal()] = 140;
        }
        catch (NoSuchFieldError loc_64) {}
        try {
            loc_1[OpCode.F2L.ordinal()] = 141;
        }
        catch (NoSuchFieldError loc_65) {}
        try {
            loc_1[OpCode.FADD.ordinal()] = 99;
        }
        catch (NoSuchFieldError loc_66) {}
        try {
            loc_1[OpCode.FALOAD.ordinal()] = 49;
        }
        catch (NoSuchFieldError loc_67) {}
        try {
            loc_1[OpCode.FASTORE.ordinal()] = 82;
        }
        catch (NoSuchFieldError loc_68) {}
        try {
            loc_1[OpCode.FCMPG.ordinal()] = 151;
        }
        catch (NoSuchFieldError loc_69) {}
        try {
            loc_1[OpCode.FCMPL.ordinal()] = 150;
        }
        catch (NoSuchFieldError loc_70) {}
        try {
            loc_1[OpCode.FCONST_0.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_71) {}
        try {
            loc_1[OpCode.FCONST_1.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_72) {}
        try {
            loc_1[OpCode.FCONST_2.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_73) {}
        try {
            loc_1[OpCode.FDIV.ordinal()] = 111;
        }
        catch (NoSuchFieldError loc_74) {}
        try {
            loc_1[OpCode.FLOAD.ordinal()] = 24;
        }
        catch (NoSuchFieldError loc_75) {}
        try {
            loc_1[OpCode.FLOAD_0.ordinal()] = 35;
        }
        catch (NoSuchFieldError loc_76) {}
        try {
            loc_1[OpCode.FLOAD_1.ordinal()] = 36;
        }
        catch (NoSuchFieldError loc_77) {}
        try {
            loc_1[OpCode.FLOAD_2.ordinal()] = 37;
        }
        catch (NoSuchFieldError loc_78) {}
        try {
            loc_1[OpCode.FLOAD_3.ordinal()] = 38;
        }
        catch (NoSuchFieldError loc_79) {}
        try {
            loc_1[OpCode.FLOAD_W.ordinal()] = 205;
        }
        catch (NoSuchFieldError loc_80) {}
        try {
            loc_1[OpCode.FMUL.ordinal()] = 107;
        }
        catch (NoSuchFieldError loc_81) {}
        try {
            loc_1[OpCode.FNEG.ordinal()] = 119;
        }
        catch (NoSuchFieldError loc_82) {}
        try {
            loc_1[OpCode.FREM.ordinal()] = 115;
        }
        catch (NoSuchFieldError loc_83) {}
        try {
            loc_1[OpCode.FRETURN.ordinal()] = 175;
        }
        catch (NoSuchFieldError loc_84) {}
        try {
            loc_1[OpCode.FSTORE.ordinal()] = 57;
        }
        catch (NoSuchFieldError loc_85) {}
        try {
            loc_1[OpCode.FSTORE_0.ordinal()] = 68;
        }
        catch (NoSuchFieldError loc_86) {}
        try {
            loc_1[OpCode.FSTORE_1.ordinal()] = 69;
        }
        catch (NoSuchFieldError loc_87) {}
        try {
            loc_1[OpCode.FSTORE_2.ordinal()] = 70;
        }
        catch (NoSuchFieldError loc_88) {}
        try {
            loc_1[OpCode.FSTORE_3.ordinal()] = 71;
        }
        catch (NoSuchFieldError loc_89) {}
        try {
            loc_1[OpCode.FSTORE_W.ordinal()] = 210;
        }
        catch (NoSuchFieldError loc_90) {}
        try {
            loc_1[OpCode.FSUB.ordinal()] = 103;
        }
        catch (NoSuchFieldError loc_91) {}
        try {
            loc_1[OpCode.GETFIELD.ordinal()] = 181;
        }
        catch (NoSuchFieldError loc_92) {}
        try {
            loc_1[OpCode.GETSTATIC.ordinal()] = 179;
        }
        catch (NoSuchFieldError loc_93) {}
        try {
            loc_1[OpCode.GOTO.ordinal()] = 168;
        }
        catch (NoSuchFieldError loc_94) {}
        try {
            loc_1[OpCode.GOTO_W.ordinal()] = 200;
        }
        catch (NoSuchFieldError loc_95) {}
        try {
            loc_1[OpCode.I2B.ordinal()] = 146;
        }
        catch (NoSuchFieldError loc_96) {}
        try {
            loc_1[OpCode.I2C.ordinal()] = 147;
        }
        catch (NoSuchFieldError loc_97) {}
        try {
            loc_1[OpCode.I2D.ordinal()] = 136;
        }
        catch (NoSuchFieldError loc_98) {}
        try {
            loc_1[OpCode.I2F.ordinal()] = 135;
        }
        catch (NoSuchFieldError loc_99) {}
        try {
            loc_1[OpCode.I2L.ordinal()] = 134;
        }
        catch (NoSuchFieldError loc_100) {}
        try {
            loc_1[OpCode.I2S.ordinal()] = 148;
        }
        catch (NoSuchFieldError loc_101) {}
        try {
            loc_1[OpCode.IADD.ordinal()] = 97;
        }
        catch (NoSuchFieldError loc_102) {}
        try {
            loc_1[OpCode.IALOAD.ordinal()] = 47;
        }
        catch (NoSuchFieldError loc_103) {}
        try {
            loc_1[OpCode.IAND.ordinal()] = 127;
        }
        catch (NoSuchFieldError loc_104) {}
        try {
            loc_1[OpCode.IASTORE.ordinal()] = 80;
        }
        catch (NoSuchFieldError loc_105) {}
        try {
            loc_1[OpCode.ICONST_0.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_106) {}
        try {
            loc_1[OpCode.ICONST_1.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_107) {}
        try {
            loc_1[OpCode.ICONST_2.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_108) {}
        try {
            loc_1[OpCode.ICONST_3.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_109) {}
        try {
            loc_1[OpCode.ICONST_4.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_110) {}
        try {
            loc_1[OpCode.ICONST_5.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_111) {}
        try {
            loc_1[OpCode.ICONST_M1.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_112) {}
        try {
            loc_1[OpCode.IDIV.ordinal()] = 109;
        }
        catch (NoSuchFieldError loc_113) {}
        try {
            loc_1[OpCode.IFEQ.ordinal()] = 154;
        }
        catch (NoSuchFieldError loc_114) {}
        try {
            loc_1[OpCode.IFGE.ordinal()] = 157;
        }
        catch (NoSuchFieldError loc_115) {}
        try {
            loc_1[OpCode.IFGT.ordinal()] = 158;
        }
        catch (NoSuchFieldError loc_116) {}
        try {
            loc_1[OpCode.IFLE.ordinal()] = 159;
        }
        catch (NoSuchFieldError loc_117) {}
        try {
            loc_1[OpCode.IFLT.ordinal()] = 156;
        }
        catch (NoSuchFieldError loc_118) {}
        try {
            loc_1[OpCode.IFNE.ordinal()] = 155;
        }
        catch (NoSuchFieldError loc_119) {}
        try {
            loc_1[OpCode.IFNONNULL.ordinal()] = 199;
        }
        catch (NoSuchFieldError loc_120) {}
        try {
            loc_1[OpCode.IFNULL.ordinal()] = 198;
        }
        catch (NoSuchFieldError loc_121) {}
        try {
            loc_1[OpCode.IF_ACMPEQ.ordinal()] = 166;
        }
        catch (NoSuchFieldError loc_122) {}
        try {
            loc_1[OpCode.IF_ACMPNE.ordinal()] = 167;
        }
        catch (NoSuchFieldError loc_123) {}
        try {
            loc_1[OpCode.IF_ICMPEQ.ordinal()] = 160;
        }
        catch (NoSuchFieldError loc_124) {}
        try {
            loc_1[OpCode.IF_ICMPGE.ordinal()] = 163;
        }
        catch (NoSuchFieldError loc_125) {}
        try {
            loc_1[OpCode.IF_ICMPGT.ordinal()] = 164;
        }
        catch (NoSuchFieldError loc_126) {}
        try {
            loc_1[OpCode.IF_ICMPLE.ordinal()] = 165;
        }
        catch (NoSuchFieldError loc_127) {}
        try {
            loc_1[OpCode.IF_ICMPLT.ordinal()] = 162;
        }
        catch (NoSuchFieldError loc_128) {}
        try {
            loc_1[OpCode.IF_ICMPNE.ordinal()] = 161;
        }
        catch (NoSuchFieldError loc_129) {}
        try {
            loc_1[OpCode.IINC.ordinal()] = 133;
        }
        catch (NoSuchFieldError loc_130) {}
        try {
            loc_1[OpCode.IINC_W.ordinal()] = 213;
        }
        catch (NoSuchFieldError loc_131) {}
        try {
            loc_1[OpCode.ILOAD.ordinal()] = 22;
        }
        catch (NoSuchFieldError loc_132) {}
        try {
            loc_1[OpCode.ILOAD_0.ordinal()] = 27;
        }
        catch (NoSuchFieldError loc_133) {}
        try {
            loc_1[OpCode.ILOAD_1.ordinal()] = 28;
        }
        catch (NoSuchFieldError loc_134) {}
        try {
            loc_1[OpCode.ILOAD_2.ordinal()] = 29;
        }
        catch (NoSuchFieldError loc_135) {}
        try {
            loc_1[OpCode.ILOAD_3.ordinal()] = 30;
        }
        catch (NoSuchFieldError loc_136) {}
        try {
            loc_1[OpCode.ILOAD_W.ordinal()] = 203;
        }
        catch (NoSuchFieldError loc_137) {}
        try {
            loc_1[OpCode.IMUL.ordinal()] = 105;
        }
        catch (NoSuchFieldError loc_138) {}
        try {
            loc_1[OpCode.INEG.ordinal()] = 117;
        }
        catch (NoSuchFieldError loc_139) {}
        try {
            loc_1[OpCode.INSTANCEOF.ordinal()] = 194;
        }
        catch (NoSuchFieldError loc_140) {}
        try {
            loc_1[OpCode.INVOKEDYNAMIC.ordinal()] = 187;
        }
        catch (NoSuchFieldError loc_141) {}
        try {
            loc_1[OpCode.INVOKEINTERFACE.ordinal()] = 186;
        }
        catch (NoSuchFieldError loc_142) {}
        try {
            loc_1[OpCode.INVOKESPECIAL.ordinal()] = 184;
        }
        catch (NoSuchFieldError loc_143) {}
        try {
            loc_1[OpCode.INVOKESTATIC.ordinal()] = 185;
        }
        catch (NoSuchFieldError loc_144) {}
        try {
            loc_1[OpCode.INVOKEVIRTUAL.ordinal()] = 183;
        }
        catch (NoSuchFieldError loc_145) {}
        try {
            loc_1[OpCode.IOR.ordinal()] = 129;
        }
        catch (NoSuchFieldError loc_146) {}
        try {
            loc_1[OpCode.IREM.ordinal()] = 113;
        }
        catch (NoSuchFieldError loc_147) {}
        try {
            loc_1[OpCode.IRETURN.ordinal()] = 173;
        }
        catch (NoSuchFieldError loc_148) {}
        try {
            loc_1[OpCode.ISHL.ordinal()] = 121;
        }
        catch (NoSuchFieldError loc_149) {}
        try {
            loc_1[OpCode.ISHR.ordinal()] = 123;
        }
        catch (NoSuchFieldError loc_150) {}
        try {
            loc_1[OpCode.ISTORE.ordinal()] = 55;
        }
        catch (NoSuchFieldError loc_151) {}
        try {
            loc_1[OpCode.ISTORE_0.ordinal()] = 60;
        }
        catch (NoSuchFieldError loc_152) {}
        try {
            loc_1[OpCode.ISTORE_1.ordinal()] = 61;
        }
        catch (NoSuchFieldError loc_153) {}
        try {
            loc_1[OpCode.ISTORE_2.ordinal()] = 62;
        }
        catch (NoSuchFieldError loc_154) {}
        try {
            loc_1[OpCode.ISTORE_3.ordinal()] = 63;
        }
        catch (NoSuchFieldError loc_155) {}
        try {
            loc_1[OpCode.ISTORE_W.ordinal()] = 208;
        }
        catch (NoSuchFieldError loc_156) {}
        try {
            loc_1[OpCode.ISUB.ordinal()] = 101;
        }
        catch (NoSuchFieldError loc_157) {}
        try {
            loc_1[OpCode.IUSHR.ordinal()] = 125;
        }
        catch (NoSuchFieldError loc_158) {}
        try {
            loc_1[OpCode.IXOR.ordinal()] = 131;
        }
        catch (NoSuchFieldError loc_159) {}
        try {
            loc_1[OpCode.JSR.ordinal()] = 169;
        }
        catch (NoSuchFieldError loc_160) {}
        try {
            loc_1[OpCode.JSR_W.ordinal()] = 201;
        }
        catch (NoSuchFieldError loc_161) {}
        try {
            loc_1[OpCode.L2D.ordinal()] = 139;
        }
        catch (NoSuchFieldError loc_162) {}
        try {
            loc_1[OpCode.L2F.ordinal()] = 138;
        }
        catch (NoSuchFieldError loc_163) {}
        try {
            loc_1[OpCode.L2I.ordinal()] = 137;
        }
        catch (NoSuchFieldError loc_164) {}
        try {
            loc_1[OpCode.LADD.ordinal()] = 98;
        }
        catch (NoSuchFieldError loc_165) {}
        try {
            loc_1[OpCode.LALOAD.ordinal()] = 48;
        }
        catch (NoSuchFieldError loc_166) {}
        try {
            loc_1[OpCode.LAND.ordinal()] = 128;
        }
        catch (NoSuchFieldError loc_167) {}
        try {
            loc_1[OpCode.LASTORE.ordinal()] = 81;
        }
        catch (NoSuchFieldError loc_168) {}
        try {
            loc_1[OpCode.LCMP.ordinal()] = 149;
        }
        catch (NoSuchFieldError loc_169) {}
        try {
            loc_1[OpCode.LCONST_0.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_170) {}
        try {
            loc_1[OpCode.LCONST_1.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_171) {}
        try {
            loc_1[OpCode.LDC.ordinal()] = 19;
        }
        catch (NoSuchFieldError loc_172) {}
        try {
            loc_1[OpCode.LDC2_W.ordinal()] = 21;
        }
        catch (NoSuchFieldError loc_173) {}
        try {
            loc_1[OpCode.LDC_W.ordinal()] = 20;
        }
        catch (NoSuchFieldError loc_174) {}
        try {
            loc_1[OpCode.LDIV.ordinal()] = 110;
        }
        catch (NoSuchFieldError loc_175) {}
        try {
            loc_1[OpCode.LEAVE.ordinal()] = 215;
        }
        catch (NoSuchFieldError loc_176) {}
        try {
            loc_1[OpCode.LLOAD.ordinal()] = 23;
        }
        catch (NoSuchFieldError loc_177) {}
        try {
            loc_1[OpCode.LLOAD_0.ordinal()] = 31;
        }
        catch (NoSuchFieldError loc_178) {}
        try {
            loc_1[OpCode.LLOAD_1.ordinal()] = 32;
        }
        catch (NoSuchFieldError loc_179) {}
        try {
            loc_1[OpCode.LLOAD_2.ordinal()] = 33;
        }
        catch (NoSuchFieldError loc_180) {}
        try {
            loc_1[OpCode.LLOAD_3.ordinal()] = 34;
        }
        catch (NoSuchFieldError loc_181) {}
        try {
            loc_1[OpCode.LLOAD_W.ordinal()] = 204;
        }
        catch (NoSuchFieldError loc_182) {}
        try {
            loc_1[OpCode.LMUL.ordinal()] = 106;
        }
        catch (NoSuchFieldError loc_183) {}
        try {
            loc_1[OpCode.LNEG.ordinal()] = 118;
        }
        catch (NoSuchFieldError loc_184) {}
        try {
            loc_1[OpCode.LOOKUPSWITCH.ordinal()] = 172;
        }
        catch (NoSuchFieldError loc_185) {}
        try {
            loc_1[OpCode.LOR.ordinal()] = 130;
        }
        catch (NoSuchFieldError loc_186) {}
        try {
            loc_1[OpCode.LREM.ordinal()] = 114;
        }
        catch (NoSuchFieldError loc_187) {}
        try {
            loc_1[OpCode.LRETURN.ordinal()] = 174;
        }
        catch (NoSuchFieldError loc_188) {}
        try {
            loc_1[OpCode.LSHL.ordinal()] = 122;
        }
        catch (NoSuchFieldError loc_189) {}
        try {
            loc_1[OpCode.LSHR.ordinal()] = 124;
        }
        catch (NoSuchFieldError loc_190) {}
        try {
            loc_1[OpCode.LSTORE.ordinal()] = 56;
        }
        catch (NoSuchFieldError loc_191) {}
        try {
            loc_1[OpCode.LSTORE_0.ordinal()] = 64;
        }
        catch (NoSuchFieldError loc_192) {}
        try {
            loc_1[OpCode.LSTORE_1.ordinal()] = 65;
        }
        catch (NoSuchFieldError loc_193) {}
        try {
            loc_1[OpCode.LSTORE_2.ordinal()] = 66;
        }
        catch (NoSuchFieldError loc_194) {}
        try {
            loc_1[OpCode.LSTORE_3.ordinal()] = 67;
        }
        catch (NoSuchFieldError loc_195) {}
        try {
            loc_1[OpCode.LSTORE_W.ordinal()] = 209;
        }
        catch (NoSuchFieldError loc_196) {}
        try {
            loc_1[OpCode.LSUB.ordinal()] = 102;
        }
        catch (NoSuchFieldError loc_197) {}
        try {
            loc_1[OpCode.LUSHR.ordinal()] = 126;
        }
        catch (NoSuchFieldError loc_198) {}
        try {
            loc_1[OpCode.LXOR.ordinal()] = 132;
        }
        catch (NoSuchFieldError loc_199) {}
        try {
            loc_1[OpCode.MONITORENTER.ordinal()] = 195;
        }
        catch (NoSuchFieldError loc_200) {}
        try {
            loc_1[OpCode.MONITOREXIT.ordinal()] = 196;
        }
        catch (NoSuchFieldError loc_201) {}
        try {
            loc_1[OpCode.MULTIANEWARRAY.ordinal()] = 197;
        }
        catch (NoSuchFieldError loc_202) {}
        try {
            loc_1[OpCode.NEW.ordinal()] = 188;
        }
        catch (NoSuchFieldError loc_203) {}
        try {
            loc_1[OpCode.NEWARRAY.ordinal()] = 189;
        }
        catch (NoSuchFieldError loc_204) {}
        try {
            loc_1[OpCode.NOP.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_205) {}
        try {
            loc_1[OpCode.POP.ordinal()] = 88;
        }
        catch (NoSuchFieldError loc_206) {}
        try {
            loc_1[OpCode.POP2.ordinal()] = 89;
        }
        catch (NoSuchFieldError loc_207) {}
        try {
            loc_1[OpCode.PUTFIELD.ordinal()] = 182;
        }
        catch (NoSuchFieldError loc_208) {}
        try {
            loc_1[OpCode.PUTSTATIC.ordinal()] = 180;
        }
        catch (NoSuchFieldError loc_209) {}
        try {
            loc_1[OpCode.RET.ordinal()] = 170;
        }
        catch (NoSuchFieldError loc_210) {}
        try {
            loc_1[OpCode.RETURN.ordinal()] = 178;
        }
        catch (NoSuchFieldError loc_211) {}
        try {
            loc_1[OpCode.RET_W.ordinal()] = 214;
        }
        catch (NoSuchFieldError loc_212) {}
        try {
            loc_1[OpCode.SALOAD.ordinal()] = 54;
        }
        catch (NoSuchFieldError loc_213) {}
        try {
            loc_1[OpCode.SASTORE.ordinal()] = 87;
        }
        catch (NoSuchFieldError loc_214) {}
        try {
            loc_1[OpCode.SIPUSH.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_215) {}
        try {
            loc_1[OpCode.SWAP.ordinal()] = 96;
        }
        catch (NoSuchFieldError loc_216) {}
        try {
            loc_1[OpCode.TABLESWITCH.ordinal()] = 171;
        }
        catch (NoSuchFieldError loc_217) {}
        return Instruction.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType() {
        final int[] loc_0 = Instruction.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[OperandType.values().length];
        try {
            loc_1[OperandType.BranchTarget.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[OperandType.BranchTargetWide.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[OperandType.Constant.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[OperandType.DynamicCallSite.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[OperandType.FieldReference.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[OperandType.I1.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[OperandType.I2.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[OperandType.I8.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[OperandType.Local.ordinal()] = 16;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[OperandType.LocalI1.ordinal()] = 17;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[OperandType.LocalI2.ordinal()] = 18;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[OperandType.MethodReference.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[OperandType.None.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_14) {}
        try {
            loc_1[OperandType.PrimitiveTypeCode.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_15) {}
        try {
            loc_1[OperandType.Switch.ordinal()] = 15;
        }
        catch (NoSuchFieldError loc_16) {}
        try {
            loc_1[OperandType.TypeReference.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_17) {}
        try {
            loc_1[OperandType.TypeReferenceU1.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_18) {}
        try {
            loc_1[OperandType.WideConstant.ordinal()] = 14;
        }
        catch (NoSuchFieldError loc_19) {}
        return Instruction.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType = loc_1;
    }
}
