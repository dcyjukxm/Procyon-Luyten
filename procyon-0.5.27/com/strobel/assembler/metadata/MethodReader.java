package com.strobel.assembler.metadata;

import java.lang.reflect.*;
import com.strobel.assembler.ir.*;
import java.util.*;
import com.strobel.assembler.ir.attributes.*;
import com.strobel.core.*;

public class MethodReader
{
    private final MethodDefinition _methodDefinition;
    private final CodeAttribute _code;
    private final IMetadataScope _scope;
    private final MethodBody _methodBody;
    private final TypeReference _declaringType;
    private final int _modifiers;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
    
    public MethodReader(final MethodDefinition methodDefinition, final IMetadataScope scope) {
        super();
        this._methodDefinition = VerifyArgument.notNull(methodDefinition, "methodDefinition");
        this._scope = VerifyArgument.notNull(scope, "scope");
        this._declaringType = methodDefinition.getDeclaringType();
        this._modifiers = methodDefinition.getModifiers();
        this._code = SourceAttribute.find("Code", methodDefinition.getSourceAttributes());
        (this._methodBody = new MethodBody(methodDefinition)).setCodeSize(this._code.getCode().size());
        this._methodBody.setMaxStackSize(this._code.getMaxStack());
        this._methodBody.setMaxLocals(this._code.getMaxLocals());
    }
    
