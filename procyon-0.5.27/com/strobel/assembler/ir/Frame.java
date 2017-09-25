package com.strobel.assembler.ir;

import com.strobel.decompiler.*;
import com.strobel.util.*;
import java.util.*;
import com.strobel.core.*;
import com.strobel.assembler.metadata.*;

public final class Frame
{
    public static final FrameValue[] EMPTY_VALUES;
    public static final Frame NEW_EMPTY;
    public static final Frame SAME;
    private final FrameType _frameType;
    private final List<FrameValue> _localValues;
    private final List<FrameValue> _stackValues;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FrameType;
    
    static {
        EMPTY_VALUES = EmptyArrayCache.fromElementType(FrameValue.class);
        NEW_EMPTY = new Frame(FrameType.New, EmptyArrayCache.fromElementType(FrameValue.class), EmptyArrayCache.fromElementType(FrameValue.class));
        SAME = new Frame(FrameType.Same, EmptyArrayCache.fromElementType(FrameValue.class), EmptyArrayCache.fromElementType(FrameValue.class));
    }
    
    public Frame(final FrameType frameType, final FrameValue[] localValues, final FrameValue[] stackValues) {
        super();
        this._frameType = VerifyArgument.notNull(frameType, "frameType");
        this._localValues = ArrayUtilities.asUnmodifiableList((FrameValue[])VerifyArgument.notNull(localValues, "localValues").clone());
        this._stackValues = ArrayUtilities.asUnmodifiableList((FrameValue[])VerifyArgument.notNull(stackValues, "stackValues").clone());
    }
    
    private Frame(final FrameType frameType, final List<FrameValue> localValues, final List<FrameValue> stackValues) {
        super();
        this._frameType = frameType;
        this._localValues = localValues;
        this._stackValues = stackValues;
    }
    
    public final FrameType getFrameType() {
        return this._frameType;
    }
    
    public final List<FrameValue> getLocalValues() {
        return this._localValues;
    }
    
    public final List<FrameValue> getStackValues() {
        return this._stackValues;
    }
    
