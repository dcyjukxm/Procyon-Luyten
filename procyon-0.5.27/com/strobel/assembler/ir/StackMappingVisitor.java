package com.strobel.assembler.ir;

import com.strobel.assembler.ir.attributes.*;
import com.strobel.assembler.metadata.annotations.*;
import java.util.*;
import com.strobel.decompiler.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public class StackMappingVisitor implements MethodVisitor
{
    private final MethodVisitor _innerVisitor;
    private int _maxLocals;
    private List<FrameValue> _stack;
    private List<FrameValue> _locals;
    private Map<Instruction, TypeReference> _initializations;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    public StackMappingVisitor() {
        super();
        this._stack = new ArrayList<FrameValue>();
        this._locals = new ArrayList<FrameValue>();
        this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        this._innerVisitor = null;
    }
    
    public StackMappingVisitor(final MethodVisitor innerVisitor) {
        super();
        this._stack = new ArrayList<FrameValue>();
        this._locals = new ArrayList<FrameValue>();
        this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        this._innerVisitor = innerVisitor;
    }
    
    public final Frame buildFrame() {
        return new Frame(FrameType.New, this._locals.toArray(new FrameValue[this._locals.size()]), this._stack.toArray(new FrameValue[this._stack.size()]));
    }
    
    public final int getStackSize() {
        return (this._stack == null) ? 0 : this._stack.size();
    }
    
    public final int getLocalCount() {
        return (this._locals == null) ? 0 : this._locals.size();
    }
    
    public final FrameValue getStackValue(final int offset) {
        VerifyArgument.inRange(0, this.getStackSize(), offset, "offset");
        return this._stack.get(this._stack.size() - offset - 1);
    }
    
    public final FrameValue getLocalValue(final int slot) {
        VerifyArgument.inRange(0, this.getLocalCount(), slot, "slot");
        return this._locals.get(slot);
    }
    
    public final Map<Instruction, TypeReference> getInitializations() {
        return Collections.unmodifiableMap(this._initializations);
    }
    
    public final FrameValue[] getStackSnapshot() {
        if (this._stack == null || this._stack.isEmpty()) {
            return FrameValue.EMPTY_VALUES;
        }
        return this._stack.toArray(new FrameValue[this._stack.size()]);
    }
    
    public final FrameValue[] getLocalsSnapshot() {
        if (this._locals == null || this._locals.isEmpty()) {
            return FrameValue.EMPTY_VALUES;
        }
        return this._locals.toArray(new FrameValue[this._locals.size()]);
    }
    
    @Override
    public boolean canVisitBody() {
        return true;
    }
    
    @Override
    public InstructionVisitor visitBody(final MethodBody body) {
        if (this._innerVisitor != null && this._innerVisitor.canVisitBody()) {
            return new InstructionAnalyzer(body, this._innerVisitor.visitBody(body), (InstructionAnalyzer)null);
        }
        return new InstructionAnalyzer(body, (InstructionAnalyzer)null);
    }
    
    @Override
    public void visitEnd() {
        if (this._innerVisitor != null) {
            this._innerVisitor.visitEnd();
        }
    }
    
    @Override
    public void visitFrame(final Frame frame) {
        VerifyArgument.notNull(frame, "frame");
        if (frame.getFrameType() != FrameType.New) {
            throw Error.stackMapperCalledWithUnexpandedFrame(frame.getFrameType());
        }
        if (this._innerVisitor != null) {
            this._innerVisitor.visitFrame(frame);
        }
        if (this._locals != null) {
            this._locals.clear();
            this._stack.clear();
        }
        else {
            this._locals = new ArrayList<FrameValue>();
            this._stack = new ArrayList<FrameValue>();
            this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        }
        for (final FrameValue frameValue : frame.getLocalValues()) {
            this._locals.add(frameValue);
        }
        for (final FrameValue frameValue : frame.getStackValues()) {
            this._stack.add(frameValue);
        }
    }
    
    @Override
    public void visitLineNumber(final Instruction instruction, final int lineNumber) {
        if (this._innerVisitor != null) {
            this._innerVisitor.visitLineNumber(instruction, lineNumber);
        }
    }
    
    @Override
    public void visitAttribute(final SourceAttribute attribute) {
        if (this._innerVisitor != null) {
            this._innerVisitor.visitAttribute(attribute);
        }
    }
    
    @Override
    public void visitAnnotation(final CustomAnnotation annotation, final boolean visible) {
        if (this._innerVisitor != null) {
            this._innerVisitor.visitAnnotation(annotation, visible);
        }
    }
    
    @Override
    public void visitParameterAnnotation(final int parameter, final CustomAnnotation annotation, final boolean visible) {
        if (this._innerVisitor != null) {
            this._innerVisitor.visitParameterAnnotation(parameter, annotation, visible);
        }
    }
    
    protected final FrameValue get(final int local) {
        this._maxLocals = Math.max(this._maxLocals, local);
        return (local < this._locals.size()) ? this._locals.get(local) : FrameValue.TOP;
    }
    
    protected final void set(final int local, final FrameValue value) {
        this._maxLocals = Math.max(this._maxLocals, local);
        if (this._locals == null) {
            this._locals = new ArrayList<FrameValue>();
            this._stack = new ArrayList<FrameValue>();
            this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        }
        while (local >= this._locals.size()) {
            this._locals.add(FrameValue.TOP);
        }
        this._locals.set(local, value);
        if (value.getType().isDoubleWord()) {
            this._locals.set(local + 1, FrameValue.TOP);
        }
    }
    
    protected final void set(final int local, final TypeReference type) {
        this._maxLocals = Math.max(this._maxLocals, local);
        if (this._locals == null) {
            this._locals = new ArrayList<FrameValue>();
            this._stack = new ArrayList<FrameValue>();
            this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        }
        while (local >= this._locals.size()) {
            this._locals.add(FrameValue.TOP);
        }
        if (type == null) {
            this._locals.set(local, FrameValue.TOP);
            return;
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                this._locals.set(local, FrameValue.INTEGER);
                break;
            }
            case 6: {
                this._locals.set(local, FrameValue.LONG);
                if (local + 1 >= this._locals.size()) {
                    this._locals.add(FrameValue.TOP);
                    break;
                }
                this._locals.set(local + 1, FrameValue.TOP);
                break;
            }
            case 7: {
                this._locals.set(local, FrameValue.FLOAT);
                break;
            }
            case 8: {
                this._locals.set(local, FrameValue.DOUBLE);
                if (local + 1 >= this._locals.size()) {
                    this._locals.add(FrameValue.TOP);
                    break;
                }
                this._locals.set(local + 1, FrameValue.TOP);
                break;
            }
            case 9:
            case 10:
            case 11:
            case 12: {
                this._locals.set(local, FrameValue.makeReference(type));
                break;
            }
            case 13: {
                throw new IllegalArgumentException("Cannot set local to type void.");
            }
        }
    }
    
    protected final FrameValue pop() {
        return this._stack.remove(this._stack.size() - 1);
    }
    
    protected final FrameValue peek() {
        return this._stack.get(this._stack.size() - 1);
    }
    
    protected final void pop(final int count) {
        final int size = this._stack.size();
        for (int end = size - count, i = size - 1; i >= end; --i) {
            this._stack.remove(i);
        }
    }
    
    protected final void push(final TypeReference type) {
        if (this._stack == null) {
            this._locals = new ArrayList<FrameValue>();
            this._stack = new ArrayList<FrameValue>();
            this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5: {
                this._stack.add(FrameValue.INTEGER);
                break;
            }
            case 6: {
                this._stack.add(FrameValue.LONG);
                this._stack.add(FrameValue.TOP);
                break;
            }
            case 7: {
                this._stack.add(FrameValue.FLOAT);
                break;
            }
            case 8: {
                this._stack.add(FrameValue.DOUBLE);
                this._stack.add(FrameValue.TOP);
                break;
            }
            case 9:
            case 10:
            case 11:
            case 12: {
                this._stack.add(FrameValue.makeReference(type));
                break;
            }
        }
    }
    
    protected final void push(final FrameValue value) {
        if (this._stack == null) {
            this._locals = new ArrayList<FrameValue>();
            this._stack = new ArrayList<FrameValue>();
            this._initializations = new IdentityHashMap<Instruction, TypeReference>();
        }
        this._stack.add(value);
    }
    
    protected void initialize(final FrameValue value, final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        final Object parameter = value.getParameter();
        final FrameValue initializedValue = FrameValue.makeReference(type);
        if (parameter instanceof Instruction) {
            this._initializations.put((Instruction)parameter, type);
        }
        for (int i = 0; i < this._stack.size(); ++i) {
            if (this._stack.get(i) == value) {
                this._stack.set(i, initializedValue);
            }
        }
        for (int i = 0; i < this._locals.size(); ++i) {
            if (this._locals.get(i) == value) {
                this._locals.set(i, initializedValue);
            }
        }
    }
    
    public void pruneLocals() {
        while (!this._locals.isEmpty() && this._locals.get(this._locals.size() - 1) == FrameValue.OUT_OF_SCOPE) {
            this._locals.remove(this._locals.size() - 1);
        }
        for (int i = 0; i < this._locals.size(); ++i) {
            if (this._locals.get(i) == FrameValue.OUT_OF_SCOPE) {
                this._locals.set(i, FrameValue.TOP);
            }
        }
    }
    
    static /* synthetic */ List access$0(final StackMappingVisitor param_0) {
        return param_0._stack;
    }
    
    static /* synthetic */ Map access$1(final StackMappingVisitor param_0) {
        return param_0._initializations;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = StackMappingVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return StackMappingVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    private final class InstructionAnalyzer implements InstructionVisitor
    {
        private final InstructionVisitor _innerVisitor;
        private final MethodBody _body;
        private final CoreMetadataFactory _factory;
        private boolean _afterExecute;
        private final Stack<FrameValue> _temp;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior;
        
        private InstructionAnalyzer(final StackMappingVisitor param_0, final MethodBody body) {
            this(param_0, body, (InstructionVisitor)null);
        }
        
        private InstructionAnalyzer(final MethodBody body, final InstructionVisitor innerVisitor) {
            super();
            this._temp = new Stack<FrameValue>();
            this._body = VerifyArgument.notNull(body, "body");
            this._innerVisitor = innerVisitor;
            if (body.getMethod().isConstructor()) {
                StackMappingVisitor.this.set(0, FrameValue.UNINITIALIZED_THIS);
            }
            this._factory = CoreMetadataFactory.make(this._body.getMethod().getDeclaringType(), this._body.getMethod());
        }
        
        @Override
        public void visit(final Instruction instruction) {
            if (this._innerVisitor != null) {
                this._innerVisitor.visit(instruction);
            }
            instruction.accept(this);
            this.execute(instruction);
            this._afterExecute = true;
            try {
                instruction.accept(this);
            }
            finally {
                this._afterExecute = false;
            }
            this._afterExecute = false;
        }
        
        @Override
        public void visit(final OpCode code) {
            if (this._afterExecute) {
                if (code.isStore()) {
                    final FrameValue value = this._temp.isEmpty() ? StackMappingVisitor.this.pop() : this._temp.pop();
                    if (code.getStackChange() == -2) {
                        final FrameValue doubleOrLong = this._temp.isEmpty() ? StackMappingVisitor.this.pop() : this._temp.pop();
                        StackMappingVisitor.this.set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code), doubleOrLong);
                        StackMappingVisitor.this.set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code) + 1, value);
                    }
                    else {
                        StackMappingVisitor.this.set(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code), value);
                    }
                }
            }
            else if (code.isLoad()) {
                final FrameValue value = StackMappingVisitor.this.get(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code));
                StackMappingVisitor.this.push(value);
                if (value.getType().isDoubleWord()) {
                    StackMappingVisitor.this.push(StackMappingVisitor.this.get(OpCodeHelpers.getLoadStoreMacroArgumentIndex(code) + 1));
                }
            }
        }
        
        @Override
        public void visitConstant(final OpCode code, final TypeReference value) {
        }
        
        @Override
        public void visitConstant(final OpCode code, final int value) {
        }
        
        @Override
        public void visitConstant(final OpCode code, final long value) {
        }
        
        @Override
        public void visitConstant(final OpCode code, final float value) {
        }
        
        @Override
        public void visitConstant(final OpCode code, final double value) {
        }
        
        @Override
        public void visitConstant(final OpCode code, final String value) {
        }
        
        @Override
        public void visitBranch(final OpCode code, final Instruction target) {
        }
        
        @Override
        public void visitVariable(final OpCode code, final VariableReference variable) {
            if (this._afterExecute) {
                if (code.isStore()) {
                    final FrameValue value = this._temp.isEmpty() ? StackMappingVisitor.this.pop() : this._temp.pop();
                    if (code.getStackChange() == -2) {
                        final FrameValue doubleOrLong = this._temp.isEmpty() ? StackMappingVisitor.this.pop() : this._temp.pop();
                        StackMappingVisitor.this.set(variable.getSlot(), doubleOrLong);
                        StackMappingVisitor.this.set(variable.getSlot() + 1, value);
                    }
                    else {
                        StackMappingVisitor.this.set(variable.getSlot(), value);
                    }
                }
            }
            else if (code.isLoad()) {
                final FrameValue value = StackMappingVisitor.this.get(variable.getSlot());
                StackMappingVisitor.this.push(value);
                if (code.getStackChange() == 2) {
                    StackMappingVisitor.this.push(StackMappingVisitor.this.get(variable.getSlot() + 1));
                }
            }
        }
        
        @Override
        public void visitVariable(final OpCode code, final VariableReference variable, final int operand) {
        }
        
        @Override
        public void visitType(final OpCode code, final TypeReference type) {
        }
        
        @Override
        public void visitMethod(final OpCode code, final MethodReference method) {
        }
        
        @Override
        public void visitDynamicCallSite(final OpCode opCode, final DynamicCallSite callSite) {
        }
        
        @Override
        public void visitField(final OpCode code, final FieldReference field) {
        }
        
        @Override
        public void visitLabel(final Label label) {
        }
        
        @Override
        public void visitSwitch(final OpCode code, final SwitchInfo switchInfo) {
        }
        
        @Override
        public void visitEnd() {
        }
        
        private void execute(final Instruction instruction) {
            final OpCode code = instruction.getOpCode();
            this._temp.clear();
            if (code.isLoad() || code.isStore()) {
                return;
            }
            Label_1728: {
                switch ($SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior()[code.getStackBehaviorPop().ordinal()]) {
                    case 2: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 3:
                    case 4: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 5: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 6: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 7: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 8: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 9: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 10: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 11: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 12: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 13: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 14: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 15: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 16: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 17: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 18: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 19: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 20: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 21: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 22: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 23: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 24: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 25: {
                        this._temp.push(StackMappingVisitor.this.pop());
                        this._temp.push(StackMappingVisitor.this.pop());
                        break;
                    }
                    case 41: {
                        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[code.ordinal()]) {
                            case 183:
                            case 184:
                            case 185:
                            case 186:
                            case 187: {
                                IMethodSignature method;
                                if (code == OpCode.INVOKEDYNAMIC) {
                                    method = instruction.getOperand(0).getMethodType();
                                }
                                else {
                                    method = instruction.getOperand(0);
                                }
                                final List<ParameterDefinition> parameters = method.getParameters();
                                if (code == OpCode.INVOKESPECIAL && ((MethodReference)method).isConstructor()) {
                                    final FrameValue firstParameter = StackMappingVisitor.this.getStackValue(this.computeSize(parameters));
                                    final FrameValueType firstParameterType = firstParameter.getType();
                                    if (firstParameterType == FrameValueType.UninitializedThis || firstParameterType == FrameValueType.Uninitialized) {
                                        TypeReference initializedType;
                                        if (firstParameterType == FrameValueType.UninitializedThis) {
                                            initializedType = this._body.getMethod().getDeclaringType();
                                        }
                                        else {
                                            initializedType = ((MethodReference)method).getDeclaringType();
                                        }
                                        if (initializedType.isGenericDefinition()) {
                                            final Instruction next = instruction.getNext();
                                            if (next != null && next.getOpCode().isStore()) {
                                                final int slot = InstructionHelper.getLoadOrStoreSlot(next);
                                                final VariableDefinition variable = this._body.getVariables().tryFind(slot, next.getEndOffset());
                                                if (variable != null && variable.isFromMetadata() && variable.getVariableType() instanceof IGenericInstance && StringUtilities.equals(initializedType.getInternalName(), variable.getVariableType().getInternalName())) {
                                                    initializedType = variable.getVariableType();
                                                }
                                            }
                                        }
                                        StackMappingVisitor.this.initialize(firstParameter, initializedType);
                                    }
                                }
                                for (final ParameterDefinition parameter : parameters) {
                                    final TypeReference parameterType = parameter.getParameterType();
                                    switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[parameterType.getSimpleType().ordinal()]) {
                                        case 6:
                                        case 8: {
                                            this._temp.push(StackMappingVisitor.this.pop());
                                            this._temp.push(StackMappingVisitor.this.pop());
                                            continue;
                                        }
                                        default: {
                                            this._temp.push(StackMappingVisitor.this.pop());
                                            continue;
                                        }
                                    }
                                }
                                if (code != OpCode.INVOKESTATIC && code != OpCode.INVOKEDYNAMIC) {
                                    this._temp.push(StackMappingVisitor.this.pop());
                                    break Label_1728;
                                }
                                break Label_1728;
                            }
                            case 192: {
                                this._temp.push(StackMappingVisitor.this.pop());
                                while (!StackMappingVisitor.access$0(StackMappingVisitor.this).isEmpty()) {
                                    StackMappingVisitor.this.pop();
                                }
                                break Label_1728;
                            }
                            case 197: {
                                for (int dimensions = instruction.getOperand(1).intValue(), i = 0; i < dimensions; ++i) {
                                    this._temp.push(StackMappingVisitor.this.pop());
                                }
                                break Label_1728;
                            }
                        }
                        break;
                    }
                }
            }
            if (code.isArrayLoad()) {
                final FrameValue frameValue = this._temp.pop();
                final Object parameter2 = frameValue.getParameter();
                switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[code.ordinal()]) {
                    case 47:
                    case 52:
                    case 53:
                    case 54: {
                        StackMappingVisitor.this.push(FrameValue.INTEGER);
                        break;
                    }
                    case 48: {
                        StackMappingVisitor.this.push(FrameValue.LONG);
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                    case 49: {
                        StackMappingVisitor.this.push(FrameValue.FLOAT);
                        break;
                    }
                    case 50: {
                        StackMappingVisitor.this.push(FrameValue.DOUBLE);
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                    case 51: {
                        if (parameter2 instanceof TypeReference) {
                            StackMappingVisitor.this.push(((TypeReference)parameter2).getElementType());
                            break;
                        }
                        if (frameValue.getType() == FrameValueType.Null) {
                            StackMappingVisitor.this.push(FrameValue.NULL);
                            break;
                        }
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                }
                return;
            }
            if (code == OpCode.JSR || code == OpCode.JSR) {
                StackMappingVisitor.this.set(0, FrameValue.makeAddress(instruction.getNext()));
            }
            Label_3523: {
                switch ($SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior()[code.getStackBehaviorPush().ordinal()]) {
                    case 27: {
                        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[code.ordinal()]) {
                            case 19:
                            case 20: {
                                final Object op = instruction.getOperand(0);
                                if (op instanceof String) {
                                    StackMappingVisitor.this.push(this._factory.makeNamedType("java.lang.String"));
                                    break;
                                }
                                if (op instanceof TypeReference) {
                                    StackMappingVisitor.this.push(this._factory.makeNamedType("java.lang.Class"));
                                    break;
                                }
                                if (op instanceof Long) {
                                    StackMappingVisitor.this.push(FrameValue.LONG);
                                    StackMappingVisitor.this.push(FrameValue.TOP);
                                    break;
                                }
                                if (op instanceof Float) {
                                    StackMappingVisitor.this.push(FrameValue.FLOAT);
                                    break;
                                }
                                if (op instanceof Double) {
                                    StackMappingVisitor.this.push(FrameValue.DOUBLE);
                                    StackMappingVisitor.this.push(FrameValue.TOP);
                                    break;
                                }
                                if (op instanceof Integer) {
                                    StackMappingVisitor.this.push(FrameValue.INTEGER);
                                    break;
                                }
                                break;
                            }
                            case 179:
                            case 181: {
                                final FieldReference field = instruction.getOperand(0);
                                StackMappingVisitor.this.push(field.getFieldType());
                                break;
                            }
                        }
                        break;
                    }
                    case 28: {
                        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[code.ordinal()]) {
                            case 90: {
                                final FrameValue value = this._temp.pop();
                                StackMappingVisitor.this.push(value);
                                StackMappingVisitor.this.push(value);
                                break;
                            }
                            case 96: {
                                FrameValue t2 = this._temp.pop();
                                t2 = this._temp.pop();
                                StackMappingVisitor.this.push(t2);
                                StackMappingVisitor.this.push(t2);
                                break;
                            }
                        }
                        break;
                    }
                    case 29: {
                        FrameValue t2 = this._temp.pop();
                        t2 = this._temp.pop();
                        StackMappingVisitor.this.push(t2);
                        StackMappingVisitor.this.push(t2);
                        StackMappingVisitor.this.push(t2);
                        break;
                    }
                    case 30: {
                        final FrameValue t3 = this._temp.pop();
                        final FrameValue t4 = this._temp.pop();
                        final FrameValue t5 = this._temp.pop();
                        StackMappingVisitor.this.push(t5);
                        StackMappingVisitor.this.push(t3);
                        StackMappingVisitor.this.push(t4);
                        StackMappingVisitor.this.push(t5);
                        break;
                    }
                    case 31: {
                        final Number constant = instruction.getOperand(0);
                        if (constant instanceof Double) {
                            StackMappingVisitor.this.push(FrameValue.DOUBLE);
                            StackMappingVisitor.this.push(FrameValue.TOP);
                            break;
                        }
                        StackMappingVisitor.this.push(FrameValue.LONG);
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                    case 32: {
                        FrameValue t2 = this._temp.pop();
                        t2 = this._temp.pop();
                        StackMappingVisitor.this.push(t2);
                        StackMappingVisitor.this.push(t2);
                        StackMappingVisitor.this.push(t2);
                        StackMappingVisitor.this.push(t2);
                        break;
                    }
                    case 33: {
                        final FrameValue t3 = this._temp.pop();
                        final FrameValue t4 = this._temp.pop();
                        final FrameValue t5 = this._temp.pop();
                        StackMappingVisitor.this.push(t4);
                        StackMappingVisitor.this.push(t5);
                        StackMappingVisitor.this.push(t3);
                        StackMappingVisitor.this.push(t4);
                        StackMappingVisitor.this.push(t5);
                        break;
                    }
                    case 34: {
                        final FrameValue t6 = this._temp.pop();
                        final FrameValue t7 = this._temp.pop();
                        final FrameValue t8 = this._temp.pop();
                        final FrameValue t9 = this._temp.pop();
                        StackMappingVisitor.this.push(t8);
                        StackMappingVisitor.this.push(t9);
                        StackMappingVisitor.this.push(t6);
                        StackMappingVisitor.this.push(t7);
                        StackMappingVisitor.this.push(t8);
                        StackMappingVisitor.this.push(t9);
                        break;
                    }
                    case 35: {
                        StackMappingVisitor.this.push(FrameValue.INTEGER);
                        break;
                    }
                    case 36: {
                        StackMappingVisitor.this.push(FrameValue.LONG);
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                    case 37: {
                        StackMappingVisitor.this.push(FrameValue.FLOAT);
                        break;
                    }
                    case 38: {
                        StackMappingVisitor.this.push(FrameValue.DOUBLE);
                        StackMappingVisitor.this.push(FrameValue.TOP);
                        break;
                    }
                    case 39: {
                        switch ($SWITCH_TABLE$com$strobel$assembler$ir$OpCode()[code.ordinal()]) {
                            case 188: {
                                StackMappingVisitor.this.push(FrameValue.makeUninitializedReference(instruction));
                                break Label_3523;
                            }
                            case 189:
                            case 190: {
                                StackMappingVisitor.this.push(instruction.getOperand(0).makeArrayType());
                                break Label_3523;
                            }
                            case 193:
                            case 197: {
                                StackMappingVisitor.this.push(instruction.getOperand(0));
                                break Label_3523;
                            }
                            case 2: {
                                StackMappingVisitor.this.push(FrameValue.NULL);
                                break Label_3523;
                            }
                            default: {
                                StackMappingVisitor.this.push(StackMappingVisitor.this.pop());
                                break Label_3523;
                            }
                        }
                        break;
                    }
                    case 40: {
                        StackMappingVisitor.this.push(FrameValue.makeAddress(instruction.getNext()));
                        break;
                    }
                    case 42: {
                        IMethodSignature signature;
                        if (code == OpCode.INVOKEDYNAMIC) {
                            signature = instruction.getOperand(0).getMethodType();
                        }
                        else {
                            signature = instruction.getOperand(0);
                        }
                        TypeReference returnType = signature.getReturnType();
                        if (returnType.getSimpleType() != JvmType.Void) {
                            if (code != OpCode.INVOKESTATIC && code != OpCode.INVOKEDYNAMIC) {
                                TypeReference typeReference;
                                if (code == OpCode.INVOKESPECIAL) {
                                    typeReference = ((MethodReference)signature).getDeclaringType();
                                }
                                else {
                                    final Object parameter3 = this._temp.peek().getParameter();
                                    typeReference = ((parameter3 instanceof Instruction) ? StackMappingVisitor.access$1(StackMappingVisitor.this).get(parameter3) : parameter3);
                                }
                                final TypeReference targetType = this.substituteTypeArguments(typeReference, (MemberReference)signature);
                                returnType = this.substituteTypeArguments(this.substituteTypeArguments(signature.getReturnType(), (MemberReference)signature), targetType);
                            }
                            else if (instruction.getNext() != null && instruction.getNext().getOpCode().isStore()) {
                                final Instruction next2 = instruction.getNext();
                                final int slot2 = InstructionHelper.getLoadOrStoreSlot(next2);
                                final VariableDefinition variable2 = this._body.getVariables().tryFind(slot2, next2.getEndOffset());
                                if (variable2 != null && variable2.isFromMetadata()) {
                                    returnType = this.substituteTypeArguments(variable2.getVariableType(), signature.getReturnType());
                                }
                            }
                        }
                        if (returnType.isWildcardType()) {
                            returnType = (returnType.hasSuperBound() ? returnType.getSuperBound() : returnType.getExtendsBound());
                        }
                        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[returnType.getSimpleType().ordinal()]) {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                            case 5: {
                                StackMappingVisitor.this.push(FrameValue.INTEGER);
                                break Label_3523;
                            }
                            case 6: {
                                StackMappingVisitor.this.push(FrameValue.LONG);
                                StackMappingVisitor.this.push(FrameValue.TOP);
                                break Label_3523;
                            }
                            case 7: {
                                StackMappingVisitor.this.push(FrameValue.FLOAT);
                                break Label_3523;
                            }
                            case 8: {
                                StackMappingVisitor.this.push(FrameValue.DOUBLE);
                                StackMappingVisitor.this.push(FrameValue.TOP);
                                break Label_3523;
                            }
                            case 9:
                            case 10:
                            case 11:
                            case 12: {
                                StackMappingVisitor.this.push(FrameValue.makeReference(returnType));
                                break Label_3523;
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        private int computeSize(final List<ParameterDefinition> parameters) {
            int size = 0;
            for (final ParameterDefinition parameter : parameters) {
                size += parameter.getSize();
            }
            return size;
        }
        
        private TypeReference substituteTypeArguments(final TypeReference type, final MemberReference member) {
            if (type instanceof ArrayType) {
                final ArrayType arrayType = (ArrayType)type;
                final TypeReference elementType = this.substituteTypeArguments(arrayType.getElementType(), member);
                if (!MetadataResolver.areEquivalent(elementType, arrayType.getElementType())) {
                    return elementType.makeArrayType();
                }
                return type;
            }
            else {
                if (type instanceof IGenericInstance) {
                    final IGenericInstance genericInstance = (IGenericInstance)type;
                    final List<TypeReference> newTypeArguments = new ArrayList<TypeReference>();
                    boolean isChanged = false;
                    for (final TypeReference typeArgument : genericInstance.getTypeArguments()) {
                        final TypeReference newTypeArgument = this.substituteTypeArguments(typeArgument, member);
                        newTypeArguments.add(newTypeArgument);
                        isChanged |= (newTypeArgument != typeArgument);
                    }
                    return isChanged ? type.makeGenericType(newTypeArguments) : type;
                }
                if (type instanceof GenericParameter) {
                    final GenericParameter genericParameter = (GenericParameter)type;
                    final IGenericParameterProvider owner = genericParameter.getOwner();
                    if (member.getDeclaringType() instanceof ArrayType) {
                        return member.getDeclaringType().getElementType();
                    }
                    if (owner instanceof MethodReference && member instanceof MethodReference) {
                        final MethodReference method = (MethodReference)member;
                        final MethodReference ownerMethod = (MethodReference)owner;
                        if (method.isGenericMethod() && MetadataResolver.areEquivalent(ownerMethod.getDeclaringType(), method.getDeclaringType()) && StringUtilities.equals(ownerMethod.getName(), method.getName()) && StringUtilities.equals(ownerMethod.getErasedSignature(), method.getErasedSignature())) {
                            if (method instanceof IGenericInstance) {
                                final List<TypeReference> typeArguments = ((IGenericInstance)member).getTypeArguments();
                                return typeArguments.get(genericParameter.getPosition());
                            }
                            return method.getGenericParameters().get(genericParameter.getPosition());
                        }
                    }
                    else if (owner instanceof TypeReference) {
                        TypeReference declaringType;
                        if (member instanceof TypeReference) {
                            declaringType = (TypeReference)member;
                        }
                        else {
                            declaringType = member.getDeclaringType();
                        }
                        if (MetadataResolver.areEquivalent((TypeReference)owner, declaringType)) {
                            if (declaringType instanceof IGenericInstance) {
                                final List<TypeReference> typeArguments2 = ((IGenericInstance)declaringType).getTypeArguments();
                                return typeArguments2.get(genericParameter.getPosition());
                            }
                            if (!declaringType.isGenericDefinition()) {
                                declaringType = declaringType.resolve();
                            }
                            if (declaringType != null && declaringType.isGenericDefinition()) {
                                return declaringType.getGenericParameters().get(genericParameter.getPosition());
                            }
                        }
                    }
                }
                return type;
            }
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
            final int[] loc_0 = InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
            return InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$OpCode() {
            final int[] loc_0 = InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode;
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
            return InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$ir$OpCode = loc_1;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior() {
            final int[] loc_0 = InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior;
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
            return InstructionAnalyzer.$SWITCH_TABLE$com$strobel$assembler$ir$StackBehavior = loc_1;
        }
    }
}