    public MethodBody readBody() {
        final Buffer b = this._code.getCode();
        b.position(0);
        final InstructionCollection body = this._methodBody.getInstructions();
        final VariableDefinitionCollection variables = this._methodBody.getVariables();
        final LocalVariableTableAttribute localVariableTable = SourceAttribute.find("LocalVariableTable", this._code.getAttributes());
        final LocalVariableTableAttribute localVariableTypeTable = SourceAttribute.find("LocalVariableTypeTable", this._code.getAttributes());
        final boolean hasThis = !Modifier.isStatic(this._modifiers);
        final List<ParameterDefinition> parameters = this._methodDefinition.getParameters();
        if (hasThis) {
            final ParameterDefinition thisParameter = new ParameterDefinition(0, "this", this._declaringType);
            final VariableDefinition thisVariable = new VariableDefinition(0, "this", this._methodDefinition, this._declaringType);
            thisVariable.setScopeStart(0);
            thisVariable.setScopeEnd(this._code.getCodeSize());
            thisVariable.setFromMetadata(false);
            thisVariable.setParameter(thisParameter);
            variables.add(thisVariable);
            this._methodBody.setThisParameter(thisParameter);
        }
        for (int i = 0; i < parameters.size(); ++i) {
            final ParameterDefinition parameter = parameters.get(i);
            final int variableSlot = parameter.getSlot();
            final VariableDefinition variable = new VariableDefinition(variableSlot, parameter.getName(), this._methodDefinition, parameter.getParameterType());
            variable.setScopeStart(0);
            variable.setScopeEnd(this._code.getCodeSize());
            variable.setTypeKnown(true);
            variable.setFromMetadata(false);
            variable.setParameter(parameter);
            variables.add(variable);
        }
        if (localVariableTable != null) {
            this.processLocalVariableTable(variables, localVariableTable, parameters);
        }
        if (localVariableTypeTable != null) {
            this.processLocalVariableTable(variables, localVariableTypeTable, parameters);
        }
        for (final VariableDefinition variable2 : variables) {
            if (!variable2.isFromMetadata()) {
                variable2.setScopeStart(-1);
                variable2.setScopeEnd(-1);
            }
        }
        final Fixup[] fixups = new Fixup[b.size()];
        while (b.position() < b.size()) {
            final int offset = b.position();
            int code = b.readUnsignedByte();
            if (code == 196) {
                code = (code << 8 | b.readUnsignedByte());
            }
            final OpCode op = OpCode.get(code);
            Instruction instruction = null;
            switch ($SWITCH_TABLE$com$strobel$assembler$ir$OperandType()[op.getOperandType().ordinal()]) {
                case 1: {
                    if (op.isLoad() || op.isStore()) {
                        variables.reference(OpCodeHelpers.getLoadStoreMacroArgumentIndex(op), op, offset);
                    }
                    instruction = Instruction.create(op);
                    break;
                }
                case 2: {
                    instruction = Instruction.create(op, BuiltinTypes.fromPrimitiveTypeCode(b.readUnsignedByte()));
                    break;
                }
                case 3: {
                    final int typeToken = b.readUnsignedShort();
                    instruction = Instruction.create(op, this._scope.lookupType(typeToken));
                    break;
                }
                case 4: {
                    instruction = Instruction.create(op, this._scope.lookupType(b.readUnsignedShort()), b.readUnsignedByte());
                    break;
                }
                case 5: {
                    instruction = Instruction.create(op, this._scope.lookupDynamicCallSite(b.readUnsignedShort()));
                    b.readUnsignedByte();
                    b.readUnsignedByte();
                    break;
                }
                case 6: {
                    instruction = Instruction.create(op, this._scope.lookupMethod(b.readUnsignedShort()));
                    if (op == OpCode.INVOKEINTERFACE) {
                        b.readUnsignedByte();
                        b.readUnsignedByte();
                        break;
                    }
                    break;
                }
                case 7: {
                    instruction = Instruction.create(op, this._scope.lookupField(b.readUnsignedShort()));
                    break;
                }
                case 8:
                case 9: {
                    instruction = new Instruction(op);
                    int targetOffset;
                    if (op.isWide()) {
                        targetOffset = offset + this._scope.lookupConstant(b.readUnsignedShort());
                    }
                    else if (op.getOperandType() == OperandType.BranchTargetWide) {
                        targetOffset = offset + b.readInt();
                    }
                    else {
                        targetOffset = offset + b.readShort();
                    }
                    if (targetOffset < offset) {
                        final Instruction target = body.atOffset(targetOffset);
                        if (!target.hasLabel()) {
                            target.setLabel(new Label(targetOffset));
                        }
                        instruction.setOperand(target);
                        break;
                    }
                    if (targetOffset == offset) {
                        instruction.setOperand(instruction);
                        instruction.setLabel(new Label(offset));
                        break;
                    }
                    if (targetOffset > b.size()) {
                        instruction.setOperand(new Instruction(targetOffset, OpCode.NOP));
                        break;
                    }
                    final Fixup oldFixup = fixups[targetOffset];
                    final Fixup newFixup = new Fixup() {
                        @Override
                        public void fix(final Instruction target) {
                            instruction.setOperand(target);
                        }
                    };
                    fixups[targetOffset] = ((oldFixup != null) ? Fixup.combine(oldFixup, newFixup) : newFixup);
                    break;
                }
                case 10: {
                    instruction = Instruction.create(op, b.readByte());
                    break;
                }
                case 11: {
                    instruction = Instruction.create(op, b.readShort());
                    break;
                }
                case 12: {
                    instruction = Instruction.create(op, b.readLong());
                    break;
                }
                case 13: {
                    instruction = new Instruction(op, this._scope.lookupConstant(b.readUnsignedByte()));
                    break;
                }
                case 14: {
                    final int constantToken = b.readUnsignedShort();
                    instruction = new Instruction(op, this._scope.lookupConstant(constantToken));
                    break;
                }
                case 15: {
                    while (b.position() % 4 != 0) {
                        b.readByte();
                    }
                    final SwitchInfo switchInfo = new SwitchInfo();
                    final int defaultOffset = offset + b.readInt();
                    instruction = Instruction.create(op, switchInfo);
                    if (defaultOffset < offset) {
                        switchInfo.setDefaultTarget(body.atOffset(defaultOffset));
                    }
                    else if (defaultOffset == offset) {
                        switchInfo.setDefaultTarget(instruction);
                    }
                    else {
                        switchInfo.setDefaultTarget(new Instruction(defaultOffset, OpCode.NOP));
                        final Fixup oldFixup2 = fixups[defaultOffset];
                        final Fixup newFixup2 = new Fixup() {
                            @Override
                            public void fix(final Instruction target) {
                                switchInfo.setDefaultTarget(target);
                            }
                        };
                        fixups[defaultOffset] = ((oldFixup2 != null) ? Fixup.combine(oldFixup2, newFixup2) : newFixup2);
                    }
                    if (op == OpCode.TABLESWITCH) {
                        final int low = b.readInt();
                        final int high = b.readInt();
                        final Instruction[] targets = new Instruction[high - low + 1];
                        switchInfo.setLowValue(low);
                        switchInfo.setHighValue(high);
                        for (int j = 0; j < targets.length; ++j) {
                            final int targetIndex = j;
                            final int targetOffset2 = offset + b.readInt();
                            if (targetOffset2 < offset) {
                                targets[targetIndex] = body.atOffset(targetOffset2);
                            }
                            else if (targetOffset2 == offset) {
                                targets[targetIndex] = instruction;
                            }
                            else {
                                targets[targetIndex] = new Instruction(targetOffset2, OpCode.NOP);
                                final Fixup oldFixup3 = fixups[targetOffset2];
                                final Fixup newFixup3 = new Fixup() {
                                    @Override
                                    public void fix(final Instruction target) {
                                        targets[targetIndex] = target;
                                    }
                                };
                                fixups[targetOffset2] = ((oldFixup3 != null) ? Fixup.combine(oldFixup3, newFixup3) : newFixup3);
                            }
                        }
                        switchInfo.setTargets(targets);
                        break;
                    }
                    final int pairCount = b.readInt();
                    final int[] keys = new int[pairCount];
                    final Instruction[] targets = new Instruction[pairCount];
                    for (int j = 0; j < pairCount; ++j) {
                        final int targetIndex = j;
                        keys[targetIndex] = b.readInt();
                        final int targetOffset2 = offset + b.readInt();
                        if (targetOffset2 < offset) {
                            targets[targetIndex] = body.atOffset(targetOffset2);
                        }
                        else if (targetOffset2 == offset) {
                            targets[targetIndex] = instruction;
                        }
                        else {
                            targets[targetIndex] = new Instruction(targetOffset2, OpCode.NOP);
                            final Fixup oldFixup3 = fixups[targetOffset2];
                            final Fixup newFixup3 = new Fixup() {
                                @Override
                                public void fix(final Instruction target) {
                                    targets[targetIndex] = target;
                                }
                            };
                            fixups[targetOffset2] = ((oldFixup3 != null) ? Fixup.combine(oldFixup3, newFixup3) : newFixup3);
                        }
                    }
                    switchInfo.setKeys(keys);
                    switchInfo.setTargets(targets);
                    break;
                }
                case 16: {
                    int variableSlot2;
                    if (op.isWide()) {
                        variableSlot2 = b.readUnsignedShort();
                    }
                    else {
                        variableSlot2 = b.readUnsignedByte();
                    }
                    final VariableReference variable3 = variables.reference(variableSlot2, op, offset);
                    if (variableSlot2 < 0) {
                        instruction = new Instruction(op, new ErrorOperand("!!! BAD LOCAL: " + variableSlot2 + " !!!"));
                        break;
                    }
                    instruction = Instruction.create(op, variable3);
                    break;
                }
                case 17: {
                    int variableSlot2;
                    if (op.isWide()) {
                        variableSlot2 = b.readUnsignedShort();
                    }
                    else {
                        variableSlot2 = b.readUnsignedByte();
                    }
                    final VariableReference variable4 = variables.reference(variableSlot2, op, offset);
                    final int operand = b.readByte();
                    if (variableSlot2 < 0) {
                        instruction = new Instruction(op, new Object[] { new ErrorOperand("!!! BAD LOCAL: " + variableSlot2 + " !!!"), operand });
                        break;
                    }
                    instruction = Instruction.create(op, variable4, operand);
                    break;
                }
                case 18: {
                    int variableSlot2;
                    if (op.isWide()) {
                        variableSlot2 = b.readUnsignedShort();
                    }
                    else {
                        variableSlot2 = b.readUnsignedByte();
                    }
                    final VariableReference variable4 = variables.reference(variableSlot2, op, offset);
                    final int operand = b.readShort();
                    if (variableSlot2 < 0) {
                        instruction = new Instruction(op, new Object[] { new ErrorOperand("!!! BAD LOCAL: " + variableSlot2 + " !!!"), operand });
                        break;
                    }
                    instruction = Instruction.create(op, variable4, operand);
                    break;
                }
                default: {
                    throw new IllegalStateException("Unrecognized opcode: " + code);
                }
            }
            instruction.setOffset(offset);
            body.add(instruction);
            final Fixup fixup = fixups[offset];
            if (fixup != null) {
                if (!instruction.hasLabel()) {
                    instruction.setLabel(new Label(offset));
                }
                fixup.fix(instruction);
            }
        }
        int labelCount = 0;
        for (int k = 0; k < body.size(); ++k) {
            final Instruction instruction2 = body.get(k);
            final OpCode code2 = instruction2.getOpCode();
            final Object operand2 = instruction2.hasOperand() ? instruction2.getOperand(0) : null;
            if (operand2 instanceof VariableDefinition) {
                final VariableDefinition currentVariable = (VariableDefinition)operand2;
                int effectiveOffset;
                if (code2.isStore()) {
                    effectiveOffset = instruction2.getOffset() + code2.getSize() + code2.getOperandType().getBaseSize();
                }
                else {
                    effectiveOffset = instruction2.getOffset();
                }
                VariableDefinition actualVariable = variables.tryFind(currentVariable.getSlot(), effectiveOffset);
                if (actualVariable == null && code2.isStore()) {
                    actualVariable = variables.find(currentVariable.getSlot(), effectiveOffset + code2.getSize() + code2.getOperandType().getBaseSize());
                }
                if (actualVariable != currentVariable) {
                    if (instruction2.getOperandCount() > 1) {
                        final Object[] operands = new Object[instruction2.getOperandCount()];
                        operands[0] = actualVariable;
                        for (int l = 1; l < operands.length; ++l) {
                            operands[l] = instruction2.getOperand(l);
                        }
                        instruction2.setOperand(operands);
                    }
                    else {
                        instruction2.setOperand(actualVariable);
                    }
                }
            }
            if (instruction2.hasLabel()) {
                instruction2.getLabel().setIndex(labelCount++);
            }
        }
        final List<ExceptionTableEntry> exceptionTable = this._code.getExceptionTableEntries();
        if (!exceptionTable.isEmpty()) {
            this._methodBody.getExceptionHandlers().addAll(ExceptionHandlerMapper.run(body, exceptionTable));
        }
        return this._methodBody;
    }
    