    public final Frame withEmptyStack() {
        if (this._frameType != FrameType.New) {
            throw new IllegalStateException("Can only call withEmptyStack() on New frames.");
        }
        return new Frame(this._frameType, this._localValues, Collections.emptyList());
    }
    
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Frame)) {
            return false;
        }
        final Frame frame = (Frame)o;
        return frame._frameType == this._frameType && CollectionUtilities.sequenceDeepEquals(frame._localValues, this._localValues) && CollectionUtilities.sequenceDeepEquals(frame._stackValues, this._stackValues);
    }
    
    @Override
    public final int hashCode() {
        int result = this._frameType.hashCode();
        for (int i = 0; i < this._localValues.size(); ++i) {
            result = HashUtilities.combineHashCodes((Object)result, this._localValues.get(i));
        }
        for (int i = 0; i < this._stackValues.size(); ++i) {
            result = HashUtilities.combineHashCodes((Object)result, this._stackValues.get(i));
        }
        return result;
    }
    
    @Override
    public final String toString() {
        final PlainTextOutput writer = new PlainTextOutput();
        DecompilerHelpers.writeFrame(writer, this);
        return writer.toString();
    }
    
    public static Frame computeDelta(final Frame previous, final Frame current) {
        VerifyArgument.notNull(previous, "previous");
        VerifyArgument.notNull(current, "current");
        final List<FrameValue> previousLocals = previous._localValues;
        final List<FrameValue> currentLocals = current._localValues;
        final List<FrameValue> currentStack = current._stackValues;
        final int previousLocalCount = previousLocals.size();
        final int currentLocalCount = currentLocals.size();
        final int currentStackSize = currentStack.size();
        int localCount = previousLocalCount;
        FrameType type = FrameType.Full;
        if (currentStackSize == 0) {
            switch (currentLocalCount - previousLocalCount) {
                case -3:
                case -2:
                case -1: {
                    type = FrameType.Chop;
                    localCount = currentLocalCount;
                    break;
                }
                case 0: {
                    type = FrameType.Same;
                    break;
                }
                case 1:
                case 2:
                case 3: {
                    type = FrameType.Append;
                    break;
                }
            }
        }
        else if (currentLocalCount == localCount && currentStackSize == 1) {
            type = FrameType.Same1;
        }
        if (type != FrameType.Full) {
            for (int i = 0; i < localCount; ++i) {
                if (!currentLocals.get(i).equals(previousLocals.get(i))) {
                    type = FrameType.Full;
                    break;
                }
            }
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$ir$FrameType()[type.ordinal()]) {
            case 1: {
                return new Frame(type, currentLocals.subList(previousLocalCount, currentLocalCount).toArray(new FrameValue[currentLocalCount - previousLocalCount]), EmptyArrayCache.fromElementType(FrameValue.class));
            }
            case 2: {
                return new Frame(type, previousLocals.subList(currentLocalCount, previousLocalCount).toArray(new FrameValue[previousLocalCount - currentLocalCount]), EmptyArrayCache.fromElementType(FrameValue.class));
            }
            case 3: {
                return new Frame(type, currentLocals, currentStack);
            }
            case 5: {
                return Frame.SAME;
            }
            case 6: {
                return new Frame(type, EmptyArrayCache.fromElementType(FrameValue.class), new FrameValue[] { currentStack.get(currentStackSize - 1) });
            }
            default: {
                throw ContractUtils.unreachable();
            }
        }
    }
    
    public static Frame merge(final Frame input, final Frame output, final Frame next, final Map<Instruction, TypeReference> initializations) {
        VerifyArgument.notNull(input, "input");
        VerifyArgument.notNull(output, "output");
        VerifyArgument.notNull(next, "next");
        final List<FrameValue> inputLocals = input._localValues;
        final List<FrameValue> outputLocals = output._localValues;
        final int inputLocalCount = inputLocals.size();
        final int outputLocalCount = outputLocals.size();
        final int nextLocalCount = next._localValues.size();
        final int tempLocalCount = Math.max(nextLocalCount, inputLocalCount);
        final FrameValue[] nextLocals = next._localValues.toArray(new FrameValue[tempLocalCount]);
        boolean changed = false;
        for (int i = 0; i < inputLocalCount; ++i) {
            FrameValue t;
            if (i < outputLocalCount) {
                t = outputLocals.get(i);
            }
            else {
                t = inputLocals.get(i);
            }
            if (initializations != null) {
                t = initialize(initializations, t);
            }
            changed |= merge(t, nextLocals, i);
        }
        final List<FrameValue> inputStack = input._stackValues;
        final List<FrameValue> outputStack = output._stackValues;
        final int inputStackSize = inputStack.size();
        final int outputStackSize = outputStack.size();
        final int nextStackSize = next._stackValues.size();
        final FrameValue[] nextStack = next._stackValues.toArray(new FrameValue[nextStackSize]);
        for (int j = 0, max = Math.min(nextStackSize, inputStackSize); j < max; ++j) {
            FrameValue t = inputStack.get(j);
            if (initializations != null) {
                t = initialize(initializations, t);
            }
            changed |= merge(t, nextStack, j);
        }
        for (int j = inputStackSize, max = Math.min(nextStackSize, outputStackSize); j < max; ++j) {
            FrameValue t = outputStack.get(j);
            if (initializations != null) {
                t = initialize(initializations, t);
            }
            changed |= merge(t, nextStack, j);
        }
        if (!changed) {
            return next;
        }
        final int newLocalCount = nextLocalCount;
        return new Frame(FrameType.New, (nextLocals.length == newLocalCount) ? nextLocals : Arrays.copyOf(nextLocals, newLocalCount), nextStack);
    }
    
    private static FrameValue initialize(final Map<Instruction, TypeReference> initializations, FrameValue t) {
        if (t == null) {
            return t;
        }
        final Object parameter = t.getParameter();
        if (parameter instanceof Instruction) {
            final TypeReference initializedType = initializations.get(parameter);
            if (initializedType != null) {
                t = FrameValue.makeReference(initializedType);
            }
        }
        return t;
    }
    
    private static boolean merge(final FrameValue t, final FrameValue[] values, final int index) {
        final FrameValue u = values[index];
        if (Comparer.equals(t, u)) {
            return false;
        }
        if (t == FrameValue.EMPTY) {
            return false;
        }
        if (t == FrameValue.NULL && u == FrameValue.NULL) {
            return false;
        }
        if (u == FrameValue.EMPTY) {
            values[index] = t;
            return true;
        }
        final FrameValueType tType = t.getType();
        final FrameValueType uType = u.getType();
        FrameValue v;
        if (uType == FrameValueType.Reference) {
            if (t == FrameValue.NULL) {
                return false;
            }
            if (tType == FrameValueType.Reference) {
                v = FrameValue.makeReference(MetadataHelper.findCommonSuperType((TypeReference)t.getParameter(), (TypeReference)u.getParameter()));
            }
            else {
                v = FrameValue.TOP;
            }
        }
        else if (u == FrameValue.NULL && tType == FrameValueType.Reference) {
            v = t;
        }
        else {
            v = FrameValue.TOP;
        }
        if (!u.equals(v)) {
            values[index] = v;
            return true;
        }
        return false;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$ir$FrameType() {
        final int[] loc_0 = Frame.$SWITCH_TABLE$com$strobel$assembler$ir$FrameType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[FrameType.values().length];
        try {
            loc_1[FrameType.Append.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[FrameType.Chop.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[FrameType.Full.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[FrameType.New.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[FrameType.Same.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[FrameType.Same1.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_7) {}
        return Frame.$SWITCH_TABLE$com$strobel$assembler$ir$FrameType = loc_1;
    }
}