    private void processLocalVariableTable(final VariableDefinitionCollection variables, final LocalVariableTableAttribute table, final List<ParameterDefinition> parameters) {
        for (final LocalVariableTableEntry entry : table.getEntries()) {
            final int slot = entry.getIndex();
            final int scopeStart = entry.getScopeOffset();
            final int scopeEnd = scopeStart + entry.getScopeLength();
            VariableDefinition variable = variables.tryFind(slot, scopeStart);
            if (variable == null) {
                variable = new VariableDefinition(slot, entry.getName(), this._methodDefinition, entry.getType());
                variables.add(variable);
            }
            else if (!StringUtilities.isNullOrEmpty(entry.getName())) {
                variable.setName(entry.getName());
            }
            variable.setVariableType(entry.getType());
            variable.setTypeKnown(true);
            variable.setFromMetadata(true);
            variable.setScopeStart(scopeStart);
            variable.setScopeEnd(scopeEnd);
            if (entry.getScopeOffset() == 0) {
                ParameterDefinition parameter = null;
                for (int j = 0; j < parameters.size(); ++j) {
                    if (parameters.get(j).getSlot() == entry.getIndex()) {
                        parameter = parameters.get(j);
                        break;
                    }
                }
                if (parameter == null || parameter.hasName()) {
                    continue;
                }
                parameter.setName(entry.getName());
            }
        }
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OperandType() {
        final int[] loc_0 = MethodReader.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType;
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
        return MethodReader.$SWITCH_TABLE$com$strobel$assembler$ir$OperandType = loc_1;
    }
    
    private abstract static class Fixup
    {
        public abstract void fix(final Instruction param_0);
        
        public static Fixup combine(final Fixup first, final Fixup second) {
            Fixup[] fixups;
            if (first instanceof MultiFixup) {
                final MultiFixup m1 = (MultiFixup)first;
                if (second instanceof MultiFixup) {
                    final MultiFixup m2 = (MultiFixup)second;
                    fixups = new Fixup[MultiFixup.access$0(m1).length + MultiFixup.access$0(m2).length];
                    System.arraycopy(MultiFixup.access$0(m2), 0, fixups, MultiFixup.access$0(m1).length, MultiFixup.access$0(m2).length);
                }
                else {
                    fixups = new Fixup[MultiFixup.access$0(m1).length + 1];
                    fixups[MultiFixup.access$0(m1).length] = second;
                }
                System.arraycopy(MultiFixup.access$0(m1), 0, fixups, 0, MultiFixup.access$0(m1).length);
            }
            else if (second instanceof MultiFixup) {
                final MultiFixup m3 = (MultiFixup)second;
                fixups = new Fixup[1 + MultiFixup.access$0(m3).length];
                System.arraycopy(MultiFixup.access$0(m3), 0, fixups, 1, MultiFixup.access$0(m3).length);
            }
            else {
                fixups = new Fixup[] { first, second };
            }
            return new MultiFixup(fixups, null);
        }
        
        private static final class MultiFixup extends Fixup
        {
            private final Fixup[] _fixups;
            
            private MultiFixup(final Fixup... fixups) {
                super(null);
                this._fixups = VerifyArgument.noNullElements(fixups, "fixups");
            }
            
            @Override
            public void fix(final Instruction target) {
                Fixup[] loc_1;
                for (int loc_0 = (loc_1 = this._fixups).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final Fixup fixup = loc_1[loc_2];
                    fixup.fix(target);
                }
            }
            
            static /* synthetic */ Fixup[] access$0(final MultiFixup param_0) {
                return param_0._fixups;
            }
        }
    }
}
